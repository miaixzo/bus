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
package org.miaixz.bus.image.nimble;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Value of one EXIF/TIFF tag, kept as its raw bytes with the byte order of the TIFF block it comes from, and decoded on
 * demand.
 *
 * @author Kimi Liu
 */
public final class ExifValue {

    /**
     * TIFF type identifier for unsigned 8-bit integer components.
     */
    public static final int BYTE = 1;
    /**
     * TIFF type identifier for NUL-terminated ASCII text components.
     */
    public static final int ASCII = 2;
    /**
     * TIFF type identifier for unsigned 16-bit integer components.
     */
    public static final int SHORT = 3;
    /**
     * TIFF type identifier for unsigned 32-bit integer components.
     */
    public static final int LONG = 4;
    /**
     * TIFF type identifier for unsigned rational components.
     */
    public static final int RATIONAL = 5;
    /**
     * TIFF type identifier for signed 8-bit integer components.
     */
    public static final int SBYTE = 6;
    /**
     * TIFF type identifier for uninterpreted byte components.
     */
    public static final int UNDEFINED = 7;
    /**
     * TIFF type identifier for signed 16-bit integer components.
     */
    public static final int SSHORT = 8;
    /**
     * TIFF type identifier for signed 32-bit integer components.
     */
    public static final int SLONG = 9;
    /**
     * TIFF type identifier for signed rational components.
     */
    public static final int SRATIONAL = 10;
    /**
     * TIFF type identifier for 32-bit floating-point components.
     */
    public static final int FLOAT = 11;
    /**
     * TIFF type identifier for 64-bit floating-point components.
     */
    public static final int DOUBLE = 12;

    /**
     * Unsigned TIFF rational denominator marking an unknown EXIF value.
     */
    private static final long UNKNOWN_DENOMINATOR = 0xFFFFFFFFL;

    /**
     * TIFF component type identifier.
     */
    private final int type;
    /**
     * Number of TIFF components in the raw value.
     */
    private final int count;
    /**
     * Raw encoded bytes backing this value.
     */
    private final byte[] data;
    /**
     * Byte order of the TIFF block containing this value.
     */
    private final ByteOrder order;

    /**
     * Stores a validated TIFF component sequence for decoding on demand.
     *
     * @param type  the TIFF component type identifier
     * @param count the number of TIFF components encoded by the value
     * @param data  the encoded payload bytes
     * @param order the byte order of the source TIFF payload
     */
    ExifValue(int type, int count, byte[] data, ByteOrder order) {
        this.type = type;
        this.count = count;
        this.data = data;
        this.order = order;
    }

    /**
     * Size in bytes of one component of the given TIFF type, or 0 for an unknown type.
     *
     * @param type the TIFF component type identifier
     * @return Size in bytes of one component of the given TIFF type, or 0 for an unknown type
     */
    static int componentSize(int type) {
        return switch (type) {
            case BYTE, ASCII, SBYTE, UNDEFINED -> 1;
            case SHORT, SSHORT -> 2;
            case LONG, SLONG, FLOAT -> 4;
            case RATIONAL, SRATIONAL, DOUBLE -> 8;
            default -> 0;
        };
    }

    /**
     * Returns tIFF component type identifier.
     *
     * @return the TIFF component type identifier
     */
    public int getType() {
        return type;
    }

    /**
     * Returns number of TIFF components in the raw value.
     *
     * @return number of TIFF components in the raw value
     */
    public int getCount() {
        return count;
    }

    /**
     * Copies the encoded TIFF component bytes.
     *
     * @return a copy of the raw value bytes
     */
    public byte[] bytes() {
        return data.clone();
    }

    /**
     * Decodes a text value: ASCII, or BYTE / UNDEFINED holding characters (e.g. ExifVersion). EXIF declares ASCII but
     * cameras commonly write UTF-8, so the bytes are decoded as UTF-8 up to the first NUL.
     *
     * @return the trimmed text, empty when there is none
     */
    public String asString() {
        int end = 0;
        while (end < data.length && data[end] != 0) {
            end++;
        }
        return new String(data, 0, end, StandardCharsets.UTF_8).trim();
    }

    /**
     * Decodes an integer value (BYTE, SHORT, LONG and their signed variants, UNDEFINED as unsigned bytes).
     *
     * @return the values, empty for a non-integer type
     */
    public long[] asLongs() {
        if (!isInteger()) {
            return new long[0];
        }
        ByteBuffer buf = buffer();
        long[] values = new long[count];
        for (int i = 0; i < count; i++) {
            values[i] = switch (type) {
                case SBYTE -> buf.get();
                case SHORT -> Short.toUnsignedLong(buf.getShort());
                case SSHORT -> buf.getShort();
                case LONG -> Integer.toUnsignedLong(buf.getInt());
                case SLONG -> buf.getInt();
                default -> Byte.toUnsignedLong(buf.get()); // BYTE, UNDEFINED
            };
        }
        return values;
    }

    /**
     * Tests whether the TIFF components use a rational or floating-point encoding.
     *
     * @return true for the rational and floating-point types
     */
    public boolean isFractional() {
        return switch (type) {
            case RATIONAL, SRATIONAL, FLOAT, DOUBLE -> true;
            default -> false;
        };
    }

    /**
     * Tests whether the TIFF type can be decoded as integral components.
     *
     * @return whether this value uses an integer-compatible TIFF type
     */
    private boolean isInteger() {
        return switch (type) {
            case BYTE, UNDEFINED, SBYTE, SHORT, SSHORT, LONG, SLONG -> true;
            default -> false;
        };
    }

    /**
     * Decodes a numeric value as decimals. A rational whose denominator is 0 or FFFFFFFFH (the EXIF markers of an
     * unknown value) gives {@link Double#NaN}.
     *
     * @return the values, empty for a non-numeric type
     */
    public double[] asDoubles() {
        ByteBuffer buf = buffer();
        double[] values = new double[count];
        switch (type) {
            case RATIONAL -> {
                for (int i = 0; i < count; i++) {
                    values[i] = rational(Integer.toUnsignedLong(buf.getInt()), Integer.toUnsignedLong(buf.getInt()));
                }
            }
            case SRATIONAL -> {
                for (int i = 0; i < count; i++) {
                    int numerator = buf.getInt();
                    int denominator = buf.getInt();
                    // FFFFFFFFH is -1 as a signed value: still the "unknown" marker
                    values[i] = denominator == -1 ? Double.NaN : rational(numerator, denominator);
                }
            }
            case FLOAT -> {
                for (int i = 0; i < count; i++) {
                    values[i] = buf.getFloat();
                }
            }
            case DOUBLE -> {
                for (int i = 0; i < count; i++) {
                    values[i] = buf.getDouble();
                }
            }
            default -> {
                return Arrays.stream(asLongs()).asDoubleStream().toArray();
            }
        }
        return values;
    }

    /**
     * Decodes a rational component, treating reserved unknown denominators as NaN.
     *
     * @param numerator   the rational numerator
     * @param denominator the rational denominator, including EXIF unknown-value sentinels
     * @return the quotient, or NaN for an unknown denominator
     */
    private static double rational(long numerator, long denominator) {
        if (denominator == 0 || denominator == UNKNOWN_DENOMINATOR) {
            return Double.NaN;
        }
        return numerator / (double) denominator;
    }

    /**
     * Wraps the raw EXIF bytes using their TIFF byte order.
     *
     * @return a new buffer view positioned at the first component
     */
    private ByteBuffer buffer() {
        return ByteBuffer.wrap(data).order(order);
    }

    /**
     * Formats the TIFF component type and count without exposing raw metadata bytes.
     *
     * @return the diagnostic TIFF value description
     */
    @Override
    public String toString() {
        return "ExifValue{type=" + type + ", count=" + count + '}';
    }

}
