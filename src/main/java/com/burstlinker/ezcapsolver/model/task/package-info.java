/**
 * Task parameter models, one per family of task types.
 *
 * <p>Named {@code ...TaskParams} because that is what they are: parameters, with no task id,
 * no status, and <strong>no task type</strong>. One parameter set backs several types — the
 * five ReCaptcha V2 variants all take the same fields — and which one runs is decided by the
 * method called on the client, not by a field here.
 *
 * <p>Anything this release does not model goes through {@code put}, flattened next to the
 * declared fields, so a parameter the service adds is usable without waiting for a release:
 *
 * <pre>{@code
 * HCaptchaTaskParams params = HCaptchaTaskParams.builder()
 *         .websiteUrl("https://example.com")
 *         .websiteKey("...")
 *         .lang("en-US")
 *         .build();
 * params.put("someNewParameter", 42);
 * }</pre>
 *
 * <p>A declared field always wins over a pass-through entry of the same name.
 */
package com.burstlinker.ezcapsolver.model.task;
