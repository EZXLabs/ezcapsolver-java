package com.burstlinker.ezcapsolver;

import com.burstlinker.ezcapsolver.exception.EzCaptchaException;
import com.burstlinker.ezcapsolver.internal.Redaction;
import java.time.Duration;
import lombok.Builder;
import lombok.Getter;
import okhttp3.OkHttpClient;

/**
 * How a client talks to the service.
 *
 * <p>Everything checkable is checked while building rather than on the first request, so a
 * mistake surfaces <strong>before anything is billed</strong>.
 *
 * <pre>{@code
 * ClientConfig config = ClientConfig.builder()
 *         .clientKey("...")                       // falls back to EZCAPTCHA_API_KEY
 *         .timeout(Duration.ofSeconds(30))
 *         .syncTimeout(Duration.ofSeconds(240))
 *         .pollInterval(Duration.ofSeconds(3))
 *         .maxPollAttempts(50)
 *         .build();
 * }</pre>
 */
@Getter
public final class ClientConfig {

    /** Serves asynchronous tasks and balance queries. */
    public static final String DEFAULT_ASYNC_BASE_URL = "https://api.ez-captcha.com";

    /**
     * Serves synchronous tasks. The service splits the two across hosts, so this is a
     * separate setting rather than a path on the other one.
     */
    public static final String DEFAULT_SYNC_BASE_URL = "https://sync.ez-captcha.com";

    /** Read when no client key is supplied explicitly. */
    public static final String DEFAULT_CLIENT_KEY_ENV = "EZCAPTCHA_API_KEY";

    /** Bounds one call to an asynchronous endpoint or the balance query. */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Bounds one call to the synchronous task endpoint.
     *
     * <p>Eight times the other one, and the two <strong>must not be merged</strong>. A
     * synchronous call blocks until the worker answers, which the service allows three
     * minutes for on some task types. Cutting that off at 30 seconds aborts a call that has
     * already been billed and <em>returns no task id</em>, so the work cannot be recovered.
     * The extra minute over the service's own 180-second deadline is margin.
     */
    public static final Duration DEFAULT_SYNC_TIMEOUT = Duration.ofSeconds(240);

    /** Delay between result queries while waiting for an asynchronous task. */
    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(3);

    /**
     * Result queries before a wait gives up.
     *
     * <p>Together with {@link #DEFAULT_POLL_INTERVAL} this is 150 seconds of waiting. The
     * product is a wall-clock proxy for the <strong>five minutes</strong> the service holds a
     * result — past that the task id comes back as {@code ERROR_TASK_NOT_EXIST} — so the
     * default deliberately lands inside that window with room to spare.
     */
    public static final int DEFAULT_MAX_POLL_ATTEMPTS = 50;

    /**
     * The API key, resolved: either what was passed, or the
     * {@value #DEFAULT_CLIENT_KEY_ENV} environment variable.
     */
    private final String clientKey;

    /** Bounds one asynchronous or balance request. */
    private final Duration timeout;

    /** Bounds one synchronous task request. */
    private final Duration syncTimeout;

    /** Delay between result queries. */
    private final Duration pollInterval;

    /**
     * Result queries before a wait gives up.
     *
     * <p>{@code pollInterval x maxPollAttempts} is the budget. Task types differ widely in how
     * long they take, so every call that waits also accepts its own budget rather than having
     * this one forced onto all of them.
     */
    private final int maxPollAttempts;

    /** Optional application identifier, sent with every created task. */
    private final Integer appId;

    /**
     * Proxy the <strong>SDK itself</strong> dials out through, as
     * {@code http://user:pass@host:port}.
     *
     * <p>Unrelated to the {@code proxy} field on a task, which is the one the worker uses to
     * reach the protected site. Confusing the two is easy and the symptom is puzzling, so the
     * two are never spelled the same way in this SDK.
     */
    private final String proxy;

    /** Sent as {@code User-Agent}. */
    private final String userAgent;

    /** Base URL for asynchronous tasks and the balance query. */
    private final String asyncBaseUrl;

    /** Base URL for synchronous tasks. */
    private final String syncBaseUrl;

    /**
     * An {@link OkHttpClient} to build on, for callers who need their own interceptors,
     * connection pool or TLS setup. {@code null} to let the SDK create one.
     *
     * <p>The SDK derives from it with {@code newBuilder()} rather than using it directly, so
     * the connection and thread pools are shared while the two timeout budgets stay separate.
     * Any call timeout set on it is overridden.
     */
    private final OkHttpClient okHttpClient;

    /**
     * The builder's only exit, which is what makes validation unavoidable.
     *
     * <p>Defaults are applied here rather than through {@code @Builder.Default} so that
     * resolving, defaulting and checking all read in one place — with the annotation they
     * would be spread across ten field declarations and a separate validate method.
     */
    @Builder(toBuilder = true)
    private ClientConfig(
            String clientKey,
            Duration timeout,
            Duration syncTimeout,
            Duration pollInterval,
            Integer maxPollAttempts,
            Integer appId,
            String proxy,
            String userAgent,
            String asyncBaseUrl,
            String syncBaseUrl,
            OkHttpClient okHttpClient) {

        this.clientKey = requireClientKey(clientKey);
        this.timeout = requirePositive(orDefault(timeout, DEFAULT_TIMEOUT), "timeout");
        this.syncTimeout = requirePositive(orDefault(syncTimeout, DEFAULT_SYNC_TIMEOUT), "syncTimeout");
        this.pollInterval = orDefault(pollInterval, DEFAULT_POLL_INTERVAL);
        this.maxPollAttempts = orDefault(maxPollAttempts, DEFAULT_MAX_POLL_ATTEMPTS);
        requirePollingBudget(this.pollInterval, this.maxPollAttempts);
        this.appId = appId;
        this.proxy = proxy;
        this.userAgent = requireText(orDefault(userAgent, Version.DEFAULT_USER_AGENT), "userAgent");
        this.asyncBaseUrl = requireUrl(orDefault(asyncBaseUrl, DEFAULT_ASYNC_BASE_URL), "asyncBaseUrl");
        this.syncBaseUrl = requireUrl(orDefault(syncBaseUrl, DEFAULT_SYNC_BASE_URL), "syncBaseUrl");
        this.okHttpClient = okHttpClient;
    }

    /**
     * Rejects a budget that cannot produce a result.
     *
     * <p>Shared with the per-call overrides on the client, so a budget passed to one request is
     * held to the same rule as one set on the client — and both are checked before anything is
     * billed rather than on the first request.
     *
     * @throws EzCaptchaException when either value is not positive
     */
    static void requirePollingBudget(Duration interval, int maxAttempts) {
        if (interval == null || interval.isNegative() || interval.isZero()) {
            throw EzCaptchaException.config("pollInterval must be positive, got " + interval);
        }
        if (maxAttempts <= 0) {
            throw EzCaptchaException.config("maxPollAttempts must be positive, got " + maxAttempts);
        }
    }

    private static <T> T orDefault(T value, T fallback) {
        return value == null ? fallback : value;
    }

    private static String requireClientKey(String clientKey) {
        String resolved = isBlank(clientKey) ? System.getenv(DEFAULT_CLIENT_KEY_ENV) : clientKey;
        if (isBlank(resolved)) {
            throw EzCaptchaException.config(
                    "a client key is required; pass clientKey(...) or set " + DEFAULT_CLIENT_KEY_ENV);
        }
        if (!hasValidKeyCharacters(resolved)) {
            throw EzCaptchaException.config("client key contains invalid characters");
        }
        return resolved;
    }

    /**
     * Whether the key is printable ASCII with no surrounding whitespace.
     *
     * <p>The key also travels in a request header, and OkHttp rejects a header value with
     * control characters by throwing {@link IllegalArgumentException} while the request is
     * built — outside the SDK's own exception hierarchy, on every call. Checking here turns
     * that into one configuration error at construction.
     */
    private static boolean hasValidKeyCharacters(String key) {
        if (!key.equals(key.trim())) {
            return false;
        }
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (c < 0x20 || c > 0x7e) {
                return false;
            }
        }
        return true;
    }

    private static Duration requirePositive(Duration value, String name) {
        if (value.isZero() || value.isNegative()) {
            throw EzCaptchaException.config(name + " must be positive, got " + value);
        }
        return value;
    }

    private static String requireText(String value, String name) {
        if (isBlank(value)) {
            throw EzCaptchaException.config(name + " must not be blank");
        }
        return value;
    }

    private static String requireUrl(String value, String name) {
        requireText(value, name);
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            throw EzCaptchaException.config(
                    name + " must start with http:// or https://, got " + value);
        }
        // Trailing slashes are tolerated rather than rejected; the path joiner handles them,
        // and a copy-pasted URL ending in one is not a mistake worth failing a build over.
        return value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /** Both credentials are masked; this gets printed in diagnostics often enough to matter. */
    @Override
    public String toString() {
        return "ClientConfig{clientKey="
                + Redaction.mask(clientKey)
                + ", proxy="
                + Redaction.mask(proxy)
                + ", asyncBaseUrl="
                + asyncBaseUrl
                + ", syncBaseUrl="
                + syncBaseUrl
                + ", timeout="
                + timeout
                + ", syncTimeout="
                + syncTimeout
                + ", pollInterval="
                + pollInterval
                + ", maxPollAttempts="
                + maxPollAttempts
                + ", appId="
                + appId
                + ", userAgent="
                + userAgent
                + "}";
    }
}
