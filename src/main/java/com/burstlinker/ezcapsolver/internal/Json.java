package com.burstlinker.ezcapsolver.internal;

import com.burstlinker.ezcapsolver.exception.SolutionDecodeException;
import com.burstlinker.ezcapsolver.model.solution.Required;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.Locale;

/**
 * The single {@link ObjectMapper} every request and response goes through.
 *
 * <p>One shared instance rather than one per client: an {@code ObjectMapper} is thread-safe
 * once configured, and its introspection cache is the reason {@link WireNames} is cheap.
 * Handing each client its own would multiply that cache for no benefit.
 *
 * <p>Public only because {@code EzCapSolverClient} and {@code Transport} sit in the API package
 * and cannot reach a package-private type from here. Not API — see the package javadoc.
 */
public final class Json {

    private static final ObjectMapper MAPPER = newMapper();

    private Json() {
        throw new AssertionError("no instances");
    }

    /** The configured mapper. Safe to use from any thread. */
    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /**
     * Turns a solution payload into its model, enforcing the {@link Required} fields.
     *
     * <p>The check runs against the incoming JSON rather than the decoded object, because a
     * missing key and an explicit {@code null} both leave a {@code null} field behind and
     * only the former is a contract break. Jackson cannot express this itself:
     * {@code @JsonProperty(required = true)} is ignored for setter-based properties.
     *
     * @param raw the solution object exactly as it arrived
     * @param type the model to build
     * @param taskId the billed task, carried into the failure so the result stays recoverable
     * @throws SolutionDecodeException if a required key is absent or the shape does not fit
     */
    public static <S> S decodeSolution(JsonNode raw, Class<S> type, String taskId) {
        if (raw == null || !raw.isObject()) {
            throw new SolutionDecodeException(
                    "solution is not a JSON object, got " + describe(raw), raw, taskId, null);
        }
        for (String required : WireNames.requiredOf(type)) {
            if (!raw.has(required)) {
                throw new SolutionDecodeException(
                        "solution is missing required field '"
                                + required
                                + "' for "
                                + type.getSimpleName()
                                + "; the worker may have renamed it",
                        raw,
                        taskId,
                        null);
            }
        }
        try {
            return MAPPER.treeToValue(raw, type);
        } catch (JsonProcessingException e) {
            throw new SolutionDecodeException(
                    "solution does not fit " + type.getSimpleName() + ": " + e.getOriginalMessage(),
                    raw,
                    taskId,
                    e);
        }
    }

    private static String describe(JsonNode node) {
        return node == null ? "nothing" : node.getNodeType().toString().toLowerCase(Locale.ROOT);
    }

    private static ObjectMapper newMapper() {
        // The builder rather than setters on a constructed mapper: the setters are
        // deprecated in 2.x because they mutate an instance that is only thread-safe once
        // configured, and they are gone in 3.x. Building it this way is also the shape that
        // ports straight across if the Java floor ever rises far enough to allow Jackson 3.
        return JsonMapper.builder()

                // A null field is an unset field, and an unset field must not reach the
                // wire: the service applies its own default for an absent key, which is
                // exactly what a caller who left a wrapper null is asking for. Sending
                // `"pageAction": null` instead would override that default with nothing.
                //
                // The JsonInclude.Value form rather than the Include shortcut: the shortcut
                // is deprecated in 2.21 and gone in 3.x. USE_DEFAULTS on the content side
                // leaves map values and collection elements alone, so a caller who puts an
                // explicit null in `extra` still gets it sent.
                .defaultPropertyInclusion(
                        JsonInclude.Value.construct(
                                JsonInclude.Include.NON_NULL, JsonInclude.Include.USE_DEFAULTS))

                // Workers ship faster than this SDK, so an unmodelled key is the normal
                // case, not a fault. Solution models route those into `extra`; the envelope
                // types ignore them. Failing here would turn every service-side addition
                // into a broken release.
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

                // Duplicate-key detection is deliberately NOT enabled for reading. A task
                // result arrives already billed and cannot be fetched again once its five
                // minutes are up, so refusing to parse one over a malformed key would throw
                // away something the caller paid for. Tests enable
                // JsonParser.Feature.STRICT_DUPLICATE_DETECTION on their own parser to prove
                // this SDK never *emits* a duplicate, which is the half of the problem
                // actually under our control.

                // Task parameter models are plain data. A model with nothing to serialize is
                // a bug worth seeing rather than an empty object worth shipping.
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .build();
    }
}
