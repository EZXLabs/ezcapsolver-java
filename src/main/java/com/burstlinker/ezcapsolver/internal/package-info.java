/**
 * SDK internals. <strong>Not API — do not use, do not depend on.</strong>
 *
 * <p>Everything here is {@code public} for one reason only: it is called from more than one
 * package, and below Java 9 there is no visibility between package-private and public. The
 * package name is the contract the language cannot express, which is the same convention
 * {@code okhttp3.internal} and {@code jdk.internal} use.
 *
 * <p>Nothing here carries a compatibility promise. Classes may be renamed, changed or removed
 * in any release, including a patch. They are excluded from the generated javadoc and from the
 * binary-compatibility check.
 *
 * <p>Internals the compiler <em>can</em> hide are deliberately not here: {@code Transport} is
 * called only from {@link com.burstlinker.ezcapsolver.EzCapSolverClient}, so it stays
 * package-private next to it, where the rule is enforced rather than merely stated.
 */
package com.burstlinker.ezcapsolver.internal;
