package com.burstlinker.ezcapsolver;

import com.burstlinker.ezcapsolver.exception.ApiException;
import com.burstlinker.ezcapsolver.exception.EzCaptchaException;
import com.burstlinker.ezcapsolver.exception.TransportException;
import com.burstlinker.ezcapsolver.exception.UnexpectedResponseException;
import com.burstlinker.ezcapsolver.internal.Json;
import com.burstlinker.ezcapsolver.internal.Redaction;
import com.burstlinker.ezcapsolver.model.ResponseMeta;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One HTTP exchange with the service, and the classification of what came back.
 *
 * <p>Package-private, which is Java's own way of saying "not API" — the compiler refuses a
 * reference from outside this package, so it needs no naming convention to back it up. Its one
 * caller is {@link EzCapSolverClient}.
 *
 * <p><strong>One call means one HTTP request. This never retries.</strong> Creating a task is
 * billed and is not idempotent, and the service bans a key that repeats certain errors, so
 * retrying is the caller's decision. The one retry the SDK performs lives a layer up, in the
 * polling loop, and only for a throttled <em>query</em>.
 */
final class Transport {

    private static final Logger log = LoggerFactory.getLogger("com.burstlinker.ezcapsolver");

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    /** Header carrying the client key on every request, alongside the {@code clientKey} body field. */
    private static final String CLIENT_KEY_HEADER = "X-API-Key";

    /** Characters of a body kept in a trace log. An error envelope fits; a token gets cut. */
    private static final int TRACE_PREVIEW_CHARS = 256;

    private final ClientConfig config;

    /**
     * Two clients, not one, because the two timeout budgets must not be merged.
     *
     * <p>Both come from {@code newBuilder()} on the same base, so they share a connection
     * pool, dispatcher and thread pools — the split costs nothing but the wrapper object.
     * OkHttp's {@code callTimeout} is per client, not per request, which is why this is
     * structural rather than an argument someone could pass the wrong value for.
     */
    private final OkHttpClient asyncHttp;

    private final OkHttpClient syncHttp;

    Transport(ClientConfig config) {
        this.config = config;
        OkHttpClient base = baseClient(config);
        this.asyncHttp = base.newBuilder().callTimeout(config.getTimeout().toMillis(), TimeUnit.MILLISECONDS).build();
        this.syncHttp =
                base.newBuilder().callTimeout(config.getSyncTimeout().toMillis(), TimeUnit.MILLISECONDS).build();
    }

    /** An asynchronous endpoint or the balance query: short budget, async host. */
    <T extends ResponseMeta> T postAsync(String path, Object body, Class<T> type) {
        return post(asyncHttp, join(config.getAsyncBaseUrl(), path), body, type);
    }

    /**
     * The synchronous task endpoint: long budget, sync host.
     *
     * <p>Pairing the host with the budget here is deliberate. Sending a synchronous task to
     * the async host, or cutting one off at the async timeout, both waste a billed call that
     * returns no task id to recover it with.
     */
    <T extends ResponseMeta> T postSync(String path, Object body, Class<T> type) {
        return post(syncHttp, join(config.getSyncBaseUrl(), path), body, type);
    }

    private <T extends ResponseMeta> T post(
            OkHttpClient http, String url, Object body, Class<T> type) {

        String payload;
        try {
            payload = Json.mapper().writeValueAsString(body);
        } catch (JsonProcessingException e) {
            // Not a transport failure: nothing was sent. A body that will not serialize is a
            // programming error in the caller's own model.
            throw EzCaptchaException.config("request body is not serializable: " + e.getOriginalMessage());
        }

        logRequest(url, payload);

        Request request =
                new Request.Builder()
                        .url(url)
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("User-Agent", config.getUserAgent())
                        .header(CLIENT_KEY_HEADER, config.getClientKey())
                        .post(RequestBody.create(payload, JSON_MEDIA_TYPE))
                        .build();

        String operation = "POST " + url;
        long started = System.nanoTime();
        try (Response response = http.newCall(request).execute()) {
            String responseBody = response.body() == null ? "" : response.body().string();
            logResponse(url, response.code(), started, responseBody);
            return parse(response.code(), responseBody, type);
        } catch (IOException e) {
            // Covers DNS, TCP, TLS, the call timeout and a truncated body. None of them
            // produced a usable response, so none of them can say whether the service
            // accepted the request.
            throw new TransportException(operation, e);
        }
    }

    /**
     * Turns a status and body into either a decoded result or a classified failure.
     *
     * <p>Both of the service's signals have to be read, and they disagree often enough to
     * matter: a failed task arrives as HTTP 200 with {@code errorId} 1, and most business
     * errors arrive as HTTP 500 rather than as a service fault. Branching on the status line
     * alone gets both wrong.
     */
    private <T extends ResponseMeta> T parse(int status, String body, Class<T> type) {
        boolean ok = status >= 200 && status < 300;

        JsonNode envelope;
        try {
            envelope = Json.mapper().readTree(body);
        } catch (JsonProcessingException e) {
            // A non-success status whose body is not JSON -- an HTML page from a gateway,
            // say -- is still a failure from the service, so it is reported with the same
            // shape as one that carried an envelope. That keeps the status readable from one
            // place.
            if (!ok) {
                throw statusOnly(status, body);
            }
            throw new UnexpectedResponseException(
                    "response body is not JSON: " + e.getOriginalMessage(),
                    UnexpectedResponseException.preview(body));
        }

        // Covers a body that parsed but is not an object -- a bare string or array from a
        // proxy -- as well as an empty body, which readTree turns into a missing node.
        if (envelope == null || !envelope.isObject()) {
            if (!ok) {
                throw statusOnly(status, body);
            }
            throw new UnexpectedResponseException(
                    "response body is not a JSON object",
                    UnexpectedResponseException.preview(body));
        }

        // errorId is the only success criterion. A non-empty errorCode is not a second
        // signal: every code the service defines already comes with a non-zero errorId, and
        // treating the code as authoritative would let one added on the success side turn a
        // solved task into an error.
        int errorId = envelope.path("errorId").asInt(0);
        if (errorId != 0) {
            throw new ApiException(
                    envelope.path("errorCode").asText(null),
                    envelope.path("errorDescription").asText(null),
                    status,
                    validationErrors(envelope),
                    envelope.path("requestId").asText(null),
                    null);
        }
        if (!ok) {
            throw statusOnly(status, body);
        }

        try {
            return Json.mapper().treeToValue(envelope, type);
        } catch (JsonProcessingException e) {
            throw new UnexpectedResponseException(
                    "response does not match the expected shape: " + e.getOriginalMessage(),
                    UnexpectedResponseException.preview(body));
        }
    }

    /**
     * Pulls the field-level messages out of a validation failure. The key is absent on every
     * other kind of error.
     */
    private static Map<String, String> validationErrors(JsonNode envelope) {
        JsonNode errors = envelope.get("errors");
        if (errors == null || !errors.isObject()) {
            return null;
        }
        Map<String, String> extracted = new LinkedHashMap<String, String>();
        for (Map.Entry<String, JsonNode> field : errors.properties()) {
            extracted.put(field.getKey(), field.getValue().asText());
        }
        return extracted;
    }

    /** A non-success status whose body carried no envelope, keeping a slice for diagnosis. */
    private static ApiException statusOnly(int status, String body) {
        return new ApiException(
                null, UnexpectedResponseException.preview(body), status, null, null, null);
    }

    private static OkHttpClient baseClient(ClientConfig config) {
        OkHttpClient.Builder builder =
                config.getOkHttpClient() != null
                        ? config.getOkHttpClient().newBuilder()
                        : new OkHttpClient.Builder();

        String proxyUrl = config.getProxy();
        if (proxyUrl == null || proxyUrl.trim().isEmpty()) {
            return builder.build();
        }

        URI uri;
        try {
            uri = new URI(proxyUrl);
        } catch (URISyntaxException e) {
            throw EzCaptchaException.config("proxy is not a valid URL: " + Redaction.PLACEHOLDER);
        }
        if (uri.getHost() == null || uri.getPort() < 0) {
            throw EzCaptchaException.config(
                    "proxy must include a host and a port, as http://user:pass@host:port");
        }

        Proxy.Type kind =
                "socks".equalsIgnoreCase(uri.getScheme()) || "socks5".equalsIgnoreCase(uri.getScheme())
                        ? Proxy.Type.SOCKS
                        : Proxy.Type.HTTP;
        builder.proxy(new Proxy(kind, new InetSocketAddress(uri.getHost(), uri.getPort())));

        String userInfo = uri.getUserInfo();
        if (userInfo != null && !userInfo.isEmpty()) {
            int separator = userInfo.indexOf(':');
            final String user = separator < 0 ? userInfo : userInfo.substring(0, separator);
            final String password = separator < 0 ? "" : userInfo.substring(separator + 1);
            final String credential = Credentials.basic(user, password);

            // This is the whole reason the SDK is not on HttpURLConnection: authenticating an
            // HTTPS proxy there needs a JVM-wide system property plus a process-global
            // Authenticator, neither of which a library may set on its host application.
            builder.proxyAuthenticator(
                    new okhttp3.Authenticator() {
                        @Override
                        public Request authenticate(Route route, Response response) {
                            // Returning null gives up instead of looping: the proxy rejected
                            // the credential once, and it will reject it again.
                            if (response.request().header("Proxy-Authorization") != null) {
                                return null;
                            }
                            return response.request()
                                    .newBuilder()
                                    .header("Proxy-Authorization", credential)
                                    .build();
                        }
                    });
        }
        return builder.build();
    }

    /** Joins a base URL and a path, tolerating a slash on either side or both. */
    static String join(String baseUrl, String path) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String suffix = path.startsWith("/") ? path : "/" + path;
        return base + suffix;
    }

    /**
     * Records an outgoing body with credentials replaced.
     *
     * <p>Guarded by the level check, so at the default level this costs one boolean and never
     * parses or re-serialises anything.
     */
    private void logRequest(String url, String payload) {
        if (!log.isTraceEnabled()) {
            return;
        }
        String rendered;
        try {
            rendered = Json.mapper().writeValueAsString(Redaction.redact(Json.mapper().readTree(payload)));
        } catch (JsonProcessingException e) {
            rendered = "<unrenderable>";
        }
        log.trace("sending API request url={} body={}", url, preview(rendered));
    }

    private void logResponse(String url, int status, long startedNanos, String body) {
        if (!log.isTraceEnabled()) {
            return;
        }
        // The correlation id is the service's own, carried in the body: it is assigned
        // upstream of the service and propagated down, so there is nothing for the SDK to
        // generate or send.
        log.trace(
                "received API response url={} status={} elapsed_ms={} bytes={} body={}",
                url,
                status,
                (System.nanoTime() - startedNanos) / 1_000_000L,
                body.length(),
                preview(body));
    }

    private static String preview(String body) {
        return body.length() <= TRACE_PREVIEW_CHARS ? body : body.substring(0, TRACE_PREVIEW_CHARS) + "...";
    }
}
