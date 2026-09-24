package com.burstlinker.ezcapsolver.model.task;

import com.burstlinker.ezcapsolver.model.solution.DataDomeSolution;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Answers a DataDome challenge after an interception. Runs through the synchronous endpoint.
 *
 * <p>Every field name here is snake_case, unlike {@link DataDomeTagsTaskParams}, whose are flat
 * lowercase. The two go to different workers.
 *
 * <p>Task type: {@code DataDomeTaskProxyless}
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DataDomeTaskParams extends BaseTaskParams<DataDomeSolution> {

    /** Fetches the challenge URL. A string on the wire, not a number. */
    public static final String STEP_ONE = "1";

    /** Produces the validation instructions. */
    public static final String STEP_TWO = "2";

    /** The base64-encoded challenge HTML. Required. */
    @JsonProperty("html_b64")
    private String htmlB64;

    /**
     * The step of the workflow. Required; the service accepts only {@value #STEP_ONE} and
     * {@value #STEP_TWO}.
     *
     * <p>Defaults to {@value #STEP_ONE}, and is always sent: the service validates against a
     * closed set, so an absent value would be rejected rather than defaulted.
     */
    @Builder.Default
    private String step = STEP_ONE;

    /**
     * An optional base64-encoded image, defaulting to an empty string.
     *
     * <p>Sent even when empty, so that step one and step two are the same shape on the wire.
     */
    @Builder.Default
    private String image = "";

    /**
     * The page or challenge URL.
     *
     * <p>The service declares it optional, but the real workflow needs it, and it is what site
     * allow-listing is checked against: with an allow-list configured, a referer outside it is
     * rejected with {@code ERROR_WEBSITE_NOT_ALLOWED}.
     */
    private String referer;

    /** The optional parent page URL. */
    @JsonProperty("parent_url")
    private String parentUrl;

    /** The optional equipment identifier. */
    private String equipment;

    @Override
    public Class<DataDomeSolution> solutionType() {
        return DataDomeSolution.class;
    }
}
