package com.burstlinker.ezcapsolver.model.solution;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The PerimeterX clearance cookies.
 *
 * <p>The worker returns them as top-level fields, not nested under a cookies object. The Java
 * names drop the leading underscore the wire names carry; the wire names themselves are
 * untouched.
 *
 * <p>Task type: {@code PerimeterX}
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PerimeterXSolution extends BaseSolution {

    /** The {@code _px3} clearance cookie — the value that actually passes the check. */
    @Required
    @JsonProperty("_px3")
    private String px3;

    /** The {@code _pxvid} visitor identifier. */
    @JsonProperty("_pxvid")
    private String pxVid;

    /** The {@code _pxde} data-enrichment cookie. */
    @JsonProperty("_pxde")
    private String pxde;
}
