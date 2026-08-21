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
package org.miaixz.bus.auth.protocol.oidc;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Carries an OpenID Connect ID Token in JOSE compact serialization.
 * <p>
 * This sensitive wire value deliberately has no parsing or verification behavior. {@code IdTokenCodec} delegates JOSE
 * processing to the shared JWT services, and {@code IdTokenVerifier} applies the OpenID Connect validation profile.
 * </p>
 *
 * @param compact sensitive three-segment JWS or five-segment JWE representation
 * @author Kimi Liu
 */
public record IdToken(String compact) {

    /**
     * Creates an immutable ID Token after validating only compact serialization shape.
     *
     * @throws IllegalArgumentException if {@code compact} is {@code null} or blank
     * @throws ValidateException        if compact serialization has an invalid segment count or base64url shape
     */
    public IdToken {
        compact = Assert.notBlank(compact, "OpenID Connect ID Token must not be blank");
        final String[] segments = compact.split("\\.", -1);
        if (segments.length != 3 && segments.length != 5) {
            throw new ValidateException("OpenID Connect ID Token must contain three JWS or five JWE segments");
        }
        for (String segment : segments) {
            requireBase64Url(segment);
        }
        if (segments.length == 3 && (segments[0].isEmpty() || segments[1].isEmpty() || segments[2].isEmpty())) {
            throw new ValidateException("OpenID Connect signed ID Token segments must not be empty");
        }
        if (segments.length == 5
                && (segments[0].isEmpty() || segments[2].isEmpty() || segments[3].isEmpty() || segments[4].isEmpty())) {
            throw new ValidateException("OpenID Connect encrypted ID Token required segments must not be empty");
        }
    }

    /**
     * Validates one compact segment without decoding it.
     *
     * @param value possibly empty base64url segment
     * @throws ValidateException if a character is outside the unpadded base64url alphabet
     */
    private static void requireBase64Url(final String value) {
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (!((character >= Symbol.C_UPPER_A && character <= Symbol.C_UPPER_Z)
                    || (character >= Symbol.C_LOWER_A && character <= Symbol.C_LOWER_Z)
                    || (character >= Symbol.C_ZERO && character <= Symbol.C_NINE) || character == Symbol.C_MINUS
                    || character == Symbol.C_UNDERLINE)) {
                throw new ValidateException("OpenID Connect ID Token segments must use unpadded base64url encoding");
            }
        }
    }

    /**
     * Returns a fixed diagnostic representation without bearer material.
     *
     * @return redacted ID Token label
     */
    @Override
    public String toString() {
        return "IdToken[compact=[REDACTED]]";
    }

}
