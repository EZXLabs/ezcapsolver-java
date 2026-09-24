package com.burstlinker.ezcapsolver;

import com.burstlinker.ezcapsolver.exception.SolutionDecodeException;
import com.burstlinker.ezcapsolver.internal.Json;
import com.burstlinker.ezcapsolver.internal.WireNames;
import com.burstlinker.ezcapsolver.model.solution.HCaptchaSolution;
import com.burstlinker.ezcapsolver.model.task.HCaptchaTaskParams;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The rules that decide what actually reaches the wire, and what survives coming back.
 *
 * <p>These guard the two places where Jackson's defaults are wrong for this SDK: an
 * any-getter that would let pass-through data shadow a declared field, and a
 * {@code required} annotation databind silently ignores.
 *
 * <p>No test here reaches the real API. Creating a task is billed.
 */
class WireContractTest {

    /**
     * A parser that refuses a document containing the same key twice.
     *
     * <p>{@code readTree} on the normal mapper keeps the last of a duplicate pair and says
     * nothing, which is exactly the failure being tested for — so asserting on a parsed tree
     * would pass no matter what. This is the only way to see the emitted text as strictly as
     * a suspicious consumer would.
     */
    private static final ObjectMapper STRICT =
            JsonMapper.builder(
                            JsonFactory.builder()
                                    .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
                                    .build())
                    .build();

    /**
     * HCaptchaTaskParams's wire keys, written out by hand.
     *
     * <p>Deliberately not derived from {@link WireNames}: that is the code under test, and a
     * set computed from it would agree with itself while both were wrong. This list comes
     * from the task catalog.
     */
    private static final Set<String> HCAPTCHA_TASK_KEYS =
            Collections.unmodifiableSet(
                    new HashSet<String>(
                            Arrays.asList(
                                    "websiteURL", "websiteKey", "lang", "invisible", "rqdata", "proxy")));

    private static HCaptchaTaskParams params() {
        return HCaptchaTaskParams.builder()
                .websiteUrl("https://example.com")
                .websiteKey("site-key")
                .lang("en-US")
                .build();
    }

    private static JsonNode tree(Object value) throws IOException {
        return Json.mapper().readTree(Json.mapper().writeValueAsString(value));
    }

    @Nested
    @DisplayName("outbound: task parameters")
    class Outbound {

        @Test
        @DisplayName("the declared key set is exactly what the catalog documents")
        void declaredKeysMatchTheCatalog() {
            assertEquals(HCAPTCHA_TASK_KEYS, WireNames.of(HCaptchaTaskParams.class));
        }

        @Test
        @DisplayName("pass-through entries are flattened beside the declared fields")
        void extraIsFlattenedNotNested() throws IOException {
            HCaptchaTaskParams params = params();
            params.put("someNewParameter", 42);

            JsonNode json = tree(params);
            assertEquals(42, json.get("someNewParameter").asInt());
            // Nested under a key of its own it would be invisible to the service.
            assertFalse(json.has("extra"), "extra must not appear as a nested object");
        }

        @Test
        @DisplayName("a declared field beats a pass-through entry of the same name")
        void declaredFieldWinsTheCollision() throws IOException {
            HCaptchaTaskParams params = params();
            params.put("websiteURL", "https://shadowed.example");

            JsonNode json = tree(params);
            assertEquals("https://example.com", json.get("websiteURL").asText());
        }

        @Test
        @DisplayName("no duplicate key is emitted, even with every declared name shadowed")
        void noDuplicateKeyIsEverEmitted() throws IOException {
            HCaptchaTaskParams params = params();
            params.setRqData("rq");
            params.setProxy("http://user:pass@127.0.0.1:8080");
            for (String key : HCAPTCHA_TASK_KEYS) {
                params.put(key, "shadow");
            }

            String text = Json.mapper().writeValueAsString(params);

            // Jackson's any-getter appends its map without deduplicating, so without the
            // filter this document carries all six keys twice and still parses fine on a
            // lenient reader -- with `shadow` winning, because most parsers keep the last.
            assertDoesNotThrow(() -> STRICT.readTree(text), "emitted a duplicate key: " + text);

            JsonNode json = Json.mapper().readTree(text);
            for (String key : HCAPTCHA_TASK_KEYS) {
                assertFalse(
                        "shadow".equals(json.get(key).asText()),
                        key + " was overwritten by a pass-through entry");
            }
        }

        @Test
        @DisplayName("get reads back an entry that lost the collision")
        void callerViewKeepsTheLosingEntry() {
            HCaptchaTaskParams params = params();
            params.put("websiteURL", "https://shadowed.example");

            // The caller's view reports what they put in; silently emptying it would make a
            // collision impossible to diagnose from the outside.
            assertEquals("https://shadowed.example", params.get("websiteURL"));
            assertEquals("https://shadowed.example", params.getExtra().get("websiteURL"));
        }

        @Test
        @DisplayName("get returns null for a key that was never put")
        void getIsNullForAnAbsentKey() {
            HCaptchaTaskParams params = params();

            assertNull(params.get("neverSet"));
            // A declared field is not reachable through the pass-through map -- it has its
            // own getter, and conflating the two would make a null here ambiguous.
            assertNull(params.get("websiteURL"));
            assertEquals("https://example.com", params.getWebsiteUrl());
        }

        @Test
        @DisplayName("the pass-through map stays read-only from outside")
        void getExtraCannotBeMutated() {
            HCaptchaTaskParams params = params();
            params.put("a", 1);

            // Handing out the live map would let a caller bypass put() and, more to the
            // point, mutate a task while it is being serialized.
            assertThrows(
                    UnsupportedOperationException.class, () -> params.getExtra().put("b", 2));
        }

        @Test
        @DisplayName("an unset optional field stays off the wire entirely")
        void unsetOptionalsAreOmitted() throws IOException {
            JsonNode json = tree(params());

            // Sending `"rqdata": null` would override the service's own default with nothing.
            assertFalse(json.has("rqdata"));
            assertFalse(json.has("proxy"));
        }

        @Test
        @DisplayName("invisible=false is sent rather than omitted")
        void falseIsAnAnswerNotAnAbsence() throws IOException {
            JsonNode json = tree(params());

            assertTrue(json.has("invisible"), "a primitive boolean must always be sent");
            assertFalse(json.get("invisible").asBoolean());
        }

        @Test
        @DisplayName("the task type is not a field on the model")
        void modelCarriesNoType() throws IOException {
            // One parameter model backs several task types, so the type is injected by the
            // request envelope. A field here would let a caller contradict the method they
            // called.
            assertFalse(tree(params()).has("type"));
        }

        @Test
        @DisplayName("toString masks the proxy credentials")
        void proxyIsRedactedInToString() {
            HCaptchaTaskParams params = params();
            params.setProxy("http://user:hunter2@127.0.0.1:8080");

            String text = params.toString();
            assertFalse(text.contains("hunter2"), "proxy password leaked into toString");
            assertTrue(text.contains("[REDACTED]"), "a configured proxy should still be visible");
            // And an unset proxy stays distinguishable from a hidden one.
            assertFalse(params().toString().contains("[REDACTED]"));
        }
    }

    @Nested
    @DisplayName("inbound: solutions")
    class Inbound {

        private HCaptchaSolution decode(String body) throws IOException {
            return Json.decodeSolution(Json.mapper().readTree(body), HCaptchaSolution.class, "task-1");
        }

        @Test
        @DisplayName("a key the model does not declare is kept, not dropped")
        void unmodelledKeysSurvive() throws IOException {
            HCaptchaSolution solution =
                    decode(
                            "{\"generated_pass_UUID\":\"pass\",\"ua\":\"UA/1.0\",\"lang\":\"en\","
                                    + "\"brandNewField\":\"keep me\"}");

            assertEquals("pass", solution.getGeneratedPassUuid());
            assertEquals("UA/1.0", solution.getUa());
            assertEquals("keep me", solution.get("brandNewField"));
        }

        @Test
        @DisplayName("get returns null for a key the response did not carry")
        void getIsNullForAnAbsentKey() throws IOException {
            HCaptchaSolution solution = decode("{\"generated_pass_UUID\":\"pass\"}");

            assertNull(solution.get("neverSent"));
            // A declared field is not reachable here either, so a null always means "not in
            // the response" rather than "not modelled".
            assertNull(solution.get("ua"));
        }

        @Test
        @DisplayName("a declared key never leaks into extra")
        void declaredKeysDoNotReachExtra() throws IOException {
            HCaptchaSolution solution =
                    decode("{\"generated_pass_UUID\":\"pass\",\"ua\":\"UA/1.0\",\"lang\":\"en\"}");

            assertTrue(solution.getExtra().isEmpty(), "extra held: " + solution.getExtra());
        }

        @Test
        @DisplayName("an absent optional field is null, not a failure")
        void optionalFieldsStayOptional() throws IOException {
            HCaptchaSolution solution = decode("{\"generated_pass_UUID\":\"pass\"}");

            assertEquals("pass", solution.getGeneratedPassUuid());
            assertNull(solution.getUa());
            assertNull(solution.getLang());
        }

        @Test
        @DisplayName("an absent required field fails with the raw payload in hand")
        void missingRequiredFieldIsRejected() {
            SolutionDecodeException thrown =
                    assertThrows(
                            SolutionDecodeException.class, () -> decode("{\"ua\":\"UA/1.0\"}"));

            assertTrue(
                    thrown.getMessage().contains("generated_pass_UUID"),
                    "the message should name the missing wire key, got: " + thrown.getMessage());

            // The task was billed. Both of these are what make the result recoverable.
            assertEquals("task-1", thrown.getTaskId());
            assertNotNull(thrown.getRaw());
            assertEquals("UA/1.0", thrown.getRaw().get("ua").asText());
        }

        @Test
        @DisplayName("an explicit null does not satisfy a required field")
        void nullDoesNotCountAsPresent() throws IOException {
            // Present-but-null and absent both leave the field null, so this asserts the
            // check reads the JSON rather than the decoded object.
            HCaptchaSolution solution = decode("{\"generated_pass_UUID\":null}");
            assertNull(solution.getGeneratedPassUuid());
        }

        @Test
        @DisplayName("a solution that is not an object is refused")
        void nonObjectPayloadIsRejected() {
            SolutionDecodeException thrown =
                    assertThrows(
                            SolutionDecodeException.class, () -> decode("\"just-a-string\""));

            assertTrue(thrown.getMessage().contains("not a JSON object"));
        }

        @Test
        @DisplayName("required keys are read from the annotation, in wire spelling")
        void requiredKeysUseTheWireName() {
            assertEquals(
                    Collections.singleton("generated_pass_UUID"),
                    WireNames.requiredOf(HCaptchaSolution.class));
        }

        @Test
        @DisplayName("re-serializing a decoded solution emits no duplicate key")
        void solutionRoundTripsCleanly() throws IOException {
            HCaptchaSolution solution =
                    decode("{\"generated_pass_UUID\":\"pass\",\"ua\":\"UA/1.0\",\"unmodelled\":1}");

            String text = Json.mapper().writeValueAsString(solution);
            assertDoesNotThrow(() -> STRICT.readTree(text), "emitted a duplicate key: " + text);

            JsonNode json = Json.mapper().readTree(text);
            assertEquals("pass", json.get("generated_pass_UUID").asText());
            assertEquals(1, json.get("unmodelled").asInt());
        }
    }
}
