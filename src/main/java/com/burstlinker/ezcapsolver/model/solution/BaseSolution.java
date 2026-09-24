package com.burstlinker.ezcapsolver.model.solution;

import com.burstlinker.ezcapsolver.model.ExtraFields;

/**
 * The base of every solution model: the worker fields this release does not declare, kept
 * rather than dropped.
 *
 * <p>The service does not define these shapes — workers do, and workers ship faster than this
 * SDK. A field added on their side has to stay reachable without waiting for a release, so
 * unmodelled keys are collected by {@link ExtraFields} instead of being discarded:
 *
 * <pre>{@code
 * Object value = solution.get("someNewField");
 * }</pre>
 *
 * <p>Together with the untouched JSON on {@code Solved.raw}, that is what makes a result
 * lossless.
 *
 * <p>Two rules hold for every subclass:
 *
 * <ul>
 *   <li>a field is declared {@link Required} only where the service has confirmed the worker
 *       always returns it; anything merely usual stays optional and decodes to {@code null};
 *   <li>a shape without a reliable sample gets <strong>no declared fields at all</strong>. A
 *       guessed model is worse than none, because it reads as a contract.
 * </ul>
 */
public abstract class BaseSolution extends ExtraFields {}
