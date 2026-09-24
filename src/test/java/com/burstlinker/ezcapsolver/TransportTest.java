package com.burstlinker.ezcapsolver;

import com.burstlinker.ezcapsolver.exception.ApiException;
import com.burstlinker.ezcapsolver.exception.EzCaptchaException;
import com.burstlinker.ezcapsolver.exception.TransportException;
import com.burstlinker.ezcapsolver.exception.UnexpectedResponseException;
import com.burstlinker.ezcapsolver.model.CreateTaskResponse;
import com.burstlinker.ezcapsolver.model.QueryTaskResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * How one HTTP exchange is classified.
 *
 * <p>The interesting cases are the ones where the HTTP status and the service's own signal
 * disagree, which is the normal case rather than the exotic one: business errors arrive as
 * HTTP 500 and failed tasks as HTTP 200.
 *
 * <p>No test here reaches the real API. Creating a task is billed.
 */
class TransportTest {

    private MockWebServer server;
    private Transport transport;

    @BeforeEach
    void start() throws IOException {
        server = new MockWebServer();
        server.start();
        transport = new Transport(configFor(server));
    }

    @AfterEach
    void stop() throws IOException {
        server.shutdown();
    }

    private static ClientConfig configFor(MockWebServer server) {
        String base = server.url("/").toString();
        return ClientConfig.builder()
                .clientKey("test-key")
                .asyncBaseUrl(base)
                .syncBaseUrl(base)
                .timeout(Duration.ofSeconds(5))
                .syncTimeout(Duration.ofSeconds(5))
                .build();
    }

    private void enqueue(int status, String body) {
        server.enqueue(
                new MockResponse()
                        .setResponseCode(status)
                        .setHeader("Content-Type", "application/json")
                        .setBody(body));
    }

    private CreateTaskResponse createTask() {
        return transport.postAsync("/createTask", Collections.singletonMap("clientKey", "k"), CreateTaskResponse.class);
    }

    @Nested
    @DisplayName("success")
    class Success {

        @Test
        @DisplayName("a 200 with errorId 0 decodes into the response model")
        void decodesSuccess() {
            enqueue(200, "{\"errorId\":0,\"taskId\":\"task-1\",\"requestId\":\"req-1\"}");

            CreateTaskResponse response = createTask();

            assertEquals("task-1", response.getTaskId());
            assertEquals("req-1", response.getRequestId());
            assertTrue(response.isSuccess());
        }

        @Test
        @DisplayName("every request carries the headers the service expects")
        void sendsExpectedHeaders() throws InterruptedException {
            enqueue(200, "{\"errorId\":0,\"taskId\":\"task-1\"}");
            createTask();

            RecordedRequest sent = server.takeRequest();
            assertEquals("POST", sent.getMethod());
            assertEquals("/createTask", sent.getPath());
            assertEquals(Version.DEFAULT_USER_AGENT, sent.getHeader("User-Agent"));
            assertEquals("application/json", sent.getHeader("Accept"));
            assertEquals("test-key", sent.getHeader("X-API-Key"));
        }

        @Test
        @DisplayName("the SDK does not invent a correlation id of its own")
        void doesNotSendRequestId() throws InterruptedException {
            enqueue(200, "{\"errorId\":0,\"taskId\":\"task-1\",\"requestId\":\"req-1\"}");
            CreateTaskResponse response = createTask();

            // The correlation id is assigned at the gateway and propagated down; the SDK only
            // reads it back off the response. Sending one from here would compete with the id
            // the rest of the platform is already tracing on.
            assertNull(
                    server.takeRequest().getHeader("X-Request-Id"),
                    "the SDK must not send a correlation id");
            assertEquals("req-1", response.getRequestId(), "the service's id is what gets kept");
        }

    }

    @Nested
    @DisplayName("the status line and errorId disagree")
    class Disagreement {

        @Test
        @DisplayName("HTTP 500 with an error envelope is an ApiException, not a transport fault")
        void businessErrorArrivesAs500() {
            // This is the normal shape of a business error, not an edge case.
            enqueue(500, "{\"errorId\":1,\"errorCode\":\"ERROR_ZERO_BALANCE\","
                    + "\"errorDescription\":\"insufficient balance\"}");

            ApiException thrown = assertThrows(ApiException.class, TransportTest.this::createTask);

            assertEquals("ERROR_ZERO_BALANCE", thrown.getErrorCode());
            assertEquals("insufficient balance", thrown.getErrorDescription());
            assertEquals(500, thrown.getHttpStatus());
            assertTrue(thrown.isAuthenticationError(), "zero balance feeds the ban counter");
            assertTrue(thrown.isTerminal());
            assertFalse(thrown.isRateLimited());
        }

        @Test
        @DisplayName("HTTP 200 with errorId 1 is still a failure")
        void failedTaskArrivesAs200() {
            // A failed task comes back 200. Trusting the status line would report it solved.
            enqueue(200, "{\"errorId\":1,\"errorCode\":\"ERROR_CAPTCHA_UNSOLVABLE\",\"status\":\"error\"}");

            ApiException thrown =
                    assertThrows(
                            ApiException.class,
                            () -> transport.postAsync("/getTaskResult", Collections.emptyMap(), QueryTaskResponse.class));

            assertEquals("ERROR_CAPTCHA_UNSOLVABLE", thrown.getErrorCode());
            assertEquals(200, thrown.getHttpStatus());
            // A worker code is not in the service's own table, so it is neither terminal nor
            // rate limited as far as this SDK can tell -- which is exactly why retry cannot
            // be driven off !isTerminal().
            assertFalse(thrown.isTerminal());
            assertFalse(thrown.isRateLimited());
        }

        @Test
        @DisplayName("a throttled query is marked rate limited and nothing else")
        void throttlingIsItsOwnCategory() {
            enqueue(429, "{\"errorId\":1,\"errorCode\":\"ERROR_REQUEST_LIMIT\"}");

            ApiException thrown = assertThrows(ApiException.class, TransportTest.this::createTask);

            assertTrue(thrown.isRateLimited());
            // Must not feed the ban counter: polling through a refusal would otherwise dig
            // the hole deeper.
            assertFalse(thrown.isAuthenticationError());
            assertFalse(thrown.isTerminal());
        }
    }

    @Nested
    @DisplayName("malformed responses")
    class Malformed {

        @Test
        @DisplayName("a non-2xx with an HTML body is an ApiException carrying the status")
        void gatewayHtmlKeepsTheStatus() {
            // A gateway or WAF page. Still a failure from the service's side, so it is
            // reported with the same shape rather than as a decode error.
            server.enqueue(new MockResponse().setResponseCode(502).setBody("<html>bad gateway</html>"));

            ApiException thrown = assertThrows(ApiException.class, TransportTest.this::createTask);

            assertEquals(502, thrown.getHttpStatus());
            assertTrue(thrown.getErrorDescription().contains("bad gateway"));
            assertNull(thrown.getErrorCode());
            // The rendered message still has to be actionable with no code to show.
            assertTrue(thrown.getMessage().contains("UNKNOWN_API_ERROR"));
            assertTrue(thrown.getMessage().contains("502"));
        }

        @Test
        @DisplayName("a 200 that is not JSON is a decode failure, not an API error")
        void successStatusWithGarbageBody() {
            server.enqueue(new MockResponse().setResponseCode(200).setBody("<html>hello</html>"));

            UnexpectedResponseException thrown =
                    assertThrows(UnexpectedResponseException.class, TransportTest.this::createTask);

            assertTrue(thrown.getBody().contains("hello"), "the body is the only thing left to diagnose with");
        }

        @Test
        @DisplayName("a 200 carrying a JSON array is refused")
        void successStatusWithNonObjectJson() {
            server.enqueue(new MockResponse().setResponseCode(200).setBody("[1,2,3]"));

            UnexpectedResponseException thrown =
                    assertThrows(UnexpectedResponseException.class, TransportTest.this::createTask);

            assertTrue(thrown.getReason().contains("not a JSON object"));
        }

        @Test
        @DisplayName("validation messages survive onto the exception and into the message")
        void validationErrorsAreKept() {
            enqueue(400, "{\"errorId\":1,\"errorCode\":\"ERROR_REQUEST_PARAMETERS\","
                    + "\"errorDescription\":\"invalid\",\"errors\":{\"websiteURL\":\"must not be blank\"}}");

            ApiException thrown = assertThrows(ApiException.class, TransportTest.this::createTask);

            assertEquals("must not be blank", thrown.getErrors().get("websiteURL"));
            assertTrue(thrown.getMessage().contains("websiteURL: must not be blank"));
        }
    }

    @Nested
    @DisplayName("transport faults")
    class Faults {

        @Test
        @DisplayName("a dropped connection is a TransportException with the cause reachable")
        void droppedConnection() {
            server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

            TransportException thrown =
                    assertThrows(TransportException.class, TransportTest.this::createTask);

            assertTrue(thrown.getOperation().startsWith("POST http"));
            assertNotNull(thrown.getCause(), "the underlying IOException must stay reachable");
            // Nothing was billed, so there is no task to recover.
            assertNull(EzCaptchaException.taskIdOf(thrown));
        }

        @Test
        @DisplayName("the operation never contains the client key")
        void operationCarriesNoCredentials() {
            server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

            TransportException thrown =
                    assertThrows(TransportException.class, TransportTest.this::createTask);

            assertFalse(thrown.getOperation().contains("test-key"));
            assertFalse(thrown.getMessage().contains("test-key"));
        }
    }

    @Nested
    @DisplayName("the two timeout budgets")
    class Budgets {

        @Test
        @DisplayName("a slow reply that the async budget cuts off, the sync budget tolerates")
        void budgetsAreIndependent() {
            // The whole point of keeping them apart, made observable. A synchronous task
            // blocks until the worker answers -- up to three minutes on some types -- and
            // cutting that off at the async timeout wastes a call that is already billed and
            // returns no task id to recover it with.
            ClientConfig config =
                    ClientConfig.builder()
                            .clientKey("test-key")
                            .asyncBaseUrl(server.url("/").toString())
                            .syncBaseUrl(server.url("/").toString())
                            .timeout(Duration.ofMillis(300))
                            .syncTimeout(Duration.ofSeconds(5))
                            .build();
            Transport split = new Transport(config);

            server.enqueue(slowReply());
            assertThrows(
                    TransportException.class,
                    () -> split.postAsync("/createTask", Collections.emptyMap(), CreateTaskResponse.class),
                    "the short budget should have given up on a 1s reply");

            server.enqueue(slowReply());
            CreateTaskResponse response =
                    split.postSync("/createSyncTask", Collections.emptyMap(), CreateTaskResponse.class);
            assertEquals("task-1", response.getTaskId(), "the long budget should have waited");
        }

        private MockResponse slowReply() {
            return new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody("{\"errorId\":0,\"taskId\":\"task-1\"}")
                    .setBodyDelay(1, TimeUnit.SECONDS);
        }

        @Test
        @DisplayName("each endpoint goes to the host paired with its budget")
        void hostsArePairedWithBudgets() throws Exception {
            MockWebServer syncHost = new MockWebServer();
            syncHost.start();
            try {
                ClientConfig config =
                        ClientConfig.builder()
                                .clientKey("test-key")
                                .asyncBaseUrl(server.url("/").toString())
                                .syncBaseUrl(syncHost.url("/").toString())
                                .build();
                Transport split = new Transport(config);

                syncHost.enqueue(
                        new MockResponse()
                                .setResponseCode(200)
                                .setHeader("Content-Type", "application/json")
                                .setBody("{\"errorId\":0,\"taskId\":\"task-1\"}"));

                split.postSync("/createSyncTask", Collections.emptyMap(), CreateTaskResponse.class);

                // The service splits the two across hosts. Sending a sync task to the async
                // host is another way to waste a billed call.
                assertEquals("/createSyncTask", syncHost.takeRequest().getPath());
                assertEquals(0, server.getRequestCount(), "the async host should not have been touched");
            } finally {
                syncHost.shutdown();
            }
        }
    }

    @Nested
    @DisplayName("URL joining")
    class Urls {

        @Test
        @DisplayName("a slash on either side, or both, produces one slash")
        void tolerantJoin() {
            assertEquals("https://a.example/createTask", Transport.join("https://a.example", "/createTask"));
            assertEquals("https://a.example/createTask", Transport.join("https://a.example/", "/createTask"));
            assertEquals("https://a.example/createTask", Transport.join("https://a.example/", "createTask"));
            assertEquals("https://a.example/createTask", Transport.join("https://a.example", "createTask"));
        }
    }
}
