package com.burstlinker.ezcapsolver.model.task;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The value sent in a task's {@code type} field.
 *
 * <p><strong>Open strings, not an enum.</strong> A task type the service adds after this
 * release still works — pass the string and go, without waiting for an SDK update:
 *
 * <pre>{@code
 * client.solveRaw("BrandNewTaskType", params);
 * }</pre>
 *
 * <p>The constants carry the task catalog's own irregularities verbatim, because the SDK is
 * part of the published documentation: {@code FuncaptchaTaskProxyless} has a lowercase
 * {@code c} where {@code FunCaptchaClassification} does not, and {@code AkamaiWEBTaskProxyless}
 * is shouted. Do not tidy these up.
 *
 * <p>Exactly one is deliberately normalised, in every language SDK alike: the catalog writes
 * the V3 Enterprise S9 type as {@code RecaptchaV3EnterpriseTaskProxylessS9}, and the SDKs send
 * {@code ReCaptchaV3EnterpriseTaskProxylessS9} so that one capitalisation runs through the
 * whole ReCaptcha family. The service matches task types case-insensitively, so it reaches the
 * same worker.
 */
public final class TaskType {

    private TaskType() {
        throw new AssertionError("no instances");
    }

    // -- Asynchronous: created with /createTask, then polled --------------------------

    public static final String RECAPTCHA_V2_TASK_PROXYLESS = "ReCaptchaV2TaskProxyless";
    public static final String RECAPTCHA_V2_TASK_PROXYLESS_S9 = "ReCaptchaV2TaskProxylessS9";
    public static final String RECAPTCHA_V2_S_TASK_PROXYLESS = "ReCaptchaV2STaskProxyless";
    public static final String RECAPTCHA_V2_ENTERPRISE_TASK_PROXYLESS =
            "ReCaptchaV2EnterpriseTaskProxyless";
    public static final String RECAPTCHA_V2_S_ENTERPRISE_TASK_PROXYLESS =
            "ReCaptchaV2SEnterpriseTaskProxyless";
    public static final String RECAPTCHA_V3_TASK_PROXYLESS = "ReCaptchaV3TaskProxyless";
    public static final String RECAPTCHA_V3_TASK_PROXYLESS_S9 = "ReCaptchaV3TaskProxylessS9";
    public static final String RECAPTCHA_V3_ENTERPRISE_TASK_PROXYLESS =
            "ReCaptchaV3EnterpriseTaskProxyless";
    public static final String RECAPTCHA_V3_ENTERPRISE_TASK_PROXYLESS_S9 =
            "ReCaptchaV3EnterpriseTaskProxylessS9";
    public static final String FUNCAPTCHA_TASK_PROXYLESS = "FuncaptchaTaskProxyless";
    public static final String PERIMETER_X = "PerimeterX";
    public static final String HCAPTCHA = "HCaptcha";
    public static final String CLOUDFLARE_5S_TASK = "CloudFlare5STask";
    public static final String CLOUDFLARE_TURNSTILE_TASK = "CloudFlareTurnstileTask";

    // -- Synchronous: executed with /createSyncTask, answered inline -------------------

    public static final String RECAPTCHA_V2_CLASSIFICATION = "ReCaptchaV2Classification";
    public static final String FUNCAPTCHA_CLASSIFICATION = "FunCaptchaClassification";
    public static final String HCAPTCHA_CLASSIFICATION = "HCaptchaClassification";
    public static final String AKAMAI_WEB_TASK_PROXYLESS = "AkamaiWEBTaskProxyless";
    public static final String AKAMAI_SBSD_TASK_PROXYLESS = "AkamaiSBSDTaskProxyless";
    public static final String TLS_TASK = "TlsTask";
    public static final String DATADOME_TASK_PROXYLESS = "DataDomeTaskProxyless";
    public static final String DATADOME_TAGS_TASK_PROXYLESS = "DataDomeTagsTaskProxyless";
    public static final String INCAPSULA_TASK_PROXYLESS = "IncapsulaTaskProxyless";

    /**
     * Every task type this release models, in the order the task catalog documents them.
     *
     * <p>Being listed here says nothing about availability: whether a type can be used depends
     * on service-side configuration and on the caller's plan, neither of which the SDK can
     * predict.
     */
    public static final List<String> KNOWN =
            Collections.unmodifiableList(
                    Arrays.asList(
                            RECAPTCHA_V2_TASK_PROXYLESS,
                            RECAPTCHA_V2_TASK_PROXYLESS_S9,
                            RECAPTCHA_V2_S_TASK_PROXYLESS,
                            RECAPTCHA_V2_ENTERPRISE_TASK_PROXYLESS,
                            RECAPTCHA_V2_S_ENTERPRISE_TASK_PROXYLESS,
                            RECAPTCHA_V2_CLASSIFICATION,
                            RECAPTCHA_V3_TASK_PROXYLESS,
                            RECAPTCHA_V3_TASK_PROXYLESS_S9,
                            RECAPTCHA_V3_ENTERPRISE_TASK_PROXYLESS,
                            RECAPTCHA_V3_ENTERPRISE_TASK_PROXYLESS_S9,
                            FUNCAPTCHA_TASK_PROXYLESS,
                            FUNCAPTCHA_CLASSIFICATION,
                            PERIMETER_X,
                            HCAPTCHA,
                            HCAPTCHA_CLASSIFICATION,
                            AKAMAI_WEB_TASK_PROXYLESS,
                            AKAMAI_SBSD_TASK_PROXYLESS,
                            TLS_TASK,
                            CLOUDFLARE_5S_TASK,
                            CLOUDFLARE_TURNSTILE_TASK,
                            DATADOME_TASK_PROXYLESS,
                            DATADOME_TAGS_TASK_PROXYLESS,
                            INCAPSULA_TASK_PROXYLESS));

    /**
     * {@link #KNOWN} again, as a set.
     *
     * <p>{@link #KNOWN} stays a list because its order is the task catalog's own and worth
     * keeping for anything that iterates it. Membership is a different question, and answering
     * it by scanning the list would be the odd one out next to {@link #SYNC_TYPES}.
     */
    private static final Set<String> KNOWN_SET =
            Collections.unmodifiableSet(new HashSet<String>(KNOWN));

    /** The subset the service documents as running through {@code /createSyncTask}. */
    private static final Set<String> SYNC_TYPES =
            Collections.unmodifiableSet(
                    new HashSet<String>(
                            Arrays.asList(
                                    RECAPTCHA_V2_CLASSIFICATION,
                                    FUNCAPTCHA_CLASSIFICATION,
                                    HCAPTCHA_CLASSIFICATION,
                                    AKAMAI_WEB_TASK_PROXYLESS,
                                    AKAMAI_SBSD_TASK_PROXYLESS,
                                    TLS_TASK,
                                    DATADOME_TASK_PROXYLESS,
                                    DATADOME_TAGS_TASK_PROXYLESS,
                                    INCAPSULA_TASK_PROXYLESS)));

    /** Whether this release models the type. Comparison is exact, as sent on the wire. */
    public static boolean isKnown(String taskType) {
        return KNOWN_SET.contains(taskType);
    }

    /**
     * Whether the service documents the type as synchronous.
     *
     * <p><strong>Informational; nothing in the SDK routes on it.</strong> Every type has both
     * a {@code solveX} that polls and a {@code syncSolveX} that does not, and the method you
     * call decides the endpoint.
     *
     * <p>It matters because the service may reject a type on the endpoint it does not serve —
     * with {@code ERROR_TASK_TYPE_NOT_ALLOWED} on the synchronous side. That rejection happens
     * <strong>before the task is billed</strong>, so guessing wrong costs a round trip rather
     * than a task.
     */
    public static boolean isSync(String taskType) {
        return SYNC_TYPES.contains(taskType);
    }
}
