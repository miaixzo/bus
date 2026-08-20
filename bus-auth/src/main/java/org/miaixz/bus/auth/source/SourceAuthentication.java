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
package org.miaixz.bus.auth.source;

import java.util.Set;

import org.miaixz.bus.auth.Capability;

/**
 * Declares the two application-level capabilities used to enter external Source authentication.
 * <p>
 * These capabilities orchestrate browser redirects, OAuth device interaction, and direct credential authentication
 * without claiming that the facade itself is an OAuth, OpenID Connect, SAML, LDAP, or Vendor protocol operation. The
 * selected Source runtime remains responsible for invoking and validating its actual industry-standard protocol.
 * </p>
 *
 * @author Kimi Liu
 */
public final class SourceAuthentication {

    /**
     * Starts a browser, device, or direct Source authentication interaction.
     */
    public static final Capability<SourceAuthenticationRequest.Initiation, SourceAuthenticationInitiation> INITIATE = initiate(
            Set.of(Capability.Interaction.DIRECT, Capability.Interaction.REDIRECT, Capability.Interaction.DEVICE));

    /**
     * Completes a correlated browser callback or device polling interaction and returns a verified external identity.
     */
    public static final Capability<SourceAuthenticationRequest.Completion, SourceAuthenticationResult> COMPLETE = complete(
            Set.of(Capability.Interaction.REDIRECT, Capability.Interaction.DEVICE));

    /**
     * Prevents construction of the capability declaration container.
     */
    private SourceAuthentication() {
        // No initialization required.
    }

    /**
     * Creates the initiation declaration implemented by one concrete Source scheme.
     * <p>
     * The returned capability retains the canonical initiation key and Q/S contract while narrowing its interaction
     * metadata to the modes that the concrete runtime actually implements.
     * </p>
     *
     * @param interactions non-empty subset of direct, redirect, and device initiation interactions
     * @return immutable profile-specific initiation capability
     * @throws IllegalArgumentException if the set is null or empty
     */
    public static Capability<SourceAuthenticationRequest.Initiation, SourceAuthenticationInitiation> initiate(
            final Set<Capability.Interaction> interactions) {
        return new Capability<>(Capability.Key.application("source-authentication.initiate"),
                SourceAuthenticationRequest.Initiation.class, SourceAuthenticationInitiation.class,
                Capability.Direction.SOURCE, interactions, Capability.Security.PUBLIC);
    }

    /**
     * Creates the completion declaration implemented by one concrete Source scheme.
     * <p>
     * Direct and one-time-code interactions complete during initiation, so completion declarations accept only redirect
     * and device interactions.
     * </p>
     *
     * @param interactions non-empty subset of redirect and device completion interactions
     * @return immutable profile-specific completion capability
     * @throws IllegalArgumentException if the set is null, empty, or contains a direct interaction
     */
    public static Capability<SourceAuthenticationRequest.Completion, SourceAuthenticationResult> complete(
            final Set<Capability.Interaction> interactions) {
        if (interactions != null && interactions.contains(Capability.Interaction.DIRECT)) {
            throw new IllegalArgumentException("Source authentication completion does not support direct interaction");
        }
        return new Capability<>(Capability.Key.application("source-authentication.complete"),
                SourceAuthenticationRequest.Completion.class, SourceAuthenticationResult.class,
                Capability.Direction.SOURCE, interactions, Capability.Security.PUBLIC);
    }

}
