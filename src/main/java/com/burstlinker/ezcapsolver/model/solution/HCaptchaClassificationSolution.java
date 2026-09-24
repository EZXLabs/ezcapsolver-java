package com.burstlinker.ezcapsolver.model.solution;

/**
 * An HCaptcha classification result.
 *
 * <p><strong>The shape is not confirmed</strong>, for the same reason as
 * {@link FunCaptchaClassificationSolution}: no reliable sample, so no declared fields.
 * Everything is reachable through {@code get(String)} and {@code Solved.raw}.
 *
 * <p>Task type: {@code HCaptchaClassification}
 */
public class HCaptchaClassificationSolution extends BaseSolution {}
