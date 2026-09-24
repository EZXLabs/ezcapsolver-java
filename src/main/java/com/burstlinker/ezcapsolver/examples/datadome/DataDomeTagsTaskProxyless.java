package com.burstlinker.ezcapsolver.examples.datadome;

import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.DataDomeSolution;
import com.burstlinker.ezcapsolver.model.task.DataDomeTagsTaskParams;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reports a fingerprint to DataDome on the normal browsing path, in exchange for a cookie.
 *
 * <p>Task type: {@code DataDomeTagsTaskProxyless}
 *
 * <p>Two modes, and the packet counter follows from which one you pick: {@code ch} fixes
 * {@code bpc} at 1, while {@code le} starts at 2 and counts up. The defaults here are the
 * {@code ch} pair, so an {@code le} flow has to set both.
 *
 * <p>{@code cid} and {@code fields} default to an empty string and an empty object and are
 * always sent. That is deliberate: the service rejects a JSON {@code null} for {@code fields},
 * and an omitted key is exactly what a {@code null} becomes on the far side.
 *
 * <p>Field names here are flat lowercase, unlike {@code DataDomeTaskProxyless}'s snake_case.
 *
 * <p>Runs on the synchronous endpoint.
 */
public class DataDomeTagsTaskProxyless {

    public static void main(String[] args) {
        EzCapSolverClient client = EzCapSolverClient.builder().build();

        Map<String, Object> fields = new LinkedHashMap<String, Object>();
        fields.put("tags_url", "https://example.com/js/tags.js");

        DataDomeTagsTaskParams params =
                DataDomeTagsTaskParams.builder()
                        // Read from the site's inline snippet as window.ddjskey.
                        .ddk("YOUR_DDJSKEY")
                        .jsType(DataDomeTagsTaskParams.JS_TYPE_CH)
                        .bpc(DataDomeTagsTaskParams.MIN_PACKET_COUNTER)
                        .referer("https://example.com/search")
                        .ua("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .fields(fields)
                        .build();

        Solved<DataDomeSolution> solved = client.syncSolveDataDomeTagsTaskProxyless(params);

        System.out.println("Task ID: " + solved.getTaskId());
        System.out.println("Kind:    " + solved.getSolution().getKind());
        System.out.println("Body:    " + solved.getSolution().getBody());
    }
}
