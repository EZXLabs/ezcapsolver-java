package com.burstlinker.ezcapsolver.model.task;

import com.burstlinker.ezcapsolver.internal.Redaction;
import com.burstlinker.ezcapsolver.model.solution.HCaptchaSolution;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Parameters for an HCaptcha pass.
 *
 * <p>Task type: {@code HCaptcha}
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HCaptchaTaskParams extends BaseTaskParams<HCaptchaSolution> {

    /**
     * URL of the page containing the challenge. Required, though the service checks only
     * that it is non-blank rather than that it parses.
     */
    @JsonProperty("websiteURL")
    private String websiteUrl;

    /** The HCaptcha site key. Required. */
    private String websiteKey;

    /** Browser language. Required. Only {@code en-US} is supported at the moment. */
    private String lang;

    /**
     * Whether the challenge runs without a visible checkbox. Required: {@code true} when the
     * site shows no HCaptcha checkbox, {@code false} when it does.
     *
     * <p>Always sent, {@code false} included — it is an answer, not an absence.
     */
    private boolean invisible;

    /**
     * The {@code rqdata} value, required by the sites that publish one. Omitted when unset.
     *
     * <p>The wire name is all lowercase here, unlike the Cloudflare tasks, whose equivalent
     * field is {@code rqData} and carries an object rather than a string. The two are not
     * interchangeable.
     */
    @JsonProperty("rqdata")
    private String rqData;

    /**
     * Proxy the worker uses to reach the site, as
     * {@code protocol://username:password@host:port}. Optional and not validated here.
     *
     * <p>Unrelated to the proxy the SDK itself dials out through, which is set on the client.
     */
    @ToString.Exclude
    private String proxy;

    @Override
    public Class<HCaptchaSolution> solutionType() {
        return HCaptchaSolution.class;
    }

    /** Keeps the embedded credentials out of logs while still showing whether one is set. */
    @ToString.Include(name = "proxy")
    private String maskedProxy() {
        return Redaction.mask(proxy);
    }
}
