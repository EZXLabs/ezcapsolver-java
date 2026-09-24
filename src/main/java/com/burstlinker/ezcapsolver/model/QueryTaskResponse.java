package com.burstlinker.ezcapsolver.model;

import com.burstlinker.ezcapsolver.exception.UnexpectedResponseException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * What {@code /getTaskResult} and {@code /createSyncTask} return.
 *
 * <p>The solution is kept as untouched JSON. Decoding it into a model is a separate step, so
 * a model that does not fit loses nothing: the raw value is still here, and the task was
 * already paid for.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class QueryTaskResponse extends ResponseMeta {

    /**
     * The state as the service spelled it, unvalidated.
     *
     * <p>Kept as the raw string rather than an enum so an unrecognised value survives into
     * the error message instead of becoming a parse failure with no detail.
     * {@link #status()} is the validated view.
     */
    private String status;

    /**
     * The identifier the synchronous endpoint assigns.
     *
     * <p>{@code /createSyncTask} returns one on both the success and the failure path.
     * {@code /getTaskResult} does not echo it back, so it is {@code null} there.
     */
    private String taskId;

    /**
     * The solution exactly as the worker produced it.
     *
     * <p>{@code null} when the response carried no {@code solution} field at all, which means
     * the task has not finished. That is <strong>different</strong> from a JSON {@code null},
     * which means the worker finished and returned nothing. Use {@link #hasSolution()} rather
     * than a null check, which cannot tell the two apart.
     */
    private JsonNode solution;

    /**
     * Whether the response carried a {@code solution} field at all, a JSON {@code null}
     * included.
     */
    @JsonIgnore
    public boolean hasSolution() {
        return solution != null;
    }

    /**
     * The state as one of the three values the service defines.
     *
     * @throws UnexpectedResponseException if the service sent anything else
     */
    @JsonIgnore
    public TaskStatus status() {
        TaskStatus resolved = TaskStatus.fromWire(status);
        if (resolved == null) {
            throw new UnexpectedResponseException(
                    "unknown task status '" + status + "'", null);
        }
        return resolved;
    }

    /** Whether the task is still being worked on. */
    @JsonIgnore
    public boolean isProcessing() {
        return TaskStatus.PROCESSING == TaskStatus.fromWire(status);
    }

    /** Whether the task completed successfully. */
    @JsonIgnore
    public boolean isReady() {
        return TaskStatus.READY == TaskStatus.fromWire(status);
    }

    /** Whether the task failed. */
    @JsonIgnore
    public boolean isError() {
        return TaskStatus.ERROR == TaskStatus.fromWire(status);
    }
}
