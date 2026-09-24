package com.burstlinker.ezcapsolver.exception;

/**
 * The base of every failure this SDK reports. Catching this one type covers all of them.
 *
 * <p>Failures are layered by what the caller can do about them, not by where they came
 * from: a configuration mistake needs a code change, a transport fault needs the network
 * looked at, an API error needs its code inspected. Each layer has its own subclass, with
 * one exception — see {@link #config(String)}.
 *
 * <p>Everything here is <strong>unchecked</strong>. Checked exceptions would put a
 * {@code throws} clause on every call site, and none of these are recoverable where they
 * are thrown: the caller has to decide what to do, usually several frames up.
 */
public class EzCaptchaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Prefix that marks a configuration failure, since it has no dedicated subclass. */
    private static final String CONFIG_PREFIX = "invalid client configuration: ";

    public EzCaptchaException(String message) {
        super(message);
    }

    public EzCaptchaException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Reports an invalid client configuration.
     *
     * <p>This is the <strong>only</strong> failure without a subclass of its own: a
     * configuration mistake carries one sentence of explanation and no structured data
     * worth a type. Add new configuration checks through this factory rather than
     * introducing a class for them.
     *
     * <p>Configuration is validated once, when the client is built, so a bad setting
     * surfaces before any task is created — which is to say, before anything is billed.
     */
    public static EzCaptchaException config(String reason) {
        return new EzCaptchaException(CONFIG_PREFIX + reason);
    }

    /**
     * The id of a task that was created and billed, when {@code error} left one behind, or
     * {@code null} when nothing was billed.
     *
     * <p>Creating a task is what costs money, so a failure after that point leaves a result
     * worth recovering: wait on this id again instead of creating a second task. The service
     * holds a result for five minutes after creation.
     *
     * <p>A {@code null} means the failure happened before or during task creation, so there
     * is nothing to recover.
     *
     * <p>Which exception type carries the id is an implementation detail. This is the one
     * place to ask, so a caller never has to know which of three types it is holding:
     *
     * <pre>{@code
     * } catch (EzCaptchaException e) {
     *     String taskId = EzCaptchaException.taskIdOf(e);
     *     if (taskId != null) {
     *         solved = client.waitForResult(taskId, HCaptchaSolution.class);
     *     }
     * }
     * }</pre>
     *
     * <p>The cause chain is walked, not just the top exception, because a wrapper can sit
     * between the caller and the failure that knows the id.
     */
    public static String taskIdOf(Throwable error) {
        for (Throwable current = error; current != null; current = current.getCause()) {
            String taskId = taskIdOfOne(current);
            if (taskId != null && !taskId.isEmpty()) {
                return taskId;
            }
            // A cause chain can be self-referential if someone initialises it by hand.
            if (current.getCause() == current) {
                break;
            }
        }
        return null;
    }

    private static String taskIdOfOne(Throwable error) {
        if (error instanceof WaitInterruptedException) {
            return ((WaitInterruptedException) error).getTaskId();
        }
        if (error instanceof PollingExhaustedException) {
            return ((PollingExhaustedException) error).getTaskId();
        }
        if (error instanceof ApiException) {
            return ((ApiException) error).getTaskId();
        }
        if (error instanceof SolutionDecodeException) {
            return ((SolutionDecodeException) error).getTaskId();
        }
        return null;
    }
}
