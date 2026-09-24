package com.burstlinker.ezcapsolver.examples.funcaptcha;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.FunCaptchaSolution;
import com.burstlinker.ezcapsolver.model.task.FunCaptchaTaskParams;

/**
 * Solves a FunCaptcha / Arkose Labs challenge.
 *
 * <p>Task type: {@code FuncaptchaTaskProxyless}
 *
 * <p>Note the lowercase {@code c} in the task type. That is the catalog's own spelling, kept
 * verbatim rather than tidied up — {@code FunCaptchaClassification} really does capitalise it
 * differently. The SDK is part of the published documentation, so these irregularities stay.
 *
 * <p>{@code data} is the blob Arkose calls {@code blob}: a per-session value the page produces.
 * Sites that use it reject a token obtained without it.
 *
 * <p>Docs: https://docs.ezxlabs.com/docs/captcha/api/funcaptcha
 */
public class FuncaptchaTaskProxyless {

    public static void main(String[] args) {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        FunCaptchaTaskParams params =
                FunCaptchaTaskParams.builder()
                        .websiteUrl("https://example.com/signup")
                        .websiteKey("YOUR_PUBLIC_KEY")
                        .apiJsSubdomain("client-api.arkoselabs.com")
                        .data(System.getenv("EZCAPTCHA_FUNCAPTCHA_BLOB"))
                        .proxy(System.getenv("EZCAPTCHA_PROXY"))
                        .build();

        Solved<FunCaptchaSolution> solved = client.solveFuncaptchaTaskProxyless(params);

        System.out.println("Task ID: " + solved.getTaskId());
        System.out.println("Token:   " + solved.getSolution().getToken());
    }
}
