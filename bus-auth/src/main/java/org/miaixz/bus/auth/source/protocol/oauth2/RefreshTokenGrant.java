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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Represents an RFC 6749 refresh-token grant with an optional narrowed scope.
 * <p>
 * The refresh token is sensitive authorization material and is never exposed by the diagnostic representation.
 * </p>
 *
 * @param refreshToken refresh token previously issued to the client
 * @param scope        optional requested scope that cannot exceed the original grant
 * @author Kimi Liu
 */
public record RefreshTokenGrant(String refreshToken, Optional<Scope> scope) implements TokenRequest.Grant {

    /**
     * Creates and validates an immutable refresh-token grant.
     *
     * @throws IllegalArgumentException if the token or scope container is {@code null}, or the token is empty
     * @throws ValidateException        if the token contains a character outside RFC 6749 {@code VSCHAR}
     */
    public RefreshTokenGrant {
        Assert.notEmpty(refreshToken, "OAuth 2.x refresh token must not be empty");
        for (int index = 0; index < refreshToken.length(); index++) {
            final char character = refreshToken.charAt(index);
            if (character < 0x20 || character > 0x7e) {
                throw new ValidateException("OAuth 2.x refresh token contains a character outside RFC 6749 VSCHAR");
            }
        }
        Assert.notNull(scope, "OAuth 2.x refresh-token scope container must not be null");
        scope = Optional.ofNullable(scope.getOrNull());
    }

    /**
     * Returns a diagnostic representation without refresh-token material.
     *
     * @return redacted grant summary
     */
    @Override
    public String toString() {
        return "RefreshTokenGrant[refreshToken=[REDACTED], scope=" + scope + Symbol.BRACKET_RIGHT;
    }

}
