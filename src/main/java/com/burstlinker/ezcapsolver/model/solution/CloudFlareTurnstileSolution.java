package com.burstlinker.ezcapsolver.model.solution;

import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The token returned by a Turnstile task.
 *
 * <p>Task type: {@code CloudFlareTurnstileTask}
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CloudFlareTurnstileSolution extends BaseSolution {

    /** The value to submit to the protected site. */
    @Required
    private String token;

    /** Request headers to replay alongside the token. */
    private Map<String, String> header;
}
