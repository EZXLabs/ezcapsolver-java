package com.burstlinker.ezcapsolver.model.task;

import com.burstlinker.ezcapsolver.model.solution.AkamaiSbsdSolution;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Produces Akamai SBSD sensor data. Runs through the synchronous endpoint.
 *
 * <p>Task type: {@code AkamaiSBSDTaskProxyless}
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AkamaiSbsdTaskParams extends BaseTaskParams<AkamaiSbsdSolution> {

    /** URL of the page the challenge belongs to. Required. */
    private String pageUrl;

    /** URL of the SBSD script. Required. */
    private String sbsdUrl;

    /** The existing {@code bm_so} or equivalent cookie value. Required. */
    private String bmSo;

    /** The browser User-Agent. Required. */
    private String ua;

    /** The browser language. Required. */
    private String lang;

    /** The base64-encoded SBSD script. Required. */
    @JsonProperty("script_base64")
    private String scriptBase64;

    @Override
    public Class<AkamaiSbsdSolution> solutionType() {
        return AkamaiSbsdSolution.class;
    }
}
