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
package org.miaixz.bus.auth.protocol.oauth2;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Represents the RFC 8628 device-code grant submitted to the standard OAuth token endpoint.
 * <p>
 * The device code is a short-lived credential and is never included in diagnostic rendering. An authenticated client
 * can omit the client identifier; an unauthenticated client supplies it according to RFC 8628.
 * </p>
 *
 * @param deviceCode opaque device verification code
 * @param clientId   optional identifier for a client not authenticated by the token endpoint
 * @author Kimi Liu
 */
public record DeviceCodeGrant(String deviceCode, Optional<String> clientId) implements TokenRequest.Grant {

    /**
     * Creates and validates an immutable device-code grant.
     *
     * @throws IllegalArgumentException if the code or client identifier container is {@code null}, or code is empty
     * @throws ValidateException        if a present client identifier is empty or contains a character outside VSCHAR
     */
    public DeviceCodeGrant {
        Assert.notEmpty(deviceCode, "OAuth 2.x device code must not be empty");
        Assert.notNull(clientId, "OAuth 2.x device-code client identifier container must not be null");
        final String client = clientId.getOrNull();
        if (client != null) {
            Assert.notEmpty(client, "OAuth 2.x device-code client identifier must not be empty when present");
            for (int index = 0; index < client.length(); index++) {
                final char character = client.charAt(index);
                if (character < 0x20 || character > 0x7e) {
                    throw new ValidateException(
                            "OAuth 2.x device-code client identifier contains a character outside VSCHAR");
                }
            }
        }
        clientId = Optional.ofNullable(client);
    }

    /**
     * Returns a diagnostic representation without the device credential.
     *
     * @return redacted device-code grant summary
     */
    @Override
    public String toString() {
        return "DeviceCodeGrant[deviceCode=[REDACTED], clientId=" + clientId + Symbol.BRACKET_RIGHT;
    }

}
