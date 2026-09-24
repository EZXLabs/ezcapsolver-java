package com.burstlinker.ezcapsolver.examples.recaptchav3;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.ReCaptchaSolution;
import com.burstlinker.ezcapsolver.model.task.ReCaptchaV3TaskParams;

/**
 * Solves a reCAPTCHA v3 score challenge through the S9 worker pool.
 *
 * <p>Task type: {@code ReCaptchaV3TaskProxylessS9}
 *
 * <p>Same parameters as {@code ReCaptchaV3TaskProxyless}; a different pool of workers.
 *
 * <p>Docs: https://docs.ezxlabs.com/docs/captcha/api/recaptcha-v3
 */
public class ReCaptchaV3TaskProxylessS9 {

    public static void main(String[] args) {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        ReCaptchaV3TaskParams params =
                ReCaptchaV3TaskParams.builder()
                        .websiteUrl("https://example.com/checkout")
                        .websiteKey("YOUR_SITE_KEY")
                        .pageAction("checkout")
                        .build();

        Solved<ReCaptchaSolution> solved = client.solveReCaptchaV3TaskProxylessS9(params);

        System.out.println("Task ID: " + solved.getTaskId());
        System.out.println("Token:   " + solved.getSolution().getToken());
    }
}
