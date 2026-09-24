package com.burstlinker.ezcapsolver.exception;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A structured error reported by the service (E3).
 *
 * <p>Covers both a business error carried by an HTTP 200 and any non-2xx status, so
 * {@link #getHttpStatus()} is always readable from one place. The service signals failure
 * with {@code errorId}, <strong>not</strong> with the HTTP status: most business errors
 * arrive as HTTP 500, and a failed task arrives as HTTP 200. Branching on the status line
 * gets both cases wrong.
 *
 * <p>Three questions can be asked of it, and they answer different things:
 *
 * <pre>{@code
 * if (e.isAuthenticationError()) …   // stop; retrying earns a ban
 * if (e.isRateLimited())         …   // the query was throttled, the task is untouched
 * if (e.isTerminal())            …   // an identical request fails identically
 * }</pre>
 */
public class ApiException extends EzCaptchaException {

    private static final long serialVersionUID = 1L;

    /**
     * Codes that trigger the service's per-key ban counter.
     *
     * <p>From the {@code @ApiDefenses} annotations on the service's {@code
     * AsyncTaskController}: {@code /createTask} bans a key for three minutes after thirty of
     * these within one minute, {@code /getTaskResult} for one minute after thirty of the
     * first two. Do not edit from memory.
     */
    private static final Set<String> AUTHENTICATION_ERROR_CODES =
            frozen("ERROR_KEY_DOES_NOT_EXIST", "ERROR_KEY_NOT_AVAILABLE", "ERROR_ZERO_BALANCE");

    /**
     * Codes for which an identical request yields an identical failure.
     *
     * <p>Derived from the service's {@code TaskResponseCode} enum. The rest are left out
     * because an internal error, a rate limit, a ban and the two synchronous worker faults
     * can all clear on their own.
     */
    private static final Set<String> TERMINAL_ERROR_CODES =
            frozen(
                    "ERROR_CONTENT_TYPE_ERROR",
                    "ERROR_KEY_DOES_NOT_EXIST",
                    "ERROR_KEY_NOT_AVAILABLE",
                    "ERROR_NOT_FOUND",
                    "ERROR_PACKAGE_NOT_EXIST",
                    "ERROR_PACKAGE_TASK_TYPE_NOT_SUPPORTED",
                    "ERROR_REQUEST_METHOD",
                    "ERROR_REQUEST_PARAMETERS",
                    "ERROR_REQUEST_PROXY_MISSING",
                    "ERROR_SUBSCRIPTION_EXPIRED",
                    "ERROR_TASK_NOT_EXIST",
                    "ERROR_TASK_TYPE_NOT_ALLOWED",
                    "ERROR_TASK_TYPE_NOT_AVAILABLE",
                    "ERROR_TASK_TYPE_NOT_SUPPORTED",
                    "ERROR_WEBSITE_NOT_ALLOWED",
                    "ERROR_ZERO_BALANCE");

    /**
     * The service's two throttling codes, both HTTP 429.
     *
     * <p>Neither says anything about a task: the query is refused before the service looks it
     * up, so the task keeps running and the next poll can still find it. {@code
     * /getTaskResult} counts only the first two authentication codes towards its ban counter,
     * so polling through a refusal does not dig the hole deeper.
     */
    private static final Set<String> RATE_LIMITED_ERROR_CODES =
            frozen("ERROR_REQUEST_LIMIT", "ERROR_REQUEST_BANNED");

    private final String errorCode;
    private final String errorDescription;
    private final int httpStatus;
    private final Map<String, String> errors;
    private final String requestId;
    private final String taskId;

    public ApiException(
            String errorCode,
            String errorDescription,
            int httpStatus,
            Map<String, String> errors,
            String requestId,
            String taskId) {
        super(render(errorCode, errorDescription, httpStatus, errors));
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        this.httpStatus = httpStatus;
        this.errors = errors == null ? Collections.<String, String>emptyMap() : errors;
        this.requestId = requestId;
        this.taskId = taskId;
    }

    /**
     * Returns a copy of this error carrying the id of the task it belongs to.
     *
     * <p>The service does not echo the task id back on a failure, so the SDK attaches it
     * after the fact. That id is the difference between a recoverable billed task and a
     * wasted one.
     */
    public ApiException withTaskId(String taskId) {
        return new ApiException(errorCode, errorDescription, httpStatus, errors, requestId, taskId);
    }

    /**
     * The stable machine-readable code, such as {@code ERROR_ZERO_BALANCE}. Empty when the
     * response carried no error envelope.
     *
     * <p>An open set: a code produced by a worker is not in the service's own table, so it is
     * exposed verbatim rather than folded into an enum.
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * The human-readable description. When the response was not JSON, this holds a slice of
     * the raw body instead.
     */
    public String getErrorDescription() {
        return errorDescription;
    }

    /** The HTTP status that carried the error. Always set. */
    public int getHttpStatus() {
        return httpStatus;
    }

    /** Field path to validation message. Empty except on a parameter validation failure. */
    public Map<String, String> getErrors() {
        return errors;
    }

    /** The service-side request tracking identifier, when the response carried one. */
    public String getRequestId() {
        return requestId;
    }

    /**
     * The task this failure belongs to, or {@code null} when nothing was billed.
     *
     * <p>{@link EzCaptchaException#taskIdOf} is the general way to ask this question without
     * knowing which exception type carries it.
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * Whether this is a credential or balance problem.
     *
     * <p>A caller that retries these is not merely wasting a call: the service counts them
     * per key, and thirty within a minute earn a three-minute ban. <strong>Stop rather than
     * back off.</strong>
     */
    public boolean isAuthenticationError() {
        return AUTHENTICATION_ERROR_CODES.contains(errorCode);
    }

    /**
     * Whether resending the identical request would produce the identical failure.
     *
     * <p>An unknown code returns {@code false}, because a code this release has not seen may
     * well be transient; that keeps the SDK from talking a caller out of a retry that would
     * have worked. The codes it does not call terminal are not promised to be retryable
     * either — the retry policy stays with the caller.
     */
    public boolean isTerminal() {
        return TERMINAL_ERROR_CODES.contains(errorCode);
    }

    /**
     * Whether the service refused the request for throttling rather than for anything about
     * the request itself.
     *
     * <p>Both codes are transient by construction: a rate limit resets with its window and a
     * ban expires on its own. The polling loop treats one as a skipped attempt instead of a
     * failed task, and a caller polling by hand should do the same.
     *
     * <p>This is <strong>narrower than {@code !isTerminal()}</strong>, which is also false for
     * every unrecognised code — including the worker codes that report a genuinely failed
     * task. Driving retry off the negation would poll forever on a task that already failed.
     */
    public boolean isRateLimited() {
        return RATE_LIMITED_ERROR_CODES.contains(errorCode);
    }

    /**
     * Renders everything actionable onto one line, because callers commonly log the message
     * and nothing else.
     */
    private static String render(
            String errorCode, String errorDescription, int httpStatus, Map<String, String> errors) {
        String code = isBlank(errorCode) ? "UNKNOWN_API_ERROR" : errorCode;
        String description =
                isBlank(errorDescription) ? "The API returned an unspecified error" : errorDescription;

        StringBuilder message = new StringBuilder();
        message.append(code).append(": ").append(description).append(" (HTTP ").append(httpStatus).append(')');
        if (errors != null && !errors.isEmpty()) {
            // Sorted so the message is reproducible across runs and diffable in a log.
            Map<String, String> sorted = new TreeMap<String, String>(errors);
            String separator = "; ";
            for (Map.Entry<String, String> entry : sorted.entrySet()) {
                message.append(separator).append(entry.getKey()).append(": ").append(entry.getValue());
                separator = ", ";
            }
        }
        return message.toString();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isEmpty();
    }

    private static Set<String> frozen(String... codes) {
        return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(codes)));
    }
}
