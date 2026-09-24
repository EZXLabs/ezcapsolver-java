package com.burstlinker.ezcapsolver;

import com.burstlinker.ezcapsolver.exception.*;
import com.burstlinker.ezcapsolver.internal.Json;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.HCaptchaSolution;
import com.burstlinker.ezcapsolver.model.task.HCaptchaTaskParams;
import com.burstlinker.ezcapsolver.model.task.TaskType;
import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The orchestration on top of the four endpoints.
 *
 * <p>Most of what is asserted here is one rule seen from different angles: <strong>once a task
 * exists it has been billed, so no failure may lose its id.</strong> Losing it means paying
 * for work that can never be collected.
 *
 * <p>No test here reaches the real API. Creating a task is billed.
 */
class EzCapSolverClientTest {

    private MockWebServer server;
    private EzCapSolverClient client;

    @BeforeEach
    void start() throws IOException {
        server = new MockWebServer();
        server.start();
        client = clientWith(Duration.ofMillis(1), 3);
    }

    @AfterEach
    void stop() throws IOException {
        server.shutdown();
    }

    private EzCapSolverClient clientWith(Duration pollInterval, int maxPollAttempts) {
        String base = server.url("/").toString();
        return EzCapSolverClient.builder()
                .clientKey("test-key")
                .asyncBaseUrl(base)
                .syncBaseUrl(base)
                .timeout(Duration.ofSeconds(5))
                .syncTimeout(Duration.ofSeconds(5))
                .pollInterval(pollInterval)
                .maxPollAttempts(maxPollAttempts)
                .build();
    }

    private void enqueue(String body) {
        enqueue(200, body);
    }

    private void enqueue(int status, String body) {
        server.enqueue(
                new MockResponse()
                        .setResponseCode(status)
                        .setHeader("Content-Type", "application/json")
                        .setBody(body));
    }

    private static HCaptchaTaskParams params() {
        return HCaptchaTaskParams.builder()
                .websiteUrl("https://example.com")
                .websiteKey("site-key")
                .lang("en-US")
                .build();
    }

    private static final String READY =
            "{\"errorId\":0,\"status\":\"ready\",\"requestId\":\"req-result\","
                    + "\"solution\":{\"generated_pass_UUID\":\"pass-1\",\"ua\":\"UA/1.0\"}}";

    private static final String PROCESSING = "{\"errorId\":0,\"status\":\"processing\"}";

    private static final String CREATED =
            "{\"errorId\":0,\"taskId\":\"task-1\",\"requestId\":\"req-create\"}";

    @Nested
    @DisplayName("the happy path")
    class HappyPath {

        @Test
        @DisplayName("solve creates a task, polls, and decodes the solution")
        void solveEndToEnd() {
            enqueue(CREATED);
            enqueue(PROCESSING);
            enqueue(READY);

            Solved<HCaptchaSolution> solved = client.solveHCaptcha(params());

            assertEquals("pass-1", solved.getSolution().getGeneratedPassUuid());
            assertEquals("UA/1.0", solved.getSolution().getUa());
            assertEquals("task-1", solved.getTaskId());
            assertEquals(3, server.getRequestCount(), "create + processing + ready");
        }

        @Test
        @DisplayName("the raw solution travels alongside the typed one")
        void rawIsKept() {
            enqueue(CREATED);
            enqueue(READY);

            Solved<HCaptchaSolution> solved = client.solveHCaptcha(params());

            // What makes a worker-side field rename an inconvenience rather than an outage.
            assertNotNull(solved.getRaw());
            assertEquals("pass-1", solved.getRaw().get("generated_pass_UUID").asText());
        }

        @Test
        @DisplayName("requestId comes from the result, falling back to the creating call")
        void requestIdPrefersTheResult() {
            enqueue(CREATED);
            enqueue(READY);
            assertEquals("req-result", client.solveHCaptcha(params()).getRequestId());

            enqueue(CREATED);
            enqueue("{\"errorId\":0,\"status\":\"ready\",\"solution\":{\"generated_pass_UUID\":\"p\"}}");
            assertEquals(
                    "req-create",
                    client.solveHCaptcha(params()).getRequestId(),
                    "with no id on the result, the creating call's is the fallback");
        }

        @Test
        @DisplayName("the request body carries the type, the key and the flattened params")
        void requestBodyShape() throws Exception {
            enqueue(CREATED);
            enqueue(READY);
            client.solveHCaptcha(params());

            JsonNode body = Json.mapper().readTree(server.takeRequest().getBody().readUtf8());
            assertEquals("test-key", body.get("clientKey").asText());

            JsonNode task = body.get("task");
            assertEquals(TaskType.HCAPTCHA, task.get("type").asText());
            assertEquals("https://example.com", task.get("websiteURL").asText());
            // Flattened into the task object, not nested under a key of its own.
            assertFalse(task.has("extra"));
            // appId is unset, so it must not appear at all rather than appear as null.
            assertFalse(body.has("appId"));
        }

        @Test
        @DisplayName("the SDK's task type overrides one smuggled in through extra")
        void typeAlwaysWins() throws Exception {
            HCaptchaTaskParams params = params();
            params.put("type", "SomethingElse");

            enqueue(CREATED);
            enqueue(READY);
            client.solveHCaptcha(params);

            JsonNode task =
                    Json.mapper().readTree(server.takeRequest().getBody().readUtf8()).get("task");
            assertEquals(TaskType.HCAPTCHA, task.get("type").asText());
        }

        @Test
        @DisplayName("the first result query waits rather than firing immediately")
        void waitsBeforeTheFirstQuery() {
            // A task created a moment ago is still queued; asking at once only spends a
            // request to be told `processing`.
            EzCapSolverClient slow = clientWith(Duration.ofMillis(300), 2);
            enqueue(CREATED);
            enqueue(READY);

            long started = System.nanoTime();
            slow.solveHCaptcha(params());
            long elapsedMs = (System.nanoTime() - started) / 1_000_000L;

            assertTrue(elapsedMs >= 250, "expected a wait before the first query, took " + elapsedMs + "ms");
        }

        @Test
        @DisplayName("getBalance returns the number")
        void balance() {
            enqueue("{\"errorId\":0,\"balance\":12.3456}");

            // compareTo, not equals: BigDecimal.equals also compares scale, so 12.3456
            // and 12.34560 would be reported as different balances.
            assertEquals(0, client.getBalance().compareTo(new BigDecimal("12.3456")));
        }

        @Test
        @DisplayName("a balance response with no balance is refused rather than read as zero")
        void missingBalanceIsNotZero() {
            enqueue("{\"errorId\":0}");
            // Returning 0.0 would read as "out of credit" and stop a caller spending for a
            // reason that is not true.
            assertThrows(EzCaptchaException.class, () -> client.getBalance());
        }
    }

    @Nested
    @DisplayName("a billed task is never lost")
    class BilledTaskSurvives {

        @Test
        @DisplayName("an exhausted budget reports the task id")
        void pollingExhausted() {
            enqueue(CREATED);
            enqueue(PROCESSING);
            enqueue(PROCESSING);
            enqueue(PROCESSING);

            PollingExhaustedException thrown =
                    assertThrows(PollingExhaustedException.class, () -> client.solveHCaptcha(params()));

            assertEquals("task-1", thrown.getTaskId());
            assertEquals(3, thrown.getAttempts());
            assertEquals("task-1", EzCaptchaException.taskIdOf(thrown));
        }

        @Test
        @DisplayName("an API error during polling carries the task id")
        void apiErrorWhilePolling() {
            enqueue(CREATED);
            enqueue(500, "{\"errorId\":1,\"errorCode\":\"ERROR_TASK_NOT_EXIST\"}");

            ApiException thrown =
                    assertThrows(ApiException.class, () -> client.solveHCaptcha(params()));

            // The service does not echo the id back, so the SDK attaches it.
            assertEquals("task-1", thrown.getTaskId());
            assertEquals("task-1", EzCaptchaException.taskIdOf(thrown));
        }

        @Test
        @DisplayName("a dropped connection during polling is wrapped so the id survives")
        void transportFaultWhilePolling() {
            enqueue(CREATED);
            server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

            WaitInterruptedException thrown =
                    assertThrows(WaitInterruptedException.class, () -> client.solveHCaptcha(params()));

            assertEquals("task-1", thrown.getTaskId());
            assertEquals("req-create", thrown.getRequestId());
            // The original classification must stay reachable through the wrapper.
            assertNotNull(thrown.getCause());
            assertEquals("task-1", EzCaptchaException.taskIdOf(thrown));
        }

        @Test
        @DisplayName("an undecodable solution keeps both the id and the raw payload")
        void solutionDecodeFailure() {
            enqueue(CREATED);
            // The worker renamed the field this release declares required.
            enqueue("{\"errorId\":0,\"status\":\"ready\",\"solution\":{\"renamed_pass\":\"p\"}}");

            SolutionDecodeException thrown =
                    assertThrows(SolutionDecodeException.class, () -> client.solveHCaptcha(params()));

            assertEquals("task-1", thrown.getTaskId());
            // The answer was paid for; it is still here even though the model did not fit.
            assertEquals("p", thrown.getRaw().get("renamed_pass").asText());
            assertEquals("task-1", EzCaptchaException.taskIdOf(thrown));
        }

        @Test
        @DisplayName("a ready result with no solution still reports the id")
        void readyWithoutSolution() {
            enqueue(CREATED);
            enqueue("{\"errorId\":0,\"status\":\"ready\"}");

            WaitInterruptedException thrown =
                    assertThrows(WaitInterruptedException.class, () -> client.solveHCaptcha(params()));

            assertEquals("task-1", thrown.getTaskId());
            assertTrue(thrown.getCause().getMessage().contains("solution"));
        }

        @Test
        @DisplayName("a recovered wait picks the task up where solve left it")
        void waitForResultRecovers() {
            enqueue(CREATED);
            enqueue(PROCESSING);
            enqueue(PROCESSING);
            enqueue(PROCESSING);

            String taskId =
                    EzCaptchaException.taskIdOf(
                            assertThrows(EzCaptchaException.class, () -> client.solveHCaptcha(params())));
            assertNotNull(taskId);

            // The whole point: no second task, so nothing is billed twice.
            enqueue(READY);
            Solved<HCaptchaSolution> solved = client.waitForResult(taskId, HCaptchaSolution.class);
            assertEquals("pass-1", solved.getSolution().getGeneratedPassUuid());
        }

        @Test
        @DisplayName("a failure before the task exists reports no id")
        void nothingBilledMeansNothingToRecover() {
            enqueue(500, "{\"errorId\":1,\"errorCode\":\"ERROR_ZERO_BALANCE\"}");

            ApiException thrown =
                    assertThrows(ApiException.class, () -> client.solveHCaptcha(params()));

            assertNull(thrown.getTaskId());
            assertNull(EzCaptchaException.taskIdOf(thrown), "nothing was billed");
        }

        @Test
        @DisplayName("a created task with no id is refused rather than returned blank")
        void createdWithoutTaskId() {
            enqueue("{\"errorId\":0}");

            // errorId said success, so a task may exist -- and with no id it is unreachable.
            assertThrows(EzCaptchaException.class, () -> client.solveHCaptcha(params()));
        }
    }

    @Nested
    @DisplayName("throttling during polling")
    class Throttling {

        @Test
        @DisplayName("a throttled query is retried rather than ending the wait")
        void rateLimitedPollIsRetried() {
            enqueue(CREATED);
            enqueue(429, "{\"errorId\":1,\"errorCode\":\"ERROR_REQUEST_LIMIT\"}");
            enqueue(429, "{\"errorId\":1,\"errorCode\":\"ERROR_REQUEST_BANNED\"}");
            enqueue(READY);

            // The service refused the query before looking the task up, so the task kept
            // running. Ending the wait here would abandon something already paid for.
            Solved<HCaptchaSolution> solved = client.solveHCaptcha(params());

            assertEquals("pass-1", solved.getSolution().getGeneratedPassUuid());
            assertEquals(4, server.getRequestCount());
        }

        @Test
        @DisplayName("throttling still consumes the budget, so it cannot loop forever")
        void rateLimitedPollsAreNotFree() {
            enqueue(CREATED);
            for (int i = 0; i < 3; i++) {
                enqueue(429, "{\"errorId\":1,\"errorCode\":\"ERROR_REQUEST_LIMIT\"}");
            }

            PollingExhaustedException thrown =
                    assertThrows(PollingExhaustedException.class, () -> client.solveHCaptcha(params()));
            assertEquals("task-1", thrown.getTaskId());
        }

        @Test
        @DisplayName("a worker failure ends the wait instead of being retried")
        void workerFailureIsNotRetried() {
            enqueue(CREATED);
            enqueue("{\"errorId\":1,\"errorCode\":\"ERROR_CAPTCHA_UNSOLVABLE\",\"status\":\"error\"}");

            // isTerminal() is false for this code, as it is for every code this release does
            // not know. Driving retry off !isTerminal() would poll a dead task to exhaustion.
            ApiException thrown =
                    assertThrows(ApiException.class, () -> client.solveHCaptcha(params()));

            assertFalse(thrown.isRateLimited());
            assertEquals(2, server.getRequestCount(), "the wait should have ended on the first answer");
        }
    }

    @Nested
    @DisplayName("the synchronous endpoint")
    class Sync {

        @Test
        @DisplayName("syncSolve answers from the creating request")
        void syncSolveEndToEnd() {
            enqueue("{\"errorId\":0,\"taskId\":\"sync-1\",\"status\":\"ready\","
                    + "\"solution\":{\"generated_pass_UUID\":\"pass-2\"}}");

            Solved<HCaptchaSolution> solved = client.syncSolveHCaptcha(params());

            assertEquals("pass-2", solved.getSolution().getGeneratedPassUuid());
            // The sync endpoint does assign an id, and it is kept: it is the only handle on a
            // task that has already been billed.
            assertEquals("sync-1", solved.getTaskId());
            assertEquals(1, server.getRequestCount());
        }

        @Test
        @DisplayName("a usable solution is accepted whatever the status field says")
        void statusIsNotGatekeeper() {
            // Being strict about a field nobody reads would throw away a billed result.
            enqueue("{\"errorId\":0,\"taskId\":\"sync-1\","
                    + "\"solution\":{\"generated_pass_UUID\":\"pass-2\"}}");

            assertEquals(
                    "pass-2", client.syncSolveHCaptcha(params()).getSolution().getGeneratedPassUuid());
        }
    }

    @Nested
    @DisplayName("the escape hatch")
    class Raw {

        @Test
        @DisplayName("an unmodelled task type works without an SDK update")
        void solveRawUnknownType() throws Exception {
            enqueue(CREATED);
            enqueue("{\"errorId\":0,\"status\":\"ready\",\"solution\":{\"anything\":\"at all\"}}");

            Solved<JsonNode> solved =
                    client.solveRaw(
                            "BrandNewTaskType", Collections.singletonMap("websiteURL", "https://x"));

            assertEquals("at all", solved.getSolution().get("anything").asText());
            assertEquals("task-1", solved.getTaskId());

            RecordedRequest sent = server.takeRequest();
            JsonNode task = Json.mapper().readTree(sent.getBody().readUtf8()).get("task");
            assertEquals("BrandNewTaskType", task.get("type").asText());
            assertEquals("https://x", task.get("websiteURL").asText());
        }

        @Test
        @DisplayName("the same escape hatch exists on the synchronous endpoint")
        void syncSolveRawUnknownType() throws Exception {
            enqueue("{\"errorId\":0,\"taskId\":\"sync-raw-1\",\"status\":\"ready\","
                    + "\"solution\":{\"anything\":\"at all\"}}");

            Solved<JsonNode> solved =
                    client.syncSolveRaw(
                            "BrandNewSyncTaskType",
                            Collections.singletonMap("websiteURL", "https://x"));

            assertEquals("at all", solved.getSolution().get("anything").asText());
            // The sync endpoint assigns an id of its own, and it is the only handle on a task
            // that has already been billed.
            assertEquals("sync-raw-1", solved.getTaskId());
            assertEquals(1, server.getRequestCount(), "answered without a second request");

            RecordedRequest sent = server.takeRequest();
            assertEquals("/createSyncTask", sent.getPath());
            JsonNode task = Json.mapper().readTree(sent.getBody().readUtf8()).get("task");
            assertEquals("BrandNewSyncTaskType", task.get("type").asText());
            assertEquals("https://x", task.get("websiteURL").asText());
        }
    }

    @Nested
    @DisplayName("argument checks happen before anything is billed")
    class Guards {

        @Test
        @DisplayName("new EzCapSolverClient() is builder().build() with no arguments")
        void noArgConstructor() {
            boolean keyInEnvironment =
                    System.getenv(ClientConfig.DEFAULT_CLIENT_KEY_ENV) != null;

            if (!keyInEnvironment) {
                // No key anywhere, so the shorthand has to refuse exactly like the long form.
                assertThrows(EzCaptchaException.class, () -> new EzCapSolverClient());
                return;
            }

            ClientConfig shorthand = new EzCapSolverClient().config();
            ClientConfig longhand = EzCapSolverClient.builder().build().config();

            assertEquals(longhand.getClientKey(), shorthand.getClientKey());
            assertEquals(longhand.getTimeout(), shorthand.getTimeout());
            assertEquals(longhand.getSyncTimeout(), shorthand.getSyncTimeout());
            assertEquals(longhand.getPollInterval(), shorthand.getPollInterval());
            assertEquals(longhand.getMaxPollAttempts(), shorthand.getMaxPollAttempts());
            assertEquals(longhand.getAsyncBaseUrl(), shorthand.getAsyncBaseUrl());
            assertEquals(longhand.getSyncBaseUrl(), shorthand.getSyncBaseUrl());
            assertEquals(longhand.getUserAgent(), shorthand.getUserAgent());
        }

        @Test
        @DisplayName("the key and config constructors match their of(...) equivalents")
        void keyAndConfigConstructors() {
            ClientConfig fromKey = new EzCapSolverClient("a-key").config();
            ClientConfig viaFactory = EzCapSolverClient.of("a-key").config();

            assertEquals("a-key", fromKey.getClientKey());
            assertEquals(viaFactory.getTimeout(), fromKey.getTimeout());
            assertEquals(viaFactory.getUserAgent(), fromKey.getUserAgent());

            // The config constructor keeps the instance it was handed rather than rebuilding it.
            ClientConfig built = ClientConfig.builder().clientKey("a-key").build();
            assertSame(built, new EzCapSolverClient(built).config());
            assertSame(built, EzCapSolverClient.of(built).config());

            // Null is refused before a Transport is ever constructed.
            assertThrows(
                    EzCaptchaException.class, () -> new EzCapSolverClient((ClientConfig) null));
        }

        @Test
        @DisplayName("a blank task type is refused without a request")
        void blankTaskType() {
            assertThrows(EzCaptchaException.class, () -> client.solveRaw("", Collections.emptyMap()));
            assertEquals(0, server.getRequestCount());
        }

        @Test
        @DisplayName("a blank task id is refused without a request")
        void blankTaskId() {
            assertThrows(
                    EzCaptchaException.class, () -> client.waitForResult("", HCaptchaSolution.class));
            assertEquals(0, server.getRequestCount());
        }

        @Test
        @DisplayName("an unusable per-call budget is refused before the task is created")
        void badPerCallBudget() {
            assertThrows(
                    EzCaptchaException.class,
                    () -> client.solve(TaskType.HCAPTCHA, params(), Duration.ZERO, 5));
            assertEquals(0, server.getRequestCount(), "nothing may be billed on a bad budget");
        }

        @Test
        @DisplayName("task parameters that are not an object are refused")
        void nonObjectParams() {
            assertThrows(EzCaptchaException.class, () -> client.solveRaw("SomeType", "a bare string"));
            assertEquals(0, server.getRequestCount());
        }
    }

    @Nested
    @DisplayName("the client key header")
    class ClientKeyHeader {

        /** Drives all four endpoints once and checks each request carried the key. */
        private void assertEveryEndpointSendsTheKey(EzCapSolverClient subject) throws InterruptedException {
            enqueue(CREATED);
            enqueue(READY);
            enqueue(READY);
            enqueue("{\"errorId\":0,\"balance\":1.5}");

            subject.solve(TaskType.HCAPTCHA, params());
            subject.syncSolve(TaskType.HCAPTCHA, params());
            subject.getBalance();

            String[] paths = {"/createTask", "/getTaskResult", "/createSyncTask", "/getBalance"};
            for (String path : paths) {
                RecordedRequest sent = server.takeRequest(1, java.util.concurrent.TimeUnit.SECONDS);
                assertNotNull(sent, "no request reached " + path);
                assertEquals(path, sent.getPath());
                assertEquals("test-key", sent.getHeader("X-API-Key"), path + " must carry the key");
            }
        }

        @Test
        @DisplayName("every endpoint sends it")
        void everyEndpointSendsTheKey() throws InterruptedException {
            assertEveryEndpointSendsTheKey(client);
        }

        @Test
        @DisplayName("a caller-supplied OkHttpClient still sends it")
        void aSuppliedOkHttpClientStillSendsTheKey() throws InterruptedException {
            // The supplied client carries none of the SDK's own headers, so the key has to be
            // added per request rather than baked into the client.
            String base = server.url("/").toString();
            EzCapSolverClient supplied = EzCapSolverClient.builder()
                    .clientKey("test-key")
                    .asyncBaseUrl(base)
                    .syncBaseUrl(base)
                    .pollInterval(Duration.ofMillis(1))
                    .okHttpClient(new okhttp3.OkHttpClient())
                    .build();

            assertEveryEndpointSendsTheKey(supplied);
        }
    }
}
