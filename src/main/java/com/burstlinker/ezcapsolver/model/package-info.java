/**
 * Results, response envelopes, and the vocabulary shared across the API surface.
 *
 * <p>{@link com.burstlinker.ezcapsolver.model.Solved} is what a successful call hands back: the
 * typed solution, plus <strong>the untouched JSON it came from</strong>. Both halves matter —
 * keeping the raw value is what turns a worker-side field rename into an inconvenience
 * rather than an outage.
 *
 * <p>{@link com.burstlinker.ezcapsolver.model.ExtraFields} is the base of every task parameter
 * and solution model. It carries the keys this release does not declare, in both directions,
 * so nothing is silently dropped.
 *
 * <p>{@link com.burstlinker.ezcapsolver.model.ResponseMeta} and its two subclasses are the wire
 * envelopes. Their {@code errorId} is the <strong>only</strong> success criterion; the HTTP
 * status is not, and neither is a non-empty error code.
 */
package com.burstlinker.ezcapsolver.model;
