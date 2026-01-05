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

import org.miaixz.bus.core.xyz.CompareKit;

/**
 * A mutable {@code long} wrapper.
 *
 * @author Kimi Liu
 * @see Long
 * @since Java 17+
 */
public class MutableLong extends Number implements Comparable<MutableLong>, Mutable<Number> {

    @Serial
    private static final long serialVersionUID = 2852270769985L;

    /**
     * The mutable value.
     */
    private long value;

    /**
     * Constructs a new MutableLong with a default value of 0.
     */
    public MutableLong() {

    }

    /**
     * Constructs a new MutableLong with the specified value.
     *
     * @param value The initial value.
     */
    public MutableLong(final long value) {
        this.value = value;
    }

    /**
     * Constructs a new MutableLong with the value from the specified Number.
     *
     * @param value The initial value as a Number.
     */
    public MutableLong(final Number value) {
        this(value.longValue());
    }

    /**
     * Constructs a new MutableLong with the value parsed from the specified String.
     *
     * @param value The initial value as a String.
     * @throws NumberFormatException if the String cannot be parsed to a long.
     */
    public MutableLong(final String value) throws NumberFormatException {
        this.value = Long.parseLong(value);
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    @Override
    public Long get() {
        return this.value;
    }

    /**
     * Sets the value.
     *
     * @param value The new value.
     */
    public void set(final long value) {
        this.value = value;
    }

    /**
     * Set method.
     */
    @Override
    public void set(final Number value) {
        this.value = value.longValue();
    }

    /**
     * Increments the value by one.
     *
     * @return This MutableLong instance.
     */
    public MutableLong increment() {
        value++;
        return this;
    }

    /**
     * Decrements the value by one.
     *
     * @return This MutableLong instance.
     */
    public MutableLong decrement() {
        value--;
        return this;
    }

    /**
     * Adds the specified value to this MutableLong.
     *
     * @param operand The value to add.
     * @return This MutableLong instance.
     */
    public MutableLong add(final long operand) {
        this.value += operand;
        return this;
    }

    /**
     * Adds the value of the specified Number to this MutableLong.
     *
     * @param operand The value to add, must not be null.
     * @return This MutableLong instance.
     * @throws NullPointerException if the operand is null.
     */
    public MutableLong add(final Number operand) {
        this.value += operand.longValue();
        return this;
    }

    /**
     * Subtracts the specified value from this MutableLong.
     *
     * @param operand The value to subtract.
     * @return This MutableLong instance.
     */
    public MutableLong subtract(final long operand) {
        this.value -= operand;
        return this;
    }

    /**
     * Subtracts the value of the specified Number from this MutableLong.
     *
     * @param operand The value to subtract, must not be null.
     * @return This MutableLong instance.
     * @throws NullPointerException if the operand is null.
     */
    public MutableLong subtract(final Number operand) {
        this.value -= operand.longValue();
        return this;
    }

    /**
     * Returns the value of this MutableLong as an {@code int}.
     *
     * @return The numeric value represented by this object after conversion to type {@code int}.
     */
    @Override
    public int intValue() {
        return (int) value;
    }

    /**
     * Returns the value of this MutableLong as a {@code long}.
     *
     * @return The numeric value represented by this object after conversion to type {@code long}.
     */
    @Override
    public long longValue() {
        return value;
    }

    /**
     * Returns the value of this MutableLong as a {@code float}.
     *
     * @return The numeric value represented by this object after conversion to type {@code float}.
     */
    @Override
    public float floatValue() {
        return value;
    }

    /**
     * Returns the value of this MutableLong as a {@code double}.
     *
     * @return The numeric value represented by this object after conversion to type {@code double}.
     */
    @Override
    public double doubleValue() {
        return value;
    }

    /**
     * Compares this object to the specified object. The objects are considered equal if all of the following conditions
     * are met:
     * <ol>
     * <li>The other object is not null.</li>
     * <li>The other object is an instance of {@code MutableLong}.</li>
     * <li>The long value of the other object is equal to this object's value.</li>
     * </ol>
     *
     * @param object The object to compare against.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof MutableLong) {
            return value == ((MutableLong) object).longValue();
        }
        return false;
    }

    /**
     * Returns the hash code for this MutableLong.
     *
     * @return The hash code based on the current value.
     */
    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    /**
     * Compares this {@code MutableLong} object with the specified {@code MutableLong} object.
     *
     * @param other The other {@code MutableLong} object to compare against.
     * @return 0 if the values are equal, a negative integer if this value is less than the other value, or a positive
     *         integer if this value is greater than the other value.
     */
    @Override
    public int compareTo(final MutableLong other) {
        return CompareKit.compare(this.value, other.value);
    }

    /**
     * Returns the string representation of this MutableLong.
     *
     * @return The string representation of the current value.
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
