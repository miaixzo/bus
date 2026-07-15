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
package org.miaixz.bus.fabric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;

/**
 * Immutable case-insensitive header collection with insertion-ordered multi-values.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Headers {

    /**
     * Header names and values stored as adjacent pairs.
     */
    private final String[] namesAndValues;

    /**
     * Creates immutable headers.
     *
     * @param namesAndValues source name/value pairs
     */
    private Headers(final String[] namesAndValues) {
        this(namesAndValues, true);
    }

    /**
     * Creates immutable headers.
     *
     * @param namesAndValues source name/value pairs
     * @param copy           whether to copy the array
     */
    private Headers(final String[] namesAndValues, final boolean copy) {
        this.namesAndValues = namesAndValues.length == 0 || !copy ? namesAndValues : namesAndValues.clone();
    }

    /**
     * Returns the empty headers.
     *
     * @return empty headers
     */
    public static Headers empty() {
        return Instances.get(Headers.class.getName() + ".empty", () -> new Headers(new String[0], false));
    }

    /**
     * Creates headers from alternating name/value pairs.
     *
     * @param namesAndValues alternating header names and values
     * @return immutable headers
     */
    public static Headers of(final String... namesAndValues) {
        if (namesAndValues == null) {
            throw new ValidateException("Header pairs must not be null");
        }
        if ((namesAndValues.length & 1) != 0) {
            throw new ValidateException("Header pairs must alternate names and values");
        }
        if (namesAndValues.length == 0) {
            return empty();
        }
        final Builder builder = builder();
        for (int i = 0; i < namesAndValues.length; i += 2) {
            final String name = namesAndValues[i] == null ? null : namesAndValues[i].trim();
            final String value = namesAndValues[i + 1] == null ? null : namesAndValues[i + 1].trim();
            builder.add(name, value);
        }
        return builder.build();
    }

    /**
     * Creates headers from a single-value map.
     *
     * @param headers source headers
     * @return immutable headers
     */
    public static Headers of(final Map<String, String> headers) {
        if (headers == null) {
            throw new ValidateException("Header map must not be null");
        }
        if (headers.isEmpty()) {
            return empty();
        }
        final Builder builder = builder();
        for (final Map.Entry<String, String> entry : headers.entrySet()) {
            final String name = entry.getKey() == null ? null : entry.getKey().trim();
            final String value = entry.getValue() == null ? null : entry.getValue().trim();
            builder.add(name, value);
        }
        return builder.build();
    }

    /**
     * Creates a headers builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the last value for a header name.
     *
     * @param name header name
     * @return last value or null
     */
    public String get(final String name) {
        for (int i = namesAndValues.length - 2; i >= 0; i -= 2) {
            if (name.equalsIgnoreCase(namesAndValues[i])) {
                return namesAndValues[i + 1];
            }
        }
        return null;
    }

    /**
     * Returns all values for a header name.
     *
     * @param name header name
     * @return immutable values
     */
    public List<String> values(final String name) {
        ArrayList<String> result = null;
        for (int i = 0; i < namesAndValues.length; i += 2) {
            if (name.equalsIgnoreCase(namesAndValues[i])) {
                if (result == null) {
                    result = new ArrayList<>(2);
                }
                result.add(namesAndValues[i + 1]);
            }
        }
        if (result == null) {
            return List.of();
        }
        return switch (result.size()) {
            case 0 -> List.of();
            case 1 -> List.of(result.get(0));
            default -> Collections.unmodifiableList(result);
        };
    }

    /**
     * Returns whether a header name exists.
     *
     * @param name header name
     * @return true when present
     */
    public boolean contains(final String name) {
        for (int i = 0; i < namesAndValues.length; i += 2) {
            if (name.equalsIgnoreCase(namesAndValues[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the header pair count.
     *
     * @return header pair count
     */
    public int size() {
        return namesAndValues.length / 2;
    }

    /**
     * Returns the header name at an index.
     *
     * @param index header index
     * @return header name
     */
    public String name(final int index) {
        return namesAndValues[index * 2];
    }

    /**
     * Returns the header value at an index.
     *
     * @param index header index
     * @return header value
     */
    public String value(final int index) {
        return namesAndValues[index * 2 + 1];
    }

    /**
     * Parses the Content-Length header.
     *
     * @return content length, or -1 when absent
     */
    public int contentLength() {
        final String value = get(HTTP.CONTENT_LENGTH);
        if (value == null) {
            return -1;
        }
        try {
            final int length = Integer.parseInt(value);
            if (length < 0) {
                throw new ProtocolException("Content-Length must be non-negative");
            }
            return length;
        } catch (final NumberFormatException e) {
            throw new ProtocolException("Invalid Content-Length", e);
        }
    }

    /**
     * Returns headers with a single replacement value for a name.
     *
     * @param name  header name
     * @param value header value
     * @return updated headers
     */
    public Headers with(final String name, final String value) {
        return new Builder(this).set(name, value).build();
    }

    /**
     * Returns headers without a name.
     *
     * @param name header name
     * @return updated headers
     */
    public Headers without(final String name) {
        return new Builder(this).remove(name).build();
    }

    /**
     * Returns a mutable builder initialized with this header snapshot.
     *
     * @return builder
     */
    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * Returns an immutable map snapshot.
     *
     * @return immutable headers map
     */
    public Map<String, List<String>> asMap() {
        final LinkedHashMap<String, String> names = new LinkedHashMap<>();
        final LinkedHashMap<String, ArrayList<String>> values = new LinkedHashMap<>();
        for (int i = 0; i < namesAndValues.length; i += 2) {
            final String key = namesAndValues[i].toLowerCase(Locale.ROOT);
            names.putIfAbsent(key, namesAndValues[i]);
            values.computeIfAbsent(key, ignored -> new ArrayList<>(2)).add(namesAndValues[i + 1]);
        }
        final LinkedHashMap<String, List<String>> copy = new LinkedHashMap<>();
        for (final Map.Entry<String, ArrayList<String>> entry : values.entrySet()) {
            copy.put(names.get(entry.getKey()), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Validates a header name.
     *
     * @param name header name
     * @return valid name
     */
    private static String validateName(final String name) {
        if (name == null || name.isEmpty()) {
            throw new ValidateException("Header name must be non-blank and single-line");
        }
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) <= Symbol.C_SPACE) {
                throw new ValidateException("Header name must be non-blank and single-line");
            }
        }
        return name;
    }

    /**
     * Validates a header value.
     *
     * @param value header value
     * @return validated value
     */
    private static String validateValue(final String value) {
        if (value == null) {
            throw new ValidateException("Header value must be non-blank and single-line");
        }
        for (int i = 0; i < value.length(); i++) {
            final char current = value.charAt(i);
            if (current == Symbol.C_CR || current == Symbol.C_LF) {
                throw new ValidateException("Header value must be non-blank and single-line");
            }
        }
        return value;
    }

    /**
     * Builder for immutable headers.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Mutable name/value pairs.
         */
        private String[] namesAndValues;

        /**
         * Number of used slots.
         */
        private int size;

        /**
         * Creates an empty builder.
         */
        private Builder() {
            this.namesAndValues = new String[20];
        }

        /**
         * Creates a builder from existing headers.
         *
         * @param headers source headers
         */
        private Builder(final Headers headers) {
            this.size = headers.namesAndValues.length;
            this.namesAndValues = size == 0 ? new String[20] : headers.namesAndValues.clone();
        }

        /**
         * Appends a value to a header name.
         *
         * @param name  header name
         * @param value header value
         * @return this builder
         */
        public Builder add(final String name, final String value) {
            final String checkedName = validateName(name);
            final String checkedValue = validateValue(value);
            ensure(2);
            namesAndValues[size++] = checkedName;
            namesAndValues[size++] = checkedValue;
            return this;
        }

        /**
         * Replaces all values for a header name.
         *
         * @param name  header name
         * @param value header value
         * @return this builder
         */
        public Builder set(final String name, final String value) {
            final String checkedName = validateName(name);
            final String checkedValue = validateValue(value);
            removeAll(checkedName);
            ensure(2);
            namesAndValues[size++] = checkedName;
            namesAndValues[size++] = checkedValue;
            return this;
        }

        /**
         * Removes all values for a header name.
         *
         * @param name header name
         * @return this builder
         */
        public Builder remove(final String name) {
            removeAll(validateName(name));
            return this;
        }

        /**
         * Builds immutable headers.
         *
         * @return immutable headers
         */
        public Headers build() {
            if (size == 0) {
                return empty();
            }
            return new Headers(Arrays.copyOf(namesAndValues, size), false);
        }

        /**
         * Removes all values for a validated name.
         *
         * @param name validated header name
         */
        private void removeAll(final String name) {
            for (int i = 0; i < size; i += 2) {
                if (namesAndValues[i].equalsIgnoreCase(name)) {
                    final int moved = size - i - 2;
                    if (moved > 0) {
                        System.arraycopy(namesAndValues, i + 2, namesAndValues, i, moved);
                    }
                    size -= 2;
                    namesAndValues[size] = null;
                    namesAndValues[size + 1] = null;
                    i -= 2;
                }
            }
        }

        /**
         * Ensures free slots.
         *
         * @param extra extra slot count
         */
        private void ensure(final int extra) {
            final int required = size + extra;
            if (required <= namesAndValues.length) {
                return;
            }
            int capacity = namesAndValues.length == 0 ? 20 : namesAndValues.length << 1;
            while (capacity < required) {
                capacity <<= 1;
            }
            namesAndValues = Arrays.copyOf(namesAndValues, capacity);
        }

    }

}
