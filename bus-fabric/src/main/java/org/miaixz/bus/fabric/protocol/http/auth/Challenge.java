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
package org.miaixz.bus.fabric.protocol.http.auth;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Parsed HTTP authentication challenge.
 *
 * @param scheme     authentication scheme
 * @param realm      optional realm
 * @param parameters ordered parameters
 * @author Kimi Liu
 * @since Java 21+
 */
public record Challenge(String scheme, String realm, Map<String, String> parameters) {

    /**
     * Creates a normalized challenge.
     *
     * @param scheme     authentication scheme
     * @param realm      optional realm
     * @param parameters ordered parameters
     */
    public Challenge {
        scheme = normalizeToken(scheme, "Challenge scheme");
        if (StringKit.containsAny(realm, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Challenge realm must be single-line");
        }
        final LinkedHashMap<String, String> copy = new LinkedHashMap<>();
        if (parameters != null) {
            for (final Map.Entry<String, String> entry : parameters.entrySet()) {
                copy.put(
                        normalizeToken(entry.getKey(), "Challenge parameter name"),
                        validateValue(entry.getValue(), "Challenge parameter value"));
            }
        }
        parameters = Collections.unmodifiableMap(copy);
    }

    /**
     * Parses a challenge header.
     *
     * @param header header value
     * @return parsed challenge
     */
    public static Challenge parse(final String header) {
        final String value = validateValue(header, "Challenge header").trim();
        final int space = firstSpace(value);
        final String scheme = space < 0 ? value : value.substring(0, space);
        final LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
        if (space >= 0) {
            parseParameters(value.substring(space + 1).trim(), parameters);
        }
        return new Challenge(scheme, parameters.get("realm"), parameters);
    }

    /**
     * Returns immutable challenge parameters.
     *
     * @return challenge parameters
     */
    @Override
    public Map<String, String> parameters() {
        return parameters;
    }

    /**
     * Parses comma separated auth parameters.
     *
     * @param source     parameter source
     * @param parameters target parameters
     */
    private static void parseParameters(final String source, final LinkedHashMap<String, String> parameters) {
        int index = 0;
        while (index < source.length()) {
            index = skipWhitespaceAndComma(source, index);
            if (index >= source.length()) {
                return;
            }
            final int equals = source.indexOf(Symbol.C_EQUAL, index);
            if (equals < 0) {
                throw new ValidateException("Challenge parameter must contain Symbol.C_EQUAL");
            }
            final String name = normalizeToken(source.substring(index, equals).trim(), "Challenge parameter name");
            final ValueResult result = readValue(source, equals + 1);
            parameters.put(name, result.value());
            index = result.next();
        }
    }

    /**
     * Reads one parameter value.
     *
     * @param source full parameter text containing the value
     * @param index  start index
     * @return value result
     */
    private static ValueResult readValue(final String source, final int index) {
        int current = skipWhitespace(source, index);
        if (current >= source.length()) {
            throw new ValidateException("Challenge parameter value must not be missing");
        }
        if (source.charAt(current) == Symbol.C_DOUBLE_QUOTES) {
            return readQuoted(source, current + 1);
        }
        final int comma = source.indexOf(Symbol.C_COMMA, current);
        final int end = comma < 0 ? source.length() : comma;
        final String value = validateValue(source.substring(current, end).trim(), "Challenge parameter value");
        return new ValueResult(value, end);
    }

    /**
     * Reads a quoted-string value.
     *
     * @param source full parameter text containing the quoted value
     * @param index  first content index
     * @return value result
     */
    private static ValueResult readQuoted(final String source, final int index) {
        final StringBuilder builder = new StringBuilder();
        int current = index;
        while (current < source.length()) {
            final char value = source.charAt(current++);
            if (value == Symbol.C_DOUBLE_QUOTES) {
                return new ValueResult(validateValue(builder.toString(), "Challenge parameter value"),
                        skipUntilComma(source, current));
            }
            if (value == Symbol.C_BACKSLASH) {
                if (current >= source.length()) {
                    throw new ProtocolException("Challenge quoted value has invalid escape");
                }
                final char escaped = source.charAt(current++);
                if (escaped != Symbol.C_BACKSLASH && escaped != Symbol.C_DOUBLE_QUOTES) {
                    throw new ProtocolException("Challenge quoted value has invalid escape");
                }
                builder.append(escaped);
            } else {
                builder.append(value);
            }
        }
        throw new ValidateException("Challenge quoted value must be closed");
    }

    /**
     * Finds the first whitespace.
     *
     * @param value normalized challenge header text
     * @return first whitespace index, or {@code -1} when the scheme stands alone
     */
    private static int firstSpace(final String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isWhitespace(value.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Skips whitespace and comma separators.
     *
     * @param value parameter text being scanned
     * @param index starting character index
     * @return next index
     */
    private static int skipWhitespaceAndComma(final String value, final int index) {
        int current = index;
        while (current < value.length()
                && (Character.isWhitespace(value.charAt(current)) || value.charAt(current) == Symbol.C_COMMA)) {
            current++;
        }
        return current;
    }

    /**
     * Skips whitespace.
     *
     * @param value parameter text being scanned
     * @param index starting character index
     * @return next index
     */
    private static int skipWhitespace(final String value, final int index) {
        int current = index;
        while (current < value.length() && Character.isWhitespace(value.charAt(current))) {
            current++;
        }
        return current;
    }

    /**
     * Skips trailing whitespace before the next comma.
     *
     * @param value parameter text following a closing quote
     * @param index first character after the closing quote
     * @return comma or end index
     */
    private static int skipUntilComma(final String value, final int index) {
        int current = skipWhitespace(value, index);
        if (current < value.length() && value.charAt(current) != Symbol.C_COMMA) {
            throw new ValidateException("Challenge parameter separator must be comma");
        }
        return current;
    }

    /**
     * Normalizes a token.
     *
     * @param value candidate authentication scheme or parameter name
     * @param name  field name
     * @return normalized token
     */
    private static String normalizeToken(final String value, final String name) {
        final String checked = validateValue(value, name);
        for (int i = 0; i < checked.length(); i++) {
            if (!isToken(checked.charAt(i))) {
                throw new ValidateException(name + " must be a token");
            }
        }
        return checked.toLowerCase(Locale.ROOT);
    }

    /**
     * Validates a single-line value.
     *
     * @param value candidate challenge header or parameter value
     * @param name  field name
     * @return validated non-blank single-line value
     */
    private static String validateValue(final String value, final String name) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException(name + " must be non-blank and single-line");
        }
        return value;
    }

    /**
     * Returns whether a character is allowed in an auth token.
     *
     * @param value character
     * @return true when token character
     */
    private static boolean isToken(final char value) {
        return Character.isLetterOrDigit(value) || (Symbol.NOT + Symbol.HASH + Symbol.DOLLAR + Symbol.PERCENT
                + Symbol.AND + Symbol.SINGLE_QUOTE + Symbol.STAR + Symbol.PLUS + Symbol.MINUS + Symbol.DOT
                + Symbol.CARET + Symbol.UNDERLINE + "`" + Symbol.OR + Symbol.TILDE).indexOf(value) >= 0;
    }

    /**
     * Parsed value and next index.
     *
     * @param value parsed value
     * @param next  next index
     */
    private record ValueResult(String value, int next) {

    }

}
