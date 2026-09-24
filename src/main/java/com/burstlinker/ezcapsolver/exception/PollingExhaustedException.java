package com.burstlinker.ezcapsolver.exception;

import java.time.Duration;

/**
 * The polling budget ran out before the task reached a terminal state (E4).
 *
 * <p>The task may still be running, and <strong>it has already been billed</strong>. Hand
 * {@link #getTaskId()} to {@code waitForResult} rather than paying for the same work twice.
 *
 * <p>The service holds a result for <strong>five minutes</strong> after creation; past that
 * the id comes back as {@code ERROR_TASK_NOT_EXIST}. That window is what the default budget
 * is sized against, so a budget that reaches this exception has usually not run out of
 * patience — it has run out of task.
 */
public class PollingExhaustedException extends EzCaptchaException {

    private static final long serialVersionUID = 1L;

    private final String taskId;
    private final int attempts;
    private final Duration interval;

    public PollingExhaustedException(String taskId, int attempts, Duration interval) {
        super(
                "task "
                        + taskId
                        + " did not complete after "
                        + attempts
                        + " polling attempts at "
                        + interval
                        + " intervals");
        this.taskId = taskId;
        this.attempts = attempts;
        this.interval = interval;
    }

    /** The unfinished, already billed task. */
    public String getTaskId() {
        return taskId;
    }

    /** How many result queries completed. */
    public int getAttempts() {
        return attempts;
    }

    /** The delay that was applied between queries. */
    public Duration getInterval() {
        return interval;
    }
}
