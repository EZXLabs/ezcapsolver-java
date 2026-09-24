package com.burstlinker.ezcapsolver.model;

/**
 * The state the service reports for a task.
 *
 * <p>The service defines exactly three. An unrecognised value is a contract break, reported
 * as {@link com.burstlinker.ezcapsolver.exception.UnexpectedResponseException
 * UnexpectedResponseException} rather than quietly treated as "still processing" —
 * that mistake would turn one broken response into a full polling budget of wasted requests
 * and then an exhausted-budget error pointing at nothing.
 */
public enum TaskStatus {

    /** Still being worked on. */
    PROCESSING("processing"),

    /** Succeeded; the solution is available. */
    READY("ready"),

    /**
     * Failed. Such a response also carries a non-zero {@code errorId}, so it surfaces as an
     * {@link com.burstlinker.ezcapsolver.exception.ApiException ApiException} rather than as a
     * result.
     */
    ERROR("error");

    private final String wire;

    TaskStatus(String wire) {
        this.wire = wire;
    }

    /** The value as it appears in JSON. */
    public String wire() {
        return wire;
    }

    /**
     * Resolves a wire value, or returns {@code null} if the service sent something outside
     * the three it defines.
     *
     * <p>Matching is case-insensitive: the status is compared, never echoed back, so being
     * strict about case would reject a usable response to no benefit.
     */
    public static TaskStatus fromWire(String value) {
        if (value == null) {
            return null;
        }
        for (TaskStatus status : values()) {
            if (status.wire.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return wire;
    }
}
