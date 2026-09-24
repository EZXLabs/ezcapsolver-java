package com.burstlinker.ezcapsolver.exception;

/**
 * A failure below the HTTP response (E2): DNS, TCP, TLS, or a timeout. The request never
 * produced a response.
 *
 * <p>Whether it is safe to send the request again depends on what was being sent. Creating a
 * task is billed and is not idempotent, and <strong>a timeout cannot tell you whether the
 * service already accepted the task</strong> — the SDK therefore never retries one on its
 * own. Reading a result, by contrast, is free and safe to repeat.
 */
public class TransportException extends EzCaptchaException {

    private static final long serialVersionUID = 1L;

    private final String operation;

    public TransportException(String operation, Throwable cause) {
        super(operation + ": " + cause, cause);
        this.operation = operation;
    }

    /**
     * What was being attempted, such as {@code POST https://api.ez-captcha.com/createTask}.
     * Never contains credentials.
     */
    public String getOperation() {
        return operation;
    }
}
