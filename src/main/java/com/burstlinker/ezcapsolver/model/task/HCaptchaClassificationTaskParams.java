package com.burstlinker.ezcapsolver.model.task;

import com.burstlinker.ezcapsolver.model.solution.HCaptchaClassificationSolution;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Asks a worker to read one or more HCaptcha images. Runs through the synchronous endpoint.
 *
 * <p><strong>Every field is optional.</strong> Different classification modules take different
 * input combinations, and the service declares no validation at all for this type.
 *
 * <p>The solution shape is not confirmed yet; see {@link HCaptchaClassificationSolution}.
 *
 * <p>Task type: {@code HCaptchaClassification}
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HCaptchaClassificationTaskParams
        extends BaseTaskParams<HCaptchaClassificationSolution> {

    /** A single base64-encoded image. */
    private String image;

    /** A collection of base64-encoded images. */
    private List<String> images;

    /** A collection of anchor images. */
    private List<String> anchors;

    /** The classification question. */
    private String question;

    /** Selects the classification module. */
    private String module;

    @Override
    public Class<HCaptchaClassificationSolution> solutionType() {
        return HCaptchaClassificationSolution.class;
    }
}
