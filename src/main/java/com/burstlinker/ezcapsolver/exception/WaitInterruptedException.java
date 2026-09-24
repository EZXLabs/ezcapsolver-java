package com.burstlinker.ezcapsolver.exception;

/**
 * A task that was created and <strong>billed</strong>, but whose result never arrived: the
 * wait broke off on a dropped connection, a gateway, an interrupt, or a response that did not
 * match the contract.
 *
 * <p>{@code solve} creates the task internally, so this exception is the only place its
 * identifier appears. Recover by waiting on the <em>same</em> task again rather than creating
 * a second one — the service holds a result for five minutes after creation, and a new task
 * is billed again.
 *
 * <pre>{@code
 * String taskId = EzCaptchaException.taskIdOf(e);
 * if (taskId != null) {
 *     Solved<HCaptchaSolution> solved = client.waitForResult(taskId, HCaptchaSolution.class);
 * }
 * }</pre>
 *
 * <p>It wraps the underlying failure rather than replacing it, so the original cause stays
 * reachable through {@link #getCause()}.
 *
 * <p>Failures that already carry the identifier are <strong>not</strong> wrapped in this: a
 * business failure stays an {@link ApiException} with its task id set, and an exhausted
 * budget stays a {@link PollingExhaustedException}.
 */
public class WaitInterruptedException extends EzCaptchaException {

    private static final long serialVersionUID = 1L;

    private final String taskId;
    private final String requestId;

    public WaitInterruptedException(String taskId, String requestId, Throwable cause) {
        super("task " + taskId + " was created but waiting for its result failed: " + cause, cause);
        this.taskId = taskId;
        this.requestId = requestId;
    }

    /** The task that was created and billed. */
    public String getTaskId() {
        return taskId;
    }

    /** The correlation id of the creating request, when the service supplied one. */
    public String getRequestId() {
        return requestId;
    }
}
