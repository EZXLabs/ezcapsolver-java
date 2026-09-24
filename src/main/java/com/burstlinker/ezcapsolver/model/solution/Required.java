package com.burstlinker.ezcapsolver.model.solution;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a solution field the worker is confirmed to always return. Its absence is a decoding
 * failure.
 *
 * <p>Without this, a renamed worker field would decode to {@code null} and surface as an
 * empty token several frames later, at the point where it gets submitted to the protected
 * site — which is precisely the silent failure typed models are supposed to prevent. Failing
 * at the decode keeps the task id and the raw JSON in hand, so the result that was paid for
 * is still recoverable.
 *
 * <p>Apply it only where the service has confirmed the field is constant. A field that is
 * merely usually present must stay optional: a worker omitting it should yield {@code null},
 * not an exception.
 *
 * <p>This is not {@code @JsonProperty(required = true)}, which databind does not enforce on
 * ordinary setter-based properties at all — it only affects creator parameters and generated
 * schemas. Reusing it would have looked like a check while doing nothing.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Required {}
