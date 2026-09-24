package com.burstlinker.ezcapsolver.model.solution;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The token returned by a FunCaptcha (Arkose Labs) task.
 *
 * <p>Task type: {@code FuncaptchaTaskProxyless}
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FunCaptchaSolution extends BaseSolution {

    /** The value to submit to the protected site. */
    @Required
    private String token;
}
