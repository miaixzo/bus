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
 * Represents an RFC 7662 token introspection request.
 * <p>
 * The token is opaque sensitive material. The optional token type hint can optimize lookup but never changes the
 * authorization server's obligation to inspect supported token types when the hint is absent or inaccurate.
 * </p>
 *
 * @param token         opaque token submitted for introspection
 * @param tokenTypeHint optional registered token type hint
 * @author Kimi Liu
 */
public record IntrospectionRequest(String token, Optional<String> tokenTypeHint) {

    /**
     * Creates and validates an immutable introspection request.
     *
     * @throws IllegalArgumentException if the token or hint container is {@code null}, or token is empty
     * @throws ValidateException        if a present hint is empty or contains an invalid registration-name character
     */
    public IntrospectionRequest {
        Assert.notEmpty(token, "OAuth 2.x introspection token must not be empty");
        Assert.notNull(tokenTypeHint, "OAuth 2.x introspection token type hint container must not be null");
        final String hint = tokenTypeHint.getOrNull();
        if (hint != null) {
            validateHint(hint);
        }
        tokenTypeHint = Optional.ofNullable(hint);
    }

    /**
     * Validates an extensible token type hint registration name.
     *
     * @param value hint wire value
     * @throws IllegalArgumentException if the hint is empty
     * @throws ValidateException        if a character is not valid in an OAuth registration name
     */
    private static void validateHint(final String value) {
        Assert.notEmpty(value, "OAuth 2.x introspection token type hint must not be empty when present");
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            final boolean valid = character >= Symbol.C_UPPER_A && character <= Symbol.C_UPPER_Z
                    || character >= Symbol.C_LOWER_A && character <= Symbol.C_LOWER_Z
                    || character >= Symbol.C_ZERO && character <= Symbol.C_NINE || character == Symbol.C_MINUS
                    || character == Symbol.C_DOT || character == Symbol.C_UNDERLINE;
            if (!valid) {
                throw new ValidateException(
                        "OAuth 2.x introspection token type hint contains an invalid registration-name character");
            }
        }
    }

    /**
     * Returns a diagnostic representation without the inspected token.
     *
     * @return redacted introspection request summary
     */
    @Override
    public String toString() {
        return "IntrospectionRequest[token=[REDACTED], tokenTypeHint=" + tokenTypeHint + Symbol.BRACKET_RIGHT;
    }

}
