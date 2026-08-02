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
package org.miaixz.bus.sensitive;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.logger.Loggable;
import org.miaixz.bus.logger.Operator;

/**
 * Redacts structured sensitive values from logger arguments while preserving provider-side formatting.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Sanitizer implements Operator {

    /**
     * Stable replacement used for every protected value.
     */
    public static final String REDACTED = Symbol.BRACKET_LEFT + "REDACTED" + Symbol.BRACKET_RIGHT;

    /**
     * Maximum recursive depth used for nested collection arguments.
     */
    private static final int MAX_DEPTH = 16;

    /**
     * Normalized names whose corresponding values must never reach a logging provider.
     */
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            Http.Header.AUTHORIZATION.toLowerCase(Locale.ROOT),
            normalize(Http.Header.PROXY_AUTHORIZATION),
            Http.Header.COOKIE.toLowerCase(Locale.ROOT),
            normalize(Http.Header.SET_COOKIE),
            "password",
            "passwd",
            "secret",
            "clientsecret",
            "token",
            "accesstoken",
            "refreshtoken",
            "apikey",
            "privatekey",
            "credential",
            "credentials");

    /**
     * Creates an independent sensitive log operator.
     */
    public Sanitizer() {
        // No initialization required.
    }

    /**
     * Redacts placeholder arguments identified by their adjacent field names and sanitizes nested maps.
     *
     * @param loggable current loggable data
     * @return sanitized loggable data
     */
    @Override
    public Loggable apply(Loggable loggable) {
        Object[] arguments = loggable.arguments();
        String format = loggable.format();
        int searchIndex = 0;
        for (int argumentIndex = 0; argumentIndex < arguments.length; argumentIndex++) {
            int placeholderIndex = findPlaceholder(format, searchIndex);
            String key = placeholderIndex < 0 ? null : extractKey(format, placeholderIndex);
            arguments[argumentIndex] = sanitize(key, arguments[argumentIndex], 0);
            if (placeholderIndex >= 0) {
                searchIndex = placeholderIndex + 2;
            }
        }
        return new Loggable(loggable.level(), loggable.throwable(), format, arguments);
    }

    /**
     * Returns whether the supplied field name is covered by the sensitive-key policy.
     *
     * @param key field name
     * @return {@code true} when the corresponding value must be redacted
     */
    public boolean isSensitive(String key) {
        return SENSITIVE_KEYS.contains(normalize(key));
    }

    /**
     * Sanitizes one named value for callers that assemble structured diagnostic data.
     *
     * @param key   field name
     * @param value field value
     * @return sanitized value
     */
    public Object sanitize(String key, Object value) {
        return sanitize(key, value, 0);
    }

    /**
     * Redacts one named value supplied by a structured diagnostic producer.
     *
     * @param key   diagnostic field name
     * @param value diagnostic field value
     * @return sanitized diagnostic value
     */
    @Override
    public Object applyValue(String key, Object value) {
        return sanitize(key, value);
    }

    /**
     * Finds the next unescaped provider placeholder.
     *
     * @param format      message format
     * @param searchIndex starting offset
     * @return placeholder offset, or {@code -1}
     */
    private int findPlaceholder(String format, int searchIndex) {
        if (format == null) {
            return -1;
        }
        int index = Math.max(0, searchIndex);
        while ((index = format.indexOf(Symbol.BRACE_LEFT + Symbol.BRACE_RIGHT, index)) >= 0) {
            if (index == 0 || format.charAt(index - 1) != Symbol.C_BACKSLASH) {
                return index;
            }
            index += 2;
        }
        return -1;
    }

    /**
     * Extracts a field name from the assignment immediately preceding a placeholder.
     *
     * @param format           message format
     * @param placeholderIndex placeholder offset
     * @return field name, or {@code null} when the placeholder is positional only
     */
    private String extractKey(String format, int placeholderIndex) {
        int index = placeholderIndex - 1;
        while (index >= 0 && Character.isWhitespace(format.charAt(index))) {
            index--;
        }
        if (index < 0 || (format.charAt(index) != Symbol.C_EQUAL && format.charAt(index) != Symbol.C_COLON)) {
            return null;
        }
        index--;
        while (index >= 0 && Character.isWhitespace(format.charAt(index))) {
            index--;
        }
        int end = index + 1;
        while (index >= 0 && isKeyCharacter(format.charAt(index))) {
            index--;
        }
        return end == index + 1 ? null : format.substring(index + 1, end);
    }

    /**
     * Returns whether a character may form part of a structured field name.
     *
     * @param character candidate character
     * @return {@code true} for supported field-name characters
     */
    private boolean isKeyCharacter(char character) {
        return Character.isLetterOrDigit(character) || character == Symbol.C_UNDERLINE || character == Symbol.C_MINUS
                || character == Symbol.C_DOT;
    }

    /**
     * Sanitizes nested structured values without reflecting over arbitrary application objects.
     *
     * @param key   owning field name
     * @param value field value
     * @param depth current recursive depth
     * @return sanitized value
     */
    private Object sanitize(String key, Object value, int depth) {
        if (isSensitive(key)) {
            return REDACTED;
        }
        if (value == null || depth >= MAX_DEPTH) {
            return value;
        }
        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> sanitized = new LinkedHashMap<>();
            map.forEach((nestedKey, nestedValue) -> sanitized.put(
                    nestedKey,
                    sanitize(nestedKey == null ? null : nestedKey.toString(), nestedValue, depth + 1)));
            return sanitized;
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> sanitized = new ArrayList<>();
            iterable.forEach(item -> sanitized.add(sanitize(null, item, depth + 1)));
            return sanitized;
        }
        if (value instanceof Object[] array) {
            Object[] sanitized = array.clone();
            for (int index = 0; index < sanitized.length; index++) {
                sanitized[index] = sanitize(null, sanitized[index], depth + 1);
            }
            return sanitized;
        }
        return value;
    }

    /**
     * Normalizes common field-name styles into one policy lookup key.
     *
     * @param key field name
     * @return normalized field name
     */
    private static String normalize(String key) {
        if (key == null || key.isBlank()) {
            return Normal.EMPTY;
        }
        String lowerCase = key.trim().toLowerCase(Locale.ROOT);
        StringBuilder normalized = new StringBuilder(lowerCase.length());
        lowerCase.chars().filter(Character::isLetterOrDigit).forEach(normalized::appendCodePoint);
        return normalized.toString();
    }

}
