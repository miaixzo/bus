/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.vortex.guard;

import java.util.function.Predicate;

/**
 * Verifies inbound service tokens on the gateway control plane.
 * <p>
 * The starter installs a verifier when inbound service identity is enabled. Callers outside the gateway pipeline only
 * read the installed predicate, so enabling or disabling the feature takes effect without rebuilding the filter chain.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AnchorVerifier {

    /**
     * Gateway control-plane path that must be authenticated when inbound identity is enabled.
     */
    public static final String CONTROL_PATH = "/router/sdc";

    /**
     * Installed verifier, or null when inbound service identity is disabled.
     */
    private static volatile Predicate<String> verifier;

    /**
     * Prevents additional instances.
     */
    private AnchorVerifier() {
        // No initialization required.
    }

    /**
     * Installs the inbound token verifier.
     *
     * @param value predicate accepting a valid compact service token
     */
    public static void install(final Predicate<String> value) {
        verifier = value;
    }

    /**
     * Returns whether inbound verification is enabled.
     *
     * @return true when a verifier has been installed
     */
    public static boolean installed() {
        return verifier != null;
    }

    /**
     * Verifies one compact token.
     *
     * @param compact compact service token
     * @return true when the token is valid for this process
     */
    public static boolean verify(final String compact) {
        final Predicate<String> current = verifier;
        return current != null && compact != null && current.test(compact);
    }

}
