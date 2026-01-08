/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.map;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

import org.miaixz.bus.core.text.StringJoiner;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A utility for joining {@link Map} entries into a formatted string. This class provides a fluent API for concatenating
 * key-value pairs with specified delimiters, similar to how {@link StringJoiner} works for sequences.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapJoiner {

    /**
     * The underlying joiner used to concatenate formatted map entries.
     */
    private final StringJoiner joiner;
    /**
     * The separator placed between each key and its corresponding value.
     */
    private final String keyValueSeparator;

    /**
     * Constructs a new {@code MapJoiner}.
     *
     * @param joiner            The {@link StringJoiner} used to combine the final key-value strings.
     * @param keyValueSeparator The separator to place between a key and its value (e.g., "=").
     */
    public MapJoiner(final StringJoiner joiner, final String keyValueSeparator) {
        this.joiner = joiner;
        this.keyValueSeparator = keyValueSeparator;
    }

    /**
     * Creates a new {@code MapJoiner} with specified separators.
     *
     * @param separator         The separator to place between each map entry (e.g., "&amp;").
     * @param keyValueSeparator The separator to place between a key and its value (e.g., "=").
     * @return A new {@code MapJoiner} instance.
     */
    public static MapJoiner of(final String separator, final String keyValueSeparator) {
        return of(StringJoiner.of(separator), keyValueSeparator);
    }

    /**
     * Creates a new {@code MapJoiner} with a custom {@link StringJoiner}.
     *
     * @param joiner            The {@link StringJoiner} to use for concatenating entries.
     * @param keyValueSeparator The separator to place between a key and its value.
     * @return A new {@code MapJoiner} instance.
     */
    public static MapJoiner of(final StringJoiner joiner, final String keyValueSeparator) {
        return new MapJoiner(joiner, keyValueSeparator);
    }

    /**
     * Appends all entries from the given {@link Map}, optionally filtering them.
     *
     * @param <K>       The type of keys in the map.
     * @param <V>       The type of values in the map.
     * @param map       The map whose entries are to be appended.
     * @param predicate A {@link Predicate} to filter which entries are included. If {@code null}, all entries are
     *                  appended.
     * @return This {@code MapJoiner} instance for method chaining.
     */
    public <K, V> MapJoiner append(final Map<K, V> map, final Predicate<Map.Entry<K, V>> predicate) {
        if (map == null) {
            return this;
        }
        return append(map.entrySet().iterator(), predicate);
    }

    /**
     * Appends all entries from the given {@link Iterator}, optionally filtering them.
     *
     * @param <K>       The type of keys in the entries.
     * @param <V>       The type of values in the entries.
     * @param parts     An iterator over the {@link Map.Entry} objects to append.
     * @param predicate A {@link Predicate} to filter which entries are included. If {@code null}, all entries are
     *                  appended.
     * @return This {@code MapJoiner} instance for method chaining.
     */
    public <K, V> MapJoiner append(
            final Iterator<? extends Map.Entry<K, V>> parts,
            final Predicate<Map.Entry<K, V>> predicate) {
        if (null == parts) {
            return this;
        }

        while (parts.hasNext()) {
            Map.Entry<K, V> entry = parts.next();
            if (null == predicate || predicate.test(entry)) {
                joiner.append(StringJoiner.of(this.keyValueSeparator).append(entry.getKey()).append(entry.getValue()));
            }
        }

        return this;
    }

    /**
     * Appends additional string parts to the joiner. These parts are added directly without key-value separation.
     *
     * @param params An array of strings to append.
     * @return This {@code MapJoiner} instance for method chaining.
     */
    public MapJoiner append(final String... params) {
        if (ArrayKit.isNotEmpty(params)) {
            joiner.append(StringKit.concat(false, params));
        }
        return this;
    }

    /**
     * Returns the final joined string.
     *
     * @return The string representation of the joined map entries.
     */
    @Override
    public String toString() {
        return joiner.toString();
    }

}
