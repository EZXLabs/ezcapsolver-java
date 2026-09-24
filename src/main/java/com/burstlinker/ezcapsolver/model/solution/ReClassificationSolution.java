package com.burstlinker.ezcapsolver.model.solution;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The result of a ReCaptcha V2 image classification task.
 *
 * <p>{@link #getType()} identifies the result kind; an unrecognised value is preserved rather
 * than rejected, for the caller to interpret.
 *
 * <p>Task type: {@code ReCaptchaV2Classification}
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReClassificationSolution extends BaseSolution {

    private static final String TYPE_MULTI = "multi";
    private static final String TYPE_SINGLE = "single";

    /** The worker result type, usually {@code multi} or {@code single}. */
    private String type;

    /** Whether a single image contains the requested object. Meaningful when single. */
    private boolean hasObject;

    /** The zero-based indexes of the cells to select. Meaningful when multi. */
    private List<Integer> objects;

    /**
     * Whether the worker returned a multi-cell grid result.
     *
     * <p>{@code @JsonIgnore} because Jackson reads any no-argument {@code isX()} as a bean
     * property. Without it this convenience method invents a {@code multi} key, and a
     * re-serialized solution stops matching the raw JSON it came from.
     */
    @JsonIgnore
    public boolean isMulti() {
        return TYPE_MULTI.equals(type);
    }

    /** Whether the worker returned a single-image result. See {@link #isMulti()}. */
    @JsonIgnore
    public boolean isSingle() {
        return TYPE_SINGLE.equals(type);
    }
}
