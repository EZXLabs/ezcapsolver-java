package com.ezxlabs.ezcapsolver.examples.cloudflare;

import com.ezxlabs.ezcapsolver.EzCapSolverClient;
import com.ezxlabs.ezcapsolver.model.Solved;
import com.ezxlabs.ezcapsolver.model.solution.CloudFlareTurnstileSolution;
import com.ezxlabs.ezcapsolver.model.task.CloudFlareTurnstileTaskParams;

/**
 * Requests a Cloudflare Turnstile token.
 *
 * <p>Task type: {@code CloudFlareTurnstileTask}
 *
 * <p>Unlike the five-second interstitial, the proxy here is optional.
 */
public class CloudFlareTurnstileTask {

    public static void main(String[] args) {
        EzCapSolverClient client = new EzCapSolverClient("YOUR_API_KEY");

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
