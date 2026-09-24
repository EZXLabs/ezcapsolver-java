package com.burstlinker.ezcapsolver.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A finished task: the decoded solution, plus the identifiers and the untouched JSON it came
 * from.
 *
 * <p>Both halves matter. The typed solution is what the code uses; {@link #getRaw()} is what
 * is left when a worker starts returning a shape this release does not model. Keeping the
 * raw value is the reason a worker-side change is an inconvenience rather than an outage.
 *
 * @param <S> the solution model
 */
public final class Solved<S> {

    private final S solution;
    private final JsonNode raw;
    private final String taskId;
    private final String requestId;

    public Solved(S solution, JsonNode raw, String taskId, String requestId) {
        this.solution = solution;
        this.raw = raw;
        this.taskId = taskId;
        this.requestId = requestId;
    }

    /** The decoded result. */
    public S getSolution() {
        return solution;
    }

    /**
     * The solution JSON, untouched.
     *
     * <p>Anything the typed model does not declare is still recoverable from here — as is
     * anything it does, if a field ever needs checking against what actually arrived.
     */
    public JsonNode getRaw() {
        return raw;
    }

    /**
     * The identifier the service assigned.
     *
     * <p>Set on both paths: the asynchronous one from task creation, the synchronous one from
     * alongside the result. The synchronous endpoint does assign one, and it is
     * <strong>kept</strong> rather than blanked — it is the only handle on a billed task.
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * The service-side request tracking identifier.
     *
     * <p>Taken from the request that fetched the result, falling back to the one that created
     * the task when the fetch did not carry one.
     */
    public String getRequestId() {
        return requestId;
    }

    /** Deliberately omits the solution: a token in a log is a credential in a log. */
    @Override
    public String toString() {
        return "Solved{taskId="
                + taskId
                + ", requestId="
                + requestId
                + ", solution="
                + (solution == null ? "null" : solution.getClass().getSimpleName())
                + "}";
    }
}
