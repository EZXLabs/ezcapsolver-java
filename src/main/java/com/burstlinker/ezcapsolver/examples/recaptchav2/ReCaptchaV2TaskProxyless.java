package com.burstlinker.ezcapsolver.examples.recaptchav2;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.ReCaptchaSolution;
import com.burstlinker.ezcapsolver.model.task.ReCaptchaV2TaskParams;

/**
 * Solves an ordinary reCAPTCHA v2 checkbox.
 *
 * <p>Task type: {@code ReCaptchaV2TaskProxyless}
 *
 * <p>This one points at Google's own demo page and works as written.
 *
 * <p>Docs: https://docs.ezxlabs.com/docs/captcha/api/recaptcha-v2
 */
public class ReCaptchaV2TaskProxyless {

    public static void main(String[] args) {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        ReCaptchaV2TaskParams params =
                ReCaptchaV2TaskParams.builder()
                        .websiteUrl("https://www.google.com/recaptcha/api2/demo")
                        .websiteKey("6Le-wvkSAAAAAPBMRTvw0Q4Muexq9bi0DJwx_mJ-")
                        // False because the demo page shows a checkbox. Sites that
                        // hide the widget need true here.
                        .invisible(false)
                        .build();

        Solved<ReCaptchaSolution> solved = client.solveReCaptchaV2TaskProxyless(params);

        System.out.println("Task ID: " + solved.getTaskId());
        System.out.println("Token:   " + solved.getSolution().getToken());
        System.out.println("Ua:      " + solved.getSolution().getUserAgent());
    }
}
