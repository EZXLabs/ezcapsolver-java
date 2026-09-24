package com.burstlinker.ezcapsolver.model.solution;

/**
 * A FunCaptcha classification result.
 *
 * <p><strong>The shape is not confirmed.</strong> No reliable sample exists, so this declares
 * no fields of its own: everything the worker returns is reachable through
 * {@code get(String)}, and {@code Solved.raw} keeps the untouched JSON. Fields get promoted
 * out as samples confirm them, and code reading them by name keeps working across that change.
 *
 * <p>Guessing at fields would be worse than declaring none — a wrong model reads like a
 * contract.
 *
 * <p>Task type: {@code FunCaptchaClassification}
 */
public class FunCaptchaClassificationSolution extends BaseSolution {}
