package com.burstlinker.ezcapsolver.examples.recaptchav2;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.ReCaptchaSolution;
import com.burstlinker.ezcapsolver.model.task.ReCaptchaV2TaskParams;

/**
 * Solves a reCAPTCHA v2 checkbox through the S9 worker pool.
 *
 * <p>Task type: {@code ReCaptchaV2TaskProxylessS9}
 *
 * <p>Identical parameters to {@code ReCaptchaV2TaskProxyless} — only the task type differs, which
 * is the whole reason task types are a separate argument rather than a field on the model. The S9
 * pool is a different set of workers; whether it is available depends on your plan.
 *
 * <p>Docs: https://docs.ezxlabs.com/docs/captcha/api/recaptcha-v2
 */
public class ReCaptchaV2TaskProxylessS9 {

    public static void main(String[] args) {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        ReCaptchaV2TaskParams params =
                ReCaptchaV2TaskParams.builder()
                        .websiteUrl("https://www.google.com/recaptcha/api2/demo")
                        .websiteKey("6Le-wvkSAAAAAPBMRTvw0Q4Muexq9bi0DJwx_mJ-")
                        .invisible(false)
                        .build();

        Solved<ReCaptchaSolution> solved = client.solveReCaptchaV2TaskProxylessS9(params);

        System.out.println("Task ID: " + solved.getTaskId());
        System.out.println("Token:   " + solved.getSolution().getToken());
    }
}
