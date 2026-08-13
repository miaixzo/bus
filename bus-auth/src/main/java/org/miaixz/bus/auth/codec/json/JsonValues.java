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
package org.miaixz.bus.auth.codec.json;

import java.util.*;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Reads bounded, strictly typed members from already validated JSON objects while leaving protocol error selection to
 * the caller.
 *
 * @author Kimi Liu
 */
public final class JsonValues {

    /**
     * Prevents construction of the stateless reader.
     */
    private JsonValues() {
        // No initialization required.
    }

    /**
     * Reads one required JSON object member.
     *
     * @param values source object
     * @param name   member name
     * @param error  protocol failure factory
     * @return nested object
     */
    public static Map<?, ?> object(
            final Map<?, ?> values,
            final String name,
            final Supplier<? extends RuntimeException> error) {
        final Object value = source(values).get(member(name));
        if (!(value instanceof Map<?, ?> object)) {
            throw failure(error);
        }
        return object;
    }

    /**
     * Reads one exact string, including the empty string.
     *
     * @param values       source object
     * @param name         member name
     * @param maximumBytes maximum UTF-8 bytes
     * @param error        protocol failure factory
     * @return exact string
     */
    public static String text(
            final Map<?, ?> values,
            final String name,
            final int maximumBytes,
            final Supplier<? extends RuntimeException> error) {
        final Object value = source(values).get(member(name));
        if (!(value instanceof String text) || bytes(text) > limit(maximumBytes)) {
            throw failure(error);
        }
        return text;
    }

    /**
     * Reads one required non-blank string.
     *
     * @param values       source object
     * @param name         member name
     * @param maximumBytes maximum UTF-8 bytes
     * @param error        protocol failure factory
     * @return non-blank string
     */
    public static String requiredText(
            final Map<?, ?> values,
            final String name,
            final int maximumBytes,
            final Supplier<? extends RuntimeException> error) {
        final String value = text(values, name, maximumBytes, error);
        if (value.isBlank()) {
            throw failure(error);
        }
        return value;
    }

    /**
     * Reads one optional non-blank string. Only an absent member is treated as absent.
     *
     * @param values       source object
     * @param name         member name
     * @param maximumBytes maximum UTF-8 bytes
     * @param error        protocol failure factory
     * @return absent or exact non-blank string
     */
    public static String optionalText(
            final Map<?, ?> values,
            final String name,
            final int maximumBytes,
            final Supplier<? extends RuntimeException> error) {
        final Map<?, ?> source = source(values);
        final String member = member(name);
        if (!source.containsKey(member)) {
            return null;
        }
        return requiredText(source, member, maximumBytes, error);
    }

    /**
     * Reads one exactly integral JSON number without floating-point truncation.
     *
     * @param values source object
     * @param name   member name
     * @param error  protocol failure factory
     * @return integral value
     */
    public static long integer(
            final Map<?, ?> values,
            final String name,
            final Supplier<? extends RuntimeException> error) {
        final Object value = source(values).get(member(name));
        if (!(value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long)) {
            throw failure(error);
        }
        return ((Number) value).longValue();
    }

    /**
     * Reads one non-empty ordered set of unique, non-blank strings.
     *
     * @param values           source object
     * @param name             member name
     * @param maximumItems     maximum item count
     * @param maximumItemBytes maximum UTF-8 bytes per item
     * @param error            protocol failure factory
     * @return immutable insertion-ordered strings
     */
    public static Set<String> stringSet(
            final Map<?, ?> values,
            final String name,
            final int maximumItems,
            final int maximumItemBytes,
            final Supplier<? extends RuntimeException> error) {
        final Object value = source(values).get(member(name));
        if (!(value instanceof List<?> list) || list.isEmpty() || list.size() > limit(maximumItems)) {
            throw failure(error);
        }
        final LinkedHashSet<String> result = new LinkedHashSet<>();
        for (final Object item : list) {
            if (!(item instanceof String text) || text.isBlank() || bytes(text) > limit(maximumItemBytes)
                    || !result.add(text)) {
                throw failure(error);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * Validates the source object contract.
     */
    private static Map<?, ?> source(final Map<?, ?> values) {
        return Assert.notNull(values, () -> new ValidateException("JSON object must not be null"));
    }

    /**
     * Validates a member name.
     */
    private static String member(final String name) {
        final String value = Assert.notNull(name, () -> new ValidateException("JSON member name must not be null"));
        if (value.isBlank()) {
            throw new ValidateException("JSON member name must not be blank");
        }
        return value;
    }

    /**
     * Validates one positive bound.
     */
    private static int limit(final int value) {
        if (value <= 0) {
            throw new ValidateException("JSON member limit must be positive");
        }
        return value;
    }

    /**
     * Returns the exact UTF-8 byte length.
     */
    private static int bytes(final String value) {
        return value.getBytes(Charset.UTF_8).length;
    }

    /**
     * Creates one caller-owned protocol failure.
     */
    private static RuntimeException failure(final Supplier<? extends RuntimeException> error) {
        final Supplier<? extends RuntimeException> factory = Assert
                .notNull(error, () -> new ValidateException("JSON error factory must not be null"));
        return Assert.notNull(factory.get(), () -> new ValidateException("JSON error factory returned null"));
    }

}
