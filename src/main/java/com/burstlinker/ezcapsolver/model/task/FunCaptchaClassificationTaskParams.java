package com.burstlinker.ezcapsolver.model.task;

import com.burstlinker.ezcapsolver.model.solution.FunCaptchaClassificationSolution;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Asks a worker to read one FunCaptcha image. Runs through the synchronous endpoint.
 *
 * <p>The solution shape is not confirmed yet; see {@link FunCaptchaClassificationSolution}.
 *
 * <p>Task type: {@code FunCaptchaClassification}
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FunCaptchaClassificationTaskParams
        extends BaseTaskParams<FunCaptchaClassificationSolution> {

    /** The base64-encoded challenge image. Required. */
    private String image;

    /** The classification question. Required. */
    private String question;

    @Override
    public Class<FunCaptchaClassificationSolution> solutionType() {
        return FunCaptchaClassificationSolution.class;
    }
}
