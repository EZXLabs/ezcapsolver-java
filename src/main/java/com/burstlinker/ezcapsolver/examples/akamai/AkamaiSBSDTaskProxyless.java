package com.burstlinker.ezcapsolver.examples.akamai;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.AkamaiSbsdSolution;
import com.burstlinker.ezcapsolver.model.task.AkamaiSbsdTaskParams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * Produces an Akamai SBSD payload.
 *
 * <p>Task type: {@code AkamaiSBSDTaskProxyless} — {@code SBSD} is shouted in the catalog, kept
 * verbatim here.
 *
 * <p>Unlike the Web flow this is a single round, but it needs the SBSD script itself, base64
 * encoded — not its URL.
 *
 * <p>Runs on the synchronous endpoint.
 */
public class AkamaiSBSDTaskProxyless {

    public static void main(String[] args) throws IOException {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        // The script the page loaded, fetched and base64-encoded by you.
        byte[] script = Files.readAllBytes(Paths.get("sbsd.js"));

        AkamaiSbsdTaskParams params =
                AkamaiSbsdTaskParams.builder()
                        .pageUrl("https://example.com")
                        .sbsdUrl("https://example.com/.well-known/sbsd")
                        .bmSo(System.getenv("EZCAPTCHA_AKAMAI_BM_SO"))
                        .ua("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .lang("en-US")
                        .scriptBase64(Base64.getEncoder().encodeToString(script))
                        .build();

        Solved<AkamaiSbsdSolution> solved = client.syncSolveAkamaiSBSDTaskProxyless(params);

        System.out.println("Task ID:     " + solved.getTaskId());
        System.out.println("Payload:     " + solved.getSolution().getPayload());
        System.out.println("bm_lso_time: " + solved.getSolution().getBmLsoTime());
    }
}
