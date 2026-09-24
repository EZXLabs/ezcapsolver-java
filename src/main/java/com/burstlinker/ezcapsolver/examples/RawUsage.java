package com.burstlinker.ezcapsolver.examples;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.CreateTaskResponse;
import com.burstlinker.ezcapsolver.model.QueryTaskResponse;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.TaskStatus;
import com.burstlinker.ezcapsolver.model.solution.HCaptchaSolution;
import com.burstlinker.ezcapsolver.model.task.HCaptchaTaskParams;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Polling by hand, and reaching a task type the SDK does not model.
 *
 * <p><strong>Every path here creates a billed task.</strong>
 */
public class RawUsage {

    public static void main(String[] args) {
        unmodelledTaskType();
        unmodelledParameter();
        pollByHand();
    }

    /**
     * A task type added to the service after this SDK release.
     *
     * <p>{@code solveRaw} takes the wire type name as a string and hands back the solution as a
     * {@code JsonNode}, so a new type is usable the day it ships — no SDK release, no waiting.
     */
    private static void unmodelledTaskType() {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("websiteURL", "https://example.com");
        params.put("websiteKey", "YOUR_SITE_KEY");

        Solved<JsonNode> solved = client.solveRaw("SomeBrandNewTaskType", params);

        System.out.println("Task ID:  " + solved.getTaskId());
        System.out.println("Solution: " + solved.getSolution());
    }

    /**
     * A modelled type that grew a new parameter.
     *
     * <p>Keep the typed model and add the field with {@code put}. It flattens to the same level
     * as the declared fields on the wire. A declared field always wins a conflict, so this
     * cannot quietly override {@code websiteUrl}.
     */
    private static void unmodelledParameter() {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        HCaptchaTaskParams params =
                HCaptchaTaskParams.builder()
                        .websiteUrl("https://accounts.hcaptcha.com/demo")
                        .websiteKey("338af34c-7bcb-4c7c-900b-acbec73d7d43")
                        .build();
        params.put("someParameterAddedLater", 42);

        Solved<HCaptchaSolution> solved = client.solveHCaptcha(params);
        System.out.println("Pass: " + solved.getSolution().getGeneratedPassUuid());

        // The reverse direction too: anything the worker returns that the model does not
        // declare is on the solution rather than discarded.
        System.out.println("Unmodelled field: " + solved.getSolution().get("someNewField"));
    }

    /**
     * The two endpoints, driven separately.
     *
     * <p>Worth doing when the waiting has to live somewhere else — a job queue, a scheduler, a
     * different process. Note that {@code waitForResult} already does this properly, including
     * riding out a throttled query; prefer it unless you need the task ID to outlive the call.
     */
    private static void pollByHand() {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        HCaptchaTaskParams params =
                HCaptchaTaskParams.builder()
                        .websiteUrl("https://accounts.hcaptcha.com/demo")
                        .websiteKey("338af34c-7bcb-4c7c-900b-acbec73d7d43")
                        .build();

        CreateTaskResponse created = client.createTask("HCaptcha", params);
        String taskId = created.getTaskId();

        // From this moment the task is billed. Persist the ID before doing anything else —
        // losing it means paying again. The service holds the result for five minutes.
        System.out.println("Created: " + taskId);

        QueryTaskResponse result = client.getTaskResult(taskId);

        // status() is the validated view; getStatus() is the raw string the service sent, kept
        // so an unrecognised value survives into the error message instead of being swallowed.
        if (result.status() == TaskStatus.READY) {
            System.out.println("Ready: " + result.getSolution());
        } else {
            // Hand it back to the SDK rather than writing another sleep loop.
            Solved<HCaptchaSolution> solved = client.waitForResult(taskId, HCaptchaSolution.class);
            System.out.println("Pass: " + solved.getSolution().getGeneratedPassUuid());
        }
    }
}
