/**
 * Every failure this SDK reports, layered by <strong>what the caller can do about it</strong>
 * rather than by where it came from.
 *
 * <p>All of them extend {@link com.burstlinker.ezcapsolver.exception.EzCaptchaException}, so
 * catching that one type covers everything. All of them are <strong>unchecked</strong>: a
 * {@code throws} clause on every call site would buy nothing, because none of these are
 * recoverable at the point they are thrown.
 *
 * <table border="1">
 *   <caption>The five layers</caption>
 *   <tr><th>Layer</th><th>Type</th><th>Means</th></tr>
 *   <tr>
 *     <td>E1</td>
 *     <td>{@link com.burstlinker.ezcapsolver.exception.EzCaptchaException} itself</td>
 *     <td>Bad configuration. Needs a code change; there is no structured data worth a
 *         subclass, so the reason is in the message.</td>
 *   </tr>
 *   <tr>
 *     <td>E2</td>
 *     <td>{@link com.burstlinker.ezcapsolver.exception.TransportException}</td>
 *     <td>DNS, TCP, TLS or a timeout. No response was produced.</td>
 *   </tr>
 *   <tr>
 *     <td>E3</td>
 *     <td>{@link com.burstlinker.ezcapsolver.exception.ApiException}</td>
 *     <td>The service reported a failure it understands. Inspect its code.</td>
 *   </tr>
 *   <tr>
 *     <td>E4</td>
 *     <td>{@link com.burstlinker.ezcapsolver.exception.PollingExhaustedException}</td>
 *     <td>The budget ran out. The task may still be running, and it was billed.</td>
 *   </tr>
 *   <tr>
 *     <td>E5</td>
 *     <td>{@link com.burstlinker.ezcapsolver.exception.UnexpectedResponseException},
 *         {@link com.burstlinker.ezcapsolver.exception.SolutionDecodeException}</td>
 *     <td>The response broke the contract, or a solution did not fit its model.</td>
 *   </tr>
 * </table>
 *
 * <p>{@link com.burstlinker.ezcapsolver.exception.WaitInterruptedException} sits across the
 * layers: it wraps whatever interrupted a wait, purely so the id of the already billed task
 * travels with it.
 *
 * <p>The constructors are public so a caller can build these for a test double. The SDK is
 * the only thing that produces them in practice.
 */
package com.burstlinker.ezcapsolver.exception;
