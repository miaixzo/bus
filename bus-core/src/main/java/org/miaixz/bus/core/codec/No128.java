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
package org.miaixz.bus.core.codec;

import java.io.Serial;
import java.nio.ByteOrder;
import java.util.Objects;

/**
 * Represents a 128-bit number, divided into:
 * <ul>
 * <li>Most Significant Bit (MSB), 64 bits (8 bytes)</li>
 * <li>Least Significant Bit (LSB), 64 bits (8 bytes)</li>
 * </ul>
 * This class extends {@link Number} and implements {@link Comparable} for 128-bit integer operations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class No128 extends Number implements Comparable<No128> {

    @Serial
    private static final long serialVersionUID = 2852280901728L;

    /**
     * The Most Significant Bit (MSB) of the 128-bit number, occupying 64 bits (8 bytes).
     */
    private long mostSigBits;
    /**
     * The Least Significant Bit (LSB) of the 128-bit number, occupying 64 bits (8 bytes).
     */
    private long leastSigBits;

    /**
     * Constructs a new {@code No128} instance with the specified most significant and least significant bits.
     *
     * @param mostSigBits  The 64-bit most significant part of the 128-bit number.
     * @param leastSigBits The 64-bit least significant part of the 128-bit number.
     */
    public No128(final long mostSigBits, final long leastSigBits) {
        this.mostSigBits = mostSigBits;
        this.leastSigBits = leastSigBits;
    }

    /**
     * Retrieves the Most Significant Bit (MSB) of the 128-bit number.
     *
     * @return The 64-bit most significant part.
     */
    public long getMostSigBits() {
        return mostSigBits;
    }

    /**
     * Sets the Most Significant Bit (MSB) of the 128-bit number.
     *
     * @param hiValue The new 64-bit most significant part.
     */
    public void setMostSigBits(final long hiValue) {
        this.mostSigBits = hiValue;
    }

    /**
     * Retrieves the Least Significant Bit (LSB) of the 128-bit number.
     *
     * @return The 64-bit least significant part.
     */
    public long getLeastSigBits() {
        return leastSigBits;
    }

    /**
     * Sets the Least Significant Bit (LSB) of the 128-bit number.
     *
     * @param leastSigBits The new 64-bit least significant part.
     */
    public void setLeastSigBits(final long leastSigBits) {
        this.leastSigBits = leastSigBits;
    }

    /**
     * Retrieves the high and low bits as a long array, respecting the specified byte order.
     * <ul>
     * <li>If {@link ByteOrder#LITTLE_ENDIAN}, the array will be: {@code [leastSigBits, mostSigBits]}</li>
     * <li>If {@link ByteOrder#BIG_ENDIAN}, the array will be: {@code [mostSigBits, leastSigBits]}</li>
     * </ul>
     *
     * @param byteOrder The desired byte order for the array.
     * @return A long array containing the high and low bits according to the byte order.
     */
    public long[] getLongArray(final ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return new long[] { mostSigBits, leastSigBits };
        } else {
            return new long[] { leastSigBits, mostSigBits };
        }
    }

    /**
     * Intvalue method.
     *
     * @return the int value
     */
    @Override
    public int intValue() {
        return (int) longValue();
    }

    /**
     * Longvalue method.
     *
     * @return the long value
     */
    @Override
    public long longValue() {
        return this.leastSigBits;
    }

    /**
     * Floatvalue method.
     *
     * @return the float value
     */
    @Override
    public float floatValue() {
        return longValue();
    }

    /**
     * Doublevalue method.
     *
     * @return the double value
     */
    @Override
    public double doubleValue() {
        return longValue();
    }

    /**
     * Compares this {@code No128} object to the specified object. The result is {@code true} if and only if the
     * argument is not {@code null} and is a {@code No128} object that represents the same sequence of most and least
     * significant bits.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are the same; {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof No128 no128) {
            return leastSigBits == no128.leastSigBits && mostSigBits == no128.mostSigBits;
        }
        return false;
    }

    /**
     * Returns a hash code for this {@code No128} object. The hash code is computed based on both the most significant
     * and least significant bits.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(leastSigBits, mostSigBits);
    }

    /**
     * Compares this {@code No128} object with the specified {@code No128} object for order. Returns a negative integer,
     * zero, or a positive integer as this object is less than, equal to, or greater than the specified object. The
     * comparison is performed first on the most significant bits, and then on the least significant bits if the most
     * significant bits are equal.
     *
     * @param o The {@code No128} object to be compared.
     * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object.
     */
    @Override
    public int compareTo(final No128 o) {
        final int mostSigBitsComparison = Long.compare(this.mostSigBits, o.mostSigBits);
        return mostSigBitsComparison != 0 ? mostSigBitsComparison : Long.compare(this.leastSigBits, o.leastSigBits);
    }

}
