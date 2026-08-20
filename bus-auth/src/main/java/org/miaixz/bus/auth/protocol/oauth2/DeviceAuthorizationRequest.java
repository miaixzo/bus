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
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Represents an RFC 8628 device authorization request before device-code issuance.
 *
 * @param clientId client identifier issued to the device client
 * @param scope    optional authorization scope requested for the device flow
 * @author Kimi Liu
 */
public record DeviceAuthorizationRequest(String clientId, Optional<Scope> scope) {

    /**
     * Creates and validates an immutable device authorization request.
     *
     * @throws IllegalArgumentException if the client identifier is {@code null} or empty, or scope is {@code null}
     * @throws ValidateException        if the client identifier contains a character outside RFC 6749 {@code VSCHAR}
     */
    public DeviceAuthorizationRequest {
        Assert.notEmpty(clientId, "OAuth 2.x device authorization client identifier must not be empty");
        for (int index = 0; index < clientId.length(); index++) {
            final char character = clientId.charAt(index);
            if (character < 0x20 || character > 0x7e) {
                throw new ValidateException(
                        "OAuth 2.x device authorization client identifier contains a character outside VSCHAR");
            }
        }
        Assert.notNull(scope, "OAuth 2.x device authorization scope container must not be null");
        scope = Optional.ofNullable(scope.getOrNull());
    }

}
