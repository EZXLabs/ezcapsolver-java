package com.burstlinker.ezcapsolver.model.solution;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The token returned by every ReCaptcha V2 and V3 token task — nine task types in all.
 *
 * <p>Task types: all {@code ReCaptchaV2*} and {@code ReCaptchaV3*} except
 * {@code ReCaptchaV2Classification}
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReCaptchaSolution extends BaseSolution {

    /** The value to submit to the protected site. Always returned. */
    @Required
    @JsonProperty("gRecaptchaResponse")
    private String token;

    /** The matching {@code Sec-CH-UA} request header. */
    @JsonProperty("sec_ch_ua")
    private String secChUa;

    /** The User-Agent that goes with the token. Replay it, or the token may not be accepted. */
    @JsonProperty("user_agent")
    private String userAgent;
}
