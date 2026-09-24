package com.burstlinker.ezcapsolver.exception;

import com.burstlinker.ezcapsolver.model.solution.Required;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The task succeeded but its solution could not be turned into the expected model.
 *
 * <p>This is not a failed task. The service reported {@code ready}, the work was done, and
 * <strong>the task was billed</strong> — only this SDK's reading of the answer fell over.
 * That distinction is why the untouched JSON travels with the exception: whatever the worker
 * returned is still there to be used, and losing it would mean paying for a result and
 * throwing it away.
 *
 * <pre>{@code
 * try {
 *     Solved<HCaptchaSolution> solved = client.solveHCaptcha(task);
 * } catch (SolutionDecodeException e) {
 *     log.warn("task {} solved but undecodable, raw: {}", e.getTaskId(), e.getRaw());
 * }
 * }</pre>
 *
 * <p>The usual cause is a worker renaming or dropping a field declared
 * {@link Required}. Workers ship faster than this SDK, so
 * treat it as a signal to read {@link #getRaw()} rather than as an outage.
 */
public class SolutionDecodeException extends EzCaptchaException {

    private static final long serialVersionUID = 1L;

    /** The solution JSON exactly as it arrived. Never {@code null}. */
    private final transient JsonNode raw;

    /** The task that produced it, or {@code null} for a synchronous task without one. */
    private final String taskId;

    public SolutionDecodeException(String message, JsonNode raw, String taskId, Throwable cause) {
        super(message, cause);
        this.raw = raw;
        this.taskId = taskId;
    }

    /**
     * The solution as the worker sent it.
     *
     * <p>{@code transient} because {@link JsonNode} is not serializable; a deserialized copy
     * of this exception loses the payload. Exceptions are not a transport for results, and
     * carrying the node is worth more than the theoretical round trip.
     */
    public JsonNode getRaw() {
        return raw;
    }

    /** The id of the billed task, for a retry that costs nothing. */
    public String getTaskId() {
        return taskId;
    }
}
