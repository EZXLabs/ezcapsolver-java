package com.burstlinker.ezcapsolver.examples.akamai;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.AkamaiWebSolution;
import com.burstlinker.ezcapsolver.model.task.AkamaiWebTaskParams;

/**
 * Produces Akamai Web sensor data, one round at a time.
 *
 * <p>Task type: {@code AkamaiWEBTaskProxyless} — the catalog shouts {@code WEB}, and the SDK
 * keeps it verbatim.
 *
 * <p><strong>This is a multi-round flow.</strong> Feed each round's returned {@code encodedata}
 * into the next round's {@code encodeData} and raise {@code index}. Note the casing difference
 * between the two — the service spells it differently in each direction, which is exactly the
 * kind of thing a typed model is for.
 *
 * <p>Each round is a separate billed task. The four round-state fields default to an empty
 * string and are always sent, so round one has the same shape as every later round.
 *
 * <p>Runs on the synchronous endpoint.
 */
public class AkamaiWEBTaskProxyless {

    private static final int ROUNDS = 3;

    public static void main(String[] args) {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        String encodeData = "";

        for (int index = 0; index < ROUNDS; index++) {
            AkamaiWebTaskParams params =
                    AkamaiWebTaskParams.builder()
                            .pageUrl("https://example.com")
                            // Most sites change this URL on every request, so read it from the
                            // page rather than hard-coding it. It is the URL itself, not the
                            // script the URL serves.
                            .v3Url("https://example.com/akam/13/abcdef12")
                            .ua("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                            .lang("en-US")
                            .index(index)
                            .abck(System.getenv("EZCAPTCHA_AKAMAI_ABCK"))
                            .bmsz(System.getenv("EZCAPTCHA_AKAMAI_BMSZ"))
                            .encodeData(encodeData)
                            .build();

            Solved<AkamaiWebSolution> solved = client.syncSolveAkamaiWEBTaskProxyless(params);

            System.out.println("Round " + index + " task ID: " + solved.getTaskId());
            System.out.println("Round " + index + " payload: " + solved.getSolution().getPayload());

            // Carry the state forward. Note: encodedata out, encodeData in.
            encodeData = solved.getSolution().getEncodedata();
        }
    }
}
