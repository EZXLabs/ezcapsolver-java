package com.burstlinker.ezcapsolver.model.task;

import com.burstlinker.ezcapsolver.model.solution.PerimeterXSolution;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Requests PerimeterX clearance cookies.
 *
 * <p>The canonical type name is {@code PerimeterX}. Older clients called it {@code PxCaptcha},
 * which was never the service's spelling.
 *
 * <p>Task type: {@code PerimeterX}
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PerimeterXTaskParams extends BaseTaskParams<PerimeterXSolution> {

    /** The PerimeterX application identifier. Required. */
    private String websiteKey;

    /**
     * Whether the challenge uses invisible mode. <strong>Enabling it changes how the task is
     * priced.</strong>
     *
     * <p>The wire name is {@code invisible}, without the {@code is} prefix the ReCaptcha types
     * use.
     */
    private boolean invisible;

    @Override
    public Class<PerimeterXSolution> solutionType() {
        return PerimeterXSolution.class;
    }
}
