package com.burstlinker.ezcapsolver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The envelope every API response carries.
 *
 * <p>Only four fields, because only these four appear on every response. Anything endpoint
 * specific lives on the subclass.
 */
@Getter
@Setter
@ToString
public class ResponseMeta {

    /**
     * 0 on success, non-zero on failure.
     *
     * <p><strong>The only success criterion.</strong> The HTTP status is not: most business
     * errors arrive as HTTP 500 and a failed task arrives as HTTP 200. A non-empty
     * {@code errorCode} is not a second signal either — every code the service defines
     * already comes with a non-zero {@code errorId}, and treating the code as authoritative
     * would let one added on the success side turn a solved task into an error.
     */
    private int errorId;

    /** The service-side request tracking identifier. */
    private String requestId;

    /** Present only on failure. */
    private String errorCode;

    /** Present only on failure. */
    private String errorDescription;

    /** Whether the service reported this exchange as successful. */
    @JsonIgnore
    public boolean isSuccess() {
        return errorId == 0;
    }
}
