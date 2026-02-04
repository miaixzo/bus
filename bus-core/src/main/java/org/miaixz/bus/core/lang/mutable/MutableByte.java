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
 * A mutable {@code byte} wrapper.
 *
 * @author Kimi Liu
 * @see Byte
 * @since Java 17+
 */
public class MutableByte extends Number implements Comparable<MutableByte>, Mutable<Number> {

    @Serial
    private static final long serialVersionUID = 2852269751287L;

    /**
     * The mutable value.
     */
    private byte value;

    /**
     * Constructs a new MutableByte with a default value of 0.
     */
    public MutableByte() {
    }

    /**
     * Constructs a new MutableByte with the specified value.
     *
     * @param value The initial value.
     */
    public MutableByte(final byte value) {
        this.value = value;
    }

    /**
     * Constructs a new MutableByte with the value from the specified Number.
     *
     * @param value The initial value as a Number.
     */
    public MutableByte(final Number value) {
        this(value.byteValue());
    }

    /**
     * Constructs a new MutableByte with the value parsed from the specified String.
     *
     * @param value The initial value as a String.
     * @throws NumberFormatException if the String cannot be parsed to a byte.
     */
    public MutableByte(final String value) throws NumberFormatException {
        this.value = Byte.parseByte(value);
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    @Override
    public Byte get() {
        return this.value;
    }

    /**
     * Sets the value.
     *
     * @param value The new value.
     */
    public void set(final byte value) {
        this.value = value;
    }

    /**
     * Set method.
     */
    @Override
    public void set(final Number value) {
        this.value = value.byteValue();
    }

    /**
     * Increments the value by one.
     *
     * @return This MutableByte instance.
     */
    public MutableByte increment() {
        value++;
        return this;
    }

    /**
     * Decrements the value by one.
     *
     * @return This MutableByte instance.
     */
    public MutableByte decrement() {
        value--;
        return this;
    }

    /**
     * Adds the specified value to this MutableByte.
     *
     * @param operand The value to add.
     * @return This MutableByte instance.
     */
    public MutableByte add(final byte operand) {
        this.value += operand;
        return this;
    }

    /**
     * Adds the value of the specified Number to this MutableByte.
     *
     * @param operand The value to add, must not be null.
     * @return This MutableByte instance.
     * @throws NullPointerException if the operand is null.
     */
    public MutableByte add(final Number operand) {
        this.value += operand.byteValue();
        return this;
    }

    /**
     * Subtracts the specified value from this MutableByte.
     *
     * @param operand The value to subtract.
     * @return This MutableByte instance.
     */
    public MutableByte subtract(final byte operand) {
        this.value -= operand;
        return this;
    }

    /**
     * Subtracts the value of the specified Number from this MutableByte.
     *
     * @param operand The value to subtract, must not be null.
     * @return This MutableByte instance.
     * @throws NullPointerException if the operand is null.
     */
    public MutableByte subtract(final Number operand) {
        this.value -= operand.byteValue();
        return this;
    }

    /**
     * Returns the value of this MutableByte as a {@code byte}.
     *
     * @return The numeric value represented by this object after conversion to type {@code byte}.
     */
    @Override
    public byte byteValue() {
        return value;
    }

    /**
     * Returns the value of this MutableByte as an {@code int}.
     *
     * @return The numeric value represented by this object after conversion to type {@code int}.
     */
    @Override
    public int intValue() {
        return value;
    }

    /**
     * Returns the value of this MutableByte as a {@code long}.
     *
     * @return The numeric value represented by this object after conversion to type {@code long}.
     */
    @Override
    public long longValue() {
        return value;
    }

    /**
     * Returns the value of this MutableByte as a {@code float}.
     *
     * @return The numeric value represented by this object after conversion to type {@code float}.
     */
    @Override
    public float floatValue() {
        return value;
    }

    /**
     * Returns the value of this MutableByte as a {@code double}.
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
     * <li>The other object is an instance of {@code MutableByte}.</li>
     * <li>The byte value of the other object is equal to this object's value.</li>
     * </ol>
     *
     * @param object The object to compare against.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof MutableByte) {
            return value == ((MutableByte) object).byteValue();
        }
        return false;
    }

    /**
     * Returns the hash code for this MutableByte.
     *
     * @return The hash code based on the current value.
     */
    @Override
    public int hashCode() {
        return value;
    }

    /**
     * Compares this {@code MutableByte} object with the specified {@code MutableByte} object.
     *
     * @param other The other {@code MutableByte} object to compare against.
     * @return 0 if the values are equal, a negative integer if this value is less than the other value, or a positive
     *         integer if this value is greater than the other value.
     */
    @Override
    public int compareTo(final MutableByte other) {
        return CompareKit.compare(this.value, other.value);
    }

    /**
     * Returns the string representation of this MutableByte.
     *
     * @return The string representation of the current value.
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
