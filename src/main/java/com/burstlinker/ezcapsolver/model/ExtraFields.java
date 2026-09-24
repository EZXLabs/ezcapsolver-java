package com.burstlinker.ezcapsolver.model;

import com.burstlinker.ezcapsolver.internal.WireNames;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * A model that keeps the JSON keys this release does not declare, in both directions.
 *
 * <p>Shared by task parameters and solutions because the rule is the same on the way out and
 * on the way in, and it is subtle enough that two copies would drift. Outbound, entries are
 * written <strong>next to</strong> the declared fields rather than nested under a key of
 * their own; inbound, a key no declared field claims is collected here instead of dropped.
 *
 * <pre>{@code
 * params.put("someNewParameter", 42);     // a request field this release does not model
 * Object value = solution.get("someNewField");  // a response field it does not model either
 * }</pre>
 *
 * <p>This behaves like a map but deliberately <strong>is not</strong> one. Implementing
 * {@link Map} would make Jackson serialize the whole model as a map and drop every declared
 * field, and it would leave {@code size()} and {@code entrySet()} with no honest answer —
 * there is no sensible way to say whether they cover the declared fields as well.
 *
 * <p><strong>A declared field always beats a pass-through entry of the same name</strong>,
 * and the losing entry is dropped from the request without a diagnostic. That is not
 * Jackson's default: {@code @JsonAnyGetter} appends its map verbatim and would emit the key
 * twice, letting the pass-through entry win on most parsers. {@link WireNames} is what
 * inverts it. The rule has a precedent rather than a hypothesis behind it — the Rust SDK's
 * Akamai example once shipped an <strong>empty {@code v3Url}</strong> for exactly this
 * reason.
 *
 * <p>Inbound needs no such filter: Jackson only reaches {@code @JsonAnySetter} after failing
 * to match a declared property, so a response key cannot shadow a declared field.
 */
@SuperBuilder
@EqualsAndHashCode
@ToString
public abstract class ExtraFields {

    /**
     * Pass-through entries, in insertion order.
     *
     * <p>Final with an initializer, which is what keeps it out of the generated builder: it
     * is always a fresh mutable map, and {@link #put} is the only way in.
     */
    private final Map<String, Object> extra = new LinkedHashMap<String, Object>();

    /**
     * {@code @SuperBuilder} sits on this class only so the task models further down can use
     * one — Lombok requires every class in a builder chain to carry it, and stops looking
     * only at {@code Object}. It contributes no fields of its own.
     */
    protected ExtraFields() {}

    /**
     * Records a key this release does not declare.
     *
     * <p>On a task this adds a request parameter; on a solution it is what Jackson calls for
     * every response key the model does not claim. Anything the shared mapper can write is
     * accepted as a value. A key matching a declared field is kept here but silently ignored
     * at serialization time — the collision is a property of the concrete model, and
     * refusing it here would make the escape hatch fail on a field the caller cannot see.
     *
     * <p>Returns nothing rather than {@code this}. It could only ever return
     * {@code ExtraFields}, never the concrete model, so {@code solveHCaptcha(params.put(…))}
     * would not compile and would read as a defect in this SDK. Assign first, then call.
     */
    @JsonAnySetter
    public void put(String key, Object value) {
        extra.put(key, value);
    }

    /**
     * Reads one pass-through entry, or {@code null} if there is none under that key.
     *
     * <p>Declared fields are never reachable through this — they have their own getters. A
     * {@code null} therefore means "no such key", never "the SDK does not model it".
     */
    public Object get(String key) {
        return extra.get(key);
    }

    /**
     * Every pass-through entry, including any that will lose to a declared field.
     *
     * <p>For iterating or inspecting the lot; {@link #get} reaches a single one. The view is
     * unmodifiable: handing out the live map would let a caller mutate a task while it is
     * being serialized.
     *
     * <p>{@code @JsonIgnore} keeps it from also being written as a nested object named
     * {@code extra}; {@link #serializedExtra()} is what Jackson actually reads.
     */
    @JsonIgnore
    public Map<String, Object> getExtra() {
        return Collections.unmodifiableMap(extra);
    }

    /** The entries that survive the collision rule, flattened by Jackson. */
    @JsonAnyGetter
    Map<String, Object> serializedExtra() {
        return WireNames.withoutDeclared(getClass(), extra);
    }
}
