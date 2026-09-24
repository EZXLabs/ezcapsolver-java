package com.burstlinker.ezcapsolver.examples.recaptchav3;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.ReCaptchaSolution;
import com.burstlinker.ezcapsolver.model.task.ReCaptchaV3TaskParams;

/**
 * Solves a reCAPTCHA v3 Enterprise score challenge through the S9 worker pool.
 *
 * <p>Task type: {@code ReCaptchaV3EnterpriseTaskProxylessS9}
 *
 * <p><strong>The one type whose spelling the SDKs normalise.</strong> The task catalog writes it
 * {@code RecaptchaV3EnterpriseTaskProxylessS9}, with a lowercase {@code c}. Every language SDK
 * sends {@code ReCaptchaV3EnterpriseTaskProxylessS9} instead, so that one capitalisation runs
 * through the whole ReCaptcha family. The service matches task types case-insensitively, so it
 * reaches the same worker either way.
 *
 * <p>Docs: https://docs.ezxlabs.com/docs/captcha/api/recaptcha-v3
 */
public class ReCaptchaV3EnterpriseTaskProxylessS9 {

    public static void main(String[] args) {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        ReCaptchaV3TaskParams params =
                ReCaptchaV3TaskParams.builder()
                        .websiteUrl("https://example.com/checkout")
                        .websiteKey("YOUR_ENTERPRISE_SITE_KEY")
                        .pageAction("checkout")
                        .build();

        Solved<ReCaptchaSolution> solved = client.solveReCaptchaV3EnterpriseTaskProxylessS9(params);

        System.out.println("Task ID: " + solved.getTaskId());
        System.out.println("Token:   " + solved.getSolution().getToken());
    }
}
