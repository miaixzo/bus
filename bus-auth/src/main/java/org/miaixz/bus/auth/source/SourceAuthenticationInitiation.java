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

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;

/**
 * Defines the closed application-level results of starting Source authentication.
 * <p>
 * A result either instructs the caller to redirect a browser, presents OAuth device authorization instructions, or
 * returns an already completed direct authentication result. These values are orchestration results and are not OAuth,
 * OpenID Connect, SAML, LDAP, or Vendor wire responses.
 * </p>
 *
 * @author Kimi Liu
 */
public sealed interface SourceAuthenticationInitiation permits SourceAuthenticationInitiation.Redirect,
        SourceAuthenticationInitiation.Device, SourceAuthenticationInitiation.Completed {

    /**
     * Instructs the application to redirect a user agent and retain callback correlation.
     *
     * @param location    absolute or application-approved redirect location produced by the selected Source adapter
     * @param correlation one-time callback correlation persisted by the authentication flow
     * @author Kimi Liu
     */
    record Redirect(String location, Callback.Correlation correlation) implements SourceAuthenticationInitiation {

        /**
         * Creates a browser redirect initiation result.
         *
         * @param location    redirect location
         * @param correlation callback correlation
         * @throws IllegalArgumentException if the location is blank or correlation is {@code null}
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
    record Device(String deviceCode, String userCode, String verificationUri, Optional<String> verificationUriComplete,
            Duration interval, Instant expiresAt) implements SourceAuthenticationInitiation {

        /**
         * Creates an immutable device authorization initiation result.
         *
         * @param deviceCode              opaque polling code
         * @param userCode                user-facing verification code
         * @param verificationUri         verification URI
         * @param verificationUriComplete optional complete verification URI
         * @param interval                positive minimum polling interval
         * @param expiresAt               absolute device-code expiration
         * @throws IllegalArgumentException if a required value is missing, an optional URI is blank, or the polling
         *                                  interval is not positive
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
     * Returns a verified identity immediately for a direct Source interaction.
     *
     * @param result completed Source authentication result
     * @author Kimi Liu
     */
    record Completed(SourceAuthenticationResult result) implements SourceAuthenticationInitiation {

        /**
         * Creates a completed direct initiation result.
         *
         * @param result verified Source authentication result
         * @throws IllegalArgumentException if the result is {@code null}
         */
        public Completed {
            Assert.notNull(result, "Completed Source authentication result must not be null");
        }

    }

}
