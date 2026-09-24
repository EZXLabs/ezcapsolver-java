package com.burstlinker.ezcapsolver.examples.recaptchav2;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.ReCaptchaSolution;
import com.burstlinker.ezcapsolver.model.task.ReCaptchaV2TaskParams;

/**
 * Solves a reCAPTCHA v2 that carries an {@code s} token.
 *
 * <p>Task type: {@code ReCaptchaV2STaskProxyless}
 *
 * <p>Some Google properties add an {@code s} parameter to the widget. It is short-lived, so read
 * it from the page on each attempt rather than hard-coding it — a stale value produces a token
 * the site rejects, after you have already paid for the task.
 *
 * <p>Docs: https://docs.ezxlabs.com/docs/captcha/api/recaptcha-v2
 */
public class ReCaptchaV2STaskProxyless {

    public static void main(String[] args) {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        ReCaptchaV2TaskParams params =
                ReCaptchaV2TaskParams.builder()
                        .websiteUrl("https://example.com/login")
                        .websiteKey("YOUR_SITE_KEY")
                        // Read from the page, not hard-coded.
                        .s(System.getenv("EZCAPTCHA_RECAPTCHA_S"))
                        .invisible(false)
                        .build();

        Solved<ReCaptchaSolution> solved = client.solveReCaptchaV2STaskProxyless(params);

        System.out.println("Task ID: " + solved.getTaskId());
        System.out.println("Token:   " + solved.getSolution().getToken());
    }
}
