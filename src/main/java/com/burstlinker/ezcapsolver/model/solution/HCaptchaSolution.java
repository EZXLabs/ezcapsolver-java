package com.burstlinker.ezcapsolver.model.solution;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The pass returned by an HCaptcha task.
 *
 * <p>Task type: {@code HCaptcha}
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HCaptchaSolution extends BaseSolution {

    /** The generated HCaptcha pass identifier. Always returned. */
    @Required
    @JsonProperty("generated_pass_UUID")
    private String generatedPassUuid;

    /** The User-Agent that goes with the pass. */
    private String ua;

    /** The language that goes with the pass. */
    private String lang;
}
