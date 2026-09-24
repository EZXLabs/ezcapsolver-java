package com.burstlinker.ezcapsolver.examples.recaptchav3;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.ReCaptchaSolution;
import com.burstlinker.ezcapsolver.model.task.ReCaptchaV3TaskParams;

/**
 * Solves a reCAPTCHA v3 score challenge.
 *
 * <p>Task type: {@code ReCaptchaV3TaskProxyless}
 *
 * <p>v3 never shows a widget. The site calls {@code grecaptcha.execute} with an action name, and
 * that name is part of what the score is computed against — so {@code pageAction} has to match
 * what the page actually uses, or the token scores badly even though it is valid.
 *
 * <p>Docs: https://docs.ezxlabs.com/docs/captcha/api/recaptcha-v3
 */
public class ReCaptchaV3TaskProxyless {

    public static void main(String[] args) {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        ReCaptchaV3TaskParams params =
                ReCaptchaV3TaskParams.builder()
                        .websiteUrl("https://example.com/checkout")
                        .websiteKey("YOUR_SITE_KEY")
                        .pageAction("checkout")
                        .build();

        Solved<ReCaptchaSolution> solved = client.solveReCaptchaV3TaskProxyless(params);

        System.out.println("Task ID: " + solved.getTaskId());
        System.out.println("Token:   " + solved.getSolution().getToken());
    }
}
