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
 * A mutable {@code float} wrapper.
 *
 * @author Kimi Liu
 * @see Float
 * @since Java 17+
 */
public class MutableFloat extends Number implements Comparable<MutableFloat>, Mutable<Number> {

    @Serial
    private static final long serialVersionUID = 2852270352052L;

    /**
     * The mutable value.
     */
    private float value;

    /**
     * Constructs a new MutableFloat with a default value of 0.
     */
    public MutableFloat() {

    }

    /**
     * Constructs a new MutableFloat with the specified value.
     *
     * @param value The initial value.
     */
    public MutableFloat(final float value) {
        this.value = value;
    }

    /**
     * Constructs a new MutableFloat with the value from the specified Number.
     *
     * @param value The initial value as a Number.
     */
    public MutableFloat(final Number value) {
        this(value.floatValue());
    }

    /**
     * Constructs a new MutableFloat with the value parsed from the specified String.
     *
     * @param value The initial value as a String.
     * @throws NumberFormatException if the String cannot be parsed to a float.
     */
    public MutableFloat(final String value) throws NumberFormatException {
        this.value = Float.parseFloat(value);
    }

    @Override
    public Float get() {
        return this.value;
    }

    /**
     * Sets the value.
     *
     * @param value The new value.
     */
    public void set(final float value) {
        this.value = value;
    }

    @Override
    public void set(final Number value) {
        this.value = value.floatValue();
    }

    /**
     * Increments the value by one.
     *
     * @return This MutableFloat instance.
     */
    public MutableFloat increment() {
        value++;
        return this;
    }

    /**
     * Decrements the value by one.
     *
     * @return This MutableFloat instance.
     */
    public MutableFloat decrement() {
        value--;
        return this;
    }

    /**
     * Adds the specified value to this MutableFloat.
     *
     * @param operand The value to add.
     * @return This MutableFloat instance.
     */
    public MutableFloat add(final float operand) {
        this.value += operand;
        return this;
    }

    /**
     * Adds the value of the specified Number to this MutableFloat.
     *
     * @param operand The value to add, must not be null.
     * @return This MutableFloat instance.
     * @throws NullPointerException if the operand is null.
     */
    public MutableFloat add(final Number operand) {
        this.value += operand.floatValue();
        return this;
    }

    /**
     * Subtracts the specified value from this MutableFloat.
     *
     * @param operand The value to subtract.
     * @return This MutableFloat instance.
     */
    public MutableFloat subtract(final float operand) {
        this.value -= operand;
        return this;
    }

    /**
     * Subtracts the value of the specified Number from this MutableFloat.
     *
     * @param operand The value to subtract, must not be null.
     * @return This MutableFloat instance.
     * @throws NullPointerException if the operand is null.
     */
    public MutableFloat subtract(final Number operand) {
        this.value -= operand.floatValue();
        return this;
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public long longValue() {
        return (long) value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    /**
     * Compares this object to the specified object. The objects are considered equal if all of the following conditions
     * are met:
     * <ol>
     * <li>The other object is not null.</li>
     * <li>The other object is an instance of {@code MutableFloat}.</li>
     * <li>The float value of the other object is equal to this object's value.</li>
     * </ol>
     *
     * @param object The object to compare against.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof MutableFloat) {
            return (Float.floatToIntBits(((MutableFloat) object).value) == Float.floatToIntBits(value));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Float.floatToIntBits(value);
    }

    /**
     * Compares this {@code MutableFloat} object with the specified {@code MutableFloat} object.
     *
     * @param other The other {@code MutableFloat} object to compare against.
     * @return 0 if the values are equal, a negative integer if this value is less than the other value, or a positive
     *         integer if this value is greater than the other value.
     */
    @Override
    public int compareTo(final MutableFloat other) {
        return CompareKit.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
