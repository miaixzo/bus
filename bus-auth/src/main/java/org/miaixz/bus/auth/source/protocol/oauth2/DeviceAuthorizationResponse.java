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
package org.miaixz.bus.auth.source.protocol.oauth2;

import java.net.URI;
import java.net.URISyntaxException;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Represents the standard RFC 8628 response issued by a device authorization endpoint.
 * <p>
 * Device and user codes are short-lived credentials. The complete verification URI embeds the user code and is
 * therefore omitted from diagnostics together with both codes.
 * </p>
 *
 * @param deviceCode              opaque device verification code used at the token endpoint
 * @param userCode                end-user code displayed by the device
 * @param verificationUri         end-user verification endpoint URI
 * @param verificationUriComplete optional verification URI containing the user code
 * @param expiresIn               lifetime of the device and user codes in seconds
 * @param interval                optional minimum polling interval in seconds
 * @author Kimi Liu
 */
public record DeviceAuthorizationResponse(String deviceCode, String userCode, String verificationUri,
        Optional<String> verificationUriComplete, long expiresIn, Optional<Long> interval) {

    /**
     * Creates and validates an immutable device authorization response.
     *
     * @throws IllegalArgumentException if a required value or optional container is {@code null}
     * @throws ValidateException        if a code is empty, a URI is invalid, or a duration is not positive
     */
    public DeviceAuthorizationResponse {
        Assert.notEmpty(deviceCode, "OAuth 2.x device code must not be empty");
        Assert.notEmpty(userCode, "OAuth 2.x user code must not be empty");
        validateUri(verificationUri, "OAuth 2.x verification URI");
        Assert.notNull(verificationUriComplete, "OAuth 2.x complete verification URI container must not be null");
        final String complete = verificationUriComplete.getOrNull();
        if (complete != null) {
            validateUri(complete, "OAuth 2.x complete verification URI");
        }
        if (expiresIn <= 0L) {
            throw new ValidateException("OAuth 2.x device authorization lifetime must be positive");
        }
        Assert.notNull(interval, "OAuth 2.x device polling interval container must not be null");
        final Long pollingInterval = interval.getOrNull();
        if (pollingInterval != null && pollingInterval <= 0L) {
            throw new ValidateException("OAuth 2.x device polling interval must be positive when present");
        }
        verificationUriComplete = Optional.ofNullable(complete);
        interval = Optional.ofNullable(pollingInterval);
    }

    /**
     * Validates an absolute URI without a fragment component.
     *
     * @param value URI wire value
     * @param label safe component label used in diagnostics
     * @throws IllegalArgumentException if the value is {@code null} or empty
     * @throws ValidateException        if the value is not an absolute no-fragment URI
     */
    private static void validateUri(final String value, final String label) {
        Assert.notEmpty(value, label + " must not be empty");
        try {
            final URI uri = new URI(value);
            if (!uri.isAbsolute() || uri.getRawFragment() != null) {
                throw new ValidateException(label + " must be an absolute URI without a fragment");
            }
        } catch (URISyntaxException exception) {
            throw new ValidateException(label + " must be a valid absolute URI", exception);
        }
    }

    /**
     * Returns a diagnostic representation without device, user, or embedded user-code values.
     *
     * @return redacted device authorization response summary
     */
    @Override
    public String toString() {
        return "DeviceAuthorizationResponse[deviceCode=[REDACTED], userCode=[REDACTED], verificationUri="
                + verificationUri + ", verificationUriComplete=[REDACTED], expiresIn=" + expiresIn + ", interval="
                + interval + Symbol.BRACKET_RIGHT;
    }

}
