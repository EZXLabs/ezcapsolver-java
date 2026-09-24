package com.burstlinker.ezcapsolver.examples.perimeterx;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.PerimeterXSolution;
import com.burstlinker.ezcapsolver.model.task.PerimeterXTaskParams;

/**
 * Solves a PerimeterX (HUMAN) challenge.
 *
 * <p>Task type: {@code PerimeterX}
 *
 * <p>The solution is three cookie values rather than one token. Send all three back: {@code _px3}
 * is the one that matters most, but sites do check the others.
 */
public class PerimeterX {

    public static void main(String[] args) {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        PerimeterXTaskParams params =
                PerimeterXTaskParams.builder()
                        .websiteKey("YOUR_PX_APP_ID")
                        .invisible(false)
                        .build();

        Solved<PerimeterXSolution> solved = client.solvePerimeterX(params);

        System.out.println("Task ID: " + solved.getTaskId());
        System.out.println("_px3:    " + solved.getSolution().getPx3());
        System.out.println("_pxvid:  " + solved.getSolution().getPxVid());
        System.out.println("_pxde:   " + solved.getSolution().getPxde());
    }
}
