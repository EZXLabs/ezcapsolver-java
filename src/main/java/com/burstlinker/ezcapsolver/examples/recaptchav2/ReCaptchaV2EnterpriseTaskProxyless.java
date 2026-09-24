package com.burstlinker.ezcapsolver.examples.recaptchav2;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.ReCaptchaSolution;
import com.burstlinker.ezcapsolver.model.task.ReCaptchaV2TaskParams;

/**
 * Solves a reCAPTCHA v2 Enterprise widget.
 *
 * <p>Task type: {@code ReCaptchaV2EnterpriseTaskProxyless}
 *
 * <p>Enterprise widgets are served from {@code enterprise.js} rather than {@code api.js}. The
 * parameters are the same; the worker pool is not.
 *
 * <p>Docs: https://docs.ezxlabs.com/docs/captcha/api/recaptcha-v2
 */
public class ReCaptchaV2EnterpriseTaskProxyless {

    public static void main(String[] args) {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        ReCaptchaV2TaskParams params =
                ReCaptchaV2TaskParams.builder()
                        .websiteUrl("https://example.com/login")
                        .websiteKey("YOUR_ENTERPRISE_SITE_KEY")
                        .invisible(false)
                        .build();

        Solved<ReCaptchaSolution> solved = client.solveReCaptchaV2EnterpriseTaskProxyless(params);

        System.out.println("Task ID: " + solved.getTaskId());
        System.out.println("Token:   " + solved.getSolution().getToken());
    }
}
