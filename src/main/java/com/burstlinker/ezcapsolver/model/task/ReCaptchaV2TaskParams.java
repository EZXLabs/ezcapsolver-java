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
 * Parameters shared by every ReCaptcha V2 task type: the plain, high-score, {@code s}-carrying
 * and enterprise variants all take these.
 *
 * <p>Which variant runs is chosen by the method you call, not by a field here.
 *
 * <p>Task types: {@code ReCaptchaV2TaskProxyless}, {@code ReCaptchaV2TaskProxylessS9},
 * {@code ReCaptchaV2STaskProxyless}, {@code ReCaptchaV2EnterpriseTaskProxyless},
 * {@code ReCaptchaV2SEnterpriseTaskProxyless}
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReCaptchaV2TaskParams extends BaseTaskParams<ReCaptchaSolution> {

    /** URL of the page containing the challenge. Required. */
    @JsonProperty("websiteURL")
    private String websiteUrl;

    /** The ReCaptcha site key. Required. */
    private String websiteKey;

    /**
     * Whether the challenge uses invisible mode.
     *
     * <p>Always sent, {@code false} included — it is an answer, not an absence.
     */
    // Do not rename this field to isInvisible. Lombok would generate isInvisible() rather than
    // prefixing a name that already starts with "is", and Jackson would read that getter as a
    // second property called "invisible" — sending both keys. See CONTRIBUTING.md.
    @JsonProperty("isInvisible")
    private boolean invisible;

    /** The optional security anchor parameter. */
    private String sa;

    /**
     * The optional challenge-bound {@code s} parameter. Supplying it routes the task to the
     * high-score IPv4 queue.
     */
    private String s;

    /** The optional page title. */
    private String websiteTitle;

    /**
     * Proxy the worker uses to reach the site, as
     * {@code protocol://username:password@host:port}. Optional and not validated here.
     */
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
