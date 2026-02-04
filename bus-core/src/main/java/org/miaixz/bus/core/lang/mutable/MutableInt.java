/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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

    /**
     * Gets the value.
     *
     * @return the value
     */
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

    /**
     * Set method.
     */
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

    /**
     * Returns the value of this MutableInt as an {@code int}.
     *
     * @return The numeric value represented by this object after conversion to type {@code int}.
     */
    @Override
    public int intValue() {
        return value;
    }

    /**
     * Returns the value of this MutableInt as a {@code long}.
     *
     * @return The numeric value represented by this object after conversion to type {@code long}.
     */
    @Override
    public long longValue() {
        return value;
    }

    /**
     * Returns the value of this MutableInt as a {@code float}.
     *
     * @return The numeric value represented by this object after conversion to type {@code float}.
     */
    @Override
    public float floatValue() {
        return value;
    }

    /**
     * Returns the value of this MutableInt as a {@code double}.
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

    /**
     * Returns the hash code for this MutableInt.
     *
     * @return The hash code based on the current value.
     */
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

    /**
     * Returns the string representation of this MutableInt.
     *
     * @return The string representation of the current value.
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
