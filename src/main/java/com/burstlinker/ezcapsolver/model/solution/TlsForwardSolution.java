package com.burstlinker.ezcapsolver.model.solution;

import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The upstream response a TLS forwarding task fetched.
 *
 * <p>Task type: {@code TlsTask}
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TlsForwardSolution extends BaseSolution {

    /** The worker's own response status, not the upstream one. */
    private int status;

    /** The upstream HTTP status code. This is the one that describes the fetched site. */
    private int code;

    /** The upstream response headers. */
    private Map<String, Object> headers;

    /** The upstream response cookies. */
    private Map<String, Object> cookies;

    /** The upstream response body. */
    private String body;
}
