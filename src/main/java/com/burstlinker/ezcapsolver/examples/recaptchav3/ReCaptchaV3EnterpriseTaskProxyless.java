package com.burstlinker.ezcapsolver.examples.recaptchav3;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.ReCaptchaSolution;
import com.burstlinker.ezcapsolver.model.task.ReCaptchaV3TaskParams;

/**
 * Solves a reCAPTCHA v3 Enterprise score challenge.
 *
 * <p>Task type: {@code ReCaptchaV3EnterpriseTaskProxyless}
 *
 * <p>{@code checkField} is the v3 Enterprise extra: some sites verify an additional field
 * alongside the token, and the worker needs to know which one.
 *
 * <p>Docs: https://docs.ezxlabs.com/docs/captcha/api/recaptcha-v3
 */
public class ReCaptchaV3EnterpriseTaskProxyless {

    public static void main(String[] args) {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        ReCaptchaV3TaskParams params =
                ReCaptchaV3TaskParams.builder()
                        .websiteUrl("https://example.com/checkout")
                        .websiteKey("YOUR_ENTERPRISE_SITE_KEY")
                        .pageAction("checkout")
                        .build();

        Solved<ReCaptchaSolution> solved = client.solveReCaptchaV3EnterpriseTaskProxyless(params);

        System.out.println("Task ID: " + solved.getTaskId());
        System.out.println("Token:   " + solved.getSolution().getToken());
    }
}
