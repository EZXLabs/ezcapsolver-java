package com.burstlinker.ezcapsolver.examples.datadome;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.DataDomeSolution;
import com.burstlinker.ezcapsolver.model.task.DataDomeTaskParams;

import java.util.Base64;

/**
 * Answers a DataDome challenge after an interception.
 *
 * <p>Task type: {@code DataDomeTaskProxyless}
 *
 * <p>Two steps: step one fetches the challenge URL, step two produces the validation
 * instructions. {@code step} defaults to {@code "1"} and is always sent, because the service
 * validates it against a closed set — an absent value is rejected rather than defaulted.
 *
 * <p>{@code referer} is documented as optional but the real workflow needs it, and it is what
 * site allow-listing is checked against: with an allow-list configured, a referer outside it
 * comes back as {@code ERROR_WEBSITE_NOT_ALLOWED}.
 *
 * <p>Every field name in this model is snake_case, unlike {@code DataDomeTagsTaskProxyless},
 * whose are flat lowercase. The two go to different workers.
 *
 * <p>Runs on the synchronous endpoint.
 */
public class DataDomeTaskProxyless {

    public static void main(String[] args) {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        String challengeHtml = "<html>...the intercepted challenge page...</html>";

        DataDomeTaskParams params =
                DataDomeTaskParams.builder()
                        .htmlB64(
                                Base64.getEncoder()
                                        .encodeToString(challengeHtml.getBytes(
                                                java.nio.charset.Charset.forName("UTF-8"))))
                        .step(DataDomeTaskParams.STEP_ONE)
                        .referer("https://example.com/search")
                        .build();

        Solved<DataDomeSolution> solved = client.syncSolveDataDomeTaskProxyless(params);

        System.out.println("Task ID: " + solved.getTaskId());
        System.out.println("Kind:    " + solved.getSolution().getKind());
        System.out.println("URL:     " + solved.getSolution().getUrl());
        System.out.println("Body:    " + solved.getSolution().getBody());
    }
}
