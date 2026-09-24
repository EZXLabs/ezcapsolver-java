package com.burstlinker.ezcapsolver.model.solution;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The payload returned by an Akamai SBSD task.
 *
 * <p>Task type: {@code AkamaiSBSDTaskProxyless}
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AkamaiSbsdSolution extends BaseSolution {

    /** The base64-encoded sensor payload. */
    @Required
    private String payload;

    /** The {@code bm_lso_time} produced alongside the payload. Not every response carries one. */
    @JsonProperty("bm_lso_time")
    private String bmLsoTime;
}
