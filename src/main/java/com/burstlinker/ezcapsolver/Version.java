package com.burstlinker.ezcapsolver;

/**
 * The released version of this SDK.
 *
 * <p>{@link #VERSION} must match the version in {@code pom.xml}. The release workflow
 * verifies the git tag against it, so a release cannot ship with the two out of step.
 */
public final class Version {

    /** The SDK version, matching {@code pom.xml}. */
    public static final String VERSION = "0.1.0";

    /**
     * The User-Agent sent when the caller does not supply one.
     *
     * <p>The service has no requirement here; it exists so that traffic from this SDK is
     * identifiable in a capture or a server-side log.
     */
    public static final String DEFAULT_USER_AGENT = "ezcapsolver-java/" + VERSION;

    private Version() {
        throw new AssertionError("no instances");
    }
}
