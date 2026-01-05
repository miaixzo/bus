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
package org.miaixz.bus.core.data.id;

import java.io.Serial;
import java.io.Serializable;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.Random;

import org.miaixz.bus.core.codec.No128;
import org.miaixz.bus.core.codec.binary.Crockford;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.RandomKit;

/**
 * ULID (Universally Unique Lexicographically Sortable Identifier). Its features are:
 * <ul>
 * <li>Compatible with UUID's 128-bit size.</li>
 * <li>1.21e+24 unique ULIDs per millisecond.</li>
 * <li>Lexicographically sortable.</li>
 * <li>Canonically encoded as a 26-character string, as opposed to the 36 characters of a UUID.</li>
 * <li>Uses Crockford's base32 for better efficiency and readability (5 bits per character).</li>
 * <li>Case-insensitive.</li>
 * <li>No special characters (URL safe).</li>
 * <li>Monotonic sort order (correctly detects and handles the same millisecond).</li>
 * </ul>
 * <p>
 * Reference: https://github.com/zjcscut/framework-mesh/blob/master/ulid4j/src/main/java/cn/vlts/ulid/ULID.java
 * 
 * <pre>{@code
 *   01AN4Z07BY      79KA1307SR9X4MV3
 *  |----------|    |----------------|
 *   Timestamp          Randomness
 *    48bits             80bits
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ULID implements Comparable<ULID>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852275998965L;

    /**
     * Timestamp component mask
     */
    private static final long TIMESTAMP_MASK = 0xffff000000000000L;
    /**
     * The length of randomness component of ULID
     */
    private static final int RANDOMNESS_BYTE_LEN = 10;
    /**
     * The least significant 64 bits increase overflow, 0xffffffffffffffffL + 1
     */
    private static final long OVERFLOW = 0x0000000000000000L;

    /**
     * The 128-bit value of the ULID.
     */
    private final No128 idValue;

    /**
     * Constructs a new ULID with the given 128-bit value.
     *
     * @param no128 The 128-bit value.
     */
    public ULID(final No128 no128) {
        this.idValue = no128;
    }

    /**
     * Creates a new ULID using the current system time and a new random value.
     *
     * @return A new ULID.
     */
    public static ULID of() {
        return of(System.currentTimeMillis());
    }

    /**
     * Creates a new ULID using the given timestamp and a new random value.
     *
     * @param timestamp The timestamp.
     * @return A new ULID.
     */
    public static ULID of(final long timestamp) {
        return of(timestamp, RandomKit.getRandom());
    }

    /**
     * Creates a new ULID using the given timestamp and a {@link Random} instance.
     *
     * @param timestamp The timestamp.
     * @param random    The {@link Random} instance to use for generating the random part.
     * @return A new ULID.
     */
    public static ULID of(final long timestamp, final Random random) {
        return of(timestamp, RandomKit.randomBytes(RANDOMNESS_BYTE_LEN, random));
    }

    /**
     * Creates a new ULID using the given timestamp and randomness bytes.
     *
     * @param timestamp  The timestamp.
     * @param randomness The 10-byte randomness value.
     * @return A new ULID.
     */
    public static ULID of(final long timestamp, final byte[] randomness) {
        // Timestamp can be at most 48 bits (6 bytes)
        checkTimestamp(timestamp);
        Assert.notNull(randomness);
        // Randomness part must be 80 bits (10 bytes) long
        Assert.isTrue(RANDOMNESS_BYTE_LEN == randomness.length, "Invalid randomness");

        long msb = 0;
        // Shift timestamp left by 16 bits, padding lower bits with zeros to prepare for filling in part of the random
        // bits
        msb |= timestamp << 16;
        // Fill the high 8 bits of the random part
        msb |= (long) (randomness[0x0] & 0xff) << 8;
        // Fill the low 8 bits of the random part
        msb |= randomness[0x1] & 0xff;

        return new ULID(new No128(msb, ByteKit.toLong(randomness, 2, ByteOrder.BIG_ENDIAN)));
    }

    /**
     * Creates a ULID from a Crockford's Base32 encoded string.
     *
     * @param ulidString The Crockford's Base32 encoded string.
     * @return A ULID.
     */
    public static ULID of(final String ulidString) {
        Objects.requireNonNull(ulidString, "ulidString must not be null!");
        if (ulidString.length() != 26) {
            throw new IllegalArgumentException("ulidString must be exactly 26 chars long.");
        }

        final String timeString = ulidString.substring(0, 10);
        final long time = Crockford.parseCrockford(timeString);
        checkTimestamp(time);

        final String part1String = ulidString.substring(10, 18);
        final String part2String = ulidString.substring(18);
        final long part1 = Crockford.parseCrockford(part1String);
        final long part2 = Crockford.parseCrockford(part2String);

        final long most = (time << 16) | (part1 >>> 24);
        final long least = part2 | (part1 << 40);
        return new ULID(new No128(most, least));
    }

    /**
     * Creates a ULID from a 16-byte array.
     *
     * @param data The 16-byte array (most significant bits first).
     * @return A ULID.
     */
    public static ULID of(final byte[] data) {
        Objects.requireNonNull(data, "data must not be null!");
        if (data.length != 16) {
            throw new IllegalArgumentException("data must be 16 bytes in length!");
        }
        long mostSignificantBits = 0;
        long leastSignificantBits = 0;
        for (int i = 0; i < 8; i++) {
            mostSignificantBits = (mostSignificantBits << 8) | (data[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            leastSignificantBits = (leastSignificantBits << 8) | (data[i] & 0xff);
        }
        return new ULID(new No128(mostSignificantBits, leastSignificantBits));
    }

    /**
     * Checks if the given timestamp is valid.
     *
     * @param timestamp The timestamp to check.
     */
    private static void checkTimestamp(final long timestamp) {
        Assert.isTrue(
                (timestamp & TIMESTAMP_MASK) == 0,
                "ULID does not support timestamps after +10889-08-02T05:31:50.655Z!");
    }

    /**
     * Gets the most significant 64 bits of this ULID.
     *
     * @return The most significant 64 bits.
     */
    public long getMostSignificantBits() {
        return this.idValue.getMostSigBits();
    }

    /**
     * Gets the least significant 64 bits of this ULID.
     *
     * @return The least significant 64 bits.
     */
    public long getLeastSignificantBits() {
        return this.idValue.getLeastSigBits();
    }

    /**
     * Gets the timestamp part of this ULID.
     *
     * @return The timestamp.
     */
    public long getTimestamp() {
        return this.idValue.getMostSigBits() >>> 16;
    }

    /**
     * Gets the randomness part of this ULID.
     *
     * @return The 10-byte randomness value.
     */
    public byte[] getRandomness() {
        final long msb = this.idValue.getMostSigBits();
        final long lsb = this.idValue.getLeastSigBits();
        final byte[] randomness = new byte[RANDOMNESS_BYTE_LEN];
        // No need for & 0xff here, as the extra bits will be truncated
        randomness[0x0] = (byte) (msb >>> 8);
        randomness[0x1] = (byte) msb;

        ByteKit.fill(lsb, 2, ByteOrder.BIG_ENDIAN, randomness);
        return randomness;
    }

    /**
     * Increments this ULID.
     *
     * @return The incremented ULID.
     */
    public ULID increment() {
        final long msb = this.idValue.getMostSigBits();
        final long lsb = this.idValue.getLeastSigBits();
        long newMsb = msb;
        final long newLsb = lsb + 1;
        if (newLsb == OVERFLOW) {
            newMsb += 1;
        }
        return new ULID(new No128(newMsb, newLsb));
    }

    /**
     * Gets the next monotonic ULID.
     *
     * @param timestamp The timestamp.
     * @return If the given timestamp is the same as this ULID's timestamp, returns an incremented ULID; otherwise,
     *         returns a new ULID for the given timestamp.
     */
    public ULID nextMonotonic(final long timestamp) {
        if (getTimestamp() == timestamp) {
            return increment();
        }
        return of(timestamp);
    }

    /**
     * Converts this ULID to a 16-byte array.
     *
     * @return The 16-byte array representation of this ULID.
     */
    public byte[] toBytes() {
        final long msb = this.idValue.getMostSigBits();
        final long lsb = this.idValue.getLeastSigBits();
        final byte[] result = new byte[16];
        for (int i = 0; i < 8; i++) {
            result[i] = (byte) ((msb >> ((7 - i) * 8)) & 0xFF);
        }
        for (int i = 8; i < 16; i++) {
            result[i] = (byte) ((lsb >> ((15 - i) * 8)) & 0xFF);
        }

        return result;
    }

    /**
     * Converts this ULID to a {@link UUID}.
     *
     * @return The UUID representation of this ULID.
     */
    public UUID toUUID() {
        final long msb = this.idValue.getMostSigBits();
        final long lsb = this.idValue.getLeastSigBits();
        return new UUID(msb, lsb);
    }

    /**
     * Converts this ULID to a {@link java.util.UUID}.
     *
     * @return The JDK UUID representation of this ULID.
     */
    public java.util.UUID toJdkUUID() {
        final long msb = this.idValue.getMostSigBits();
        final long lsb = this.idValue.getLeastSigBits();
        return new java.util.UUID(msb, lsb);
    }

    /**
     * Compares this object with the specified object for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final ULID o) {
        return this.idValue.compareTo(o.idValue);
    }

    /**
     * Checks if this object equals another object.
     *
     * @param object the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(final Object object) {
        if ((Objects.isNull(object)) || (object.getClass() != ULID.class)) {
            return false;
        }
        final ULID id = (ULID) object;
        return this.idValue.equals(id.idValue);
    }

    /**
     * Returns the hash code value for this object.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return this.idValue.hashCode();
    }

    /**
     * Returns the string representation of this object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        final long msb = this.idValue.getMostSigBits();
        final long lsb = this.idValue.getLeastSigBits();
        final char[] buffer = new char[26];

        Crockford.writeCrockford(buffer, getTimestamp(), 10, 0);
        long value = ((msb & 0xFFFFL) << 24);
        final long interim = (lsb >>> 40);
        value = value | interim;
        Crockford.writeCrockford(buffer, value, 8, 10);
        Crockford.writeCrockford(buffer, lsb, 8, 18);

        return new String(buffer);
    }

}
