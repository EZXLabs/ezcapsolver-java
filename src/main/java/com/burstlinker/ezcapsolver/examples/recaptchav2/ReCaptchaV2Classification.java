package com.burstlinker.ezcapsolver.examples.recaptchav2;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.ReClassificationSolution;
import com.burstlinker.ezcapsolver.model.task.ReCaptchaV2ClassificationTaskParams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * Asks a worker to read a reCAPTCHA image grid, rather than solve the widget.
 *
 * <p>Task type: {@code ReCaptchaV2Classification}
 *
 * <p>Runs on the synchronous endpoint, so it answers inline instead of being polled.
 *
 * <p>Run this one from the repository root — it reads its image from {@code examples/fixtures/}.
 *
 * <p>Docs: <a href="https://docs.ezxlabs.com/docs/captcha/api/recaptcha-v2">...</a>
 */
public class ReCaptchaV2Classification {

    public static void main(String[] args) throws IOException {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        byte[] image = Files.readAllBytes(Paths.get("examples/fixtures/crosswalks3x3.jpg"));

        ReCaptchaV2ClassificationTaskParams params =
                ReCaptchaV2ClassificationTaskParams.builder()
                        .image(Base64.getEncoder().encodeToString(image))
                        .question("/m/014xcs")
                        // Left unset, the service applies its own default of 4. Set it only
                        // when the grid is not 3x3.
                        .size(3)
                        .build();

        Solved<ReClassificationSolution> solved = client.syncSolveReCaptchaV2Classification(params);

        System.out.println("Task ID:    " + solved.getTaskId());
        System.out.println("Type:       " + solved.getSolution().getType());
        System.out.println("Has object: " + solved.getSolution().isHasObject());
        System.out.println("Objects:    " + solved.getSolution().getObjects());
        System.out.println("Multi:      " + solved.getSolution().isMulti());
    }
}
