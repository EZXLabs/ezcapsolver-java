package com.burstlinker.ezcapsolver.examples.incapsula;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.IncapsulaSolution;
import com.burstlinker.ezcapsolver.model.task.IncapsulaTaskParams;

/**
 * Produces an Incapsula (Imperva) {@code ___utmvc} payload.
 *
 * <p>Task type: {@code IncapsulaTaskProxyless}
 *
 * <p>The worker needs the obfuscated script itself, not just its URL — pass the source in
 * {@code script} and the URL it came from in {@code scriptUrl}.
 *
 * <p>Runs on the synchronous endpoint.
 */
public class IncapsulaTaskProxyless {

    public static void main(String[] args) {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        IncapsulaTaskParams params =
                IncapsulaTaskParams.builder()
                        .script("(function(){...the obfuscated script source...})()")
                        .scriptUrl("https://example.com/_Incapsula_Resource?SWJIYLWA=...")
                        .pageUrl("https://example.com")
                        .ua("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .acceptLanguage("en-US,en;q=0.9")
                        .proxy(System.getenv("EZCAPTCHA_PROXY"))
                        .build();

        Solved<IncapsulaSolution> solved = client.syncSolveIncapsulaTaskProxyless(params);

        System.out.println("Task ID: " + solved.getTaskId());
        System.out.println("Status:  " + solved.getSolution().getStatus());
        System.out.println("Data:    " + solved.getSolution().getData());
    }
}
