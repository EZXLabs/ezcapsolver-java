package com.burstlinker.ezcapsolver;

import com.burstlinker.ezcapsolver.exception.EzCaptchaException;
import com.burstlinker.ezcapsolver.internal.Json;
import com.burstlinker.ezcapsolver.internal.Redaction;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Configuration is checked while building, never on the first request — a mistake has to
 * surface before anything is billed.
 */
class ClientConfigTest {

    private static ClientConfig.ClientConfigBuilder valid() {
        return ClientConfig.builder().clientKey("test-key");
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("defaults land on the documented values")
        void defaults() {
            ClientConfig config = valid().build();

            assertEquals(ClientConfig.DEFAULT_TIMEOUT, config.getTimeout());
            assertEquals(ClientConfig.DEFAULT_SYNC_TIMEOUT, config.getSyncTimeout());
            assertEquals(ClientConfig.DEFAULT_ASYNC_BASE_URL, config.getAsyncBaseUrl());
            assertEquals(ClientConfig.DEFAULT_SYNC_BASE_URL, config.getSyncBaseUrl());
            assertEquals(Version.DEFAULT_USER_AGENT, config.getUserAgent());

            // The two budgets must stay apart: a synchronous call blocks until the worker
            // answers, and cutting it short wastes a billed call that returns no task id.
            assertTrue(
                    config.getSyncTimeout().compareTo(config.getTimeout()) > 0,
                    "the sync budget has to be the longer one");
        }

        @Test
        @DisplayName("a missing client key fails at build time")
        void missingClientKey() {
            // Only meaningful when the environment fallback is absent; on a machine with a
            // real key exported this would build successfully and prove nothing.
            Assumptions.assumeTrue(
                    System.getenv(ClientConfig.DEFAULT_CLIENT_KEY_ENV) == null,
                    ClientConfig.DEFAULT_CLIENT_KEY_ENV + " is set in this environment");

            EzCaptchaException thrown =
                    assertThrows(EzCaptchaException.class, () -> ClientConfig.builder().build());

            assertTrue(thrown.getMessage().contains("invalid client configuration"));
            assertTrue(thrown.getMessage().contains(ClientConfig.DEFAULT_CLIENT_KEY_ENV));
        }

        @Test
        @DisplayName("a blank client key is treated as absent, not as a key")
        void blankClientKey() {
            Assumptions.assumeTrue(System.getenv(ClientConfig.DEFAULT_CLIENT_KEY_ENV) == null);

            assertThrows(EzCaptchaException.class, () -> ClientConfig.builder().clientKey("   ").build());
        }

        @Test
        @DisplayName("a key with characters a request cannot carry is refused at construction")
        void invalidKeyCharacters() {
            // Left unchecked, OkHttp would throw IllegalArgumentException on every request,
            // outside the SDK's own exception hierarchy.
            for (String key : new String[] {"key\nX-Injected: 1", " padded ", "kéy", "key\u0000"}) {
                EzCaptchaException thrown =
                        assertThrows(EzCaptchaException.class, () -> ClientConfig.builder().clientKey(key).build(), key);
                assertTrue(
                        thrown.getMessage().contains("client key contains invalid characters"), thrown.getMessage());
            }
        }

        @Test
        @DisplayName("a non-positive timeout is refused")
        void nonPositiveTimeouts() {
            assertThrows(EzCaptchaException.class, () -> valid().timeout(Duration.ZERO).build());
            assertThrows(
                    EzCaptchaException.class, () -> valid().timeout(Duration.ofSeconds(-1)).build());
            assertThrows(EzCaptchaException.class, () -> valid().syncTimeout(Duration.ZERO).build());
        }

        @Test
        @DisplayName("a base URL without a scheme is refused")
        void schemelessBaseUrl() {
            EzCaptchaException thrown =
                    assertThrows(
                            EzCaptchaException.class, () -> valid().asyncBaseUrl("api.example.com").build());

            assertTrue(thrown.getMessage().contains("http://"));
        }

        @Test
        @DisplayName("a trailing slash on a base URL is tolerated")
        void trailingSlashIsFine() {
            // A copy-pasted URL ending in a slash is not a mistake worth failing a build for;
            // the path joiner handles it.
            ClientConfig config = valid().asyncBaseUrl("https://api.example.com/").build();
            assertEquals(
                    "https://api.example.com/createTask",
                    Transport.join(config.getAsyncBaseUrl(), "/createTask"));
        }

        @Test
        @DisplayName("an unusable polling budget is refused here too")
        void pollingIsValidated() {
            assertThrows(
                    EzCaptchaException.class, () -> valid().maxPollAttempts(0).build());
            assertThrows(
                    EzCaptchaException.class, () -> valid().pollInterval(Duration.ZERO).build());
        }

        @Test
        @DisplayName("the default polling budget fits inside the result TTL")
        void defaultPollingBudgetFitsTheTtl() {
            ClientConfig config = valid().build();

            assertEquals(ClientConfig.DEFAULT_POLL_INTERVAL, config.getPollInterval());
            assertEquals(ClientConfig.DEFAULT_MAX_POLL_ATTEMPTS, config.getMaxPollAttempts());

            // The service holds a result for five minutes; past that the task id comes back as
            // ERROR_TASK_NOT_EXIST and the budget is waiting for something already gone.
            long budgetSeconds =
                    config.getPollInterval().getSeconds() * config.getMaxPollAttempts();
            assertTrue(budgetSeconds <= 300, "the default budget outlives the result TTL");
        }

        @Test
        @DisplayName("toBuilder keeps the untouched settings")
        void toBuilderRoundTrips() {
            ClientConfig original = valid().appId(42).timeout(Duration.ofSeconds(9)).build();
            ClientConfig derived = original.toBuilder().appId(7).build();

            assertEquals(7, derived.getAppId().intValue());
            assertEquals(Duration.ofSeconds(9), derived.getTimeout());
            assertEquals("test-key", derived.getClientKey());
        }
    }

    @Nested
    @DisplayName("credentials never reach a log")
    class Redacted {

        @Test
        @DisplayName("toString masks both the key and the proxy")
        void configToStringIsSafe() {
            ClientConfig config =
                    valid().clientKey("sk-secret").proxy("http://user:hunter2@127.0.0.1:8080").build();

            String text = config.toString();
            assertFalse(text.contains("sk-secret"), "client key leaked");
            assertFalse(text.contains("hunter2"), "proxy password leaked");
            assertTrue(text.contains(Redaction.PLACEHOLDER));
        }

        @Test
        @DisplayName("an unset proxy stays distinguishable from a hidden one")
        void nullProxyStaysNull() {
            assertTrue(valid().build().toString().contains("proxy=null"));
        }

        @Test
        @DisplayName("credentials are masked at every nesting depth")
        void redactionIsRecursive() throws IOException {
            // proxy appears both at the top level and inside the task object. Masking only
            // the position the SDK happens to know about would leak the other one.
            JsonNode body =
                    Json.mapper()
                            .readTree(
                                    "{\"clientKey\":\"sk-secret\",\"task\":{\"type\":\"HCaptcha\","
                                            + "\"proxy\":\"http://user:hunter2@host:1\",\"websiteURL\":\"https://x\"},"
                                            + "\"list\":[{\"proxy\":\"http://deep:secret@host:2\"}]}");

            String redacted = Json.mapper().writeValueAsString(Redaction.redact(body));

            assertFalse(redacted.contains("sk-secret"));
            assertFalse(redacted.contains("hunter2"));
            assertFalse(redacted.contains("deep"), "a proxy nested inside an array leaked");
            // Everything that is not a credential has to survive, or the log is useless.
            assertTrue(redacted.contains("HCaptcha"));
            assertTrue(redacted.contains("https://x"));
        }

        @Test
        @DisplayName("redaction copies rather than mutating the body being sent")
        void redactionDoesNotMutate() throws IOException {
            JsonNode body = Json.mapper().readTree("{\"clientKey\":\"sk-secret\"}");
            Redaction.redact(body);

            // Mutating in place would strip the credential from the request itself.
            assertEquals("sk-secret", body.get("clientKey").asText());
        }
    }
}
