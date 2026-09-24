package com.burstlinker.ezcapsolver.model.solution;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Returned by both DataDome challenge steps and by both DataDome task types.
 *
 * <p>Step one carries the challenge address in {@link #getUrl()}, step two the validation
 * instructions. Earlier workers returned step one as a bare string; it is an object now, which
 * is why both steps decode into this one type — and why {@code Solved.raw} does not narrow
 * along with the model.
 *
 * <p>Task types: {@code DataDomeTaskProxyless}, {@code DataDomeTagsTaskProxyless}
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DataDomeSolution extends BaseSolution {

    /** The challenge kind, such as {@code slider} or {@code interstitial}. */
    private String kind;

    /** The challenge URL on step one, the validation endpoint on step two. */
    private String url;

    /** The validation request body, when there is one. */
    private String body;
}
