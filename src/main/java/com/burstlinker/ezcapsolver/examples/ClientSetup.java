package com.burstlinker.ezcapsolver.examples;

import com.burstlinker.ezcapsolver.ClientConfig;
import com.burstlinker.ezcapsolver.EzCapSolverClient;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Every client option, and why the two timeout budgets are separate.
 *
 * <p>Uses {@code getBalance}, which is the only endpoint that costs nothing — so this file is
 * safe to run as written. Every other example creates a billed task.
 */
public class ClientSetup {

    public static void main(String[] args) {
        minimal();
        explicit();
        System.out.println(EzCapSolverClient.builder().build().config());
    }

    /** The whole configuration, when the key is in {@code EZCAPTCHA_API_KEY}. */
    private static void minimal() {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        BigDecimal balance = client.getBalance();
        System.out.println("Balance: " + balance);
    }

    private static void explicit() {
        EzCapSolverClient client =
                EzCapSolverClient.builder()
                        // Falls back to the EZCAPTCHA_API_KEY environment variable.
                        .clientKey(System.getenv("EZCAPTCHA_API_KEY"))

                        // Two separate budgets, on purpose.
                        //
                        // timeout covers the asynchronous endpoints and the balance query,
                        // which answer immediately. syncTimeout covers /createSyncTask, which
                        // blocks until a worker finishes — the service allows some types three
                        // minutes. Collapsing these into one value means either cutting off a
                        // synchronous task that was about to succeed (paid for, and with no
                        // task ID to recover), or waiting minutes for a balance query that
                        // should have failed in seconds.
                        .timeout(Duration.ofSeconds(30))
                        .syncTimeout(Duration.ofSeconds(240))

                        // Polling budget for solve*: pollInterval x maxPollAttempts is the
                        // longest a call will wait. The first query happens after one interval,
                        // never immediately — a task is never ready the instant it is created.
                        .pollInterval(Duration.ofSeconds(3))
                        .maxPollAttempts(50)

                        // Optional: a developer/affiliate identifier.
                        .appId(null)

                        // Applies to the SDK's own connection to EzCaptchaSolver. It is NOT the proxy
                        // a worker uses — that one is a field on the task parameters.
                        .proxy(System.getenv("EZCAPTCHA_PROXY"))

                        .userAgent("my-app/1.0")

                        // Override only when pointed at a mock or a private deployment.
                        .asyncBaseUrl(ClientConfig.DEFAULT_ASYNC_BASE_URL)
                        .syncBaseUrl(ClientConfig.DEFAULT_SYNC_BASE_URL)

                        // Supply your own OkHttpClient to share a connection pool with the rest
                        // of your application. The SDK derives its two timeout variants from it
                        // with newBuilder(), so your interceptors and dispatcher are kept.
                        .okHttpClient(null)
                        .build();

        System.out.println("Balance: " + client.getBalance());
    }
}
