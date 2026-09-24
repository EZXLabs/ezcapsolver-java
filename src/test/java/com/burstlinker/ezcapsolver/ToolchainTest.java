package com.burstlinker.ezcapsolver;

import com.burstlinker.ezcapsolver.exception.EzCaptchaException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Proves the toolchain before any of it is relied on: the Java 8 floor, Lombok's
 * annotation processing, Jackson, and an OkHttp round-trip against MockWebServer.
 *
 * <p>These are not throwaway checks. Every one of them guards a promise that is otherwise
 * only asserted in a document, and each would fail silently — a Java 9 API call compiles
 * fine and only breaks on a user's Java 8 runtime; a misconfigured processor path turns
 * Lombok off and the failure looks like a hundred unrelated "cannot find symbol" errors.
 *
 * <p>No test in this repository ever reaches the real API. Creating a task is billed.
 */
class ToolchainTest {

    /**
     * The version constant and the POM must agree.
     *
     * <p>The release workflow checks this too, but only once a {@code v*} tag has been pushed —
     * the most expensive moment to discover it, since Maven Central never lets a coordinate be
     * replaced. Catching it here makes the mistake cost a test run instead.
     *
     * <p>Surefire runs with the project directory as its working directory, so the POM is
     * simply there to be read. Parsing it with a regex rather than an XML parser is deliberate:
     * the first {@code <version>} in this POM is the project's own, and pulling in a parser to
     * read one line would be the heavier mistake.
     */
    @Test
    @DisplayName("Version.VERSION matches the version in pom.xml")
    void versionConstantMatchesThePom() throws IOException {
        Path pom = Paths.get("pom.xml");
        assertTrue(Files.isRegularFile(pom), "expected to run with the project directory as the cwd");

        String xml = new String(Files.readAllBytes(pom), "UTF-8");

        // <modelVersion> and the <*.version> properties do not match this: the pattern needs a
        // '<' immediately followed by "version>", which only the project's own element has.
        Matcher m = Pattern.compile("<version>([^<]+)</version>").matcher(xml);
        assertTrue(m.find(), "no <version> element in pom.xml");

        assertEquals(
                m.group(1).trim(),
                Version.VERSION,
                "Version.VERSION and pom.xml disagree; the release workflow would reject the tag");
    }

    private MockWebServer server;

    @BeforeEach
    void startServer() throws IOException {
        // MockWebServer 4.x predates the JUnit 5 extension, which lives in the
        // mockwebserver3 artifact and is OkHttp 5 only. Driving the lifecycle by hand is
        // what keeps JUnit 4 off the test classpath.
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void stopServer() throws IOException {
        server.shutdown();
    }

    @Test
    @DisplayName("the published bytecode targets Java 8")
    void bytecodeTargetsJava8() throws IOException {
        // This is the whole Java 8 promise, made testable. `-source 8 -target 8` would
        // produce the same class file version while happily letting a Java 9+ API call
        // through, so the version alone is not the point -- `--release 8` in the POM is
        // what checks the API surface. This test catches the day someone "simplifies"
        // that property away.
        try (InputStream in = openClassFile(Version.class)) {
            assertNotNull(in, "Version.class must be on the test classpath");
            DataInputStream data = new DataInputStream(in);
            assertEquals(0xCAFEBABE, data.readInt(), "not a class file");
            data.readUnsignedShort(); // minor version, unused
            int major = data.readUnsignedShort();
            assertEquals(
                    52,
                    major,
                    "class file major version must be 52 (Java 8); got "
                            + major
                            + ". Check maven.compiler.release in pom.xml");
        }
    }

    @Test
    @DisplayName("Lombok generated the builder, the defaults and the getters")
    void lombokRan() {
        ClientConfig defaults = ClientConfig.builder().clientKey("test-key").build();
        assertEquals(ClientConfig.DEFAULT_POLL_INTERVAL, defaults.getPollInterval());
        assertEquals(ClientConfig.DEFAULT_MAX_POLL_ATTEMPTS, defaults.getMaxPollAttempts());
        assertEquals(ClientConfig.DEFAULT_TIMEOUT, defaults.getTimeout());

        ClientConfig custom =
                ClientConfig.builder()
                        .clientKey("test-key")
                        .pollInterval(Duration.ofMillis(1))
                        .maxPollAttempts(2)
                        .build();
        assertEquals(2, custom.getMaxPollAttempts());

        // toBuilder carries the untouched fields over, which is what makes an override a
        // one-liner instead of a full rebuild.
        ClientConfig overridden = custom.toBuilder().maxPollAttempts(7).build();
        assertEquals(7, overridden.getMaxPollAttempts());
        assertEquals(Duration.ofMillis(1), overridden.getPollInterval());
    }

    @Test
    @DisplayName("an unusable polling budget is rejected at build time, not at request time")
    void pollingValidation() {
        EzCaptchaException zeroAttempts =
                assertThrows(
                        EzCaptchaException.class,
                        () -> ClientConfig.builder().clientKey("k").maxPollAttempts(0).build());
        assertTrue(zeroAttempts.getMessage().contains("invalid client configuration"));

        assertThrows(
                EzCaptchaException.class,
                () -> ClientConfig.builder().clientKey("k").pollInterval(Duration.ZERO).build());
    }

    @Test
    @DisplayName("Jackson parses a response envelope and keeps unmodelled keys reachable")
    void jacksonRuns() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode parsed =
                mapper.readTree("{\"errorId\":0,\"status\":\"ready\",\"solution\":{\"token\":\"t\"}}");

        assertEquals(0, parsed.get("errorId").asInt());
        assertEquals("ready", parsed.get("status").asText());
        // The raw solution has to survive untouched -- that is the whole lossless-result
        // design, and JsonNode is what carries it.
        assertEquals("t", parsed.get("solution").get("token").asText());
    }

    @Test
    @DisplayName("OkHttp posts JSON to MockWebServer and reads the reply back")
    void okHttpRoundTrip() throws IOException, InterruptedException {
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("{\"errorId\":0,\"taskId\":\"task-1\"}"));

        OkHttpClient client =
                new OkHttpClient.Builder().callTimeout(5, TimeUnit.SECONDS).build();
        MediaType json = MediaType.parse("application/json; charset=utf-8");
        Request request =
                new Request.Builder()
                        .url(server.url("/createTask"))
                        .header("User-Agent", Version.DEFAULT_USER_AGENT)
                        .post(RequestBody.create("{\"clientKey\":\"k\"}", json))
                        .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.body());
            assertTrue(response.body().string().contains("task-1"));
        }

        RecordedRequest recorded = server.takeRequest();
        assertEquals("POST", recorded.getMethod());
        assertEquals("/createTask", recorded.getPath());
        assertEquals(Version.DEFAULT_USER_AGENT, recorded.getHeader("User-Agent"));
        assertTrue(recorded.getBody().readUtf8().contains("clientKey"));
    }

    @Test
    @DisplayName("a non-2xx status is returned rather than thrown")
    void nonSuccessStatusIsNotAnException() throws IOException {
        // errorId is the only success criterion. Most business errors arrive as HTTP 500
        // with a JSON body, so the transport must hand that body back instead of raising.
        server.enqueue(
                new MockResponse()
                        .setResponseCode(500)
                        .setHeader("Content-Type", "application/json")
                        .setBody("{\"errorId\":1,\"errorCode\":\"ERROR_ZERO_BALANCE\"}"));

        OkHttpClient client = new OkHttpClient();
        Request request =
                new Request.Builder()
                        .url(server.url("/createTask"))
                        .post(RequestBody.create("{}", MediaType.parse("application/json")))
                        .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(500, response.code());
            assertTrue(response.body().string().contains("ERROR_ZERO_BALANCE"));
        }
    }

    private static InputStream openClassFile(Class<?> type) {
        return type.getResourceAsStream("/" + type.getName().replace('.', '/') + ".class");
    }
}
