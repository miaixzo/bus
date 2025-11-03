/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.util.Map;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * An abstract base implementation of {@link Map.Entry}, inspired by Guava. This class provides default implementations
 * for {@link #equals(Object)}, {@link #hashCode()}, and {@link #toString()} methods, ensuring consistent behavior for
 * map entries. By default, the {@link #setValue(Object)} method throws an {@link UnsupportedOperationException}, making
 * entries immutable unless overridden by subclasses.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractEntry<K, V> implements Map.Entry<K, V> {

    /**
     * Sets the value associated with this entry. This default implementation throws an
     * {@link UnsupportedOperationException}, indicating that the entry is read-only. Subclasses can override this
     * method to provide mutable entry behavior.
     *
     * @param value The new value to be stored in this entry.
     * @return (Never returns, as an exception is always thrown).
     * @throws UnsupportedOperationException always, unless overridden by a subclass.
     */
    @Override
    public V setValue(final V value) {
        throw new UnsupportedOperationException("Entry is read only.");
    }

    /**
     * Compares the specified object with this entry for equality. Returns {@code true} if the given object is also a
     * map entry and the two entries represent the same mapping. More formally, two entries {@code e1} and {@code e2}
     * represent the same mapping if {@code (e1.getKey()==null ? e2.getKey()==null : e1.getKey().equals(e2.getKey()))}
     * and {@code (e1.getValue()==null ? e2.getValue()==null : e1.getValue().equals(e2.getValue()))}.
     *
     * @param object The object to be compared for equality with this map entry.
     * @return {@code true} if the specified object is equal to this map entry.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof Map.Entry<?, ?> that) {
            return ObjectKit.equals(this.getKey(), that.getKey()) && ObjectKit.equals(this.getValue(), that.getValue());
        }
        return false;
    }

    /**
     * Returns the hash code value for this map entry. The hash code of a map entry {@code e} is defined as:
     * {@code (e.getKey()==null ? 0 : e.getKey().hashCode()) ^ (e.getValue()==null ? 0 : e.getValue().hashCode())}.
     *
     * @return The hash code value for this map entry.
     */
    @Override
    public int hashCode() {
        final K k = getKey();
        final V v = getValue();
        return ((k == null) ? 0 : k.hashCode()) ^ ((v == null) ? 0 : v.hashCode());
    }

    /**
     * Returns a string representation of this map entry. The string representation consists of the key and value
     * separated by an equals sign ('='). For example, {@code "key=value"}.
     *
     * @return A string representation of this map entry.
     */
    @Override
    public String toString() {
        return getKey() + Symbol.EQUAL + getValue();
    }

}
