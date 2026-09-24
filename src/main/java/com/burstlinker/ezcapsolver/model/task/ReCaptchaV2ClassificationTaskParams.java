package com.burstlinker.ezcapsolver.model.task;

import com.burstlinker.ezcapsolver.model.solution.ReClassificationSolution;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Asks a worker to read one ReCaptcha V2 image grid. Runs through the synchronous endpoint and
 * returns its answer inline.
 *
 * <p>Task type: {@code ReCaptchaV2Classification}
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReCaptchaV2ClassificationTaskParams extends BaseTaskParams<ReClassificationSolution> {

    /** The base64-encoded challenge image. Required. */
    private String image;

    /** The object identifier or classification question. Required. */
    private String question;

    /**
     * The grid layout: 1 for a single tile, 3 for 3x3, 4 for 4x4.
     *
     * <p>Omitted when unset, leaving the service to apply its own default of 4. The range is
     * not enforced anywhere, here or service-side.
     */
    private Integer size;

    @Override
    public Class<ReClassificationSolution> solutionType() {
        return ReClassificationSolution.class;
    }
}
