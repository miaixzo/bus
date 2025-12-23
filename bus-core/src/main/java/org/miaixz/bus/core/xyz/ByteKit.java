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
package org.miaixz.bus.core.xyz;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.io.buffer.FastByteBuffer;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;

/**
 * Utility class for converting between numbers and bytes. Data type sizes are as follows:
 * <ul>
 * <li>byte: 8 bits</li>
 * <li>char: 2 bytes, 16 bits</li>
 * <li>int: 4 bytes, 32 bits</li>
 * <li>long: 8 bytes, 64 bits</li>
 * <li>float: 4 bytes, 32 bits</li>
 * <li>double: 8 bytes, 64 bits</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ByteKit {

    /**
     * Constructs a new ByteKit. Utility class constructor for static access.
     */
    private ByteKit() {
    }

    /**
     * Default byte order: Little Endian.
     */
    public static final ByteOrder DEFAULT_ORDER = ByteOrder.LITTLE_ENDIAN;
    /**
     * The byte order of the current CPU.
     */
    public static final ByteOrder CPU_ENDIAN = "little".equals(System.getProperty("sun.cpu.endian"))
            ? ByteOrder.LITTLE_ENDIAN
            : ByteOrder.BIG_ENDIAN;

    /**
     * Encodes a CharSequence into a byte array using UTF-8 charset.
     *
     * @param text The CharSequence to encode.
     * @return The encoded byte array.
     */
    public static byte[] toBytes(final CharSequence text) {
        return toBytes(text, Charset.UTF_8);
    }

    /**
     * Encodes a CharSequence into a byte array using the specified charset.
     *
     * @param text    The CharSequence to encode.
     * @param charset The charset to use. If null, the platform's default charset will be used.
     * @return The encoded byte array.
     */
    public static byte[] toBytes(final CharSequence text, final java.nio.charset.Charset charset) {
        if (text == null) {
            return null;
        }

        if (null == charset) {
            return text.toString().getBytes();
        }
        return text.toString().getBytes(charset);
    }

    /**
     * Converts an int to a byte.
     *
     * @param intValue The int value.
     * @return The byte value.
     */
    public static byte toByte(final int intValue) {
        return (byte) intValue;
    }

    /**
     * Converts a short to a byte array using the default little-endian byte order.
     *
     * @param shortValue The short value.
     * @return The byte array.
     */
    public static byte[] toBytes(final short shortValue) {
        return toBytes(shortValue, DEFAULT_ORDER);
    }

    /**
     * Converts a short to a byte array using the specified byte order.
     *
     * @param shortValue The short value.
     * @param byteOrder  The byte order.
     * @return The byte array.
     */
    public static byte[] toBytes(final short shortValue, final ByteOrder byteOrder) {
        final byte[] b = new byte[Short.BYTES];
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            b[0] = (byte) (shortValue & 0xff);
            b[1] = (byte) ((shortValue >> Byte.SIZE) & 0xff);
        } else {
            b[1] = (byte) (shortValue & 0xff);
            b[0] = (byte) ((shortValue >> Byte.SIZE) & 0xff);
        }
        return b;
    }

    /**
     * Converts a char to a byte array.
     *
     * @param data The char value.
     * @return The byte array.
     */
    public static byte[] toBytes(char data) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (data);
        bytes[1] = (byte) (data >> 8);
        return bytes;
    }

    /**
     * Converts a char array to a byte array using UTF-8 encoding.
     *
     * @param data The char array.
     * @return The byte array.
     */
    public static byte[] toBytes(char[] data) {
        CharBuffer cb = CharBuffer.allocate(data.length);
        cb.put(data);
        cb.flip();
        ByteBuffer bb = Charset.UTF_8.encode(cb);
        return bb.array();
    }

    /**
     * Converts an int to a byte array using the default little-endian byte order.
     *
     * @param intValue The int value.
     * @return The byte array.
     */
    public static byte[] toBytes(final int intValue) {
        return toBytes(intValue, DEFAULT_ORDER);
    }

    /**
     * Converts an int to a byte array using the specified byte order.
     *
     * @param intValue  The int value.
     * @param byteOrder The byte order.
     * @return The byte array.
     */
    public static byte[] toBytes(final int intValue, final ByteOrder byteOrder) {
        return fill(intValue, 0, byteOrder, new byte[Integer.BYTES]);
    }

    /**
     * Fills a given byte array with the bytes of an integer value.
     *
     * @param intValue  The int value.
     * @param start     The starting position in the byte array.
     * @param byteOrder The byte order.
     * @param bytes     The byte array to fill.
     * @return The filled byte array.
     */
    public static byte[] fill(int intValue, final int start, final ByteOrder byteOrder, final byte[] bytes) {
        final int offset = (ByteOrder.LITTLE_ENDIAN == byteOrder) ? 0 : (bytes.length - 1);
        for (int i = start; i < bytes.length; i++) {
            bytes[Math.abs(i - offset)] = (byte) (intValue & 0xFF);
            intValue >>= Byte.SIZE;
        }
        return bytes;
    }

    /**
     * Converts a long to a byte array using the default little-endian byte order. from:
     * <a href="https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java">Stack
     * Overflow</a>
     *
     * @param longValue The long value.
     * @return The byte array.
     */
    public static byte[] toBytes(final long longValue) {
        return toBytes(longValue, DEFAULT_ORDER);
    }

    /**
     * Converts a long to a byte array using the specified byte order. from:
     * <a href="https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java">Stack
     * Overflow</a>
     *
     * @param longValue The long value.
     * @param byteOrder The byte order.
     * @return The byte array.
     */
    public static byte[] toBytes(final long longValue, final ByteOrder byteOrder) {
        final byte[] result = new byte[Long.BYTES];
        return fill(longValue, 0, byteOrder, result);
    }

    /**
     * Fills a given byte array with the bytes of a long value.
     *
     * @param longValue The long value.
     * @param start     The starting position in the byte array.
     * @param byteOrder The byte order.
     * @param bytes     The byte array to fill.
     * @return The filled byte array.
     */
    public static byte[] fill(long longValue, final int start, final ByteOrder byteOrder, final byte[] bytes) {
        final int offset = (ByteOrder.LITTLE_ENDIAN == byteOrder) ? 0 : (bytes.length - 1);
        for (int i = start; i < bytes.length; i++) {
            bytes[Math.abs(i - offset)] = (byte) (longValue & 0xFF);
            longValue >>= Byte.SIZE;
        }
        return bytes;
    }

    /**
     * Converts a float to a byte array using the default little-endian byte order.
     *
     * @param floatValue The float value.
     * @return The byte array.
     */
    public static byte[] toBytes(final float floatValue) {
        return toBytes(floatValue, DEFAULT_ORDER);
    }

    /**
     * Converts a float to a byte array using the specified byte order.
     *
     * @param floatValue The float value.
     * @param byteOrder  The byte order.
     * @return The byte array.
     */
    public static byte[] toBytes(final float floatValue, final ByteOrder byteOrder) {
        return toBytes(Float.floatToIntBits(floatValue), byteOrder);
    }

    /**
     * Converts a double to a byte array using the default little-endian byte order.
     *
     * @param doubleValue The double value.
     * @return The byte array.
     */
    public static byte[] toBytes(final double doubleValue) {
        return toBytes(doubleValue, DEFAULT_ORDER);
    }

    /**
     * Converts a double to a byte array using the specified byte order. from:
     * <a href="https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java">Stack
     * Overflow</a>
     *
     * @param doubleValue The double value.
     * @param byteOrder   The byte order.
     * @return The byte array.
     */
    public static byte[] toBytes(final double doubleValue, final ByteOrder byteOrder) {
        return toBytes(Double.doubleToLongBits(doubleValue), byteOrder);
    }

    /**
     * Converts a {@link Number} to a byte array.
     *
     * @param number The number.
     * @return The byte array.
     */
    public static byte[] toBytes(final Number number) {
        return toBytes(number, DEFAULT_ORDER);
    }

    /**
     * Converts a {@link Number} to a byte array using the specified byte order.
     *
     * @param number    The number.
     * @param byteOrder The byte order.
     * @return The byte array.
     */
    public static byte[] toBytes(final Number number, final ByteOrder byteOrder) {
        if (number instanceof Byte) {
            return new byte[] { number.byteValue() };
        } else if (number instanceof Double) {
            return toBytes(number.doubleValue(), byteOrder);
        } else if (number instanceof Long) {
            return toBytes(number.longValue(), byteOrder);
        } else if (number instanceof Integer) {
            return ByteKit.toBytes(number.intValue(), byteOrder);
        } else if (number instanceof Short) {
            return ByteKit.toBytes(number.shortValue(), byteOrder);
        } else if (number instanceof Float) {
            return toBytes(number.floatValue(), byteOrder);
        } else if (number instanceof BigInteger) {
            return ((BigInteger) number).toByteArray();
        } else {
            return toBytes(number.doubleValue(), byteOrder);
        }
    }

    /**
     * Converts a byte array to a short using the default little-endian byte order.
     *
     * @param bytes The byte array.
     * @return The short value.
     */
    public static short toShort(final byte[] bytes) {
        return toShort(bytes, DEFAULT_ORDER);
    }

    /**
     * Converts a byte array to a short using the specified byte order.
     *
     * @param bytes     The byte array (must be at least 2 bytes long).
     * @param byteOrder The byte order.
     * @return The short value.
     */
    public static short toShort(final byte[] bytes, final ByteOrder byteOrder) {
        return toShort(bytes, 0, byteOrder);
    }

    /**
     * Converts a byte array to a short starting from a given position, using the specified byte order.
     *
     * @param bytes     The byte array.
     * @param start     The starting position.
     * @param byteOrder The byte order.
     * @return The short value.
     */
    public static short toShort(final byte[] bytes, final int start, final ByteOrder byteOrder) {
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            // Little-endian: LSB is at the lower address.
            return (short) (bytes[start] & 0xff | (bytes[start + 1] & 0xff) << Byte.SIZE);
        } else {
            return (short) (bytes[start + 1] & 0xff | (bytes[start] & 0xff) << Byte.SIZE);
        }
    }

    /**
     * Converts a byte array to an int using the default little-endian byte order.
     *
     * @param bytes The byte array.
     * @return The int value.
     */
    public static int toInt(final byte[] bytes) {
        return toInt(bytes, DEFAULT_ORDER);
    }

    /**
     * Converts a byte array to an int using the specified byte order.
     *
     * @param bytes     The byte array.
     * @param byteOrder The byte order.
     * @return The int value.
     */
    public static int toInt(final byte[] bytes, final ByteOrder byteOrder) {
        return toInt(bytes, 0, byteOrder);
    }

    /**
     * Converts a byte array to an int starting from a given position, using the specified byte order.
     *
     * @param bytes     The byte array.
     * @param start     The starting position.
     * @param byteOrder The byte order.
     * @return The int value.
     */
    public static int toInt(final byte[] bytes, final int start, final ByteOrder byteOrder) {
        if (bytes.length - start < Integer.BYTES) {
            throw new IllegalArgumentException("bytes length must be greater than or equal to " + Integer.BYTES);
        }

        int values = 0;
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            for (int i = (Integer.BYTES - 1); i >= 0; i--) {
                values <<= Byte.SIZE;
                values |= (bytes[i + start] & 0xFF);
            }
        } else {
            for (int i = 0; i < Integer.BYTES; i++) {
                values <<= Byte.SIZE;
                values |= (bytes[i + start] & 0xFF);
            }
        }
        return values;
    }

    /**
     * Converts a signed byte to an unsigned int.
     *
     * @param byteValue The byte value.
     * @return The unsigned int value.
     */
    public static int toUnsignedInt(final byte byteValue) {
        // Java always treats bytes as signed; we can get its unsigned value by ANDing with 0xFF.
        return byteValue & 0xFF;
    }

    /**
     * Converts a byte array to a long using the default little-endian byte order. from:
     * <a href="https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java">Stack
     * Overflow</a>
     *
     * @param bytes The byte array.
     * @return The long value.
     */
    public static long toLong(final byte[] bytes) {
        return toLong(bytes, DEFAULT_ORDER);
    }

    /**
     * Converts a byte array to a long using the specified byte order. from:
     * <a href="https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java">Stack
     * Overflow</a>
     *
     * @param bytes     The byte array.
     * @param byteOrder The byte order.
     * @return The long value.
     */
    public static long toLong(final byte[] bytes, final ByteOrder byteOrder) {
        return toLong(bytes, 0, byteOrder);
    }

    /**
     * Converts a byte array to a long starting from a given position, using the specified byte order. from:
     * <a href="https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java">Stack
     * Overflow</a>
     *
     * @param bytes     The byte array.
     * @param start     The starting position in the array.
     * @param byteOrder The byte order.
     * @return The long value.
     */
    public static long toLong(final byte[] bytes, final int start, final ByteOrder byteOrder) {
        if (bytes.length - start < Long.BYTES) {
            throw new IllegalArgumentException("bytes length must be greater than or equal to " + Long.BYTES);
        }
        long values = 0;
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            for (int i = (Long.BYTES - 1); i >= 0; i--) {
                values <<= Byte.SIZE;
                values |= (bytes[i + start] & 0xffL);
            }
        } else {
            for (int i = 0; i < Long.BYTES; i++) {
                values <<= Byte.SIZE;
                values |= (bytes[i + start] & 0xffL);
            }
        }

        return values;
    }

    /**
     * Converts a byte array to a float using the default little-endian byte order.
     *
     * @param bytes The byte array.
     * @return The float value.
     */
    public static float toFloat(final byte[] bytes) {
        return toFloat(bytes, DEFAULT_ORDER);
    }

    /**
     * Converts a byte array to a float using the specified byte order.
     *
     * @param bytes     The byte array.
     * @param byteOrder The byte order.
     * @return The float value.
     */
    public static float toFloat(final byte[] bytes, final ByteOrder byteOrder) {
        return Float.intBitsToFloat(toInt(bytes, byteOrder));
    }

    /**
     * Converts a byte array to a double using the default little-endian byte order.
     *
     * @param bytes The byte array.
     * @return The double value.
     */
    public static double toDouble(final byte[] bytes) {
        return toDouble(bytes, DEFAULT_ORDER);
    }

    /**
     * Converts a byte array to a double using the specified byte order.
     *
     * @param bytes     The byte array.
     * @param byteOrder The byte order.
     * @return The double value.
     */
    public static double toDouble(final byte[] bytes, final ByteOrder byteOrder) {
        return Double.longBitsToDouble(toLong(bytes, byteOrder));
    }

    /**
     * Converts a byte array to a specified Number type.
     *
     * @param <T>         The target number type.
     * @param bytes       The byte array.
     * @param targetClass The target number class.
     * @param byteOrder   The byte order.
     * @return The converted number.
     * @throws IllegalArgumentException if the target number type is not supported.
     */
    public static <T extends Number> T toNumber(
            final byte[] bytes,
            final Class<T> targetClass,
            final ByteOrder byteOrder) throws IllegalArgumentException {
        final Number number;
        if (Byte.class == targetClass) {
            number = bytes[0];
        } else if (Short.class == targetClass) {
            number = toShort(bytes, byteOrder);
        } else if (Integer.class == targetClass) {
            number = toInt(bytes, byteOrder);
        } else if (AtomicInteger.class == targetClass) {
            number = new AtomicInteger(toInt(bytes, byteOrder));
        } else if (Long.class == targetClass) {
            number = toLong(bytes, byteOrder);
        } else if (AtomicLong.class == targetClass) {
            number = new AtomicLong(toLong(bytes, byteOrder));
        } else if (LongAdder.class == targetClass) {
            final LongAdder longValue = new LongAdder();
            longValue.add(toLong(bytes, byteOrder));
            number = longValue;
        } else if (Float.class == targetClass) {
            number = toFloat(bytes, byteOrder);
        } else if (Double.class == targetClass) {
            number = toDouble(bytes, byteOrder);
        } else if (DoubleAdder.class == targetClass) {
            final DoubleAdder doubleAdder = new DoubleAdder();
            doubleAdder.add(toDouble(bytes, byteOrder));
            number = doubleAdder;
        } else if (BigDecimal.class == targetClass) {
            number = MathKit.toBigDecimal(toDouble(bytes, byteOrder));
        } else if (BigInteger.class == targetClass) {
            number = BigInteger.valueOf(toLong(bytes, byteOrder));
        } else if (Number.class == targetClass) {
            // If no specific type is given, default to Double.
            number = toDouble(bytes, byteOrder);
        } else {
            // Custom Number types are not supported.
            throw new IllegalArgumentException("Unsupported Number type: " + targetClass.getName());
        }

        return (T) number;
    }

    /**
     * Returns the two's-complement representation of a BigInteger as an unsigned byte array.
     *
     * @param value The value to convert.
     * @return The unsigned byte array.
     */
    public static byte[] toUnsignedByteArray(final BigInteger value) {
        final byte[] bytes = value.toByteArray();

        if (bytes[0] == 0) {
            final byte[] tmp = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, tmp, 0, tmp.length);

            return tmp;
        }

        return bytes;
    }

    /**
     * Returns the two's-complement representation of a BigInteger as an unsigned byte array of a specific length.
     *
     * @param length The desired length of the byte array.
     * @param value  The value to convert.
     * @return The unsigned byte array.
     */
    public static byte[] toUnsignedByteArray(final int length, final BigInteger value) {
        final byte[] bytes = value.toByteArray();
        if (bytes.length == length) {
            return bytes;
        }

        final int start = bytes[0] == 0 ? 1 : 0;
        final int count = bytes.length - start;

        if (count > length) {
            throw new IllegalArgumentException("standard length exceeded for value");
        }

        final byte[] tmp = new byte[length];
        System.arraycopy(bytes, start, tmp, tmp.length - count, count);
        return tmp;
    }

    /**
     * Converts an unsigned byte array to a {@link BigInteger}.
     *
     * @param buf The unsigned byte array.
     * @return The {@link BigInteger}.
     */
    public static BigInteger fromUnsignedByteArray(final byte[] buf) {
        return new BigInteger(1, buf);
    }

    /**
     * Converts a sub-array of an unsigned byte array to a {@link BigInteger}.
     *
     * @param buf    The unsigned byte array.
     * @param off    The starting offset.
     * @param length The length of the sub-array.
     * @return The {@link BigInteger}.
     */
    public static BigInteger fromUnsignedByteArray(final byte[] buf, final int off, final int length) {
        byte[] mag = buf;
        if (off != 0 || length != buf.length) {
            mag = new byte[length];
            System.arraycopy(buf, off, mag, 0, length);
        }
        return new BigInteger(1, mag);
    }

    /**
     * Concatenates multiple byte arrays into a single array.
     *
     * @param byteArrays The byte arrays to concatenate.
     * @return The concatenated byte array.
     */
    public static byte[] concat(final byte[]... byteArrays) {
        int totalLength = 0;
        for (final byte[] byteArray : byteArrays) {
            totalLength += byteArray.length;
        }

        final FastByteBuffer buffer = new FastByteBuffer(totalLength);
        for (final byte[] byteArray : byteArrays) {
            buffer.append(byteArray);
        }
        return buffer.toArrayZeroCopyIfPossible();
    }

    /**
     * Counts the number of bits set to 1 in a byte array.
     *
     * @param buf The byte array.
     * @return The number of set bits.
     * @see Integer#bitCount(int)
     */
    public static int bitCount(final byte[] buf) {
        int sum = 0;
        for (final byte b : buf) {
            sum += Integer.bitCount((b & 0xFF));
        }
        return sum;
    }

    /**
     * Gets a list of indices of the bits that are set to 1 in the given byte array.
     *
     * @param bytes The byte array.
     * @return A list of indices for set bits.
     */
    public static List<Integer> toUnsignedBitIndex(final byte[] bytes) {
        final List<Integer> idxList = new LinkedList<>();
        final StringBuilder sb = new StringBuilder();
        for (final byte b : bytes) {
            sb.append(StringKit.padPre(Integer.toBinaryString((b & 0xFF)), 8, "0"));
        }
        final String bitStr = sb.toString();
        for (int i = 0; i < bitStr.length(); i++) {
            if (bitStr.charAt(i) == '1') {
                idxList.add(i);
            }
        }
        return idxList;
    }

    /**
     * Converts bytes to an int.
     *
     * @param data      The byte array.
     * @param off       The offset.
     * @param bigEndian True for big-endian, false for little-endian.
     * @return The int value.
     */
    public static int bytesToInt(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToIntBE(data, off) : bytesToIntLE(data, off);
    }

    /**
     * Converts bytes to an int (Big-Endian).
     *
     * @param data The byte array.
     * @param off  The offset.
     * @return The int value.
     */
    public static int bytesToIntBE(byte[] data, int off) {
        return (data[off] << 24) + ((data[off + 1] & 255) << 16) + ((data[off + 2] & 255) << 8) + (data[off + 3] & 255);
    }

    /**
     * Converts bytes to an int (Little-Endian).
     *
     * @param data The byte array.
     * @param off  The offset.
     * @return The int value.
     */
    public static int bytesToIntLE(byte[] data, int off) {
        return (data[off + 3] << 24) + ((data[off + 2] & 255) << 16) + ((data[off + 1] & 255) << 8) + (data[off] & 255);
    }

    /**
     * Converts bytes to a short.
     *
     * @param data      The byte array.
     * @param off       The offset.
     * @param bigEndian True for big-endian, false for little-endian.
     * @return The short value as an int.
     */
    public static int bytesToShort(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToShortBE(data, off) : bytesToShortLE(data, off);
    }

    /**
     * Converts a byte array to a short array.
     *
     * @param data      The byte array.
     * @param s         The destination short array.
     * @param off       The offset in the destination array.
     * @param len       The number of shorts to convert.
     * @param bigEndian True for big-endian, false for little-endian.
     */
    public static void bytesToShort(byte[] data, short[] s, int off, int len, boolean bigEndian) {
        if (bigEndian)
            bytesToShortsBE(data, s, off, len);
        else
            bytesToShortLE(data, s, off, len);
    }

    /**
     * Converts bytes to a short (Big-Endian).
     *
     * @param data The byte array.
     * @param off  The offset.
     * @return The short value as an int.
     */
    public static int bytesToShortBE(byte[] data, int off) {
        return (data[off] << 8) + (data[off + 1] & 255);
    }

    /**
     * Converts bytes to a short (Little-Endian).
     *
     * @param data The byte array.
     * @param off  The offset.
     * @return The short value as an int.
     */
    public static int bytesToShortLE(byte[] data, int off) {
        return (data[off + 1] << 8) + (data[off] & 255);
    }

    /**
     * Converts a byte array to a short array (Big-Endian).
     *
     * @param data The byte array.
     * @param s    The destination short array.
     * @param off  The offset in the destination array.
     * @param len  The number of shorts to convert.
     */
    public static void bytesToShortsBE(byte[] data, short[] s, int off, int len) {
        int boff = 0;
        for (int j = 0; j < len; j++) {
            int b0 = data[boff];
            int b1 = data[boff + 1] & 0xff;
            s[off + j] = (short) ((b0 << 8) | b1);
            boff += 2;
        }
    }

    /**
     * Converts a byte array to a short array (Little-Endian).
     *
     * @param data The byte array.
     * @param s    The destination short array.
     * @param off  The offset in the destination array.
     * @param len  The number of shorts to convert.
     */
    public static void bytesToShortLE(byte[] data, short[] s, int off, int len) {
        int boff = 0;
        for (int j = 0; j < len; j++) {
            int b0 = data[boff + 1];
            int b1 = data[boff] & 0xff;
            s[off + j] = (short) ((b0 << 8) | b1);
            boff += 2;
        }
    }

    /**
     * Converts bytes to an unsigned short.
     *
     * @param data      The byte array.
     * @param off       The offset.
     * @param bigEndian True for big-endian, false for little-endian.
     * @return The unsigned short value as an int.
     */
    public static int bytesToUShort(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToUShortBE(data, off) : bytesToUShortLE(data, off);
    }

    /**
     * Converts bytes to an unsigned short (Big-Endian).
     *
     * @param data The byte array.
     * @param off  The offset.
     * @return The unsigned short value as an int.
     */
    public static int bytesToUShortBE(byte[] data, int off) {
        return ((data[off] & 255) << 8) + (data[off + 1] & 255);
    }

    /**
     * Converts bytes to an unsigned short (Little-Endian).
     *
     * @param data The byte array.
     * @param off  The offset.
     * @return The unsigned short value as an int.
     */
    public static int bytesToUShortLE(byte[] data, int off) {
        return ((data[off + 1] & 255) << 8) + (data[off] & 255);
    }

    /**
     * Converts bytes to a float.
     *
     * @param data      The byte array.
     * @param off       The offset.
     * @param bigEndian True for big-endian, false for little-endian.
     * @return The float value.
     */
    public static float bytesToFloat(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToFloatBE(data, off) : bytesToFloatLE(data, off);
    }

    /**
     * Converts bytes to a float (Big-Endian).
     *
     * @param data The byte array.
     * @param off  The offset.
     * @return The float value.
     */
    public static float bytesToFloatBE(byte[] data, int off) {
        return Float.intBitsToFloat(bytesToIntBE(data, off));
    }

    /**
     * Converts bytes to a float (Little-Endian).
     *
     * @param data The byte array.
     * @param off  The offset.
     * @return The float value.
     */
    public static float bytesToFloatLE(byte[] data, int off) {
        return Float.intBitsToFloat(bytesToIntLE(data, off));
    }

    /**
     * Converts bytes to a long.
     *
     * @param data      The byte array.
     * @param off       The offset.
     * @param bigEndian True for big-endian, false for little-endian.
     * @return The long value.
     */
    public static long bytesToLong(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToLongBE(data, off) : bytesToLongLE(data, off);
    }

    /**
     * Converts bytes to a long (Big-Endian).
     *
     * @param data The byte array.
     * @param off  The offset.
     * @return The long value.
     */
    public static long bytesToLongBE(byte[] data, int off) {
        return ((long) data[off] << 56) + ((long) (data[off + 1] & 255) << 48) + ((long) (data[off + 2] & 255) << 40)
                + ((long) (data[off + 3] & 255) << Normal._32) + ((long) (data[off + 4] & 255) << 24)
                + ((data[off + 5] & 255) << Normal._16) + ((data[off + 6] & 255) << 8) + (data[off + 7] & 255);
    }

    /**
     * Converts bytes to a long (Little-Endian).
     *
     * @param data The byte array.
     * @param off  The offset.
     * @return The long value.
     */
    public static long bytesToLongLE(byte[] data, int off) {
        return ((long) data[off + 7] << 56) + ((long) (data[off + 6] & 255) << 48)
                + ((long) (data[off + 5] & 255) << 40) + ((long) (data[off + 4] & 255) << Normal._32)
                + ((long) (data[off + 3] & 255) << 24) + ((data[off + 2] & 255) << Normal._16)
                + ((data[off + 1] & 255) << 8) + (data[off] & 255);
    }

    /**
     * Converts bytes to a double.
     *
     * @param data      The byte array.
     * @param off       The offset.
     * @param bigEndian True for big-endian, false for little-endian.
     * @return The double value.
     */
    public static double bytesToDouble(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToDoubleBE(data, off) : bytesToDoubleLE(data, off);
    }

    /**
     * Converts bytes to a double (Big-Endian).
     *
     * @param data The byte array.
     * @param off  The offset.
     * @return The double value.
     */
    public static double bytesToDoubleBE(byte[] data, int off) {
        return Double.longBitsToDouble(bytesToLongBE(data, off));
    }

    /**
     * Converts bytes to a double (Little-Endian).
     *
     * @param data The byte array.
     * @param off  The offset.
     * @return The double value.
     */
    public static double bytesToDoubleLE(byte[] data, int off) {
        return Double.longBitsToDouble(bytesToLongLE(data, off));
    }

    /**
     * Converts bytes to a VR (Value Representation) code (Big-Endian).
     *
     * @param data The byte array.
     * @param off  The offset.
     * @return The VR code as an int.
     */
    public static int bytesToVR(byte[] data, int off) {
        return bytesToUShortBE(data, off);
    }

    /**
     * Converts bytes to a DICOM tag.
     *
     * @param data      The byte array.
     * @param off       The offset.
     * @param bigEndian True for big-endian, false for little-endian.
     * @return The tag value as an int.
     */
    public static int bytesToTag(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToTagBE(data, off) : bytesToTagLE(data, off);
    }

    /**
     * Converts bytes to a DICOM tag (Big-Endian).
     *
     * @param data The byte array.
     * @param off  The offset.
     * @return The tag value as an int.
     */
    public static int bytesToTagBE(byte[] data, int off) {
        return bytesToIntBE(data, off);
    }

    /**
     * Converts bytes to a DICOM tag (Little-Endian).
     *
     * @param data The byte array.
     * @param off  The offset.
     * @return The tag value as an int.
     */
    public static int bytesToTagLE(byte[] data, int off) {
        return (data[off + 1] << 24) + ((data[off] & 255) << 16) + ((data[off + 3] & 255) << 8) + (data[off + 2] & 255);
    }

    /**
     * Converts an int to bytes and places them in a byte array.
     *
     * @param data      The int value.
     * @param bytes     The destination byte array.
     * @param off       The offset.
     * @param bigEndian True for big-endian, false for little-endian.
     * @return The destination byte array.
     */
    public static byte[] intToBytes(int data, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? intToBytesBE(data, bytes, off) : intToBytesLE(data, bytes, off);
    }

    /**
     * Converts an int to bytes (Big-Endian) and places them in a byte array.
     *
     * @param data  The int value.
     * @param bytes The destination byte array.
     * @param off   The offset.
     * @return The destination byte array.
     */
    public static byte[] intToBytesBE(int data, byte[] bytes, int off) {
        bytes[off] = (byte) (data >> 24);
        bytes[off + 1] = (byte) (data >> 16);
        bytes[off + 2] = (byte) (data >> 8);
        bytes[off + 3] = (byte) data;
        return bytes;
    }

    /**
     * Converts an int to bytes (Little-Endian) and places them in a byte array.
     *
     * @param data  The int value.
     * @param bytes The destination byte array.
     * @param off   The offset.
     * @return The destination byte array.
     */
    public static byte[] intToBytesLE(int data, byte[] bytes, int off) {
        bytes[off + 3] = (byte) (data >> 24);
        bytes[off + 2] = (byte) (data >> 16);
        bytes[off + 1] = (byte) (data >> 8);
        bytes[off] = (byte) data;
        return bytes;
    }

    /**
     * Converts a short (as int) to bytes and places them in a byte array.
     *
     * @param data      The short value (passed as an int).
     * @param bytes     The destination byte array.
     * @param off       The offset.
     * @param bigEndian True for big-endian, false for little-endian.
     * @return The destination byte array.
     */
    public static byte[] shortToBytes(int data, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? shortToBytesBE(data, bytes, off) : shortToBytesLE(data, bytes, off);
    }

    /**
     * Converts a short (as int) to bytes (Big-Endian) and places them in a byte array.
     *
     * @param data  The short value (passed as an int).
     * @param bytes The destination byte array.
     * @param off   The offset.
     * @return The destination byte array.
     */
    public static byte[] shortToBytesBE(int data, byte[] bytes, int off) {
        bytes[off] = (byte) (data >> 8);
        bytes[off + 1] = (byte) data;
        return bytes;
    }

    /**
     * Converts a short (as int) to bytes (Little-Endian) and places them in a byte array.
     *
     * @param data  The short value (passed as an int).
     * @param bytes The destination byte array.
     * @param off   The offset.
     * @return The destination byte array.
     */
    public static byte[] shortToBytesLE(int data, byte[] bytes, int off) {
        bytes[off + 1] = (byte) (data >> 8);
        bytes[off] = (byte) data;
        return bytes;
    }

    /**
     * Converts a long to bytes and places them in a byte array.
     *
     * @param data      The long value.
     * @param bytes     The destination byte array.
     * @param off       The offset.
     * @param bigEndian True for big-endian, false for little-endian.
     * @return The destination byte array.
     */
    public static byte[] longToBytes(long data, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? longToBytesBE(data, bytes, off) : longToBytesLE(data, bytes, off);
    }

    /**
     * Converts a long to bytes (Big-Endian) and places them in a byte array.
     *
     * @param data  The long value.
     * @param bytes The destination byte array.
     * @param off   The offset.
     * @return The destination byte array.
     */
    public static byte[] longToBytesBE(long data, byte[] bytes, int off) {
        bytes[off] = (byte) (data >> 56);
        bytes[off + 1] = (byte) (data >> 48);
        bytes[off + 2] = (byte) (data >> 40);
        bytes[off + 3] = (byte) (data >> 32);
        bytes[off + 4] = (byte) (data >> 24);
        bytes[off + 5] = (byte) (data >> 16);
        bytes[off + 6] = (byte) (data >> 8);
        bytes[off + 7] = (byte) data;
        return bytes;
    }

    /**
     * Converts a long to bytes (Little-Endian) and places them in a byte array.
     *
     * @param data  The long value.
     * @param bytes The destination byte array.
     * @param off   The offset.
     * @return The destination byte array.
     */
    public static byte[] longToBytesLE(long data, byte[] bytes, int off) {
        bytes[off + 7] = (byte) (data >> 56);
        bytes[off + 6] = (byte) (data >> 48);
        bytes[off + 5] = (byte) (data >> 40);
        bytes[off + 4] = (byte) (data >> 32);
        bytes[off + 3] = (byte) (data >> 24);
        bytes[off + 2] = (byte) (data >> 16);
        bytes[off + 1] = (byte) (data >> 8);
        bytes[off] = (byte) data;
        return bytes;
    }

    /**
     * Converts a float to bytes and places them in a byte array.
     *
     * @param data      The float value.
     * @param bytes     The destination byte array.
     * @param off       The offset.
     * @param bigEndian True for big-endian, false for little-endian.
     * @return The destination byte array.
     */
    public static byte[] floatToBytes(float data, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? floatToBytesBE(data, bytes, off) : floatToBytesLE(data, bytes, off);
    }

    /**
     * Converts a float to bytes (Big-Endian) and places them in a byte array.
     *
     * @param data  The float value.
     * @param bytes The destination byte array.
     * @param off   The offset.
     * @return The destination byte array.
     */
    public static byte[] floatToBytesBE(float data, byte[] bytes, int off) {
        return intToBytesBE(Float.floatToIntBits(data), bytes, off);
    }

    /**
     * Converts a float to bytes (Little-Endian) and places them in a byte array.
     *
     * @param data  The float value.
     * @param bytes The destination byte array.
     * @param off   The offset.
     * @return The destination byte array.
     */
    public static byte[] floatToBytesLE(float data, byte[] bytes, int off) {
        return intToBytesLE(Float.floatToIntBits(data), bytes, off);
    }

    /**
     * Converts a double to bytes and places them in a byte array.
     *
     * @param data      The double value.
     * @param bytes     The destination byte array.
     * @param off       The offset.
     * @param bigEndian True for big-endian, false for little-endian.
     * @return The destination byte array.
     */
    public static byte[] doubleToBytes(double data, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? doubleToBytesBE(data, bytes, off) : doubleToBytesLE(data, bytes, off);
    }

    /**
     * Converts a double to bytes (Big-Endian) and places them in a byte array.
     *
     * @param data  The double value.
     * @param bytes The destination byte array.
     * @param off   The offset.
     * @return The destination byte array.
     */
    public static byte[] doubleToBytesBE(double data, byte[] bytes, int off) {
        return longToBytesBE(Double.doubleToLongBits(data), bytes, off);
    }

    /**
     * Converts a double to bytes (Little-Endian) and places them in a byte array.
     *
     * @param data  The double value.
     * @param bytes The destination byte array.
     * @param off   The offset.
     * @return The destination byte array.
     */
    public static byte[] doubleToBytesLE(double data, byte[] bytes, int off) {
        return longToBytesLE(Double.doubleToLongBits(data), bytes, off);
    }

    /**
     * Converts a DICOM tag to bytes and places them in a byte array.
     *
     * @param data      The tag value (as int).
     * @param bytes     The destination byte array.
     * @param off       The offset.
     * @param bigEndian True for big-endian, false for little-endian.
     * @return The destination byte array.
     */
    public static byte[] tagToBytes(int data, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? tagToBytesBE(data, bytes, off) : tagToBytesLE(data, bytes, off);
    }

    /**
     * Converts a DICOM tag to bytes (Big-Endian) and places them in a byte array.
     *
     * @param data  The tag value (as int).
     * @param bytes The destination byte array.
     * @param off   The offset.
     * @return The destination byte array.
     */
    public static byte[] tagToBytesBE(int data, byte[] bytes, int off) {
        return intToBytesBE(data, bytes, off);
    }

    /**
     * Converts a DICOM tag to bytes (Little-Endian) and places them in a byte array.
     *
     * @param data  The tag value (as int).
     * @param bytes The destination byte array.
     * @param off   The offset.
     * @return The destination byte array.
     */
    public static byte[] tagToBytesLE(int data, byte[] bytes, int off) {
        bytes[off + 1] = (byte) (data >> 24);
        bytes[off] = (byte) (data >> 16);
        bytes[off + 3] = (byte) (data >> 8);
        bytes[off + 2] = (byte) data;
        return bytes;
    }

    /**
     * Swaps bytes for an array of 4-byte integers.
     *
     * @param data The byte array.
     * @param off  The offset.
     * @param len  The length of the segment to process.
     * @return The modified byte array.
     */
    public static byte[] swapInts(byte[] data, int off, int len) {
        checkLength(len, 4);
        for (int i = off, n = off + len; i < n; i += 4) {
            swap(data, i, i + 3);
            swap(data, i + 1, i + 2);
        }
        return data;
    }

    /**
     * Swaps bytes for an array of 8-byte longs.
     *
     * @param data The byte array.
     * @param off  The offset.
     * @param len  The length of the segment to process.
     * @return The modified byte array.
     */
    public static byte[] swapLongs(byte[] data, int off, int len) {
        checkLength(len, 8);
        for (int i = off, n = off + len; i < n; i += 8) {
            swap(data, i, i + 7);
            swap(data, i + 1, i + 6);
            swap(data, i + 2, i + 5);
            swap(data, i + 3, i + 4);
        }
        return data;
    }

    /**
     * Swaps bytes for an array of 2-byte shorts across multiple byte arrays.
     *
     * @param data An array of byte arrays.
     * @return The modified array of byte arrays.
     */
    public static byte[][] swapShorts(byte[][] data) {
        int carry = 0;
        for (int i = 0; i < data.length; i++) {
            byte[] b = data[i];
            if (carry != 0)
                swapLastFirst(data[i - 1], b);
            int len = b.length - carry;
            swapShorts(b, carry, len & ~1);
            carry = len & 1;
        }
        return data;
    }

    /**
     * Swaps bytes for an array of 2-byte shorts.
     *
     * @param data The byte array.
     * @param off  The offset.
     * @param len  The length of the segment to process.
     * @return The modified byte array.
     */
    public static byte[] swapShorts(byte[] data, int off, int len) {
        checkLength(len, 2);
        for (int i = off, n = off + len; i < n; i += 2)
            swap(data, i, i + 1);
        return data;
    }

    /**
     * Finds the index of a target byte in a byte array within a specified range.
     *
     * @param data   The byte array.
     * @param target The target byte.
     * @param from   The starting index (inclusive).
     * @param to     The ending index (exclusive).
     * @return The index of the target byte, or -1 if not found.
     */
    public static int indexOf(byte[] data, byte target, int from, int to) {
        for (int i = from; i < to; i++) {
            if (data[i] == target) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Counts the occurrences of a target byte in a byte array.
     *
     * @param data   The byte array.
     * @param target The target byte.
     * @return The number of occurrences.
     */
    public static int countOf(byte[] data, byte target) {
        int count = 0;
        for (byte b : data) {
            if (b == target) {
                count++;
            }
        }
        return count;
    }

    /**
     * Parses a byte array segment as Binary-Coded Decimal (BCD).
     *
     * @param data The byte array.
     * @param from The starting index (inclusive).
     * @param to   The ending index (exclusive).
     * @return The BCD string.
     */
    public static String bcd(byte[] data, int from, int to) {
        char[] chars = new char[2 * (to - from)];
        for (int i = from; i < to; i++) {
            int b = unsigned(data[i]);
            chars[2 * (i - from)] = (char) ((b >> 4) + 0x30);
            chars[2 * (i - from) + 1] = (char) ((b & 0xF) + 0x30);
        }
        return new String(chars);
    }

    /**
     * Converts a signed byte to an unsigned int value.
     *
     * @param data The byte.
     * @return The unsigned int value.
     */
    public static int unsigned(byte data) {
        if (data >= 0) {
            return data;
        }
        return Normal._256 + data;
    }

    /**
     * Calculates the XOR checksum of a byte array.
     *
     * @param data The byte array.
     * @return The XOR checksum value.
     */
    public static int xor(byte[] data) {
        int temp = 0;
        if (null != data) {
            for (int i = 0; i < data.length; i++) {
                temp ^= data[i];
            }
        }
        return temp;
    }

    /**
     * Concatenates two byte arrays into a new byte array.
     *
     * @param buf1 The first byte array.
     * @param buf2 The second byte array.
     * @return The new concatenated byte array.
     */
    public static byte[] concat(byte[] buf1, byte[] buf2) {
        byte[] buffer = new byte[buf1.length + buf2.length];
        int offset = 0;
        System.arraycopy(buf1, 0, buffer, offset, buf1.length);
        offset += buf1.length;
        System.arraycopy(buf2, 0, buffer, offset, buf2.length);
        return buffer;
    }

    /**
     * Parse a byte array into a string of hexadecimal digits including all array bytes as digits.
     *
     * @param bytes The byte array to represent.
     * @return A string of hex characters corresponding to the bytes. The string is upper case.
     */
    public static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b & 0xf0) >>> 4, 16));
            sb.append(Character.forDigit(b & 0x0f, 16));
        }
        return sb.toString().toUpperCase(Locale.ROOT);
    }

    /**
     * Parse a string of hexadecimal digits into a byte array.
     *
     * @param digits The string to be parsed.
     * @return a byte array with each pair of characters converted to a byte, or empty array if the string is not valid
     *         hex.
     */
    public static byte[] hexStringToByteArray(String digits) {
        int len = digits.length();
        // Check if string is valid hex
        if (!Pattern.VALID_HEX_PATTERN.matcher(digits).matches() || (len & 0x1) != 0) {
            return new byte[0];
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) (Character.digit(digits.charAt(i), 16) << 4
                    | Character.digit(digits.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Checks if the length is valid.
     *
     * @param len      The length.
     * @param numBytes The size of the primitive type in bytes.
     */
    private static void checkLength(int len, int numBytes) {
        if (len < 0 || (len % numBytes) != 0)
            throw new IllegalArgumentException("length: " + len);
    }

    /**
     * Swaps two bytes in a byte array.
     *
     * @param data The byte array.
     * @param a    The index of the first byte.
     * @param b    The index of the second byte.
     */
    private static void swap(byte[] data, int a, int b) {
        byte t = data[a];
        data[a] = data[b];
        data[b] = t;
    }

    /**
     * Swaps the last byte of the first array with the first byte of the second array.
     *
     * < * @param b1 The first byte array.
     * 
     * @param b2 The second byte array.
     */
    private static void swapLastFirst(byte[] b1, byte[] b2) {
        int last = b1.length - 1;
        byte t = b2[0];
        b2[0] = b1[last];
        b1[last] = t;
    }

}
