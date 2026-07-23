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
import java.util.Map;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;

/**
 * Immutable case-insensitive header collection with insertion-ordered multi-values.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Headers {

    /**
     * Shared immutable empty headers.
     */
    private static final Headers EMPTY = new Headers(new String[0], false);

    /**
     * Header names and values stored as adjacent pairs.
     */
    private final String[] namesAndValues;

    /**
     * Precomputed lowercase ASCII name hashes indexed by header-pair position.
     */
    private final int[] nameHashes;

    /**
     * Lazily initialized immutable map view.
     */
    private volatile Map<String, List<String>> mapSnapshot;

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
        this.nameHashes = new int[this.namesAndValues.length >>> 1];
        for (int index = 0; index < this.namesAndValues.length; index += 2) {
            this.nameHashes[index >>> 1] = asciiLowerHash(this.namesAndValues[index]);
        }
    }

    /**
     * Returns the empty headers.
     *
     * @return empty headers
     */
    public static Headers empty() {
        return EMPTY;
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
        final String[] pairs = new String[namesAndValues.length];
        for (int i = 0; i < namesAndValues.length; i += 2) {
            pairs[i] = validateName(namesAndValues[i]);
            pairs[i + 1] = validateValue(namesAndValues[i + 1]);
        }
        return new Headers(pairs, false);
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
            builder.add(entry.getKey(), entry.getValue());
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
        final int hash = asciiLowerHash(name);
        for (int i = namesAndValues.length - 2; i >= 0; i -= 2) {
            if (nameHashes[i >>> 1] == hash && asciiEqualsIgnoreCase(name, namesAndValues[i])) {
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
        final int hash = asciiLowerHash(name);
        ArrayList<String> result = null;
        String first = null;
        for (int i = 0; i < namesAndValues.length; i += 2) {
            if (nameHashes[i >>> 1] == hash && asciiEqualsIgnoreCase(name, namesAndValues[i])) {
                if (first == null) {
                    first = namesAndValues[i + 1];
                } else {
                    if (result == null) {
                        result = new ArrayList<>(4);
                        result.add(first);
                    }
                    result.add(namesAndValues[i + 1]);
                }
            }
        }
        if (first == null) {
            return List.of();
        }
        return result == null ? List.of(first) : Collections.unmodifiableList(result);
    }

    /**
     * Returns whether a header name exists.
     *
     * @param name header name
     * @return true when present
     */
    public boolean contains(final String name) {
        final int hash = asciiLowerHash(name);
        for (int i = 0; i < namesAndValues.length; i += 2) {
            if (nameHashes[i >>> 1] == hash && asciiEqualsIgnoreCase(name, namesAndValues[i])) {
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
    public long contentLength() {
        String value = null;
        for (int index = 0; index < namesAndValues.length; index += 2) {
            if (asciiEqualsIgnoreCase(Http.Header.CONTENT_LENGTH, namesAndValues[index])) {
                if (value != null) {
                    throw new ProtocolException("Content-Length must be unique");
                }
                value = namesAndValues[index + 1];
            }
        }
        if (value == null) {
            return -1L;
        }
        if (value.isEmpty()) {
            throw new ProtocolException("Invalid Content-Length");
        }
        for (int i = 0; i < value.length(); i++) {
            final char current = value.charAt(i);
            if (current < '0' || current > '9') {
                throw new ProtocolException("Invalid Content-Length");
            }
        }
        try {
            return Long.parseLong(value);
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
        final String checkedName = validateName(name);
        final String checkedValue = validateValue(value);
        int matches = 0;
        int matchedIndex = -1;
        for (int index = 0; index < namesAndValues.length; index += 2) {
            if (asciiEqualsIgnoreCase(checkedName, namesAndValues[index])) {
                matches++;
                matchedIndex = index;
            }
        }
        if (matches == 0) {
            final String[] pairs = Arrays.copyOf(namesAndValues, namesAndValues.length + 2);
            pairs[namesAndValues.length] = checkedName;
            pairs[namesAndValues.length + 1] = checkedValue;
            return new Headers(pairs, false);
        }
        if (matches == 1 && matchedIndex == namesAndValues.length - 2
                && namesAndValues[matchedIndex].equals(checkedName)
                && namesAndValues[matchedIndex + 1].equals(checkedValue)) {
            return this;
        }
        final String[] pairs = new String[namesAndValues.length - matches * 2 + 2];
        int target = 0;
        for (int index = 0; index < namesAndValues.length; index += 2) {
            if (!asciiEqualsIgnoreCase(checkedName, namesAndValues[index])) {
                pairs[target++] = namesAndValues[index];
                pairs[target++] = namesAndValues[index + 1];
            }
        }
        pairs[target] = checkedName;
        pairs[target + 1] = checkedValue;
        return new Headers(pairs, false);
    }

    /**
     * Returns headers without a name.
     *
     * @param name header name
     * @return updated headers
     */
    public Headers without(final String name) {
        final String checkedName = validateName(name);
        int matches = 0;
        int matchedIndex = -1;
        for (int index = 0; index < namesAndValues.length; index += 2) {
            if (asciiEqualsIgnoreCase(checkedName, namesAndValues[index])) {
                matches++;
                matchedIndex = index;
            }
        }
        if (matches == 0) {
            return this;
        }
        if (matches * 2 == namesAndValues.length) {
            return empty();
        }
        final String[] pairs = new String[namesAndValues.length - matches * 2];
        if (matches == 1) {
            System.arraycopy(namesAndValues, 0, pairs, 0, matchedIndex);
            System.arraycopy(
                    namesAndValues,
                    matchedIndex + 2,
                    pairs,
                    matchedIndex,
                    namesAndValues.length - matchedIndex - 2);
            return new Headers(pairs, false);
        }
        int target = 0;
        for (int index = 0; index < namesAndValues.length; index += 2) {
            if (!asciiEqualsIgnoreCase(checkedName, namesAndValues[index])) {
                pairs[target++] = namesAndValues[index];
                pairs[target++] = namesAndValues[index + 1];
            }
        }
        return new Headers(pairs, false);
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
        Map<String, List<String>> snapshot = mapSnapshot;
        if (snapshot != null) {
            return snapshot;
        }
        synchronized (this) {
            snapshot = mapSnapshot;
            if (snapshot == null) {
                snapshot = buildMapSnapshot();
                mapSnapshot = snapshot;
            }
        }
        return snapshot;
    }

    /**
     * Builds the immutable map view from the immutable pair array.
     *
     * @return immutable map snapshot
     */
    private Map<String, List<String>> buildMapSnapshot() {
        if (namesAndValues.length == 0) {
            return Map.of();
        }
        final LinkedHashMap<String, List<String>> values = new LinkedHashMap<>(namesAndValues.length / 2);
        for (int i = 0; i < namesAndValues.length; i += 2) {
            final String name = namesAndValues[i];
            String existingName = null;
            for (final String candidate : values.keySet()) {
                if (asciiEqualsIgnoreCase(candidate, name)) {
                    existingName = candidate;
                    break;
                }
            }
            if (existingName == null) {
                values.put(name, List.of(namesAndValues[i + 1]));
                continue;
            }
            final List<String> existing = values.get(existingName);
            final ArrayList<String> combined = new ArrayList<>(existing.size() + 1);
            combined.addAll(existing);
            combined.add(namesAndValues[i + 1]);
            values.put(existingName, List.copyOf(combined));
        }
        return Map.copyOf(values);
    }

    /**
     * Validates a header name.
     *
     * @param name header name
     * @return valid name
     */
    private static String validateName(final String name) {
        if (name == null || name.isEmpty()) {
            throw new ValidateException("Header name must be a non-empty RFC token");
        }
        for (int i = 0; i < name.length(); i++) {
            if (!isTokenCharacter(name.charAt(i))) {
                throw new ValidateException("Header name must contain only RFC token characters");
            }
        }
        return name;
    }

    /**
     * Returns whether a character is an ASCII RFC tchar.
     *
     * @param value character
     * @return true for RFC tchar
     */
    private static boolean isTokenCharacter(final char value) {
        if ((value >= '0' && value <= '9') || (value >= 'A' && value <= 'Z') || (value >= 'a' && value <= 'z')) {
            return true;
        }
        return switch (value) {
            case '!', '#', '$', '%', '&', '\'', '*', '+', '-', '.', '^', '_', '`', '|', '~' -> true;
            default -> false;
        };
    }

    /**
     * Compares validated ASCII header names without Unicode case folding.
     *
     * @param left  first validated header name
     * @param right second validated header name
     * @return {@code true} when the names match ignoring ASCII case
     */
    private static boolean asciiEqualsIgnoreCase(final String left, final String right) {
        if (left == right) {
            return true;
        }
        final int length = left.length();
        if (length != right.length()) {
            return false;
        }
        for (int index = 0; index < length; index++) {
            final char a = left.charAt(index);
            final char b = right.charAt(index);
            if (a == b) {
                continue;
            }
            final char foldedA = a >= 'A' && a <= 'Z' ? (char) (a + ('a' - 'A')) : a;
            final char foldedB = b >= 'A' && b <= 'Z' ? (char) (b + ('a' - 'A')) : b;
            if (foldedA != foldedB) {
                return false;
            }
        }
        return true;
    }

    /**
     * Computes a case-insensitive ASCII hash without creating a lowercase string.
     *
     * @param value header name to hash, or null
     * @return lowercase ASCII hash, or zero for null
     */
    private static int asciiLowerHash(final String value) {
        if (value == null) {
            return 0;
        }
        int hash = 0;
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current >= 'A' && current <= 'Z') {
                current = (char) (current + ('a' - 'A'));
            }
            hash = 31 * hash + current;
        }
        return hash;
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
            if (current == '\0' || current == '\u007f' || (current < Symbol.C_SPACE && current != Symbol.C_TAB)) {
                throw new ValidateException("Header value contains a prohibited control character");
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
            this.namesAndValues = new String[8];
        }

        /**
         * Creates a builder from existing headers.
         *
         * @param headers source headers
         */
        private Builder(final Headers headers) {
            this.size = headers.namesAndValues.length;
            this.namesAndValues = Arrays.copyOf(headers.namesAndValues, Math.max(8, size + 2));
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
         * Reuses an immutable candidate when this builder contains the same ordered pairs.
         *
         * @param candidate immutable candidate, or null
         * @return candidate on an exact match, otherwise a newly built snapshot
         */
        public Headers buildOrReuse(final Headers candidate) {
            if (candidate != null && candidate.namesAndValues.length == size) {
                boolean same = true;
                for (int index = 0; index < size; index++) {
                    if (!namesAndValues[index].equals(candidate.namesAndValues[index])) {
                        same = false;
                        break;
                    }
                }
                if (same) {
                    return candidate;
                }
            }
            return build();
        }

        /**
         * Removes all values for a validated name.
         *
         * @param name validated header name
         */
        private void removeAll(final String name) {
            for (int i = 0; i < size; i += 2) {
                if (asciiEqualsIgnoreCase(namesAndValues[i], name)) {
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
