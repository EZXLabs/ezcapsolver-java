package com.burstlinker.ezcapsolver.examples.hcaptcha;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.HCaptchaClassificationSolution;
import com.burstlinker.ezcapsolver.model.task.HCaptchaClassificationTaskParams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;

/**
 * Asks a worker to read hCaptcha images, rather than solve the widget.
 *
 * <p>Task type: {@code HCaptchaClassification}
 *
 * <p>Runs on the synchronous endpoint. <strong>Every field is optional</strong> — different
 * classification modules take different input combinations, and the service declares no
 * validation at all for this type.
 *
 * <p>The solution shape is not confirmed: {@code HCaptchaClassificationSolution} has no declared
 * fields, only the pass-through map. Read the answer with {@code get(key)}, or off
 * {@code solved.getRaw()}. Guessing at field names here would be worse than admitting the gap.
 *
 * <p>Run this one from the repository root — it reads its images from {@code examples/fixtures/}.
 *
 * <p>Docs: https://docs.ezxlabs.com/docs/captcha/api/hcaptcha
 */
public class HCaptchaClassification {

    public static void main(String[] args) throws IOException {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        String image = encode("examples/fixtures/crosswalks1x1.jpg");

        HCaptchaClassificationTaskParams params =
                HCaptchaClassificationTaskParams.builder()
                        .images(Arrays.asList(image))
                        .question("Please click each image containing a crosswalk")
                        .build();

        Solved<HCaptchaClassificationSolution> solved =
                client.syncSolveHCaptchaClassification(params);

        System.out.println("Task ID: " + solved.getTaskId());
        // Nothing is modelled, so read the whole response rather than a named field.
        System.out.println("Raw:     " + solved.getRaw());
    }

    private static String encode(String path) throws IOException {
        return Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(path)));
    }
}
