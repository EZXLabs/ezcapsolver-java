/**
 * The Java SDK for EzCaptchaSolver: a client library and nothing else — no server, no CLI.
 *
 * <p>Start at {@link com.burstlinker.ezcapsolver.EzCapSolverClient}, configure it with {@link
 * com.burstlinker.ezcapsolver.ClientConfig}, and hand it a task parameter model from {@link
 * com.burstlinker.ezcapsolver.model.task}:
 *
 * <pre>{@code
 * EzCapSolverClient client = new EzCapSolverClient();   // key from EZCAPTCHA_API_KEY
 *
 * HCaptchaTaskParams params = HCaptchaTaskParams.builder()
 *         .websiteUrl("https://example.com")
 *         .websiteKey("...")
 *         .lang("en-US")
 *         .build();
 *
 * Solved<HCaptchaSolution> solved = client.solveHCaptcha(params);
 * }</pre>
 *
 * <h2>Two things worth knowing before the first call</h2>
 *
 * <p><strong>Creating a task is billed and is not idempotent.</strong> The SDK therefore
 * never retries one on its own — a timeout cannot tell you whether the service already
 * accepted the task. The single exception is a <em>throttled result query</em> during
 * polling, which refuses the query without touching the task.
 *
 * <p><strong>A failure after a task exists is worth recovering.</strong> Ask {@link
 * com.burstlinker.ezcapsolver.exception.EzCaptchaException#taskIdOf} rather than creating a
 * second task; the service holds a result for five minutes.
 *
 * <h2>Package layout</h2>
 *
 * <ul>
 *   <li>{@link com.burstlinker.ezcapsolver.model.task} — task parameter models, one per family
 *   <li>{@link com.burstlinker.ezcapsolver.model.solution} — what the workers return
 *   <li>{@link com.burstlinker.ezcapsolver.model} — results, envelopes and shared vocabulary
 *   <li>{@link com.burstlinker.ezcapsolver.exception} — every failure, under one base class
 *   <li>{@code com.burstlinker.ezcapsolver.internal} — plumbing, <strong>not API</strong>
 * </ul>
 *
 * <p>Everything {@code public} in this package is API. The SDK's own plumbing is either
 * package-private here — {@code Transport}, whose only caller is {@link
 * com.burstlinker.ezcapsolver.EzCapSolverClient} — or lives in {@code internal}, which is where a
 * class goes when more than one package calls it and {@code public} becomes unavoidable.
 */
package com.burstlinker.ezcapsolver;
