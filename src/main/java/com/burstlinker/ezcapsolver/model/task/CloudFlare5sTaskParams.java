package com.burstlinker.ezcapsolver.model.task;

import com.burstlinker.ezcapsolver.internal.Redaction;
import com.burstlinker.ezcapsolver.model.solution.CloudFlare5sSolution;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Clears a Cloudflare five-second interstitial.
 *
 * <p>Task type: {@code CloudFlare5STask}
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CloudFlare5sTaskParams extends BaseTaskParams<CloudFlare5sSolution> {

    /** The URL protected by the challenge. Required. */
    @JsonProperty("websiteURL")
    private String websiteUrl;

    /**
     * The worker proxy. <strong>Required for this type</strong>, unlike most others.
     *
     * <p>{@code protocol://username:password@host:port}, with protocol one of http, https or
     * socks5. Both credentials are required — the service rejects an unauthenticated proxy —
     * and the host may not be a private address.
     */
    @ToString.Exclude
    private String proxy;

    /**
     * Optional challenge request data.
     *
     * <p><strong>Send it as an object, not a string.</strong> The service stringifies it itself
     * when forwarding to the worker, so pre-encoding it produces double encoding.
     */
    private Map<String, Object> rqData;

    @Override
    public Class<CloudFlare5sSolution> solutionType() {
        return CloudFlare5sSolution.class;
    }

    @ToString.Include(name = "proxy")
    private String maskedProxy() {
        return Redaction.mask(proxy);
    }
}
