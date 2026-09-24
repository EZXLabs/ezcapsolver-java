package com.burstlinker.ezcapsolver.model.solution;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The browser state a worker ended up with after clearing a five-second interstitial.
 *
 * <p><strong>There is no single token here.</strong> Replaying these headers and cookies
 * against the protected site is what actually clears the challenge, which is why the whole
 * structure is kept rather than reduced to one string.
 *
 * <p>Task type: {@code CloudFlare5STask}
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CloudFlare5sSolution extends BaseSolution {

    /** Request headers to replay. */
    private Map<String, String> header;

    /** The clearance cookies the worker obtained. */
    private Map<String, String> cookies;

    /** The browser fingerprint the worker used, such as {@code chrome149}. */
    private String tlsVersion;

    /** The challenge page content; {@code null} when the worker captured none. */
    private String body;

    /** The Turnstile token embedded in the challenge, when the flow produced one. */
    // Do not rename this field to sToken. Lombok would generate getSToken(), and Jackson
    // lower-cases a leading run of capitals when deriving a property name — giving "stoken" as
    // a second key alongside the annotated one. See CONTRIBUTING.md.
    @JsonProperty("sToken")
    private String turnstileToken;
}
