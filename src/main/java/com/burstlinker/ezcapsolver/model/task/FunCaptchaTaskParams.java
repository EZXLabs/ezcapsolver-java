package com.burstlinker.ezcapsolver.model.task;

import com.burstlinker.ezcapsolver.internal.Redaction;
import com.burstlinker.ezcapsolver.model.solution.FunCaptchaSolution;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Requests a FunCaptcha (Arkose Labs) token.
 *
 * <p>Task type: {@code FuncaptchaTaskProxyless}
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FunCaptchaTaskParams extends BaseTaskParams<FunCaptchaSolution> {

    /** URL of the page containing the challenge. Required. */
    @JsonProperty("websiteURL")
    private String websiteUrl;

    /** The FunCaptcha public key. Required. */
    private String websiteKey;

    /** The optional Arkose Labs blob, a JSON string shaped like {@code {"blob":"..."}}. */
    private String data;

    /** The optional {@code arkoselabs.com} subdomain the site uses. */
    @JsonProperty("funcaptchaApiJSSubdomain")
    private String apiJsSubdomain;

    /**
     * The optional worker proxy.
     *
     * <p><strong>FunCaptcha is the only task type using the {@code FUN} proxy format</strong> —
     * {@code protocol://host:port:username:password}, with the credentials appended rather than
     * placed before the host. Every other type takes
     * {@code protocol://username:password@host:port}.
     *
     * <p>Either way the service requires both a username and a password: an unauthenticated
     * proxy is rejected.
     */
    @ToString.Exclude
    private String proxy;

    /** Whether the supplied proxy is inside mainland China. Always sent. */
    private boolean cn;

    @Override
    public Class<FunCaptchaSolution> solutionType() {
        return FunCaptchaSolution.class;
    }

    @ToString.Include(name = "proxy")
    private String maskedProxy() {
        return Redaction.mask(proxy);
    }
}
