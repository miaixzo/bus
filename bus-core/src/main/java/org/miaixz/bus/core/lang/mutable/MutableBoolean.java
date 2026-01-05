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

/**
 * A mutable {@code boolean} wrapper.
 *
 * @author Kimi Liu
 * @see Boolean
 * @since Java 17+
 */
public class MutableBoolean implements Comparable<MutableBoolean>, Mutable<Boolean>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852269535978L;

    /**
     * The mutable value.
     */
    private boolean value;

    /**
     * Constructs a new MutableBoolean with a default value of {@code false}.
     */
    public MutableBoolean() {

    }

    /**
     * Constructs a new MutableBoolean with the specified value.
     *
     * @param value The initial value.
     */
    public MutableBoolean(final boolean value) {
        this.value = value;
    }

    /**
     * Constructs a new MutableBoolean with the value parsed from the specified String.
     *
     * @param value The initial value as a String.
     * @throws NumberFormatException if the String cannot be parsed to a boolean.
     */
    public MutableBoolean(final String value) throws NumberFormatException {
        this.value = Boolean.parseBoolean(value);
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    @Override
    public Boolean get() {
        return this.value;
    }

    /**
     * Sets the value.
     *
     * @param value The new value.
     */
    public void set(final boolean value) {
        this.value = value;
    }

    /**
     * Set method.
     */
    @Override
    public void set(final Boolean value) {
        this.value = value;
    }

    /**
     * Compares this object to the specified object. The objects are considered equal if all of the following conditions
     * are met:
     * <ol>
     * <li>The other object is not null.</li>
     * <li>The other object is an instance of {@code MutableBoolean}.</li>
     * <li>The boolean value of the other object is equal to this object's value.</li>
     * </ol>
     *
     * @param object The object to compare against.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof MutableBoolean) {
            return value == ((MutableBoolean) object).value;
        }
        return false;
    }

    /**
     * Returns the hash code value for this object.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return value ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode();
    }

    /**
     * Compares this {@code MutableBoolean} object with the specified {@code MutableBoolean} object.
     *
     * @param other The other {@code MutableBoolean} object to compare against.
     * @return 0 if the values are equal, a negative integer if this value is {@code false} and the other value is
     *         {@code true}, or a positive integer if this value is {@code true} and the other value is {@code false}.
     */
    @Override
    public int compareTo(final MutableBoolean other) {
        return Boolean.compare(this.value, other.value);
    }

    /**
     * Returns the string representation of this object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
