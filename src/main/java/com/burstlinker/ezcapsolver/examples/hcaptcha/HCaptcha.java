package com.burstlinker.ezcapsolver.examples.hcaptcha;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.HCaptchaSolution;
import com.burstlinker.ezcapsolver.model.task.HCaptchaTaskParams;

/**
 * Solves an hCaptcha challenge.
 *
 * <p>Task type: {@code HCaptcha}
 *
 * <p>This one points at hCaptcha's own demo page and works as written.
 *
 * <p>Parameters the SDK does not model go through {@code params.put(key, value)} and reach the
 * worker unchanged — no SDK release needed.
 *
 * <p>Docs: https://docs.ezxlabs.com/docs/captcha/api/hcaptcha
 */
public class HCaptcha {

    public static void main(String[] args) {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        HCaptchaTaskParams params =
                HCaptchaTaskParams.builder()
                        .websiteUrl("https://accounts.hcaptcha.com/demo")
                        .websiteKey("338af34c-7bcb-4c7c-900b-acbec73d7d43")
                        .lang("en-US")
                        .proxy(System.getenv("EZCAPTCHA_PROXY"))
                        // False because the demo page shows a checkbox. Sites that hide it
                        // need true here.
                        .invisible(false)
                        // Only the sites that publish an rqdata value need this one.
                        .rqData(System.getenv("EZCAPTCHA_HCAPTCHA_RQDATA"))
                        .build();

        Solved<HCaptchaSolution> solved = client.solveHCaptcha(params);

        System.out.println("Task ID: " + solved.getTaskId());
        System.out.println("Pass:    " + solved.getSolution().getGeneratedPassUuid());
        System.out.println("Ua:      " + solved.getSolution().getUa());
    }
}
