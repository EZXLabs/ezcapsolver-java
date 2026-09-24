package com.burstlinker.ezcapsolver.model.solution;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * One round of the Akamai Web flow.
 *
 * <p>Task type: {@code AkamaiWEBTaskProxyless}
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AkamaiWebSolution extends BaseSolution {

    /** The sensor data for this round. */
    @Required
    private String payload;

    /**
     * The encoded state to feed into the next round's {@code encodeData}.
     *
     * <p><strong>Note the casing difference</strong> — the service spells it {@code encodedata}
     * coming back and {@code encodeData} going out. The round that ends the flow carries none,
     * so its absence is not a failure.
     */
    private String encodedata;
}
