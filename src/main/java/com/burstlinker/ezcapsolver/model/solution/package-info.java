/**
 * What the workers return, one model per task family.
 *
 * <p>The service does not define these shapes — workers do, and workers ship faster than this
 * SDK. Two rules follow, and both are load-bearing:
 *
 * <ul>
 *   <li>an unmodelled key is <strong>kept</strong>, reachable through {@code get(String)},
 *       never dropped;
 *   <li>a field is marked {@link com.burstlinker.ezcapsolver.model.solution.Required} only where the
 *       service has confirmed the worker always returns it. Anything merely usual stays
 *       optional and decodes to {@code null}.
 * </ul>
 *
 * <p>A shape without a reliable sample gets <strong>no declared fields at all</strong>. A
 * guessed model is worse than none, because it reads as a contract.
 *
 * <p>Never reduce a solution to "guess one token string by priority" — that loses the
 * Cloudflare 5s headers and the TLS forwarding body, and turns a worker-side rename into a
 * silently empty string.
 */
package com.burstlinker.ezcapsolver.model.solution;
