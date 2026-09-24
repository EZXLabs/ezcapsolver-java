package com.burstlinker.ezcapsolver.model.task;

/**
 * One task's parameters, bound at compile time to the solution model it produces.
 *
 * <p>Named for what it is. These models carry <strong>parameters only</strong> — no task id,
 * no status, and in particular <strong>no task type</strong>: one parameter set backs several
 * types, the five ReCaptcha V2 variants all taking the same fields, and which variant runs is
 * chosen by the method you call. A type field would either block that reuse or let a caller
 * contradict the method they called. The type is injected by the request envelope and always
 * wins.
 *
 * @param <S> the solution model this task's result decodes into
 */
public interface TaskParams<S> {

    /**
     * The solution model for this task's result.
     *
     * <p>Generics are erased at runtime, so {@code S} alone cannot tell the decoder what to
     * build. This is what makes {@code solve(taskType, params)} usable for any modelled task
     * instead of needing one hand-written call site per type.
     *
     * <p>Every task type sharing a parameter model also shares its solution model, which is
     * what lets this live on the model rather than being passed alongside the type. A type
     * that ever breaks that pairing needs its own parameter class.
     */
    Class<S> solutionType();
}
