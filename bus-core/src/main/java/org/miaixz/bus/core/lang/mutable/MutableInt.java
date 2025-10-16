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
 * A mutable {@code int} wrapper.
 *
 * @author Kimi Liu
 * @see Integer
 * @since Java 17+
 */
public class MutableInt extends Number implements Comparable<MutableInt>, Mutable<Number> {

    @Serial
    private static final long serialVersionUID = 2852270595187L;

    /**
     * The mutable value.
     */
    private int value;

    /**
     * Constructs a new MutableInt with a default value of 0.
     */
    public MutableInt() {

    }

    /**
     * Constructs a new MutableInt with the specified value.
     *
     * @param value The initial value.
     */
    public MutableInt(final int value) {
        this.value = value;
    }

    /**
     * Constructs a new MutableInt with the value from the specified Number.
     *
     * @param value The initial value as a Number.
     */
    public MutableInt(final Number value) {
        this(value.intValue());
    }

    /**
     * Constructs a new MutableInt with the value parsed from the specified String.
     *
     * @param value The initial value as a String.
     * @throws NumberFormatException if the String cannot be parsed to an int.
     */
    public MutableInt(final String value) throws NumberFormatException {
        this.value = Integer.parseInt(value);
    }

    @Override
    public Integer get() {
        return this.value;
    }

    /**
     * Sets the value.
     *
     * @param value The new value.
     */
    public void set(final int value) {
        this.value = value;
    }

    @Override
    public void set(final Number value) {
        this.value = value.intValue();
    }

    /**
     * Increments the value by one.
     *
     * @return This MutableInt instance.
     */
    public MutableInt increment() {
        value++;
        return this;
    }

    /**
     * Decrements the value by one.
     *
     * @return This MutableInt instance.
     */
    public MutableInt decrement() {
        value--;
        return this;
    }

    /**
     * Increments the value by one and then returns the new value.
     *
     * @return The value after incrementing.
     */
    public int incrementAndGet() {
        return ++value;
    }

    /**
     * Returns the current value and then increments it by one.
     *
     * @return The original value before incrementing.
     */
    public int getAndIncrement() {
        return value++;
    }

    /**
     * Decrements the value by one and then returns the new value.
     *
     * @return The value after decrementing.
     */
    public int decrementAndGet() {
        return --value;
    }

    /**
     * Returns the current value and then decrements it by one.
     *
     * @return The original value before decrementing.
     */
    public int getAndDecrement() {
        return value--;
    }

    /**
     * Adds the specified value to this MutableInt.
     *
     * @param operand The value to add.
     * @return This MutableInt instance.
     */
    public MutableInt add(final int operand) {
        this.value += operand;
        return this;
    }

    /**
     * Adds the value of the specified Number to this MutableInt.
     *
     * @param operand The value to add, must not be null.
     * @return This MutableInt instance.
     * @throws NullPointerException if the operand is null.
     */
    public MutableInt add(final Number operand) {
        this.value += operand.intValue();
        return this;
    }

    /**
     * Subtracts the specified value from this MutableInt.
     *
     * @param operand The value to subtract.
     * @return This MutableInt instance.
     */
    public MutableInt subtract(final int operand) {
        this.value -= operand;
        return this;
    }

    /**
     * Subtracts the value of the specified Number from this MutableInt.
     *
     * @param operand The value to subtract, must not be null.
     * @return This MutableInt instance.
     * @throws NullPointerException if the operand is null.
     */
    public MutableInt subtract(final Number operand) {
        this.value -= operand.intValue();
        return this;
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public long longValue() {
        return value;
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
     * <li>The other object is an instance of {@code MutableInt}.</li>
     * <li>The integer value of the other object is equal to this object's value.</li>
     * </ol>
     *
     * @param object The object to compare against.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof MutableInt) {
            return value == ((MutableInt) object).intValue();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.value;
    }

    /**
     * Compares this {@code MutableInt} object with the specified {@code MutableInt} object.
     *
     * @param other The other {@code MutableInt} object to compare against.
     * @return 0 if the values are equal, a negative integer if this value is less than the other value, or a positive
     *         integer if this value is greater than the other value.
     */
    @Override
    public int compareTo(final MutableInt other) {
        return CompareKit.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
