package com.burstlinker.ezcapsolver;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.burstlinker.ezcapsolver.internal.Json;
import com.burstlinker.ezcapsolver.internal.Redaction;
import com.burstlinker.ezcapsolver.internal.WireNames;
import com.burstlinker.ezcapsolver.model.task.AkamaiWebTaskParams;
import com.burstlinker.ezcapsolver.model.task.DataDomeTagsTaskParams;
import com.burstlinker.ezcapsolver.model.task.DataDomeTaskParams;
import com.burstlinker.ezcapsolver.model.task.ReCaptchaV2ClassificationTaskParams;
import com.burstlinker.ezcapsolver.model.task.ReCaptchaV2TaskParams;
import com.burstlinker.ezcapsolver.model.task.ReCaptchaV3TaskParams;
import com.burstlinker.ezcapsolver.model.task.TaskParams;
import com.burstlinker.ezcapsolver.model.task.TaskType;
import com.burstlinker.ezcapsolver.model.task.TlsForwardTaskParams;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * What each task parameter model actually puts on the wire.
 *
 * <p>Three kinds of field need watching, and all three look identical in the source:
 *
 * <ul>
 *   <li>fields the service validates against a <strong>closed set</strong>, where the Java zero
 *       value is not a member and would be rejected rather than defaulted;
 *   <li>fields where <strong>absent and empty mean different things</strong> to the service;
 *   <li>booleans where <strong>{@code false} is an answer</strong> rather than an absence.
 * </ul>
 *
 * <p>No test here reaches the real API. Creating a task is billed.
 */
class TaskParamsWireTest {

    private static final ObjectMapper STRICT =
            JsonMapper.builder(
                            JsonFactory.builder()
                                    .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
                                    .build())
                    .build();


    /**
     * The wire contract, one entry per model, written out by hand.
     *
     * <p>Sorted key sets, exactly as they go out. See {@code assertPinnedKeys} for the two
     * mistakes this is here to fail on.
     */
    private static final Map<String, Set<String>> PINNED_WIRE_KEYS =
            new TreeMap<String, Set<String>>();

    private static void pin(String model, String... keys) {
        PINNED_WIRE_KEYS.put(model, new TreeSet<String>(Arrays.asList(keys)));
    }

    static {
        pin("ReCaptchaV2TaskParams", "isInvisible", "proxy", "s", "sa", "websiteKey", "websiteTitle", "websiteURL");
        pin("ReCaptchaSolution", "gRecaptchaResponse", "sec_ch_ua", "user_agent");
        pin("ReCaptchaV2ClassificationTaskParams", "image", "question", "size");
        pin("ReClassificationSolution", "hasObject", "objects", "type");
        pin("ReCaptchaV3TaskParams", "checkField", "isInvisible", "pageAction", "proxy", "websiteKey", "websiteTitle", "websiteURL");
        pin("FunCaptchaTaskParams", "cn", "data", "funcaptchaApiJSSubdomain", "proxy", "websiteKey", "websiteURL");
        pin("FunCaptchaSolution", "token");
        pin("FunCaptchaClassificationTaskParams", "image", "question");
        pin("FunCaptchaClassificationSolution");
        pin("PerimeterXTaskParams", "invisible", "websiteKey");
        pin("PerimeterXSolution", "_px3", "_pxde", "_pxvid");
        pin("HCaptchaTaskParams", "invisible", "lang", "proxy", "rqdata", "websiteKey", "websiteURL");
        pin("HCaptchaSolution", "generated_pass_UUID", "lang", "ua");
        pin("HCaptchaClassificationTaskParams", "anchors", "image", "images", "module", "question");
        pin("HCaptchaClassificationSolution");
        pin("AkamaiWebTaskParams", "abck", "bmsz", "encodeData", "index", "lang", "pageUrl", "script_base64", "ua", "v3Url");
        pin("AkamaiWebSolution", "encodedata", "payload");
        pin("AkamaiSbsdTaskParams", "bmSo", "lang", "pageUrl", "sbsdUrl", "script_base64", "ua");
        pin("AkamaiSbsdSolution", "bm_lso_time", "payload");
        pin("TlsForwardTaskParams", "body", "body_raw", "cookies", "headers", "headers_order", "method", "proxy", "tls_type", "url");
        pin("TlsForwardSolution", "body", "code", "cookies", "headers", "status");
        pin("CloudFlare5sTaskParams", "proxy", "rqData", "websiteURL");
        pin("CloudFlare5sSolution", "body", "cookies", "header", "sToken", "tlsVersion");
        pin("CloudFlareTurnstileTaskParams", "proxy", "rqData", "websiteKey", "websiteURL");
        pin("CloudFlareTurnstileSolution", "header", "token");
        pin("DataDomeTaskParams", "equipment", "html_b64", "image", "parent_url", "referer", "step");
        pin("DataDomeSolution", "body", "kind", "url");
        pin("DataDomeTagsTaskParams", "bpc", "cid", "ddk", "fields", "jstype", "referer", "ua");
        pin("IncapsulaTaskParams", "acceptLanguage", "pageUrl", "pow", "proxy", "script", "scriptUrl", "ua");
        pin("IncapsulaSolution", "data", "status");
    }

    private static JsonNode tree(Object value) throws IOException {
        return Json.mapper().readTree(Json.mapper().writeValueAsString(value));
    }

    @Nested
    @DisplayName("field defaults")
    class Defaults {

        @Test
        @DisplayName("DataDome sends step 1 and keeps the image key present")
        void dataDomeDefaults() throws IOException {
            JsonNode json = tree(new DataDomeTaskParams());

            // The service accepts only "1" and "2", so an unset field is rejected outright
            // rather than defaulted on its side.
            assertEquals(DataDomeTaskParams.STEP_ONE, json.get("step").asText());
            assertTrue(json.has("image"), "the image key stays present even with no image");
            assertEquals("", json.get("image").asText());
        }

        @Test
        @DisplayName("DataDome tags sends ch, bpc 1, an empty cid and an empty fields object")
        void dataDomeTagsDefaults() throws IOException {
            JsonNode json = tree(new DataDomeTagsTaskParams());

            assertEquals(DataDomeTagsTaskParams.JS_TYPE_CH, json.get("jstype").asText());
            assertEquals(1L, json.get("bpc").asLong(), "the counter is one-based");
            assertTrue(json.has("cid"));
            assertEquals("", json.get("cid").asText());

            // A JSON null is rejected here, and an omitted key becomes exactly that on the far
            // side. An empty object is what the service accepts.
            assertTrue(json.get("fields").isObject(), "fields must be an object, not null");
            assertEquals(0, json.get("fields").size());
        }

        @Test
        @DisplayName("TLS forwarding defaults to GET")
        void tlsDefaults() throws IOException {
            JsonNode json = tree(new TlsForwardTaskParams());

            assertEquals(TlsForwardTaskParams.METHOD_GET, json.get("method").asText());
            assertTrue(json.has("body_raw"), "the worker must not be left guessing the encoding");
            assertFalse(json.get("body_raw").asBoolean());
        }

        @Test
        @DisplayName("Akamai keeps every round-state key present, empty on the first round")
        void akamaiRoundStateKeysArePresent() throws IOException {
            JsonNode json = tree(new AkamaiWebTaskParams());

            // Round one legitimately has no encodeData. Dropping the key would make round one
            // a different shape from every later round.
            for (String key : new String[] {"abck", "bmsz", "script_base64", "encodeData"}) {
                assertTrue(json.has(key), key + " must be present on every round");
                assertEquals("", json.get(key).asText(), key);
            }
            assertEquals(0, json.get("index").asInt(), "the flow starts at round zero");
        }

        @Test
        @DisplayName("an explicit value is never overridden by its default")
        void explicitValuesWin() throws IOException {
            DataDomeTaskParams step = new DataDomeTaskParams();
            step.setStep(DataDomeTaskParams.STEP_TWO);
            assertEquals("2", tree(step).get("step").asText());

            DataDomeTagsTaskParams tags = new DataDomeTagsTaskParams();
            tags.setJsType(DataDomeTagsTaskParams.JS_TYPE_LE);
            tags.setBpc(7);
            tags.setFields(Collections.<String, Object>singletonMap("tags_url", "https://x"));
            JsonNode json = tree(tags);
            assertEquals("le", json.get("jstype").asText());
            assertEquals(7L, json.get("bpc").asLong());
            assertEquals("https://x", json.get("fields").get("tags_url").asText());

            TlsForwardTaskParams tls = new TlsForwardTaskParams();
            tls.setMethod(TlsForwardTaskParams.METHOD_POST);
            assertEquals("POST", tree(tls).get("method").asText());
        }
    }

    @Nested
    @DisplayName("present, absent and false")
    class Presence {

        @Test
        @DisplayName("V2 always states isInvisible; V3 omits it so its own default applies")
        void invisibleDiffersBetweenV2AndV3() throws IOException {
            // V2 defaults to false service-side, which is the Java zero value, so stating it
            // costs nothing and removes a guess.
            JsonNode v2 = tree(new ReCaptchaV2TaskParams());
            assertTrue(v2.has("isInvisible"));
            assertFalse(v2.get("isInvisible").asBoolean());

            // V3 defaults to *true*. A primitive would send false for an unset field and
            // silently flip that default, so the field is boxed and an unset one is omitted.
            assertFalse(
                    tree(new ReCaptchaV3TaskParams()).has("isInvisible"),
                    "an unset V3 isInvisible must not reach the wire");

            ReCaptchaV3TaskParams optOut = new ReCaptchaV3TaskParams();
            optOut.setInvisible(false);
            JsonNode v3 = tree(optOut);
            assertTrue(v3.has("isInvisible"), "an explicit false is the one value worth sending");
            assertFalse(v3.get("isInvisible").asBoolean());
        }

        @Test
        @DisplayName("a classification size is omitted so the service applies its default of 4")
        void classificationSizeIsOmitted() throws IOException {
            assertFalse(tree(new ReCaptchaV2ClassificationTaskParams()).has("size"));

            ReCaptchaV2ClassificationTaskParams sized = new ReCaptchaV2ClassificationTaskParams();
            sized.setSize(3);
            assertEquals(3, tree(sized).get("size").asInt());
        }

        @Test
        @DisplayName("an unset optional field is omitted rather than sent as null")
        void unsetOptionalsAreOmitted() throws IOException {
            JsonNode json = tree(new ReCaptchaV2TaskParams());

            // Sending null would override the service's own default with nothing.
            for (String key : new String[] {"sa", "s", "websiteTitle", "proxy", "websiteURL"}) {
                assertFalse(json.has(key), key + " should not be sent when unset");
            }
        }
    }

    @Nested
    @DisplayName("rules that hold for every model")
    class EveryModel {

        @Test
        @DisplayName("no model emits a duplicate key, and none carries a type field")
        void wireShapeIsSane() throws Exception {
            for (String taskType : TaskType.KNOWN) {
                Object params = instantiate(taskType);
                String text = Json.mapper().writeValueAsString(params);

                assertDoesNotThrow(
                        () -> STRICT.readTree(text),
                        taskType + " emitted a duplicate key: " + text);

                JsonNode json = Json.mapper().readTree(text);
                assertTrue(json.isObject(), taskType + " must serialize to an object");
                // One model backs several types, so the type is injected by the request
                // envelope. A field here would let a caller contradict the method they called.
                assertFalse(json.has("type"), taskType + " must not declare a type field");
            }
        }

        @Test
        @DisplayName("pass-through entries flatten, and a declared field still wins")
        void extraBehavesOnEveryModel() throws Exception {
            for (String taskType : TaskType.KNOWN) {
                TaskParams<?> params = instantiate(taskType);
                ((com.burstlinker.ezcapsolver.model.ExtraFields) params).put("aBrandNewKey", 1);

                JsonNode json = tree(params);
                assertEquals(1, json.get("aBrandNewKey").asInt(), taskType);
                assertFalse(json.has("extra"), taskType + ": extra must not nest");
            }
        }

        @Test
        @DisplayName("every model serializes exactly the keys pinned below, and no others")
        void wireKeysArePinned() throws Exception {
            Set<String> seen = new TreeSet<String>();
            for (String taskType : TaskType.KNOWN) {
                Method method = solveMethod(taskType);
                seen.add(assertPinnedKeys(taskType, method.getParameterTypes()[0]));

                // Solution models are exposed to the same hazard, and a phantom property there
                // means a field silently failing to decode rather than an extra key.
                Class<?> solution =
                        (Class<?>)
                                ((java.lang.reflect.ParameterizedType) method.getGenericReturnType())
                                        .getActualTypeArguments()[0];
                seen.add(assertPinnedKeys(taskType, solution));
            }
            assertEquals(
                    PINNED_WIRE_KEYS.keySet(),
                    seen,
                    "the pinned table and the reachable models have drifted apart");
        }

        /**
         * Compares a model's whole serialized key set against the pinned contract.
         *
         * <p>This is deliberately a hand-written table rather than something derived from the
         * models, because a table derived from the models cannot disagree with them. Two
         * different mistakes have to fail here:
         *
         * <p><strong>A phantom property.</strong> Lombok can invent a property nobody declared.
         * A field named {@code isInvisible} gets the getter {@code isInvisible()} — Lombok does
         * not prefix a name that already starts with {@code is} — and Jackson reads that as a
         * <em>second</em> property called {@code invisible}, so the request carries both keys.
         * It is not a duplicate key, so strict parsing sees nothing wrong, and asserting that
         * the expected key is present passes too. Only the whole set catches it.
         *
         * <p><strong>A silently renamed key.</strong> Most fields need no {@code @JsonProperty}
         * because Jackson derives the right name from the field. The ones that do need it are
         * load-bearing, and dropping one is invisible at compile time: {@code websiteUrl}
         * quietly starts going out as {@code websiteUrl} instead of {@code websiteURL}, and the
         * service rejects a task that was already billed.
         *
         * @return the model's simple name, so the caller can check the table for stale entries
         */
        private String assertPinnedKeys(String taskType, Class<?> model) {
            String name = model.getSimpleName();
            Set<String> pinned = PINNED_WIRE_KEYS.get(name);
            assertNotNull(pinned, taskType + ": " + name + " is not in the pinned table");
            assertEquals(
                    pinned,
                    new TreeSet<String>(WireNames.of(model)),
                    taskType + " (" + name + ") no longer serializes the keys it is pinned to");
            return name;
        }

        private Method solveMethod(String taskType) {
            for (Method method : EzCapSolverClient.class.getMethods()) {
                if (method.getName().equals("solve" + taskType) && method.getParameterCount() == 1) {
                    return method;
                }
            }
            throw new AssertionError("no convenience method for " + taskType);
        }

        @Test
        @DisplayName("every model with a proxy masks it in toString")
        void proxyIsNeverPrinted() throws Exception {
            Set<String> checked = new TreeSet<String>();
            for (String taskType : TaskType.KNOWN) {
                TaskParams<?> params = instantiate(taskType);
                Method setter = proxySetter(params.getClass());
                if (setter == null) {
                    continue;
                }
                setter.invoke(params, "http://user:hunter2@127.0.0.1:8080");
                String text = params.toString();

                assertFalse(text.contains("hunter2"), taskType + ": proxy password leaked");
                assertTrue(text.contains(Redaction.PLACEHOLDER), taskType + ": proxy not shown at all");
                checked.add(params.getClass().getSimpleName());
            }
            // Counted by model rather than by task type, since several types share one model.
            // Without this, a proxy field that quietly stopped being masked would drop out of
            // the loop instead of failing it.
            assertEquals(8, checked.size(), "the models carrying a proxy changed: " + checked);
        }

        private TaskParams<?> instantiate(String taskType) throws Exception {
            for (Method method : EzCapSolverClient.class.getMethods()) {
                if (method.getName().equals("solve" + taskType) && method.getParameterCount() == 1) {
                    return (TaskParams<?>)
                            method.getParameterTypes()[0].getDeclaredConstructor().newInstance();
                }
            }
            throw new AssertionError("no convenience method for " + taskType);
        }

        private Method proxySetter(Class<?> type) {
            for (Field field : type.getDeclaredFields()) {
                if (!field.getName().equals("proxy")) {
                    continue;
                }
                try {
                    return type.getMethod("setProxy", String.class);
                } catch (NoSuchMethodException e) {
                    throw new AssertionError(type.getSimpleName() + " has a proxy field but no setter");
                }
            }
            return null;
        }
    }
}
