package com.burstlinker.ezcapsolver.examples.recaptchav2;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.ReCaptchaSolution;
import com.burstlinker.ezcapsolver.model.task.ReCaptchaV2TaskParams;

/**
 * Solves a reCAPTCHA v2 Enterprise widget that also carries an {@code s} token.
 *
 * <p>Task type: {@code ReCaptchaV2SEnterpriseTaskProxyless}
 *
 * <p>The Enterprise and {@code s} variants combined. As with the plain {@code S} type, read the
 * token from the page each time.
 *
 * <p>Docs: https://docs.ezxlabs.com/docs/captcha/api/recaptcha-v2
 */
public class ReCaptchaV2SEnterpriseTaskProxyless {

    public static void main(String[] args) {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        ReCaptchaV2TaskParams params =
                ReCaptchaV2TaskParams.builder()
                        .websiteUrl("https://example.com/login")
                        .websiteKey("YOUR_ENTERPRISE_SITE_KEY")
                        .s(System.getenv("EZCAPTCHA_RECAPTCHA_S"))
                        .invisible(false)
                        .build();

        Solved<ReCaptchaSolution> solved = client.solveReCaptchaV2SEnterpriseTaskProxyless(params);

        System.out.println("Task ID: " + solved.getTaskId());
        System.out.println("Token:   " + solved.getSolution().getToken());
    }
}
