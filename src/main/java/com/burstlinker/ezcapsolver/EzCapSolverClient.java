package com.burstlinker.ezcapsolver;

import com.burstlinker.ezcapsolver.exception.*;
import com.burstlinker.ezcapsolver.internal.Json;
import com.burstlinker.ezcapsolver.model.*;
import com.burstlinker.ezcapsolver.model.solution.*;
import com.burstlinker.ezcapsolver.model.task.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * The entry point: four endpoints, the orchestration on top of them, and two convenience
 * methods per task type.
 *
 * <pre>{@code
 * // Defaults, with the key read from EZCAPTCHA_API_KEY
 * EzCapSolverClient client = new EzCapSolverClient();
 *
 * // Or configured
 * EzCapSolverClient client = EzCapSolverClient.builder().clientKey("...").build();
 *
 * HCaptchaTaskParams params = HCaptchaTaskParams.builder()
 *         .websiteUrl("https://example.com")
 *         .websiteKey("...")
 *         .lang("en-US")
 *         .build();
 *
 * Solved<HCaptchaSolution> solved = client.solveHCaptcha(params);
 * }</pre>
 *
 * <p>Thread-safe and meant to be shared. Building one per request throws away the connection
 * pool, which is most of what OkHttp is for.
 *
 * <h2>What this never does</h2>
 *
 * <p><strong>It does not retry a request.</strong> Creating a task is billed and is not
 * idempotent, and a timeout cannot tell you whether the service already accepted it. The one
 * exception is a <em>throttled result query</em> during polling, which the service refuses
 * before it looks the task up — so the task is untouched and asking again is free.
 *
 * <p><strong>It does not rate-limit itself.</strong> Both are the caller's policy to choose.
 */
public final class EzCapSolverClient {

    private static final Logger log = LoggerFactory.getLogger(EzCapSolverClient.class);

    private static final String PATH_CREATE_TASK = "/createTask";
    private static final String PATH_GET_TASK_RESULT = "/getTaskResult";
    private static final String PATH_CREATE_SYNC_TASK = "/createSyncTask";
    private static final String PATH_GET_BALANCE = "/getBalance";

    /** A ready result with no solution: the task was billed and the answer is gone. */
    private static final String MISSING_SOLUTION = "ready task result does not contain a solution";

    private final ClientConfig config;
    private final Transport transport;

    /**
     * A client on the default configuration, reading the key from the
     * {@value ClientConfig#DEFAULT_CLIENT_KEY_ENV} environment variable.
     *
     * <pre>{@code
     * EzCapSolverClient client = new EzCapSolverClient();
     * }</pre>
     *
     * <p>Equivalent to {@code EzCapSolverClient.builder().build()}. Throws {@link
     * com.burstlinker.ezcapsolver.exception.EzCaptchaException} when that variable is unset —
     * the key is the one thing with no usable default.
     */
    public EzCapSolverClient() {
        this(ClientConfig.builder().build());
    }

    /**
     * The common case: everything default except the key.
     *
     * <pre>{@code
     * EzCapSolverClient client = new EzCapSolverClient("your-key");
     * }</pre>
     *
     * <p>A blank key falls back to {@value ClientConfig#DEFAULT_CLIENT_KEY_ENV}, exactly as
     * {@link ClientConfig.ClientConfigBuilder#clientKey(String)} does.
     */
    public EzCapSolverClient(String clientKey) {
        this(ClientConfig.builder().clientKey(clientKey).build());
    }

    /**
     * A client from an already built configuration.
     *
     * <pre>{@code
     * EzCapSolverClient client = new EzCapSolverClient(config);
     * }</pre>
     */
    public EzCapSolverClient(ClientConfig config) {
        if (config == null) {
            throw EzCaptchaException.config("config must not be null");
        }
        this.config = config;
        this.transport = new Transport(config);
    }

    /** A client from an already built configuration. */
    public static EzCapSolverClient of(ClientConfig config) {
        return new EzCapSolverClient(config);
    }

    /** The common case: everything default except the key. */
    public static EzCapSolverClient of(String clientKey) {
        return new EzCapSolverClient(clientKey);
    }

    /** The configuration this client was built with. Credentials are masked in its string form. */
    public ClientConfig config() {
        return config;
    }

    // -- Endpoints ---------------------------------------------------------------------

    /**
     * Creates a task and returns its identifier, without waiting for a result.
     *
     * <p><strong>This is the call that costs money.</strong> Once it returns, a task exists
     * and has been billed whether or not anything else succeeds.
     */
    public CreateTaskResponse createTask(String taskType, Object params) {
        CreateTaskResponse created =
                transport.postAsync(PATH_CREATE_TASK, createBody(taskType, params), CreateTaskResponse.class);
        if (created.getTaskId() == null || created.getTaskId().isEmpty()) {
            // errorId said success, so a task may well exist -- and without an id it is
            // unreachable. Worth reporting loudly rather than returning a blank handle.
            throw new UnexpectedResponseException(
                    "task was created but the response carried no taskId", null);
        }
        return created;
    }

    /**
     * Queries one task's result. Free, and safe to repeat.
     *
     * <p>Returns the result whatever its state, including {@code processing}. A task that
     * <em>failed</em> arrives as an {@link ApiException} instead, because the service reports
     * it with a non-zero {@code errorId}.
     */
    public QueryTaskResponse getTaskResult(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            throw EzCaptchaException.config("taskId must not be blank");
        }
        ObjectNode body = Json.mapper().createObjectNode();
        body.put("clientKey", config.getClientKey());
        body.put("taskId", taskId);
        return transport.postAsync(PATH_GET_TASK_RESULT, body, QueryTaskResponse.class);
    }

    /**
     * Runs a task on the synchronous endpoint, which answers on the request that created it.
     *
     * <p>Billed like any other task. It blocks for as long as the worker takes, which is what
     * the separate {@code syncTimeout} budget exists for.
     */
    public QueryTaskResponse createSyncTask(String taskType, Object params) {
        return transport.postSync(PATH_CREATE_SYNC_TASK, createBody(taskType, params), QueryTaskResponse.class);
    }

    /** The account balance. The only endpoint that costs nothing to call. */
    public BigDecimal getBalance() {
        ObjectNode body = Json.mapper().createObjectNode();
        body.put("clientKey", config.getClientKey());
        QueryBalanceResponse response = transport.postAsync(PATH_GET_BALANCE, body, QueryBalanceResponse.class);
        if (response.getBalance() == null) {
            // Returning 0.0 here would read as "out of credit" and stop a caller spending for
            // a reason that is not true.
            throw new UnexpectedResponseException("balance response carried no balance", null);
        }
        return response.getBalance();
    }

    // -- Orchestration -----------------------------------------------------------------

    /** Creates a task on the asynchronous endpoint and polls until it finishes. */
    public <S> Solved<S> solve(String taskType, TaskParams<S> params) {
        return solve(taskType, params, config.getPollInterval(), config.getMaxPollAttempts());
    }

    /**
     * As {@link #solve(String, TaskParams)}, with a budget for this call only.
     *
     * <p>Task types differ widely in how long they take, so a client-wide budget sized for the
     * slowest one wastes time on every other. The product of the two still has to land inside
     * the five minutes the service holds a result.
     */
    public <S> Solved<S> solve(
            String taskType, TaskParams<S> params, Duration pollInterval, int maxPollAttempts) {
        if (params == null) {
            throw EzCaptchaException.config("task parameters must not be null");
        }
        return solveTyped(taskType, params, params.solutionType(), pollInterval, maxPollAttempts);
    }

    /**
     * Creates a task and returns its solution as untouched JSON.
     *
     * <p>The escape hatch: a task type the service adds after this release is usable
     * immediately, without waiting for an SDK update.
     *
     * <pre>{@code
     * Solved<JsonNode> solved = client.solveRaw("BrandNewTaskType",
     *         Collections.singletonMap("websiteURL", "https://example.com"));
     * }</pre>
     */
    public Solved<JsonNode> solveRaw(String taskType, Object params) {
        return solveTyped(
                taskType, params, JsonNode.class, config.getPollInterval(), config.getMaxPollAttempts());
    }

    /** Runs a task on the synchronous endpoint and decodes its inline solution. */
    public <S> Solved<S> syncSolve(String taskType, TaskParams<S> params) {
        if (params == null) {
            throw EzCaptchaException.config("task parameters must not be null");
        }
        return syncSolveTyped(taskType, params, params.solutionType());
    }

    /** {@link #solveRaw} on the synchronous endpoint. */
    public Solved<JsonNode> syncSolveRaw(String taskType, Object params) {
        return syncSolveTyped(taskType, params, JsonNode.class);
    }

    /**
     * Polls an existing task until it finishes.
     *
     * <p><strong>This is what makes a failed wait recoverable.</strong> When {@code solve}
     * gives up, the task is still running and already paid for; hand its id here rather than
     * creating a second one. {@link EzCaptchaException#taskIdOf} finds that id on any
     * exception that carries one.
     *
     * <p>The service holds a result for <strong>five minutes</strong> after creation. Past
     * that the id comes back as {@code ERROR_TASK_NOT_EXIST}.
     */
    public <S> Solved<S> waitForResult(String taskId, Class<S> solutionType) {
        return waitForResult(
                taskId, solutionType, config.getPollInterval(), config.getMaxPollAttempts());
    }

    /** As {@link #waitForResult(String, Class)}, with a budget for this call only. */
    public <S> Solved<S> waitForResult(
            String taskId, Class<S> solutionType, Duration pollInterval, int maxPollAttempts) {
        if (taskId == null || taskId.isEmpty()) {
            throw EzCaptchaException.config("taskId must not be blank");
        }
        if (solutionType == null) {
            throw EzCaptchaException.config("solutionType must not be null");
        }
        ClientConfig.requirePollingBudget(pollInterval, maxPollAttempts);
        return poll(taskId, solutionType, pollInterval, maxPollAttempts, null);
    }

    private <S> Solved<S> solveTyped(
            String taskType,
            Object params,
            Class<S> solutionType,
            Duration pollInterval,
            int maxPollAttempts) {

        // Checked before the task is created, so an unusable budget costs nothing.
        ClientConfig.requirePollingBudget(pollInterval, maxPollAttempts);

        CreateTaskResponse created = createTask(taskType, params);
        String taskId = created.getTaskId();

        // From here on the task exists and is billed. Every exit has to carry the id, or the
        // caller pays for work they cannot reach.
        try {
            return poll(taskId, solutionType, pollInterval, maxPollAttempts, created.getRequestId());
        } catch (TransportException e) {
            throw new WaitInterruptedException(taskId, created.getRequestId(), e);
        } catch (UnexpectedResponseException e) {
            throw new WaitInterruptedException(taskId, created.getRequestId(), e);
        }
        // ApiException, PollingExhaustedException, SolutionDecodeException and
        // WaitInterruptedException already carry the id, so wrapping them would only bury it
        // one level deeper.
    }

    private <S> Solved<S> syncSolveTyped(String taskType, Object params, Class<S> solutionType) {
        QueryTaskResponse result = createSyncTask(taskType, params);

        // The synchronous endpoint assigns a task id too, and it is kept rather than blanked:
        // it is the only handle on a task that has already been billed.
        String taskId = result.getTaskId();

        if (!result.hasSolution()) {
            // Deliberately checked before the status. A response carrying a usable solution is
            // usable whatever it says in `status`, and being strict there would throw away a
            // billed result over a field nobody reads.
            throw new UnexpectedResponseException(
                    "synchronous task returned no solution (status " + result.getStatus() + ")", null);
        }
        return toSolved(result, solutionType, taskId, null);
    }

    /**
     * The polling loop.
     *
     * @param createRequestId correlation id of the creating request, used when a result
     *     response carries none of its own; {@code null} when polling an existing task
     */
    private <S> Solved<S> poll(
            String taskId,
            Class<S> solutionType,
            Duration pollInterval,
            int maxPollAttempts,
            String createRequestId) {

        for (int attempt = 1; attempt <= maxPollAttempts; attempt++) {
            // Wait before the first query, not after. A task created a moment ago is still
            // queued, and asking immediately only spends a request to be told `processing`.
            sleep(pollInterval, taskId);

            QueryTaskResponse result;
            try {
                result = getTaskResult(taskId);
            } catch (ApiException e) {
                if (e.isRateLimited()) {
                    // The service refused the *query*, not the task: it never looked the task
                    // up, so the task is still running and still paid for. Spend the attempt
                    // and ask again.
                    log.warn(
                            "polling throttled, retrying: task_id={} attempt={} error_code={}",
                            taskId,
                            attempt,
                            e.getErrorCode());
                    continue;
                }
                throw e.getTaskId() != null ? e : e.withTaskId(taskId);
            }

            TaskStatus status = result.status();
            if (status == TaskStatus.PROCESSING) {
                continue;
            }
            if (status == TaskStatus.READY) {
                return toSolved(result, solutionType, taskId, createRequestId);
            }
            // Status says the task failed, but errorId said the exchange succeeded, so the
            // transport had nothing to raise. The envelope contradicts itself.
            throw new UnexpectedResponseException(
                    "task reported status 'error' with errorId 0", null);
        }
        throw new PollingExhaustedException(taskId, maxPollAttempts, pollInterval);
    }

    private <S> Solved<S> toSolved(
            QueryTaskResponse result, Class<S> solutionType, String taskId, String fallbackRequestId) {

        if (!result.hasSolution()) {
            throw new UnexpectedResponseException(MISSING_SOLUTION, null);
        }
        S solution = Json.decodeSolution(result.getSolution(), solutionType, taskId);

        // The fetching request's id is the more useful one; the creating request's is the
        // fallback so there is always something to quote in a support question.
        String requestId = result.getRequestId() != null ? result.getRequestId() : fallbackRequestId;
        return new Solved<S>(solution, result.getSolution(), taskId, requestId);
    }

    private static void sleep(Duration interval, String taskId) {
        try {
            Thread.sleep(interval.toMillis());
        } catch (InterruptedException e) {
            // Restore the flag before leaving. Swallowing it strands anything further up the
            // stack that is also waiting to be told to stop.
            Thread.currentThread().interrupt();
            throw new WaitInterruptedException(taskId, null, e);
        }
    }

    /** The {@code /createTask} and {@code /createSyncTask} body. */
    private ObjectNode createBody(String taskType, Object params) {
        if (taskType == null || taskType.isEmpty()) {
            throw EzCaptchaException.config("taskType must not be blank");
        }
        ObjectNode body = Json.mapper().createObjectNode();
        body.put("clientKey", config.getClientKey());
        if (config.getAppId() != null) {
            body.put("appId", config.getAppId());
        }
        body.set("task", taskPayload(taskType, params));
        return body;
    }

    /**
     * Merges the task type into the serialized parameters.
     *
     * <p>The type belongs in the same object as the parameters but is not a field on any
     * model: one model backs several types. It is written <strong>last</strong>, so it wins
     * over anything of the same name — including a stray {@code type} passed through the
     * pass-through map.
     */
    private ObjectNode taskPayload(String taskType, Object params) {
        ObjectNode payload;
        if (params == null) {
            payload = Json.mapper().createObjectNode();
        } else {
            JsonNode encoded = Json.mapper().valueToTree(params);
            if (encoded == null || encoded.isNull()) {
                payload = Json.mapper().createObjectNode();
            } else if (encoded.isObject()) {
                payload = (ObjectNode) encoded;
            } else {
                throw EzCaptchaException.config(
                        "task parameters must serialize to a JSON object, got " + encoded.getNodeType());
            }
        }
        payload.put("type", taskType);
        return payload;
    }

    // -- Convenience methods -----------------------------------------------------------
    //
    // Two per task type, one per endpoint, and the method name is always "solve" or
    // "syncSolve" plus the wire type name -- no exceptions, including where the catalog's own
    // spelling is irregular. TaskTypeCoverageTest holds that rule by reflection.
    //
    // Neither is a default: each says which endpoint it uses, so a caller never has to look up
    // how a type is classified. TaskType.isSync reports which one the service documents, and
    // guessing wrong costs a round trip rather than a task -- the rejection lands before
    // billing.

    /** Solves a ReCaptcha V2 challenge, polling for the result. */
    public Solved<ReCaptchaSolution> solveReCaptchaV2TaskProxyless(ReCaptchaV2TaskParams params) {
        return solve(TaskType.RECAPTCHA_V2_TASK_PROXYLESS, params);
    }

    /** Solves the same task as {@link #solveReCaptchaV2TaskProxyless} on the synchronous endpoint. */
    public Solved<ReCaptchaSolution> syncSolveReCaptchaV2TaskProxyless(ReCaptchaV2TaskParams params) {
        return syncSolve(TaskType.RECAPTCHA_V2_TASK_PROXYLESS, params);
    }

    /** Solves a ReCaptcha V2 challenge on the high-score queue, where the returned token scores at least 0.9, polling for the result. */
    public Solved<ReCaptchaSolution> solveReCaptchaV2TaskProxylessS9(ReCaptchaV2TaskParams params) {
        return solve(TaskType.RECAPTCHA_V2_TASK_PROXYLESS_S9, params);
    }

    /** Solves the same task as {@link #solveReCaptchaV2TaskProxylessS9} on the synchronous endpoint. */
    public Solved<ReCaptchaSolution> syncSolveReCaptchaV2TaskProxylessS9(ReCaptchaV2TaskParams params) {
        return syncSolve(TaskType.RECAPTCHA_V2_TASK_PROXYLESS_S9, params);
    }

    /** Solves a ReCaptcha V2 challenge carrying an {@code s} parameter, which routes it to the high-score IPv4 queue. The {@code s} parameter is not actually mandatory for this type, polling for the result. */
    public Solved<ReCaptchaSolution> solveReCaptchaV2STaskProxyless(ReCaptchaV2TaskParams params) {
        return solve(TaskType.RECAPTCHA_V2_S_TASK_PROXYLESS, params);
    }

    /** Solves the same task as {@link #solveReCaptchaV2STaskProxyless} on the synchronous endpoint. */
    public Solved<ReCaptchaSolution> syncSolveReCaptchaV2STaskProxyless(ReCaptchaV2TaskParams params) {
        return syncSolve(TaskType.RECAPTCHA_V2_S_TASK_PROXYLESS, params);
    }

    /** Solves a ReCaptcha V2 Enterprise challenge, polling for the result. */
    public Solved<ReCaptchaSolution> solveReCaptchaV2EnterpriseTaskProxyless(ReCaptchaV2TaskParams params) {
        return solve(TaskType.RECAPTCHA_V2_ENTERPRISE_TASK_PROXYLESS, params);
    }

    /** Solves the same task as {@link #solveReCaptchaV2EnterpriseTaskProxyless} on the synchronous endpoint. */
    public Solved<ReCaptchaSolution> syncSolveReCaptchaV2EnterpriseTaskProxyless(ReCaptchaV2TaskParams params) {
        return syncSolve(TaskType.RECAPTCHA_V2_ENTERPRISE_TASK_PROXYLESS, params);
    }

    /** Solves a ReCaptcha V2 Enterprise challenge carrying an {@code s} parameter, polling for the result. */
    public Solved<ReCaptchaSolution> solveReCaptchaV2SEnterpriseTaskProxyless(ReCaptchaV2TaskParams params) {
        return solve(TaskType.RECAPTCHA_V2_S_ENTERPRISE_TASK_PROXYLESS, params);
    }

    /** Solves the same task as {@link #solveReCaptchaV2SEnterpriseTaskProxyless} on the synchronous endpoint. */
    public Solved<ReCaptchaSolution> syncSolveReCaptchaV2SEnterpriseTaskProxyless(ReCaptchaV2TaskParams params) {
        return syncSolve(TaskType.RECAPTCHA_V2_S_ENTERPRISE_TASK_PROXYLESS, params);
    }

    /** Solves one ReCaptcha V2 image grid, polling for the result. */
    public Solved<ReClassificationSolution> solveReCaptchaV2Classification(ReCaptchaV2ClassificationTaskParams params) {
        return solve(TaskType.RECAPTCHA_V2_CLASSIFICATION, params);
    }

    /** Solves the same task as {@link #solveReCaptchaV2Classification} on the synchronous endpoint. */
    public Solved<ReClassificationSolution> syncSolveReCaptchaV2Classification(ReCaptchaV2ClassificationTaskParams params) {
        return syncSolve(TaskType.RECAPTCHA_V2_CLASSIFICATION, params);
    }

    /** Solves a ReCaptcha V3 challenge, polling for the result. */
    public Solved<ReCaptchaSolution> solveReCaptchaV3TaskProxyless(ReCaptchaV3TaskParams params) {
        return solve(TaskType.RECAPTCHA_V3_TASK_PROXYLESS, params);
    }

    /** Solves the same task as {@link #solveReCaptchaV3TaskProxyless} on the synchronous endpoint. */
    public Solved<ReCaptchaSolution> syncSolveReCaptchaV3TaskProxyless(ReCaptchaV3TaskParams params) {
        return syncSolve(TaskType.RECAPTCHA_V3_TASK_PROXYLESS, params);
    }

    /** Solves a ReCaptcha V3 challenge on the high-score queue, polling for the result. */
    public Solved<ReCaptchaSolution> solveReCaptchaV3TaskProxylessS9(ReCaptchaV3TaskParams params) {
        return solve(TaskType.RECAPTCHA_V3_TASK_PROXYLESS_S9, params);
    }

    /** Solves the same task as {@link #solveReCaptchaV3TaskProxylessS9} on the synchronous endpoint. */
    public Solved<ReCaptchaSolution> syncSolveReCaptchaV3TaskProxylessS9(ReCaptchaV3TaskParams params) {
        return syncSolve(TaskType.RECAPTCHA_V3_TASK_PROXYLESS_S9, params);
    }

    /** Solves a ReCaptcha V3 Enterprise challenge, polling for the result. */
    public Solved<ReCaptchaSolution> solveReCaptchaV3EnterpriseTaskProxyless(ReCaptchaV3TaskParams params) {
        return solve(TaskType.RECAPTCHA_V3_ENTERPRISE_TASK_PROXYLESS, params);
    }

    /** Solves the same task as {@link #solveReCaptchaV3EnterpriseTaskProxyless} on the synchronous endpoint. */
    public Solved<ReCaptchaSolution> syncSolveReCaptchaV3EnterpriseTaskProxyless(ReCaptchaV3TaskParams params) {
        return syncSolve(TaskType.RECAPTCHA_V3_ENTERPRISE_TASK_PROXYLESS, params);
    }

    /** Solves a ReCaptcha V3 Enterprise challenge on the high-score queue, polling for the result. */
    public Solved<ReCaptchaSolution> solveReCaptchaV3EnterpriseTaskProxylessS9(ReCaptchaV3TaskParams params) {
        return solve(TaskType.RECAPTCHA_V3_ENTERPRISE_TASK_PROXYLESS_S9, params);
    }

    /** Solves the same task as {@link #solveReCaptchaV3EnterpriseTaskProxylessS9} on the synchronous endpoint. */
    public Solved<ReCaptchaSolution> syncSolveReCaptchaV3EnterpriseTaskProxylessS9(ReCaptchaV3TaskParams params) {
        return syncSolve(TaskType.RECAPTCHA_V3_ENTERPRISE_TASK_PROXYLESS_S9, params);
    }

    /** Solves a FunCaptcha (Arkose Labs) challenge, polling for the result. */
    public Solved<FunCaptchaSolution> solveFuncaptchaTaskProxyless(FunCaptchaTaskParams params) {
        return solve(TaskType.FUNCAPTCHA_TASK_PROXYLESS, params);
    }

    /** Solves the same task as {@link #solveFuncaptchaTaskProxyless} on the synchronous endpoint. */
    public Solved<FunCaptchaSolution> syncSolveFuncaptchaTaskProxyless(FunCaptchaTaskParams params) {
        return syncSolve(TaskType.FUNCAPTCHA_TASK_PROXYLESS, params);
    }

    /** Solves one FunCaptcha image, polling for the result. */
    public Solved<FunCaptchaClassificationSolution> solveFunCaptchaClassification(FunCaptchaClassificationTaskParams params) {
        return solve(TaskType.FUNCAPTCHA_CLASSIFICATION, params);
    }

    /** Solves the same task as {@link #solveFunCaptchaClassification} on the synchronous endpoint. */
    public Solved<FunCaptchaClassificationSolution> syncSolveFunCaptchaClassification(FunCaptchaClassificationTaskParams params) {
        return syncSolve(TaskType.FUNCAPTCHA_CLASSIFICATION, params);
    }

    /** Solves a PerimeterX challenge, returning its clearance cookies, polling for the result. */
    public Solved<PerimeterXSolution> solvePerimeterX(PerimeterXTaskParams params) {
        return solve(TaskType.PERIMETER_X, params);
    }

    /** Solves the same task as {@link #solvePerimeterX} on the synchronous endpoint. */
    public Solved<PerimeterXSolution> syncSolvePerimeterX(PerimeterXTaskParams params) {
        return syncSolve(TaskType.PERIMETER_X, params);
    }

    /** Solves an HCaptcha challenge, polling for the result. */
    public Solved<HCaptchaSolution> solveHCaptcha(HCaptchaTaskParams params) {
        return solve(TaskType.HCAPTCHA, params);
    }

    /** Solves the same task as {@link #solveHCaptcha} on the synchronous endpoint. */
    public Solved<HCaptchaSolution> syncSolveHCaptcha(HCaptchaTaskParams params) {
        return syncSolve(TaskType.HCAPTCHA, params);
    }

    /** Solves one or more HCaptcha images, polling for the result. */
    public Solved<HCaptchaClassificationSolution> solveHCaptchaClassification(HCaptchaClassificationTaskParams params) {
        return solve(TaskType.HCAPTCHA_CLASSIFICATION, params);
    }

    /** Solves the same task as {@link #solveHCaptchaClassification} on the synchronous endpoint. */
    public Solved<HCaptchaClassificationSolution> syncSolveHCaptchaClassification(HCaptchaClassificationTaskParams params) {
        return syncSolve(TaskType.HCAPTCHA_CLASSIFICATION, params);
    }

    /** Solves one round of the Akamai Web sensor flow, polling for the result. */
    public Solved<AkamaiWebSolution> solveAkamaiWEBTaskProxyless(AkamaiWebTaskParams params) {
        return solve(TaskType.AKAMAI_WEB_TASK_PROXYLESS, params);
    }

    /** Solves the same task as {@link #solveAkamaiWEBTaskProxyless} on the synchronous endpoint. */
    public Solved<AkamaiWebSolution> syncSolveAkamaiWEBTaskProxyless(AkamaiWebTaskParams params) {
        return syncSolve(TaskType.AKAMAI_WEB_TASK_PROXYLESS, params);
    }

    /** Solves an Akamai SBSD sensor payload, polling for the result. */
    public Solved<AkamaiSbsdSolution> solveAkamaiSBSDTaskProxyless(AkamaiSbsdTaskParams params) {
        return solve(TaskType.AKAMAI_SBSD_TASK_PROXYLESS, params);
    }

    /** Solves the same task as {@link #solveAkamaiSBSDTaskProxyless} on the synchronous endpoint. */
    public Solved<AkamaiSbsdSolution> syncSolveAkamaiSBSDTaskProxyless(AkamaiSbsdTaskParams params) {
        return syncSolve(TaskType.AKAMAI_SBSD_TASK_PROXYLESS, params);
    }

    /** Solves one HTTP request forwarded through a worker's TLS fingerprint, polling for the result. */
    public Solved<TlsForwardSolution> solveTlsTask(TlsForwardTaskParams params) {
        return solve(TaskType.TLS_TASK, params);
    }

    /** Solves the same task as {@link #solveTlsTask} on the synchronous endpoint. */
    public Solved<TlsForwardSolution> syncSolveTlsTask(TlsForwardTaskParams params) {
        return syncSolve(TaskType.TLS_TASK, params);
    }

    /** Solves a Cloudflare five-second interstitial, polling for the result. */
    public Solved<CloudFlare5sSolution> solveCloudFlare5STask(CloudFlare5sTaskParams params) {
        return solve(TaskType.CLOUDFLARE_5S_TASK, params);
    }

    /** Solves the same task as {@link #solveCloudFlare5STask} on the synchronous endpoint. */
    public Solved<CloudFlare5sSolution> syncSolveCloudFlare5STask(CloudFlare5sTaskParams params) {
        return syncSolve(TaskType.CLOUDFLARE_5S_TASK, params);
    }

    /** Solves a Cloudflare Turnstile widget, polling for the result. */
    public Solved<CloudFlareTurnstileSolution> solveCloudFlareTurnstileTask(CloudFlareTurnstileTaskParams params) {
        return solve(TaskType.CLOUDFLARE_TURNSTILE_TASK, params);
    }

    /** Solves the same task as {@link #solveCloudFlareTurnstileTask} on the synchronous endpoint. */
    public Solved<CloudFlareTurnstileSolution> syncSolveCloudFlareTurnstileTask(CloudFlareTurnstileTaskParams params) {
        return syncSolve(TaskType.CLOUDFLARE_TURNSTILE_TASK, params);
    }

    /** Solves a DataDome challenge after an interception, polling for the result. */
    public Solved<DataDomeSolution> solveDataDomeTaskProxyless(DataDomeTaskParams params) {
        return solve(TaskType.DATADOME_TASK_PROXYLESS, params);
    }

    /** Solves the same task as {@link #solveDataDomeTaskProxyless} on the synchronous endpoint. */
    public Solved<DataDomeSolution> syncSolveDataDomeTaskProxyless(DataDomeTaskParams params) {
        return syncSolve(TaskType.DATADOME_TASK_PROXYLESS, params);
    }

    /** Solves a DataDome tags fingerprint report, polling for the result. */
    public Solved<DataDomeSolution> solveDataDomeTagsTaskProxyless(DataDomeTagsTaskParams params) {
        return solve(TaskType.DATADOME_TAGS_TASK_PROXYLESS, params);
    }

    /** Solves the same task as {@link #solveDataDomeTagsTaskProxyless} on the synchronous endpoint. */
    public Solved<DataDomeSolution> syncSolveDataDomeTagsTaskProxyless(DataDomeTagsTaskParams params) {
        return syncSolve(TaskType.DATADOME_TAGS_TASK_PROXYLESS, params);
    }

    /** Solves an Incapsula Reese84 sensor payload, polling for the result. */
    public Solved<IncapsulaSolution> solveIncapsulaTaskProxyless(IncapsulaTaskParams params) {
        return solve(TaskType.INCAPSULA_TASK_PROXYLESS, params);
    }

    /** Solves the same task as {@link #solveIncapsulaTaskProxyless} on the synchronous endpoint. */
    public Solved<IncapsulaSolution> syncSolveIncapsulaTaskProxyless(IncapsulaTaskParams params) {
        return syncSolve(TaskType.INCAPSULA_TASK_PROXYLESS, params);
    }

    // -- Builder -----------------------------------------------------------------------

    /** A client builder carrying every configuration option. */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Delegates to {@link ClientConfig}'s builder so there is one place where a setting is
     * defined, defaulted and checked — and one place to add the next one.
     */
    public static final class Builder {

        private final ClientConfig.ClientConfigBuilder delegate = ClientConfig.builder();

        private Builder() {}

        /** The API key. Falls back to {@code EZCAPTCHA_API_KEY} when left unset. */
        public Builder clientKey(String clientKey) {
            delegate.clientKey(clientKey);
            return this;
        }

        /** Bounds one asynchronous or balance request. Defaults to 30 seconds. */
        public Builder timeout(Duration timeout) {
            delegate.timeout(timeout);
            return this;
        }

        /** Bounds one synchronous task request. Defaults to 240 seconds; keep it generous. */
        public Builder syncTimeout(Duration syncTimeout) {
            delegate.syncTimeout(syncTimeout);
            return this;
        }

        /** Delay between result queries. Defaults to 3 seconds. */
        public Builder pollInterval(Duration pollInterval) {
            delegate.pollInterval(pollInterval);
            return this;
        }

        /**
         * Result queries before a wait gives up. Defaults to 50, which with the default
         * interval is 150 seconds — inside the five minutes the service holds a result.
         */
        public Builder maxPollAttempts(int maxPollAttempts) {
            delegate.maxPollAttempts(maxPollAttempts);
            return this;
        }

        /** Optional application identifier, sent with every created task. */
        public Builder appId(Integer appId) {
            delegate.appId(appId);
            return this;
        }

        /**
         * Proxy the SDK itself dials out through. Unrelated to a task's own {@code proxy},
         * which is the one the worker uses to reach the protected site.
         */
        public Builder proxy(String proxy) {
            delegate.proxy(proxy);
            return this;
        }

        /** Sent as {@code User-Agent}. */
        public Builder userAgent(String userAgent) {
            delegate.userAgent(userAgent);
            return this;
        }

        /** Base URL for asynchronous tasks and the balance query. */
        public Builder asyncBaseUrl(String asyncBaseUrl) {
            delegate.asyncBaseUrl(asyncBaseUrl);
            return this;
        }

        /** Base URL for synchronous tasks. The service splits the two across hosts. */
        public Builder syncBaseUrl(String syncBaseUrl) {
            delegate.syncBaseUrl(syncBaseUrl);
            return this;
        }

        /** An OkHttpClient to derive from, for custom interceptors, pools or TLS. */
        public Builder okHttpClient(OkHttpClient okHttpClient) {
            delegate.okHttpClient(okHttpClient);
            return this;
        }

        /**
         * Validates everything and builds the client.
         *
         * @throws EzCaptchaException if any setting is unusable
         */
        public EzCapSolverClient build() {
            return EzCapSolverClient.of(delegate.build());
        }
    }
}
