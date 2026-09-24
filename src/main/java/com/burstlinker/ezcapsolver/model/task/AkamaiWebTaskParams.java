package com.burstlinker.ezcapsolver.model.task;

import com.burstlinker.ezcapsolver.model.solution.AkamaiWebSolution;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Produces one round of Akamai Web sensor data. Runs through the synchronous endpoint.
 *
 * <p><strong>This is a multi-round flow.</strong> Feed each round's returned
 * {@code encodedata} into the next round's {@link #setEncodeData(String)} and raise
 * {@link #setIndex(int)}. Note the casing difference between the two — the service spells it
 * differently in each direction.
 *
 * <p>Task type: {@code AkamaiWEBTaskProxyless}
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AkamaiWebTaskParams extends BaseTaskParams<AkamaiWebSolution> {

    /** URL of the page the Akamai script belongs to. Required. */
    private String pageUrl;

    /**
     * URL of the Akamai v3 script. Required.
     *
     * <p>Most sites change this URL on every request, so it has to be read from the page rather
     * than hard-coded. It is <strong>the URL itself</strong>, not the script the URL serves.
     */
    private String v3Url;

    /** The browser User-Agent. Required. */
    private String ua;

    /** The browser language. Required. */
    private String lang;

    /** The current round of the flow, starting at zero. Required, and always sent. */
    private int index;

    /**
     * The current {@code _abck} cookie value.
     *
     * <p>One of four round-state fields that default to an empty string and are always sent. The
     * worker reads them on every round, including the first, where the caller genuinely has
     * nothing to supply — an empty string says that; an absent key does not.
     */
    @Builder.Default
    private String abck = "";

    /** The current {@code bm_sz} cookie value. Round state; see {@link #getAbck()}. */
    @Builder.Default
    private String bmsz = "";

    /** The base64-encoded Akamai script. Round state; see {@link #getAbck()}. */
    @JsonProperty("script_base64")
    @Builder.Default
    private String scriptBase64 = "";

    /**
     * The encoded state the previous round returned. Empty on the first round; see
     * {@link #getAbck()}.
     */
    @Builder.Default
    private String encodeData = "";

    @Override
    public Class<AkamaiWebSolution> solutionType() {
        return AkamaiWebSolution.class;
    }
}
