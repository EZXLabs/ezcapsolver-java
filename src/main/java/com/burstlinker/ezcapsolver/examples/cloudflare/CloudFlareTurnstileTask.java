package com.burstlinker.ezcapsolver.examples.cloudflare;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.CloudFlareTurnstileSolution;
import com.burstlinker.ezcapsolver.model.task.CloudFlareTurnstileTaskParams;

/**
 * Requests a Cloudflare Turnstile token.
 *
 * <p>Task type: {@code CloudFlareTurnstileTask}
 *
 * <p>Unlike the five-second interstitial, the proxy here is optional.
 */
public class CloudFlareTurnstileTask {

    public static void main(String[] args) {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        CloudFlareTurnstileTaskParams params =
                CloudFlareTurnstileTaskParams.builder()
                        .websiteUrl("https://example.com/login")
                        .websiteKey("0x4AAAAAAA...")
                        .proxy(System.getenv("EZCAPTCHA_PROXY"))
                        .build();

        Solved<CloudFlareTurnstileSolution> solved = client.solveCloudFlareTurnstileTask(params);

        System.out.println("Task ID: " + solved.getTaskId());
        System.out.println("Token:   " + solved.getSolution().getToken());
        System.out.println("Headers: " + solved.getSolution().getHeader());
    }
}
