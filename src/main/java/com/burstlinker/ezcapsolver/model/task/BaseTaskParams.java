package com.burstlinker.ezcapsolver.model.task;

import com.burstlinker.ezcapsolver.model.ExtraFields;
import lombok.experimental.SuperBuilder;

/**
 * The base of every task parameter model: an escape hatch for parameters this release does
 * not declare, plus the binding to a solution type.
 *
 * <pre>{@code
 * HCaptchaTaskParams params = HCaptchaTaskParams.builder()
 *         .websiteUrl("https://example.com")
 *         .websiteKey("...")
 *         .build();
 * params.put("someNewParameter", 42);
 * }</pre>
 *
 * <p>The pass-through behaviour and the rule that a declared field beats an entry of the same
 * name both live on {@link ExtraFields}, which the solution models share. This class adds
 * only the builder root and {@link TaskParams}.
 *
 * <p>Task parameter models are serialize-only. Nothing ever decodes one back off the wire.
 *
 * @param <S> the solution model this task's result decodes into
 */
@SuperBuilder
public abstract class BaseTaskParams<S> extends ExtraFields implements TaskParams<S> {

    /** Present so subclasses can keep a no-argument constructor alongside their builder. */
    protected BaseTaskParams() {}
}
