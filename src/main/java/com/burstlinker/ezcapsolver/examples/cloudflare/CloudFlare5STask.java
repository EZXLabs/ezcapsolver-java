package com.burstlinker.ezcapsolver.examples.cloudflare;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.CloudFlare5sSolution;
import com.burstlinker.ezcapsolver.model.task.CloudFlare5sTaskParams;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Clears a Cloudflare five-second interstitial.
 *
 * <p>Task type: {@code CloudFlare5STask}
 *
 * <p><strong>The proxy is required for this type</strong>, unlike most others, and it must carry
 * credentials — the service rejects an unauthenticated proxy, and the host may not be a private
 * address. Format: {@code protocol://username:password@host:port}, protocol being http, https or
 * socks5.
 *
 * <p>The solution is not a token: it is the headers, cookies and body of the cleared response.
 * Replay all of them, which is why the SDK models the whole thing rather than picking one string
 * out of it.
 */
public class CloudFlare5STask {

    public static void main(String[] args) {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        // Send rqData as an object, never a pre-encoded string: the service stringifies it
        // itself when forwarding to the worker, so encoding it here produces double encoding.
        Map<String, Object> rqData = new LinkedHashMap<String, Object>();
        rqData.put("chlPageData", "...");

        CloudFlare5sTaskParams params =
                CloudFlare5sTaskParams.builder()
                        .websiteUrl("https://example.com")
                        .proxy(System.getenv("EZCAPTCHA_PROXY"))
                        .rqData(rqData)
                        .build();

        Solved<CloudFlare5sSolution> solved = client.solveCloudFlare5STask(params);

        System.out.println("Task ID:     " + solved.getTaskId());
        System.out.println("TLS version: " + solved.getSolution().getTlsVersion());
        System.out.println("Cookies:     " + solved.getSolution().getCookies());
        System.out.println("Headers:     " + solved.getSolution().getHeader());
    }
}
