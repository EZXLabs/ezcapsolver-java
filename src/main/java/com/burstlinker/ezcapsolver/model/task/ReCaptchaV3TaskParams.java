package com.burstlinker.ezcapsolver.model.task;

import com.burstlinker.ezcapsolver.internal.Redaction;
import com.burstlinker.ezcapsolver.model.solution.ReCaptchaSolution;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Parameters shared by every ReCaptcha V3 task type.
 *
 * <p>Task types: {@code ReCaptchaV3TaskProxyless}, {@code ReCaptchaV3TaskProxylessS9},
 * {@code ReCaptchaV3EnterpriseTaskProxyless}, {@code ReCaptchaV3EnterpriseTaskProxylessS9}
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReCaptchaV3TaskParams extends BaseTaskParams<ReCaptchaSolution> {

    /** URL of the page containing the challenge. Required. */
    @JsonProperty("websiteURL")
    private String websiteUrl;

    /** The ReCaptcha site key. Required. */
    private String websiteKey;

    /**
     * Whether the challenge uses invisible mode.
     *
     * <p>Omitted when unset, leaving the service's own default of {@code true} to apply. Set
     * it to opt out:
     *
     * <pre>{@code
     * .invisible(false)
     * }</pre>
     */
    @JsonProperty("isInvisible")
    private Boolean invisible;

    /** The optional action configured by the protected page. */
    private String pageAction;

    /** The optional page title. */
    private String websiteTitle;

    /** The optional site-specific check field. */
    private String checkField;

    /** Proxy the worker uses to reach the site. Optional. */
    @ToString.Exclude
    private String proxy;

    @Override
    public Class<ReCaptchaSolution> solutionType() {
        return ReCaptchaSolution.class;
    }

    @ToString.Include(name = "proxy")
    private String maskedProxy() {
        return Redaction.mask(proxy);
    }
}
