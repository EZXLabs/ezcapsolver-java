package com.burstlinker.ezcapsolver.model.solution;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Reese84 payload.
 *
 * <p>Task type: {@code IncapsulaTaskProxyless}
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IncapsulaSolution extends BaseSolution {

    /** The worker's inner status code. */
    private int status;

    /**
     * The JSON string to post to the Incapsula sensor endpoint.
     *
     * <p>It is <strong>stringified JSON</strong> and has to be submitted exactly as it
     * arrived. Parsing it here would change what the caller has to send, so the SDK leaves it
     * alone.
     */
    @Required
    private String data;
}
