package com.burstlinker.ezcapsolver.model.task;

import com.burstlinker.ezcapsolver.internal.Redaction;
import com.burstlinker.ezcapsolver.model.solution.CloudFlareTurnstileSolution;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Requests a Cloudflare Turnstile token.
 *
 * <p>Task type: {@code CloudFlareTurnstileTask}
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CloudFlareTurnstileTaskParams extends BaseTaskParams<CloudFlareTurnstileSolution> {

    /** URL of the page containing the widget. Required. */
    @JsonProperty("websiteURL")
    private String websiteUrl;

    /** The Turnstile site key. Required. */
    private String websiteKey;

    /** The optional worker proxy. */
    @ToString.Exclude
    private String proxy;

    /**
     * Optional Turnstile metadata, sent as an object for the same reason as
     * {@link CloudFlare5sTaskParams#getRqData()}: the service stringifies it itself.
     */
    private Map<String, Object> rqData;

    @Override
    public Class<CloudFlareTurnstileSolution> solutionType() {
        return CloudFlareTurnstileSolution.class;
    }

    @ToString.Include(name = "proxy")
    private String maskedProxy() {
        return Redaction.mask(proxy);
    }
}
