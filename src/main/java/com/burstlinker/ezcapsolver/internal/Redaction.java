package com.burstlinker.ezcapsolver.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Keeps credentials out of anything a caller might print or log.
 *
 * <p>Two fields carry secrets: {@code clientKey}, and any {@code proxy} URL, which embeds
 * {@code user:password} in its authority. Both routinely end up in a {@code toString()} that
 * reaches a log aggregator, so masking is the default rather than something to remember.
 *
 * <p>Public only because the task parameter models call {@link #mask} from their own package.
 * Not API — see the package javadoc.
 */
public final class Redaction {

    /** What a masked value is replaced with, everywhere. */
    public static final String PLACEHOLDER = "[REDACTED]";

    private Redaction() {
        throw new AssertionError("no instances");
    }

    /**
     * Masks a secret while preserving whether one was set at all.
     *
     * <p>{@code null} stays {@code null} on purpose: "no proxy configured" and "a proxy whose
     * value is hidden" are different facts, and collapsing them makes a printed model useless
     * for the thing it gets printed for.
     */
    public static String mask(String secret) {
        return secret == null ? null : PLACEHOLDER;
    }

    /**
     * Returns a copy of a request body with every credential replaced, <strong>at any
     * nesting depth</strong>.
     *
     * <p>Depth matters: {@code proxy} appears both at the top level of a client's own config
     * and inside the {@code task} object of every request that uses one. Masking only the
     * keys the SDK happens to know the position of would leak the other.
     *
     * <p>The input is never modified — a logging call that mutated the body it was handed
     * would strip the credential from the request actually being sent.
     */
    public static JsonNode redact(JsonNode body) {
        if (body == null) {
            return null;
        }
        if (body.isObject()) {
            ObjectNode redacted = Json.mapper().createObjectNode();
            // properties() rather than the deprecated fields(): same pairs, and it is the one
            // that survives into Jackson 3.
            for (Map.Entry<String, JsonNode> field : body.properties()) {
                if (SECRET_KEYS.contains(field.getKey())) {
                    redacted.put(field.getKey(), PLACEHOLDER);
                } else {
                    redacted.set(field.getKey(), redact(field.getValue()));
                }
            }
            return redacted;
        }
        if (body.isArray()) {
            ArrayNode redacted = Json.mapper().createArrayNode();
            for (JsonNode element : body) {
                redacted.add(redact(element));
            }
            return redacted;
        }
        return body;
    }

    /**
     * JSON keys whose values are credentials.
     *
     * <p>{@code clientKey} is the API key. {@code proxy} embeds {@code user:password} in its
     * authority, so the whole value goes rather than just the userinfo — a proxy host is not
     * worth the risk of getting the parsing wrong.
     */
    private static final Set<String> SECRET_KEYS =
            Collections.unmodifiableSet(
                    new HashSet<String>(Arrays.asList("clientKey", "proxy")));
}
