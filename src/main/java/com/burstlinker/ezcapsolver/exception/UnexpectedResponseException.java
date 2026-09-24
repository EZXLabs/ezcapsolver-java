package com.burstlinker.ezcapsolver.exception;

/**
 * A response the SDK could not use (E5): it did not parse, or it parsed but broke the API
 * contract.
 *
 * <p>Distinct from {@link ApiException}, which is the service reporting a failure it
 * understands. This one means the exchange itself did not make sense — an HTML error page
 * from a gateway, a status value outside the three the service defines, or a {@code ready}
 * result with no solution attached.
 */
public class UnexpectedResponseException extends EzCaptchaException {

    private static final long serialVersionUID = 1L;

    /** Characters of a body kept in the message. Enough to identify it, not enough to bury it. */
    public static final int BODY_PREVIEW_CHARS = 512;

    private final String reason;
    private final String body;

    public UnexpectedResponseException(String reason, String body) {
        super(body == null || body.isEmpty() ? reason : reason + "; body: " + body);
        this.reason = reason;
        this.body = body;
    }

    /** What was wrong with the response. */
    public String getReason() {
        return reason;
    }

    /**
     * A slice of the raw response body, often the only thing left to diagnose with. Truncated
     * to {@value #BODY_PREVIEW_CHARS} characters.
     */
    public String getBody() {
        return body;
    }

    /**
     * Truncates a body for a message, cutting on a code point boundary so the preview stays
     * valid text rather than a split surrogate pair.
     */
    public static String preview(String body) {
        if (body == null || body.length() <= BODY_PREVIEW_CHARS) {
            return body;
        }
        int end = body.offsetByCodePoints(0, Math.min(BODY_PREVIEW_CHARS, body.codePointCount(0, body.length())));
        return body.substring(0, end) + "...";
    }
}
