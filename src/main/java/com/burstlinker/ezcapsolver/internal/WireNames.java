package com.burstlinker.ezcapsolver.internal;

import com.burstlinker.ezcapsolver.model.solution.Required;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The JSON keys a model declares, and the filter that keeps pass-through data from
 * colliding with them.
 *
 * <p>This exists because {@code @JsonAnyGetter} does not deduplicate. Jackson writes the
 * declared properties, then writes every entry of the any-getter map verbatim — so an
 * {@code extra} entry named after a declared field produces an object with <em>two</em> keys
 * of that name. Most parsers keep the last one, which hands the win to {@code extra}. The
 * contract is the exact opposite: a declared field always beats a pass-through entry of the
 * same name, and the losing entry is dropped silently.
 *
 * <p>The declared set comes from Jackson's own introspection rather than from hand-rolled
 * reflection over {@code @JsonProperty}. That is the point: the authority deciding which
 * names get written is the same one deciding which names to filter, so the two cannot drift.
 * Reading the annotations directly would have to re-implement naming strategies,
 * {@code @JsonIgnore}, getter/field merging and creator properties, and would be wrong in a
 * new way every time one of those was used.
 *
 * <p>Results are cached per class. Models are a fixed set, so this fills once and is
 * read-only afterwards.
 *
 * <p>Public only because {@code ExtraFields} calls {@link #withoutDeclared} from the model
 * package. Not API — see the package javadoc.
 */
public final class WireNames {

    private static final Map<Class<?>, Set<String>> CACHE = new ConcurrentHashMap<Class<?>, Set<String>>();
    private static final Map<Class<?>, Set<String>> REQUIRED_CACHE =
            new ConcurrentHashMap<Class<?>, Set<String>>();

    private WireNames() {
        throw new AssertionError("no instances");
    }

    /**
     * Every JSON key {@code type} serializes, excluding whatever its any-getter contributes.
     */
    public static Set<String> of(Class<?> type) {
        Set<String> cached = CACHE.get(type);
        if (cached != null) {
            return cached;
        }
        Set<String> computed = introspect(type);
        Set<String> existing = CACHE.putIfAbsent(type, computed);
        return existing != null ? existing : computed;
    }

    /**
     * Returns {@code extra} without the entries whose keys a declared field already owns.
     *
     * <p>The common case is an empty or non-colliding map, and both return the input
     * untouched — a copy is only made when there is actually something to drop.
     *
     * @param owner the concrete model class being serialized, not its declared type
     */
    public static Map<String, Object> withoutDeclared(Class<?> owner, Map<String, Object> extra) {
        if (extra == null || extra.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<String> declared = of(owner);
        if (declared.isEmpty() || Collections.disjoint(extra.keySet(), declared)) {
            return extra;
        }
        Map<String, Object> filtered = new LinkedHashMap<String, Object>(extra.size());
        for (Map.Entry<String, Object> entry : extra.entrySet()) {
            if (!declared.contains(entry.getKey())) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }

    /**
     * The JSON keys {@code type} marks {@link Required}, sorted so a failure message reads
     * the same way every run.
     *
     * <p>Empty for every model whose fields are all optional, which is most of them.
     */
    public static Set<String> requiredOf(Class<?> type) {
        Set<String> cached = REQUIRED_CACHE.get(type);
        if (cached != null) {
            return cached;
        }
        Set<String> computed = introspectRequired(type);
        Set<String> existing = REQUIRED_CACHE.putIfAbsent(type, computed);
        return existing != null ? existing : computed;
    }

    private static Set<String> introspectRequired(Class<?> type) {
        Set<String> names = new TreeSet<String>();
        for (BeanPropertyDefinition property : describe(type).findProperties()) {
            // The annotation sits on the field, but the name that has to be looked up in the
            // response is the property name Jackson resolved -- `generated_pass_UUID`, not
            // `generatedPassUuid`.
            AnnotatedField field = property.getField();
            if (field != null && field.getAnnotation(Required.class) != null) {
                names.add(property.getName());
            }
        }
        return Collections.unmodifiableSet(names);
    }

    private static BeanDescription describe(Class<?> type) {
        JavaType javaType = Json.mapper().getTypeFactory().constructType(type);
        return Json.mapper().getSerializationConfig().introspect(javaType);
    }

    private static Set<String> introspect(Class<?> type) {
        Set<String> names = new HashSet<String>();
        for (BeanPropertyDefinition property : describe(type).findProperties()) {
            // couldSerialize() is what separates a property Jackson will actually write from
            // one that only has a setter. A write-only property contributes no key, so
            // filtering an extra entry against it would drop data for no reason.
            if (property.couldSerialize()) {
                names.add(property.getName());
            }
        }
        return Collections.unmodifiableSet(names);
    }
}
