package com.burstlinker.ezcapsolver.examples;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.exception.ApiException;
import com.burstlinker.ezcapsolver.exception.EzCaptchaException;
import com.burstlinker.ezcapsolver.exception.PollingExhaustedException;
import com.burstlinker.ezcapsolver.exception.SolutionDecodeException;
import com.burstlinker.ezcapsolver.exception.TransportException;
import com.burstlinker.ezcapsolver.exception.UnexpectedResponseException;
import com.burstlinker.ezcapsolver.exception.WaitInterruptedException;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.HCaptchaSolution;
import com.burstlinker.ezcapsolver.model.task.HCaptchaTaskParams;

/**
 * Telling the failure layers apart, and recovering a task you have already paid for.
 *
 * <p><strong>Logging.</strong> The SDK logs through slf4j and ships only the API, so it stays
 * silent until you put a binding on the classpath — logback-classic, slf4j-simple, whichever
 * your application already uses. It never logs your API key: {@code ClientConfig.toString()}
 * masks both the key and the proxy URL, a proxy URL carrying credentials of its own.
 */
public class LoggingAndErrors {

    public static void main(String[] args) {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        HCaptchaTaskParams params =
                HCaptchaTaskParams.builder()
                        .websiteUrl("https://accounts.hcaptcha.com/demo")
                        .websiteKey("338af34c-7bcb-4c7c-900b-acbec73d7d43")
                        .build();

        try {
            Solved<HCaptchaSolution> solved = client.solveHCaptcha(params);
            System.out.println("Pass: " + solved.getSolution().getGeneratedPassUuid());

        } catch (ApiException e) {
            // The service answered, and said no.
            System.err.println("code:    " + e.getErrorCode());
            System.err.println("http:    " + e.getHttpStatus());
            System.err.println("request: " + e.getRequestId());

            if (e.isAuthenticationError()) {
                // Stop. These three codes trip a server-side ban counter, so retrying digs the
                // hole deeper. Fix the key or the plan first.
                System.err.println("authentication problem — do not retry");
            } else if (e.isRateLimited()) {
                // Throttled. Nothing was created, so this one is safe to retry after a wait.
                System.err.println("throttled — back off and try again");
            } else if (e.isTerminal()) {
                // Retrying with the same input will fail the same way.
                System.err.println("terminal — change the request, not the timing");
            }

        } catch (PollingExhaustedException e) {
            // The budget ran out. The task is still running and already billed.
            recover(client, e);

        } catch (TransportException e) {
            // Never reached the service, or never got an answer back.
            //
            // Note what the SDK does NOT do here: it does not retry task creation. A timeout
            // cannot tell you whether the service already accepted the task, and a blind retry
            // pays twice. Deciding that is the caller's to make, with the caller's knowledge.
            System.err.println("transport: " + e.getMessage());

        } catch (UnexpectedResponseException e) {
            // A well-formed HTTP response that broke the contract — a status outside the three
            // the service defines, or "ready" with no solution attached.
            recover(client, e);

        } catch (SolutionDecodeException e) {
            // The task succeeded and the worker returned something the model could not read.
            // The result is still there; fetch it raw rather than paying again.
            recover(client, e);

        } catch (WaitInterruptedException e) {
            // The thread was interrupted mid-wait. The task is unaffected.
            Thread.currentThread().interrupt();
            recover(client, e);

        } catch (EzCaptchaException e) {
            // The base class, and the one to catch if you only want a single arm. Configuration
            // errors arrive as this type directly, with no subclass.
            System.err.println("ezcapsolver: " + e.getMessage());
        }
    }

    /**
     * The point of the whole hierarchy: a failure after the task exists is worth recovering.
     *
     * <p>{@code taskIdOf} is the one entry point that finds the ID on any exception carrying
     * one, so this works without knowing which of the five layers failed. The service holds a
     * result for five minutes — well past that, and a second task is the only option left.
     */
    private static void recover(EzCapSolverClient client, EzCaptchaException failure) {
        String taskId = EzCaptchaException.taskIdOf(failure);
        if (taskId == null) {
            System.err.println("no task was created: " + failure.getMessage());
            return;
        }

        System.err.println("task " + taskId + " is alive and paid for; picking it back up");
        try {
            Solved<HCaptchaSolution> solved = client.waitForResult(taskId, HCaptchaSolution.class);
            System.out.println("Recovered: " + solved.getSolution().getGeneratedPassUuid());
        } catch (EzCaptchaException e) {
            System.err.println("recovery failed too: " + e.getMessage());
        }
    }
}
