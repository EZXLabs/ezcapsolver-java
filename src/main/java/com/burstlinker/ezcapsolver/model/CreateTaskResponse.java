package com.burstlinker.ezcapsolver.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Create asynchronous task response
 */
@Getter
@Setter
@ToString(callSuper = true)
public class CreateTaskResponse extends ResponseMeta {

    /**
     * Task ID
     */
    private String taskId;
}
