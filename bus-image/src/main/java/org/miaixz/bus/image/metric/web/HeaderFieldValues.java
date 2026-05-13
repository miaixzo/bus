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
package org.miaixz.bus.image.metric.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Parser for HTTP header field values with parameters and quoted strings.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HeaderFieldValues {

    /**
     * The parsed values value.
     */
    private final List<Map<String, String>> parsedValues;

    /**
     * Creates a new instance.
     *
     * @param headerFieldValue the header field value.
     */
    public HeaderFieldValues(String headerFieldValue) {
        Objects.requireNonNull(headerFieldValue, "Header field value cannot be null");
        this.parsedValues = parseHeaderFieldValue(headerFieldValue);
    }

    /**
     * Gets the values.
     *
     * @return the values.
     */
    public List<Map<String, String>> getValues() {
        return Collections.unmodifiableList(parsedValues);
    }

    /**
     * Determines whether key.
     *
     * @param key the key.
     * @return true if the condition is met; otherwise false.
     */
    public boolean hasKey(String key) {
        return parsedValues.stream().anyMatch(map -> map.containsKey(normalizeKey(key)));
    }

    /**
     * Gets the value.
     *
     * @param key the key.
     * @return the value.
     */
    public String getValue(String key) {
        String normalized = normalizeKey(key);
        return parsedValues.stream().map(map -> map.get(normalized)).filter(HeaderFieldValues::hasText).findFirst()
                .orElse(null);
    }

    /**
     * Gets the values.
     *
     * @param key the key.
     * @return the values.
     */
    public List<String> getValues(String key) {
        String normalized = normalizeKey(key);
        return parsedValues.stream().map(map -> map.get(normalized)).filter(HeaderFieldValues::hasText).toList();
    }

    /**
     * Parses the header field value.
     *
     * @param content the content.
     * @return the operation result.
     */
    private List<Map<String, String>> parseHeaderFieldValue(String content) {
        if (!hasText(content)) {
            return new ArrayList<>();
        }
        return new FieldValueParser(content).parseAll();
    }

    /**
     * Executes the normalize key operation.
     *
     * @param key the key.
     * @return the operation result.
     */
    private static String normalizeKey(String key) {
        return key == null ? "" : key.toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Determines whether text.
     *
     * @param value the value.
     * @return true if the condition is met; otherwise false.
     */
    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * Represents the FieldValueParser type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static final class FieldValueParser {

        /**
         * The chars value.
         */
        private char[] chars;

        /**
         * The position value.
         */
        private int position;

        /**
         * The start value.
         */
        private int start;

        /**
         * The end value.
         */
        private int end;

        /**
         * Creates a new instance.
         *
         * @param content the content.
         */
        FieldValueParser(String content) {
            this.chars = content.toCharArray();
        }

        /**
         * Parses the all.
         *
         * @return the operation result.
         */
        List<Map<String, String>> parseAll() {
            List<Map<String, String>> results = new ArrayList<>();
            for (String element : splitPreservingQuotes()) {
                Map<String, String> params = parseElement(element);
                if (!params.isEmpty()) {
                    results.add(params);
                }
            }
            return results;
        }

        /**
         * Executes the split preserving quotes operation.
         *
         * @return the operation result.
         */
        private String[] splitPreservingQuotes() {
            return new String(chars).split(",(?=(?:[^\"]*\"[^\"]*\")*+[^\"]*$)");
        }

        /**
         * Parses the element.
         *
         * @param element the element.
         * @return the operation result.
         */
        private Map<String, String> parseElement(String element) {
            Map<String, String> params = new HashMap<>();
            this.chars = element.toCharArray();
            this.position = 0;

            while (hasMoreCharacters()) {
                String name = parseValue();
                String value = null;
                if (hasMoreCharacters() && chars[position] == '=') {
                    position++;
                    value = parseQuotedValue();
                }
                skipSeparator();
                if (hasText(name)) {
                    params.put(name.toLowerCase(java.util.Locale.ROOT), value);
                }
            }
            return params;
        }

        /**
         * Determines whether more characters.
         *
         * @return true if the condition is met; otherwise false.
         */
        private boolean hasMoreCharacters() {
            return position < chars.length;
        }

        /**
         * Parses the value.
         *
         * @return the operation result.
         */
        private String parseValue() {
            start = position;
            end = position;
            while (hasMoreCharacters()) {
                char c = chars[position];
                if (c == '=' || c == ';') {
                    break;
                }
                end++;
                position++;
            }
            return extractValue(false);
        }

        /**
         * Parses the quoted value.
         *
         * @return the operation result.
         */
        private String parseQuotedValue() {
            start = position;
            end = position;
            boolean quoted = false;
            boolean escaped = false;

            while (hasMoreCharacters()) {
                char c = chars[position];
                if (!quoted && c == ';') {
                    break;
                }
                if (!escaped && c == '"') {
                    quoted = !quoted;
                }
                escaped = !escaped && c == '\\';
                end++;
                position++;
            }
            return extractValue(true);
        }

        /**
         * Executes the extract value operation.
         *
         * @param quoted the quoted.
         * @return the operation result.
         */
        private String extractValue(boolean quoted) {
            while (start < end && Character.isWhitespace(chars[start])) {
                start++;
            }
            while (end > start && Character.isWhitespace(chars[end - 1])) {
                end--;
            }
            if (quoted && end - start >= 2 && chars[start] == '"' && chars[end - 1] == '"') {
                start++;
                end--;
            }
            return end > start ? new String(chars, start, end - start) : null;
        }

        /**
         * Executes the skip separator operation.
         */
        private void skipSeparator() {
            if (hasMoreCharacters() && chars[position] == ';') {
                position++;
            }
        }

    }

}
