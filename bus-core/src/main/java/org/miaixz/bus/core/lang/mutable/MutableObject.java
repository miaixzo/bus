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
package org.miaixz.bus.core.lang.mutable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * A mutable {@code Object} wrapper.
 *
 * @param <T> The type of the mutable object.
 * @author Kimi Liu
 * @since Java 17+
 */
public class MutableObject<T> implements Mutable<T>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852270938613L;

    /**
     * The mutable value.
     */
    private T value;

    /**
     * Constructs a new MutableObject with a {@code null} value.
     */
    public MutableObject() {

    }

    /**
     * Constructs a new MutableObject with the specified value.
     *
     * @param value The initial value.
     */
    public MutableObject(final T value) {
        this.value = value;
    }

    /**
     * Creates a new {@code MutableObject}.
     *
     * @param value The value to wrap.
     * @param <T>   The type of the value.
     * @return A new {@code MutableObject} instance.
     */
    public static <T> MutableObject<T> of(final T value) {
        return new MutableObject<>(value);
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    @Override
    public T get() {
        return this.value;
    }

    /**
     * Set method.
     */
    @Override
    public void set(final T value) {
        this.value = value;
    }

    /**
     * Checks if this object equals another object.
     *
     * @param object the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }
        if (this == object) {
            return true;
        }
        if (this.getClass() == object.getClass()) {
            final MutableObject<?> that = (MutableObject<?>) object;
            return ObjectKit.equals(this.value, that.value);
        }
        return false;
    }

    /**
     * Returns the hash code for this MutableObject.
     *
     * @return The hash code based on the current value.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    /**
     * Returns the string representation of this MutableObject.
     *
     * @return The string representation of the current value, or "null" if the value is null.
     */
    @Override
    public String toString() {
        return value == null ? "null" : value.toString();
    }

}
