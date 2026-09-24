package com.burstlinker.ezcapsolver.model.task;

import com.burstlinker.ezcapsolver.model.solution.DataDomeSolution;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reports a fingerprint to DataDome on the normal browsing path, in exchange for a cookie.
 * Runs through the synchronous endpoint and a different worker from {@link DataDomeTaskParams}.
 *
 * <p>Field names here are flat lowercase, unlike {@link DataDomeTaskParams}'s snake_case.
 *
 * <p>Task type: {@code DataDomeTagsTaskProxyless}
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DataDomeTagsTaskParams extends BaseTaskParams<DataDomeSolution> {

    /** Challenge mode, where the packet counter is fixed at 1. */
    public static final String JS_TYPE_CH = "ch";

    /** Legacy or external mode, where the packet counter starts at 2 and increments. */
    public static final String JS_TYPE_LE = "le";

    /**
     * Lowest packet counter the service accepts.
     *
     * <p>{@code bpc} is validated as one or greater, so zero — what an uninitialised {@code long}
     * would produce — is rejected outright.
     */
    public static final long MIN_PACKET_COUNTER = 1L;

    /**
     * The DataDome JavaScript key, read from the site's inline snippet as
     * {@code window.ddjskey}. Required.
     */
    private String ddk;

    /**
     * The JavaScript mode. Required; the service accepts only {@value #JS_TYPE_CH} and
     * {@value #JS_TYPE_LE}.
     *
     * <p>Defaults to {@value #JS_TYPE_CH}, and is always sent: the service validates against a
     * closed set, so an absent value would be rejected rather than defaulted.
     */
    @JsonProperty("jstype")
    @Builder.Default
    private String jsType = JS_TYPE_CH;

    /**
     * The session identifier. Required to be present, but an empty string is valid — which is
     * why it defaults to one and is always sent.
     */
    @Builder.Default
    private String cid = "";

    /**
     * The one-based packet counter, defaulting to {@value #MIN_PACKET_COUNTER}.
     *
     * <p>{@value #JS_TYPE_CH} mode fixes it at one; {@value #JS_TYPE_LE} mode starts at two and
     * counts up, so that mode has to set it explicitly.
     */
    @Builder.Default
    private long bpc = MIN_PACKET_COUNTER;

    /** The current page URL. Required. */
    private String referer;

    /**
     * The browser User-Agent. Required. The service also accepts {@code user_agent}, but new
     * integrations should use this one.
     */
    private String ua;

    /**
     * External business fields, commonly {@code {"tags_url": "..."}}.
     *
     * <p>Required to be present but allowed to be empty, so it defaults to an empty object and
     * is always sent. A JSON {@code null} is rejected, and that is what an omitted field would
     * become on the far side.
     */
    @Builder.Default
    private Map<String, Object> fields = new LinkedHashMap<String, Object>();

    @Override
    public Class<DataDomeSolution> solutionType() {
        return DataDomeSolution.class;
    }
}
