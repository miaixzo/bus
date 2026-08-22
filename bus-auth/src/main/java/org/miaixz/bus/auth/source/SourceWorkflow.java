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

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;

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
public class SourceWorkflow {

    /**
     * Starts a browser, device, or direct Source authentication interaction.
     */
    public static final Capability<Request.Start, Stage> INITIATE = initiate(
            Set.of(Capability.Interaction.DIRECT, Capability.Interaction.REDIRECT, Capability.Interaction.DEVICE));

    /**
     * Completes a correlated browser callback or device polling interaction and returns a verified external identity.
     */
    public static final Capability<Request.Completion, ExternalIdentity> COMPLETE = complete(
            Set.of(Capability.Interaction.REDIRECT, Capability.Interaction.DEVICE));

    /**
     * Creates a capability declaration container with no per-instance workflow state.
     */
    public SourceWorkflow() {
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
    public static Capability<Request.Start, Stage> initiate(final Set<Capability.Interaction> interactions) {
        return new Capability<>(Capability.Key.application("source-authentication.initiate"), Request.Start.class,
                Stage.class, Capability.Direction.SOURCE, interactions, Capability.Security.PUBLIC);
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
    public static Capability<Request.Completion, ExternalIdentity> complete(
            final Set<Capability.Interaction> interactions) {
        if (interactions != null && interactions.contains(Capability.Interaction.DIRECT)) {
            throw new IllegalArgumentException("Source authentication completion does not support direct interaction");
        }
        return new Capability<>(Capability.Key.application("source-authentication.complete"), Request.Completion.class,
                ExternalIdentity.class, Capability.Direction.SOURCE, interactions, Capability.Security.PUBLIC);
    }

    /**
     * Defines the closed application-level requests accepted by external Source authentication.
     * <p>
     * The request family identifies an interaction and its registered Source but intentionally excludes OAuth, OpenID
     * Connect, SAML, LDAP, and Vendor wire fields. The selected Source adapter translates the request into its actual
     * protocol operation after Registry routing.
     * </p>
     *
     * @author Kimi Liu
     */
    public interface Request {

        /**
         * Marks requests that begin a Source authentication interaction.
         *
         * @author Kimi Liu
         */
        interface Start extends Request {

        }

        /**
         * Marks requests that resume and complete a previously initiated Source authentication interaction.
         *
         * @author Kimi Liu
         */
        interface Completion extends Request {

        }

        /**
         * Starts an authentication flow that redirects a user agent and later receives a callback.
         *
         * @param sourceId       registered Source identifier
         * @param callbackTarget registered callback target bound to the same Source
         * @author Kimi Liu
         */
        record BrowserStart(String sourceId, Callback.Target callbackTarget) implements Start {

            /**
             * Creates a browser interaction request and verifies its Source binding.
             */
            public BrowserStart {
                Assert.notBlank(sourceId, "Browser Source id must not be blank");
                Assert.notNull(callbackTarget, "Browser callback target must not be null");
                Assert.equals(
                        sourceId,
                        callbackTarget.sourceId(),
                        "Browser callback target must use Source {}",
                        sourceId);
            }

        }

        /**
         * Starts an OAuth device authorization interaction for a registered Source.
         *
         * @param sourceId registered Source identifier
         * @author Kimi Liu
         */
        record DeviceStart(String sourceId) implements Start {

            /**
             * Creates a device interaction request.
             */
            public DeviceStart {
                Assert.notBlank(sourceId, "Device Source id must not be blank");
            }

        }

        /**
         * Starts direct authentication using an externally stored credential reference.
         *
         * @param sourceId      registered Source identifier
         * @param principalHint non-secret principal identifier supplied to the Source
         * @param credential    reference to secret material managed by the external credential store
         * @author Kimi Liu
         */
        record Direct(String sourceId, String principalHint, Credential.Reference credential) implements Start {

            /**
             * Creates a direct authentication request without resolving secret material.
             */
            public Direct {
                Assert.notBlank(sourceId, "Direct Source id must not be blank");
                Assert.notBlank(principalHint, "Direct principal hint must not be blank");
                Assert.notNull(credential, "Direct credential reference must not be null");
            }

        }

        /**
         * Starts direct Source authentication with a platform-issued, single-use code.
         * <p>
         * The code is runtime authentication input rather than stored configuration, a credential reference, or an
         * OAuth authorization code. The selected Source adapter owns platform validation and replay protection.
         * </p>
         *
         * @param sourceId registered Source identifier
         * @param code     opaque single-use code issued for the current Source interaction
         * @author Kimi Liu
         */
        record OneTimeCode(String sourceId, String code) implements Start {

            /**
             * Creates a bounded single-use-code request.
             */
            public OneTimeCode {
                Assert.notBlank(sourceId, "One-time-code Source id must not be blank");
                Assert.notBlank(code, "One-time authentication code must not be blank");
                Assert.isFalse(
                        code.indexOf(Symbol.C_CR) >= 0 || code.indexOf(Symbol.C_LF) >= 0,
                        "One-time authentication code must not contain line breaks");
                Assert.isTrue(code.length() <= 4096, "One-time authentication code must not exceed 4096 characters");
            }

            /**
             * Returns a diagnostic representation that never reveals the single-use code.
             */
            @Override
            public String toString() {
                return "OneTimeCode[sourceId=" + sourceId + ", code=[REDACTED]]";
            }

        }

        /**
         * Completes a browser interaction from an inbound callback captured by an external endpoint.
         *
         * @param sourceId registered Source identifier selected by the route
         * @param callback raw callback transport bound to the same Source
         * @author Kimi Liu
         */
        record BrowserCallback(String sourceId, Callback.Inbound callback) implements Completion {

            /**
             * Creates a browser completion request and verifies its Source binding.
             */
            public BrowserCallback {
                Assert.notBlank(sourceId, "Browser callback Source id must not be blank");
                Assert.notNull(callback, "Browser inbound callback must not be null");
                Assert.equals(sourceId, callback.sourceId(), "Browser inbound callback must use Source {}", sourceId);
            }

        }

        /**
         * Polls completion of an OAuth device authorization interaction.
         *
         * @param sourceId   registered Source identifier
         * @param deviceCode opaque device code returned by the initiation stage
         * @author Kimi Liu
         */
        record DevicePoll(String sourceId, String deviceCode) implements Completion {

            /**
             * Creates a device polling request without interpreting the opaque device code.
             */
            public DevicePoll {
                Assert.notBlank(sourceId, "Device polling Source id must not be blank");
                Assert.notBlank(deviceCode, "Device code must not be blank");
            }

        }

    }

    /**
     * Defines the closed stages returned after starting Source authentication.
     * <p>
     * A stage either instructs the caller to redirect a browser, presents OAuth device authorization instructions, or
     * carries an identity already established by a direct interaction. These values are application orchestration
     * state, not protocol wire responses.
     * </p>
     *
     * @author Kimi Liu
     */
    public interface Stage {

        /**
         * Instructs the application to redirect a user agent and retain callback correlation.
         *
         * @param location    absolute or application-approved redirect location
         * @param correlation one-time callback correlation
         * @author Kimi Liu
         */
        record Redirect(String location, Callback.Correlation correlation) implements Stage {

            /**
             * Creates a browser redirect stage.
             */
            public Redirect {
                Assert.notBlank(location, "Source authentication redirect location must not be blank");
                Assert.notNull(correlation, "Source authentication correlation must not be null");
            }

        }

        /**
         * Presents the standard user instructions returned by an OAuth device authorization endpoint.
         *
         * @param deviceCode              opaque code used only for token endpoint polling
         * @param userCode                short code displayed to the user
         * @param verificationUri         URI at which the user enters the code
         * @param verificationUriComplete optional URI that already contains the user code
         * @param interval                minimum polling interval required by the authorization server
         * @param expiresAt               absolute expiration time of the device authorization
         * @author Kimi Liu
         */
        record Device(String deviceCode, String userCode, String verificationUri,
                Optional<String> verificationUriComplete, Duration interval, Instant expiresAt) implements Stage {

            /**
             * Creates an immutable device authorization stage.
             */
            public Device {
                Assert.notBlank(deviceCode, "Device authorization code must not be blank");
                Assert.notBlank(userCode, "Device authorization user code must not be blank");
                Assert.notBlank(verificationUri, "Device authorization verification URI must not be blank");
                Assert.notNull(verificationUriComplete, "Complete verification URI container must not be null");
                if (!verificationUriComplete.isEmpty()) {
                    Assert.notBlank(verificationUriComplete.getOrNull(), "Complete verification URI must not be blank");
                }
                verificationUriComplete = Optional.ofNullable(verificationUriComplete.getOrNull());
                Assert.notNull(interval, "Device authorization polling interval must not be null");
                Assert.isTrue(
                        !interval.isZero() && !interval.isNegative(),
                        "Device authorization polling interval must be positive");
                Assert.notNull(expiresAt, "Device authorization expiration must not be null");
            }

        }

        /**
         * Carries a verified identity immediately established by a direct Source interaction.
         *
         * @param identity verified external identity
         * @author Kimi Liu
         */
        record Completed(ExternalIdentity identity) implements Stage {

            /**
             * Creates a completed direct-authentication stage.
             */
            public Completed {
                Assert.notNull(identity, "Completed Source authentication identity must not be null");
            }

        }

    }

}
