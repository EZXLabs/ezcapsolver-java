package com.burstlinker.ezcapsolver.examples.tlsforward;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.TlsForwardSolution;
import com.burstlinker.ezcapsolver.model.task.TlsForwardTaskParams;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sends one HTTP request from a worker, using that worker's TLS fingerprint instead of yours.
 *
 * <p>Task type: {@code TlsTask}
 *
 * <p>Not a captcha at all: the worker performs the request and hands back the upstream response.
 * This is also the one task type the service exempts from site allow-listing and block-listing.
 *
 * <p>{@code headersOrder} exists because header order is itself part of what a fingerprint check
 * looks at — two requests with identical headers in a different order do not look alike.
 *
 * <p>Every field name here is snake_case. Runs on the synchronous endpoint.
 */
public class TlsTask {

    public static void main(String[] args) {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        Map<String, Object> headers = new LinkedHashMap<String, Object>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        headers.put("Accept", "application/json");

        TlsForwardTaskParams params =
                TlsForwardTaskParams.builder()
                        .tlsType("chrome153")
                        .proxy(System.getenv("EZCAPTCHA_PROXY"))
                        .method(TlsForwardTaskParams.METHOD_GET)
                        .url("https://postman-echo.com/get")
                        .headers(headers)
                        .headersOrder("User-Agent,Accept")
                        .build();

        Solved<TlsForwardSolution> solved = client.syncSolveTlsTask(params);

        System.out.println("Task ID: " + solved.getTaskId());
        System.out.println("Status:  " + solved.getSolution().getStatus());
        System.out.println("Cookies: " + solved.getSolution().getCookies());
        System.out.println("Body:    " + solved.getSolution().getBody());
    }
}
