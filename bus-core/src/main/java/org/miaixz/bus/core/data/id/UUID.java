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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import org.miaixz.bus.core.codec.No128;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Provides a class for generating universally unique identifiers (UUIDs). A UUID represents a 128-bit value. This class
 * is a copy of `java.util.UUID` and is used to generate UUID strings without hyphens.
 * <ul>
 * <li>Generate UUID: For online generation and reference of different UUID versions, see
 * <a href="https://idtools.co/uuid/v4">Generate UUID</a>.</li>
 * <li>5 Versions of UUID: For the differences between UUID versions, see
 * <a href="https://juejin.cn/post/7297225106203689001">UUID 5 version 区别</a>.</li>
 * <li>UUID Implementation Reference: For a code implementation reference, see
 * <a href="https://github.com/sake/uuid4j">sake/uuid4j</a>.</li>
 * </ul>
 * <ul>
 * <li>UUIDv1: Structure: `xxxxxxxx-xxxx-1xxx-yxxx-xxxxxxxxxxxx`. UUID v1 is represented as a 32-character hexadecimal
 * string, displayed in five groups separated by hyphens. It is <strong>time-based</strong> and accesses the host's MAC
 * address.</li>
 * <li>UUIDv2: Structure: `xxxxxxxx-xxxx-2xxx-yxxx-xxxxxxxxxxxx`. The structure of UUID v2 is the same as other UUIDs.
 * It requires <strong>DCE (Distributed Computing Environment)</strong> to generate a unique identifier. Due to privacy
 * risks associated with being based on the computer hostname, it is not widely used.</li>
 * <li>UUIDv3: Structure: `xxxxxxxx-xxxx-3xxx-yxxx-xxxxxxxxxxxx`. The structure of UUID v3 is the same as other UUIDs.
 * It requires <strong>name-based generation using MD5 hashing</strong>.</li>
 * <li>UUIDv4: Structure: `xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx`. The structure of UUID v4 is the same as other UUIDs.
 * This is the <strong>most widely used version</strong> and is generated using <strong>random numbers</strong>. The
 * default implementation is this version.</li>
 * <li>UUIDv5: Structure: `xxxxxxxx-xxxx-5xxx-yxxx-xxxxxxxxxxxx`. The structure of UUID v5 is the same as other UUIDs.
 * It requires <strong>name-based generation using SHA-1 hashing</strong>.</li>
 * <li>UUIDv6: Structure: `xxxxxxxx-xxxx-6xxx-yxxx-xxxxxxxxxxxx`. The structure of UUID v6 is the same as other UUIDs.
 * It is a <strong>version compatible with the fields of UUIDv1</strong>, combining the advantages of UUIDv1 and UUIDv4
 * to ensure time-based natural sorting and better privacy.</li>
 * <li>UUIDv7: Structure: `xxxxxxxx-xxxx-7xxx-yxxx-xxxxxxxxxxxx`. The structure of UUID v7 is the same as other UUIDs.
 * It provides a time-sorted value derived from the <strong>Unix Epoch timestamp</strong>, along with improved entropy
 * characteristics. If possible, <strong>versions 1 and 6 are recommended</strong>.</li>
 * </ul>
 * The version field holds a value that describes the type of this UUID. There are 7 different basic UUID types:
 * time-based UUIDv1, DCE security UUIDv2, name-based UUIDv3, randomly generated UUIDv4, name-based SHA-1 algorithm
 * UUIDv5, time-based randomly generated UUIDv6, and timestamp-based UUIDv7. The version values for these types are 1,
 * 2, 3, 4, 5, 6, and 7, respectively. The most commonly used is V4. These universal identifiers have different
 * variants. The methods of this class are used to manipulate the Leach-Salz variant, but the constructor allows the
 * creation of any UUID variant (described below). The layout of a variant 2 (Leach-Salz) UUID is as follows: The most
 * significant long consists of the following unsigned fields:
 * 
 * <pre>
 * 0xFFFFFFFF00000000 time_low
 * 0x00000000FFFF0000 time_mid
 * 0x000000000000F000 version
 * 0x0000000000000FFF time_hi
 * </pre>
 * 
 * The least significant long consists of the following unsigned fields:
 * 
 * <pre>
 * 0xC000000000000000 variant
 * 0x3FFF000000000000 clock_seq
 * 0x0000FFFFFFFFFFFF node
 * </pre>
 * 
 * The variant field contains a value that indicates the layout of the UUID. The bit layout described above is valid
 * only for a UUID with a variant value of 2, which indicates the Leach-Salz variant.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UUID implements java.io.Serializable, Comparable<UUID> {

    @Serial
    private static final long serialVersionUID = 2852276107807L;

    private final No128 idValue;

    /**
     * Private constructor.
     *
     * @param data The data to construct the UUID from.
     */
    private UUID(final byte[] data) {
        long msb = 0;
        long lsb = 0;
        assert data.length == 16 : "data must be 16 bytes in length";
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (data[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (data[i] & 0xff);
        }
        this.idValue = new No128(msb, lsb);
    }

    /**
     * Constructs a new {@code UUID} using the specified data.
     *
     * @param mostSigBits  The most significant 64 bits of the {@code UUID}.
     * @param leastSigBits The least significant 64 bits of the {@code UUID}.
     */
    public UUID(final long mostSigBits, final long leastSigBits) {
        this.idValue = new No128(mostSigBits, leastSigBits);
    }

    /**
     * Static factory to retrieve a type 4 (pseudo randomly generated) UUID. The {@code UUID} is generated using a
     * cryptographically strong local thread pseudo-random number generator.
     *
     * @return A randomly generated {@code UUID}.
     */
    public static UUID fastUUID() {
        return randomUUID(false);
    }

    /**
     * Static factory to retrieve a type 4 (pseudo randomly generated) UUID. The {@code UUID} is generated using a
     * cryptographically strong pseudo-random number generator.
     *
     * @return A randomly generated {@code UUID}.
     */
    public static UUID randomUUID() {
        return randomUUID(true);
    }

    /**
     * Static factory to retrieve a type 4 (pseudo randomly generated) UUID. The {@code UUID} is generated using a
     * cryptographically strong pseudo-random number generator.
     *
     * @param isSecure If {@code true}, uses {@link SecureRandom} for better security; otherwise, uses a faster but less
     *                 secure random number generator.
     * @return A randomly generated {@code UUID}.
     */
    public static UUID randomUUID(final boolean isSecure) {
        return randomUUID(isSecure ? Holder.NUMBER_GENERATOR : RandomKit.getRandom());
    }

    /**
     * Static factory to retrieve a type 4 (pseudo randomly generated) UUID. The {@code UUID} is generated using the
     * specified random number generator.
     *
     * @param random The random number generator to use.
     * @return A randomly generated {@code UUID}.
     */
    public static UUID randomUUID(final Random random) {
        final byte[] randomBytes = RandomKit.randomBytes(16, random);

        randomBytes[6] &= 0x0f; /* clear version */
        randomBytes[6] |= 0x40; /* set to version 4 */
        randomBytes[8] &= 0x3f; /* clear variant */
        randomBytes[8] |= (byte) 0x80; /* set to IETF variant */

        return new UUID(randomBytes);
    }

    /**
     * Static factory to retrieve a type 3 (name-based, MD5 hashed) UUID based on the specified byte array.
     *
     * @param name A byte array to be used to construct a {@code UUID}.
     * @return A {@code UUID} generated from the specified array.
     */
    public static UUID nameUUIDFromBytes(final byte[] name) {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (final NoSuchAlgorithmException nsae) {
            throw new InternalError("MD5 not supported");
        }
        final byte[] md5Bytes = md.digest(name);
        md5Bytes[6] &= 0x0f; /* clear version */
        md5Bytes[6] |= 0x30; /* set to version 3 */
        md5Bytes[8] &= 0x3f; /* clear variant */
        md5Bytes[8] |= (byte) 0x80; /* set to IETF variant */
        return new UUID(md5Bytes);
    }

    /**
     * Creates a {@code UUID} from the string standard representation as described in the {@link #toString()} method.
     *
     * @param name A string that specifies a {@code UUID}.
     * @return A {@code UUID} with the specified value.
     * @throws IllegalArgumentException If name does not conform to the string representation as described in
     *                                  {@link #toString()}.
     */
    public static UUID fromString(final String name) {
        final String[] components = name.split(Symbol.MINUS);
        if (components.length != 5) {
            throw new IllegalArgumentException("Invalid UUID string: " + name);
        }
        for (int i = 0; i < 5; i++) {
            components[i] = "0x" + components[i];
        }

        long mostSigBits = Long.decode(components[0]);
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[1]);
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[2]);

        long leastSigBits = Long.decode(components[3]);
        leastSigBits <<= 48;
        leastSigBits |= Long.decode(components[4]);

        return new UUID(mostSigBits, leastSigBits);
    }

    /**
     * Returns the hex value corresponding to the specified number.
     *
     * @param val    The value.
     * @param digits The number of digits.
     * @return The hex value.
     */
    private static String digits(final long val, final int digits) {
        final long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }

    /**
     * Returns the least significant 64 bits of this UUID's 128-bit value.
     *
     * @return The least significant 64 bits of this UUID's 128-bit value.
     */
    public long getLeastSignificantBits() {
        return this.idValue.getLeastSigBits();
    }

    /**
     * Returns the most significant 64 bits of this UUID's 128-bit value.
     *
     * @return The most significant 64 bits of this UUID's 128-bit value.
     */
    public long getMostSignificantBits() {
        return this.idValue.getMostSigBits();
    }

    /**
     * The version number associated with this {@code UUID}. The version number describes how this {@code UUID} was
     * generated. The version number has the following meaning:
     * <ul>
     * <li>1 Time-based UUID
     * <li>2 DCE security UUID
     * <li>3 Name-based UUID (MD5 hash)
     * <li>4 Randomly generated UUID
     * <li>5 Name-based UUID (SHA-1 hash)
     * <li>6 Time-based, randomly generated UUID (UUIDv1 + UUIDv4)
     * <li>7 Timestamp-based UUID (Unix epoch)
     * </ul>
     *
     * @return The version number of this {@code UUID}.
     */
    public int version() {
        // Version is bits masked by 0x000000000000F000 in MS long
        return (int) ((this.getMostSignificantBits() >> 12) & 0x0f);
    }

    /**
     * The variant number associated with this {@code UUID}. The variant number describes the layout of the
     * {@code UUID}. The variant number has the following meaning:
     * <ul>
     * <li>0 Reserved for NCS backward compatibility
     * <li>2 <a href="http://www.ietf.org/rfc/rfc4122.txt">IETF&nbsp;RFC&nbsp;4122</a> (Leach-Salz), used by this class
     * <li>6 Reserved, Microsoft backward compatibility
     * <li>7 Reserved for future definition
     * </ul>
     *
     * @return The variant number of this {@code UUID}.
     */
    public int variant() {
        final long leastSigBits = this.getLeastSignificantBits();
        // This field is composed of a varying number of bits.
        // 0 - - Reserved for NCS backward compatibility
        // 1 0 - The IETF aka Leach-Salz variant (used by this class)
        // 1 1 0 Reserved, Microsoft backward compatibility
        // 1 1 1 Reserved for future definition.
        return (int) ((leastSigBits >>> (64 - (leastSigBits >>> 62))) & (leastSigBits >> 63));
    }

    /**
     * The timestamp value associated with this UUID. The 60-bit timestamp value is constructed from the time_low,
     * time_mid, and time_hi fields of this {@code UUID}. The resulting timestamp is measured in 100-nanosecond units
     * since midnight, October 15, 1582 UTC. The timestamp value is only meaningful in a time-based UUID, which has
     * version type 1. If this {@code UUID} is not a time-based UUID then this method throws
     * UnsupportedOperationException.
     *
     * @return The timestamp value.
     * @throws UnsupportedOperationException If this {@code UUID} is not a version 1 UUID.
     */
    public long timestamp() throws UnsupportedOperationException {
        final long mostSigBits = this.getMostSignificantBits();
        checkTimeBase();
        return (mostSigBits & 0x0FFFL) << 48//
                | ((mostSigBits >> 16) & 0x0FFFFL) << 32//
                | mostSigBits >>> 32;
    }

    /**
     * The clock sequence value associated with this UUID. The 14-bit clock sequence value is constructed from the
     * clock_seq field of this UUID. The clock_seq field is used to guarantee temporal uniqueness in a time-based UUID.
     * The {@code clockSequence} value is only meaningful in a time-based UUID, which has version type 1. If this UUID
     * is not a time-based UUID, this method throws UnsupportedOperationException.
     *
     * @return The clock sequence of this {@code UUID}.
     * @throws UnsupportedOperationException If the version of this UUID is not 1.
     */
    public int clockSequence() throws UnsupportedOperationException {
        checkTimeBase();
        return (int) ((this.getLeastSignificantBits() & 0x3FFF000000000000L) >>> 48);
    }

    /**
     * The node value associated with this UUID. The 48-bit node value is constructed from the node field of this UUID.
     * This field is intended to hold the IEEE 802 address of the machine that generated this UUID to guarantee spatial
     * uniqueness. The node value is only meaningful in a time-based UUID, which has version type 1. If this UUID is not
     * a time-based UUID, this method throws UnsupportedOperationException.
     *
     * @return The node value of this {@code UUID}.
     * @throws UnsupportedOperationException If the version of this UUID is not 1.
     */
    public long node() throws UnsupportedOperationException {
        checkTimeBase();
        return this.getLeastSignificantBits() & 0x0000FFFFFFFFFFFFL;
    }

    /**
     * Returns a {@code String} object representing this {@code UUID}. The UUID string representation is as described by
     * this BNF:
     *
     * <pre>
     * {@code
     * UUID                   = <time_low>-<time_mid>-<time_high_and_version>-<variant_and_sequence>-<node>
     * time_low               = 4*<hexOctet>
     * time_mid               = 2*<hexOctet>
     * time_high_and_version  = 2*<hexOctet>
     * variant_and_sequence   = 2*<hexOctet>
     * node                   = 6*<hexOctet>
     * hexOctet               = <hexDigit><hexDigit>
     * hexDigit               = [0-9a-fA-F]
     * }
     * </pre>
     *
     * @return A string representation of this {@code UUID}.
     * @see #toString(boolean)
     */
    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * Returns a {@code String} object representing this {@code UUID}. The UUID string representation is as described by
     * this BNF:
     *
     * <pre>
     * {@code
     * UUID                   = <time_low>-<time_mid>-<time_high_and_version>-<variant_and_sequence>-<node>
     * time_low               = 4*<hexOctet>
     * time_mid               = 2*<hexOctet>
     * time_high_and_version  = 2*<hexOctet>
     * variant_and_sequence   = 2*<hexOctet>
     * node                   = 6*<hexOctet>
     * hexOctet               = <hexDigit><hexDigit>
     * hexDigit               = [0-9a-fA-F]
     * }
     * </pre>
     *
     * @param isSimple If {@code true}, the UUID string will not contain hyphens.
     * @return A string representation of this {@code UUID}.
     */
    public String toString(final boolean isSimple) {
        final long mostSigBits = this.getMostSignificantBits();
        final long leastSigBits = this.getLeastSignificantBits();

        final StringBuilder builder = StringKit.builder(isSimple ? 32 : 36);
        // time_low
        builder.append(digits(mostSigBits >> 32, 8));
        if (!isSimple) {
            builder.append(Symbol.C_MINUS);
        }
        // time_mid
        builder.append(digits(mostSigBits >> 16, 4));
        if (!isSimple) {
            builder.append(Symbol.C_MINUS);
        }
        // time_high_and_version
        builder.append(digits(mostSigBits, 4));
        if (!isSimple) {
            builder.append(Symbol.C_MINUS);
        }
        // variant_and_sequence
        builder.append(digits(leastSigBits >> 48, 4));
        if (!isSimple) {
            builder.append(Symbol.C_MINUS);
        }
        // node
        builder.append(digits(leastSigBits, 12));

        return builder.toString();
    }

    /**
     * Returns a hash code for this UUID.
     *
     * @return A hash code value for this UUID.
     */
    @Override
    public int hashCode() {
        final long hilo = this.getLeastSignificantBits() ^ this.getMostSignificantBits();
        return ((int) (hilo >> 32)) ^ (int) hilo;
    }

    /**
     * Compares this object to the specified object. The result is {@code true} if and only if the argument is not
     * {@code null}, is a {@code UUID} object, has the same variant, and contains the same value, bit for bit, as this
     * UUID.
     *
     * @param object The object to compare with.
     * @return {@code true} if the objects are the same; {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object object) {
        if ((null == object) || (object.getClass() != UUID.class)) {
            return false;
        }
        final UUID id = (UUID) object;

        final long mostSigBits = this.getLeastSignificantBits();
        final long leastSigBits = this.getLeastSignificantBits();
        return (mostSigBits == id.getMostSignificantBits() && leastSigBits == id.getLeastSignificantBits());
    }

    /**
     * Compares this UUID with the specified UUID. If the two UUIDs are different, and the most significant field of the
     * first UUID is greater than the corresponding field of the second UUID, the first UUID is greater than the second
     * UUID.
     *
     * @param val The {@code UUID} to which this {@code UUID} is to be compared.
     * @return -1, 0 or 1 as this {@code UUID} is less than, equal to, or greater than {@code val}.
     */
    @Override
    public int compareTo(final UUID val) {
        // The ordering is intentionally set up so that the UUIDs
        // can simply be numerically compared as two numbers
        int compare = Long.compare(this.getMostSignificantBits(), val.getMostSignificantBits());
        if (0 == compare) {
            compare = Long.compare(this.getLeastSignificantBits(), val.getLeastSignificantBits());
        }
        return compare;
    }

    /**
     * Checks if this is a time-based UUID.
     */
    private void checkTimeBase() {
        if (version() != 1) {
            throw new UnsupportedOperationException("Not a time-based UUID");
        }
    }

    /**
     * Singleton for {@link SecureRandom}.
     */
    private static class Holder {

        static final SecureRandom NUMBER_GENERATOR = RandomKit.getSecureRandom();
    }

}
