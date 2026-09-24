package com.burstlinker.ezcapsolver.examples.funcaptcha;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.FunCaptchaClassificationSolution;
import com.burstlinker.ezcapsolver.model.task.FunCaptchaClassificationTaskParams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * Asks a worker to read a FunCaptcha image, rather than solve the challenge.
 *
 * <p>Task type: {@code FunCaptchaClassification}
 *
 * <p>Runs on the synchronous endpoint.
 *
 * <p>As with {@code HCaptchaClassification}, the solution shape is not confirmed —
 * {@code FunCaptchaClassificationSolution} declares no fields. Read the answer off
 * {@code solved.getRaw()} or with {@code get(key)}.
 *
 * <p>Run this one from the repository root — it reads its image from {@code examples/fixtures/}.
 *
 * <p>Docs: https://docs.ezxlabs.com/docs/captcha/api/funcaptcha
 */
public class FunCaptchaClassification {

    public static void main(String[] args) throws IOException {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        byte[] image = Files.readAllBytes(Paths.get("examples/fixtures/crosswalks1x1.jpg"));

        FunCaptchaClassificationTaskParams params =
                FunCaptchaClassificationTaskParams.builder()
                        .image(Base64.getEncoder().encodeToString(image))
                        .question("Pick the image that is the right way up")
                        .build();

        Solved<FunCaptchaClassificationSolution> solved =
                client.syncSolveFunCaptchaClassification(params);

        System.out.println("Task ID: " + solved.getTaskId());
        System.out.println("Raw:     " + solved.getRaw());
    }
}
