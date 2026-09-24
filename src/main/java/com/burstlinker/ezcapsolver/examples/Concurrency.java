package com.burstlinker.ezcapsolver.examples;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.HCaptchaSolution;
import com.burstlinker.ezcapsolver.model.task.HCaptchaTaskParams;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Sharing one client across threads, with a cap on how many tasks are in flight.
 *
 * <p><strong>Every task below is billed.</strong> This file creates {@code TASKS} of them.
 *
 * <p>One client is enough for a whole application. It is immutable after construction and holds
 * a single OkHttp connection pool; building one per request throws that pool away and opens
 * fresh connections every time.
 *
 * <p>The cap matters for a different reason than usual: each in-flight task is money, and the
 * service applies its own rate limits. An unbounded pool turns a burst into
 * {@code ERROR_REQUEST_LIMIT} responses, which the SDK rides out during polling but which will
 * refuse task creation outright.
 */
public class Concurrency {

    private static final int TASKS = 8;
    private static final int CONCURRENCY = 3;

    public static void main(String[] args) throws Exception {
        final EzCapSolverClient client = EzCapSolverClient.builder().build();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);
        List<Future<Solved<HCaptchaSolution>>> futures =
                new ArrayList<Future<Solved<HCaptchaSolution>>>();

        try {
            for (int i = 0; i < TASKS; i++) {
                futures.add(pool.submit(solveOne(client)));
            }

            for (Future<Solved<HCaptchaSolution>> future : futures) {
                try {
                    Solved<HCaptchaSolution> solved = future.get();
                    System.out.println(
                            solved.getTaskId() + " -> " + solved.getSolution().getGeneratedPassUuid());
                } catch (Exception e) {
                    // One failure must not sink the batch: the other tasks are already paid for.
                    System.err.println("failed: " + e.getMessage());
                }
            }
        } finally {
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.MINUTES);
        }
    }

    private static Callable<Solved<HCaptchaSolution>> solveOne(final EzCapSolverClient client) {
        return new Callable<Solved<HCaptchaSolution>>() {
            @Override
            public Solved<HCaptchaSolution> call() {
                HCaptchaTaskParams params =
                        HCaptchaTaskParams.builder()
                                .websiteUrl("https://accounts.hcaptcha.com/demo")
                                .websiteKey("338af34c-7bcb-4c7c-900b-acbec73d7d43")
                                .lang("en-US")
                                .build();
                return client.solveHCaptcha(params);
            }
        };
    }
}
