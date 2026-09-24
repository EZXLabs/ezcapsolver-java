package com.burstlinker.ezcapsolver.model.task;

import com.burstlinker.ezcapsolver.internal.Redaction;
import com.burstlinker.ezcapsolver.model.solution.IncapsulaSolution;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Produces an Incapsula Reese84 sensor payload. Runs through the synchronous endpoint.
 *
 * <p>The service declares <strong>no validation for this type at all</strong>. Its own comments
 * call most of these fields mandatory, but nothing enforces it, so every field is optional
 * here: rejecting a request the service would have accepted is the worse failure.
 *
 * <p>Task type: {@code IncapsulaTaskProxyless}
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IncapsulaTaskParams extends BaseTaskParams<IncapsulaSolution> {

    /** The full source of the Reese84 sensor script. */
    private String script;

    /** The URL the script was served from, which has to match where it actually came from. */
    private String scriptUrl;

    /** URL of the page running the sensor script. */
    private String pageUrl;

    /** The browser's optional Accept-Language header, such as {@code ja-JP,ja;q=0.9,en;q=0.8}. */
    private String acceptLanguage;

    /**
     * The full browser User-Agent. The service reads only the Chrome major version from it and
     * supports 147, 148 and 149.
     */
    private String ua;

    /**
     * Optional, and it <strong>changes what the task does</strong>: supplied, the worker
     * generates the payload and submits it, returning the cookie; omitted, it only generates
     * the payload.
     *
     * <p>Note that this type never counts as having a proxy for the purposes of a plan's
     * mandatory-proxy requirement, even when this is set.
     */
    @ToString.Exclude
    private String proxy;

    /** Optional proof-of-work data, required by the sites that have PoW challenges enabled. */
    private String pow;

    @Override
    public Class<IncapsulaSolution> solutionType() {
        return IncapsulaSolution.class;
    }

    @ToString.Include(name = "proxy")
    private String maskedProxy() {
        return Redaction.mask(proxy);
    }
}
