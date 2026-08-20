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

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.core.lang.Assert;

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
public sealed interface SourceAuthenticationRequest
        permits SourceAuthenticationRequest.Initiation, SourceAuthenticationRequest.Completion {

    /**
     * Marks requests that begin a Source authentication interaction.
     *
     * @author Kimi Liu
     */
    sealed interface Initiation extends SourceAuthenticationRequest
            permits BrowserStart, DeviceStart, Direct, OneTimeCode {

    }

    /**
     * Marks requests that resume and complete a previously initiated Source authentication interaction.
     *
     * @author Kimi Liu
     */
    sealed interface Completion extends SourceAuthenticationRequest permits BrowserCallback, DevicePoll {

    }

    /**
     * Starts an authentication flow that redirects a user agent and later receives a callback.
     *
     * @param sourceId       registered Source identifier
     * @param callbackTarget registered callback target bound to the same Source
     * @author Kimi Liu
     */
    record BrowserStart(String sourceId, Callback.Target callbackTarget) implements Initiation {

        /**
         * Creates a browser interaction request and verifies its Source binding.
         *
         * @param sourceId       registered Source identifier
         * @param callbackTarget registered callback target
         * @throws IllegalArgumentException if a component is invalid or the callback belongs to another Source
         */
        public BrowserStart {
            Assert.notBlank(sourceId, "Browser Source id must not be blank");
            Assert.notNull(callbackTarget, "Browser callback target must not be null");
            Assert.equals(sourceId, callbackTarget.sourceId(), "Browser callback target must use Source {}", sourceId);
        }

    }

    /**
     * Starts an OAuth device authorization interaction for a registered Source.
     *
     * @param sourceId registered Source identifier
     * @author Kimi Liu
     */
    record DeviceStart(String sourceId) implements Initiation {

        /**
         * Creates a device interaction request.
         *
         * @param sourceId registered Source identifier
         * @throws IllegalArgumentException if the Source identifier is blank
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
    record Direct(String sourceId, String principalHint, Credential.Reference credential) implements Initiation {

        /**
         * Creates a direct authentication request without resolving secret material.
         *
         * @param sourceId      registered Source identifier
         * @param principalHint non-secret principal identifier
         * @param credential    external credential reference
         * @throws IllegalArgumentException if an identifier is blank or the credential reference is {@code null}
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
     * The code is runtime authentication input rather than stored configuration, a credential reference, or an OAuth
     * authorization code. The selected Source adapter owns its platform-specific validation and replay protection.
     * </p>
     *
     * @param sourceId registered Source identifier
     * @param code     opaque single-use code issued for the current Source interaction
     * @author Kimi Liu
     */
    record OneTimeCode(String sourceId, String code) implements Initiation {

        /**
         * Creates a single-use-code authentication request while enforcing its safe transport boundary.
         *
         * @param sourceId registered Source identifier
         * @param code     opaque single-use code
         * @throws IllegalArgumentException if the Source identifier or code is blank, the code contains a line break,
         *                                  or the code exceeds 4096 characters
         */
        public OneTimeCode {
            Assert.notBlank(sourceId, "One-time-code Source id must not be blank");
            Assert.notBlank(code, "One-time authentication code must not be blank");
            Assert.isFalse(
                    code.indexOf('\r') >= 0 || code.indexOf('\n') >= 0,
                    "One-time authentication code must not contain line breaks");
            Assert.isTrue(code.length() <= 4096, "One-time authentication code must not exceed 4096 characters");
        }

        /**
         * Returns a diagnostic representation that never reveals the single-use code.
         *
         * @return redacted request representation
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
         *
         * @param sourceId registered Source identifier
         * @param callback raw inbound callback
         * @throws IllegalArgumentException if a component is invalid or the callback belongs to another Source
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
     * @param deviceCode opaque device code returned by the initiation result
     * @author Kimi Liu
     */
    record DevicePoll(String sourceId, String deviceCode) implements Completion {

        /**
         * Creates a device polling request without interpreting the opaque device code.
         *
         * @param sourceId   registered Source identifier
         * @param deviceCode opaque device code
         * @throws IllegalArgumentException if either value is blank
         */
        public DevicePoll {
            Assert.notBlank(sourceId, "Device polling Source id must not be blank");
            Assert.notBlank(deviceCode, "Device code must not be blank");
        }

    }

}
