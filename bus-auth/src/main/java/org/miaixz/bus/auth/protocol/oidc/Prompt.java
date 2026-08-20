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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Represents the ordered, extensible OpenID Connect {@code prompt} parameter value.
 *
 * @param values ordered unique prompt registration names
 * @author Kimi Liu
 */
public record Prompt(List<String> values) {

    /**
     * Requests interactive resource-owner login.
     */
    public static final Prompt LOGIN = new Prompt(List.of("login"));

    /**
     * Prohibits user-interface display and must occur alone.
     */
    public static final Prompt NONE = new Prompt(List.of("none"));

    /**
     * Requests explicit resource-owner consent.
     */
    public static final Prompt CONSENT = new Prompt(List.of("consent"));

    /**
     * Requests account selection by the resource owner.
     */
    public static final Prompt SELECT_ACCOUNT = new Prompt(List.of("select_account"));

    /**
     * Creates and validates an immutable prompt sequence.
     *
     * @throws IllegalArgumentException if values or an entry is {@code null}, or the list is empty
     * @throws ValidateException        if a value has invalid syntax, is duplicated, or none is combined with another
     *                                  value
     */
    public Prompt {
        Assert.notEmpty(values, "OpenID Connect prompt values must not be empty");
        final Set<String> unique = new HashSet<>(values.size());
        for (String value : values) {
            validate(value);
            if (!unique.add(value)) {
                throw new ValidateException("OpenID Connect prompt values must not contain duplicates");
            }
        }
        if (values.size() > 1 && unique.contains("none")) {
            throw new ValidateException("OpenID Connect prompt none must not be combined with another value");
        }
        values = List.copyOf(values);
    }

    /**
     * Parses a strict single-space-delimited prompt parameter.
     *
     * @param value complete prompt wire value
     * @return validated immutable prompt sequence
     */
    public static Prompt parse(final String value) {
        Assert.notEmpty(value, "OpenID Connect prompt value must not be empty");
        return new Prompt(List.of(value.split(Symbol.SPACE, -1)));
    }

    /**
     * Validates one extensible prompt registration name.
     *
     * @param value candidate prompt value
     * @throws IllegalArgumentException if value is null or empty
     * @throws ValidateException        if a character is outside the OAuth registration-name grammar
     */
    private static void validate(final String value) {
        Assert.notEmpty(value, "OpenID Connect prompt registration must not be empty");
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            final boolean valid = character >= 'A' && character <= 'Z' || character >= 'a' && character <= 'z'
                    || character >= Symbol.C_ZERO && character <= Symbol.C_NINE || character == Symbol.C_MINUS
                    || character == Symbol.C_DOT || character == Symbol.C_UNDERLINE;
            if (!valid) {
                throw new ValidateException("OpenID Connect prompt contains an invalid registration-name character");
            }
        }
    }

    /**
     * Formats this prompt sequence with the required single ASCII-space delimiter.
     *
     * @return canonical prompt wire value
     */
    public String format() {
        return String.join(Symbol.SPACE, values);
    }

}
