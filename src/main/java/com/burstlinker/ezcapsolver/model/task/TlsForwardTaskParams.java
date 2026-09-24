package com.burstlinker.ezcapsolver.model.task;

import com.burstlinker.ezcapsolver.internal.Redaction;
import com.burstlinker.ezcapsolver.model.solution.TlsForwardSolution;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Sends one HTTP request from a worker, using that worker's TLS fingerprint instead of yours.
 * Runs through the synchronous endpoint and returns the upstream response.
 *
 * <p>Every field name here is snake_case. This is also the one task type the service exempts
 * from site allow-listing and block-listing.
 *
 * <p>Task type: {@code TlsTask}
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TlsForwardTaskParams extends BaseTaskParams<TlsForwardSolution> {

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_PATCH = "PATCH";

    /** The worker's TLS fingerprint identifier, such as {@code chrome}. Required. */
    @JsonProperty("tls_type")
    private String tlsType;

    /** The proxy used for the upstream request. Required for this type. */
    @ToString.Exclude
    private String proxy;

    /**
     * The upstream HTTP method. Required. The service matches case-insensitively and accepts
     * nothing outside the five constants on this class.
     *
     * <p>Defaults to {@value #METHOD_GET}, and is always sent: the service validates against a
     * closed set, so an absent value would be rejected rather than defaulted.
     */
    @Builder.Default
    private String method = METHOD_GET;

    /** The upstream URL. Required, and it has to start with http or https. */
    private String url;

    /** The optional upstream request headers. */
    private Map<String, Object> headers;

    /**
     * Optionally pins the order headers are sent in, which is part of what a fingerprint check
     * looks at.
     */
    @JsonProperty("headers_order")
    private String headersOrder;

    /** The optional upstream request cookies. */
    private Map<String, Object> cookies;

    /** The optional upstream request body. Any JSON value: a string, an object, an array. */
    private Object body;

    /**
     * Whether {@link #getBody()} is already base64-encoded.
     *
     * <p>Always sent, so the worker is never left guessing the encoding.
     */
    @JsonProperty("body_raw")
    private boolean bodyRaw;

    @Override
    public Class<TlsForwardSolution> solutionType() {
        return TlsForwardSolution.class;
    }

    @ToString.Include(name = "proxy")
    private String maskedProxy() {
        return Redaction.mask(proxy);
    }
}
