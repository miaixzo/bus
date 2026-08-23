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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Represents an ordered OAuth 2.x scope value using the RFC 6749 scope-token grammar.
 * <p>
 * Tokens remain case-sensitive and retain caller order. Duplicate tokens are rejected so encoders and policy checks
 * observe one deterministic representation without inventing business-specific scope names.
 * </p>
 *
 * @param values ordered, unique scope tokens
 * @author Kimi Liu
 */
public record Scope(List<String> values) {

    /**
     * Creates an immutable scope and validates every token against RFC 6749 Appendix A.4.
     *
     * @throws IllegalArgumentException if the list is {@code null}, empty, or contains {@code null}
     * @throws ValidateException        if a token is empty, duplicated, or contains a character outside
     *                                  {@code scope-token}
     */
    public Scope {
        Assert.notEmpty(values, "OAuth 2.x scope token list must not be empty");
        final Set<String> unique = new HashSet<>(values.size());
        for (String token : values) {
            Assert.notNull(token, "OAuth 2.x scope token must not be null");
            requireToken(token);
            if (!unique.add(token)) {
                throw new ValidateException("OAuth 2.x scope must not contain duplicate tokens");
            }
        }
        values = List.copyOf(values);
    }

    /**
     * Parses the standard space-delimited scope wire value.
     *
     * @param value complete scope parameter value
     * @return validated immutable scope
     * @throws IllegalArgumentException if the value is {@code null} or empty
     * @throws ValidateException        if spacing or a token violates RFC 6749
     */
    public static Scope parse(final String value) {
        Assert.notEmpty(value, "OAuth 2.x scope value must not be empty");
        return new Scope(List.of(value.split(Symbol.SPACE, -1)));
    }

    /**
     * Validates one non-empty scope token.
     *
     * @param token token to validate
     * @throws ValidateException if the token violates the RFC grammar
     */
    private static void requireToken(final String token) {
        if (token.isEmpty()) {
            throw new ValidateException("OAuth 2.x scope token must not be empty");
        }
        for (int index = 0; index < token.length(); index++) {
            final char character = token.charAt(index);
            if (character < 0x21 || character > 0x7e || character == 0x22 || character == 0x5c) {
                throw new ValidateException("OAuth 2.x scope token contains a character outside RFC 6749 scope-token");
            }
        }
    }

    /**
     * Formats this scope with the single ASCII space delimiter required by RFC 6749.
     *
     * @return canonical scope parameter value
     */
    public String format() {
        return String.join(Symbol.SPACE, values);
    }

}
