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

import java.awt.*;
import java.math.BigInteger;

import org.miaixz.bus.core.codec.binary.Hex;
import org.miaixz.bus.core.codec.binary.provider.Base16Provider;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Hexadecimal (abbreviated as hex or subscript 16) is a base-16 numeral system in mathematics, generally represented by
 * digits 0 to 9 and letters A to F (where A-F are 10-15). For example, the decimal number 57 is written as 111001 in
 * binary and 39 in hexadecimal. Languages like Java and C distinguish hexadecimal from decimal values by prefixing
 * hexadecimal numbers with 0x, for instance, 0x20 is decimal 32, not decimal 20. This utility class provides
 * hexadecimal related tools, including encoding and decoding inherited from {@link Hex}, as well as other conversion
 * and identification tools.
 *
 * @author Kimi Liu
 * @see Hex
 * @since Java 17+
 */
public class HexKit extends Hex {

    /**
     * Constructs a new HexKit. Utility class constructor for static access.
     */
    private HexKit() {
    }

    /**
     * Encodes a {@link Color} object into its hexadecimal string representation.
     *
     * @param color The {@link Color} object to encode.
     * @return The hexadecimal string representation of the color.
     */
    public static String encodeColor(final Color color) {
        return encodeColor(color, Symbol.HASH);
    }

    /**
     * Encodes a {@link Color} object into its hexadecimal string representation with a specified prefix.
     *
     * @param color  The {@link Color} object to encode.
     * @param prefix The prefix string, such as '#' or '0x'.
     * @return The hexadecimal string representation of the color.
     */
    public static String encodeColor(final Color color, final String prefix) {
        final StringBuilder builder = new StringBuilder(prefix);
        String colorHex;
        colorHex = Integer.toHexString(color.getRed());
        if (1 == colorHex.length()) {
            builder.append('0');
        }
        builder.append(colorHex);
        colorHex = Integer.toHexString(color.getGreen());
        if (1 == colorHex.length()) {
            builder.append('0');
        }
        builder.append(colorHex);
        colorHex = Integer.toHexString(color.getBlue());
        if (1 == colorHex.length()) {
            builder.append('0');
        }
        builder.append(colorHex);
        return builder.toString();
    }

    /**
     * Decodes a hexadecimal color string into a {@link Color} object.
     *
     * @param hexColor The hexadecimal color string, which can start with '#' or '0x'.
     * @return The {@link Color} object represented by the hexadecimal string.
     */
    public static Color decodeColor(final String hexColor) {
        return Color.decode(hexColor);
    }

    /**
     * Checks if the given string is a valid hexadecimal number. If it is, it can be decoded using the {@code decode}
     * method of the corresponding numeric type object, for example, {@code Integer.decode} for an int type hexadecimal
     * number.
     *
     * @param value The string value to check.
     * @return {@code true} if the string is a hexadecimal number, {@code false} otherwise.
     */
    public static boolean isHexNumber(final String value) {
        if (StringKit.isEmpty(value) || StringKit.startWith(value, Symbol.C_MINUS)) {
            return false;
        }
        int index = 0;
        if (value.startsWith("0x", index) || value.startsWith("0X", index)) {
            index += 2;
        } else if (value.startsWith(Symbol.HASH, index)) {
            index++;
        }
        try {
            new BigInteger(value.substring(index), 16);
        } catch (final NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Converts a given int value to its Unicode hexadecimal string representation. This is commonly used for converting
     * special characters (e.g., Chinese characters) to Unicode form. If the hexadecimal string after 'u' is less than 4
     * digits, it will be padded with leading zeros. For example:
     *
     * <pre>
     * 你 = \u4f60
     * </pre>
     *
     * @param value The int value, which can also be a char.
     * @return The Unicode hexadecimal representation.
     */
    public static String toUnicodeHex(final int value) {
        final StringBuilder builder = new StringBuilder(6);

        builder.append("\\u");
        final String hex = toHex(value);
        final int len = hex.length();
        if (len < 4) {
            builder.append("0000", 0, 4 - len); // Pad with leading zeros if less than 4 digits
        }
        builder.append(hex);

        return builder.toString();
    }

    /**
     * Converts a given char value to its Unicode hexadecimal string representation. This is commonly used for
     * converting special characters (e.g., Chinese characters) to Unicode form. If the hexadecimal string after 'u' is
     * less than 4 digits, it will be padded with leading zeros. For example:
     *
     * <pre>
     * 你 = \u4f60
     * </pre>
     *
     * @param ch The char value.
     * @return The Unicode hexadecimal representation.
     */
    public static String toUnicodeHex(final char ch) {
        return Base16Provider.CODEC_LOWER.toUnicodeHex(ch);
    }

    /**
     * Converts an int value to its hexadecimal string representation.
     *
     * @param value The int value.
     * @return The hexadecimal string representation.
     */
    public static String toHex(final int value) {
        return Integer.toHexString(value);
    }

    /**
     * Converts a hexadecimal string to an int value.
     *
     * @param value The hexadecimal string.
     * @return The int value represented by the hexadecimal string.
     */
    public static int hexToInt(final String value) {
        return Integer.parseInt(removeHexPrefix(value), 16);
    }

    /**
     * Converts a long value to its hexadecimal string representation.
     *
     * @param value The long value.
     * @return The hexadecimal string representation.
     */
    public static String toHex(final long value) {
        return Long.toHexString(value);
    }

    /**
     * Converts a hexadecimal string to a long value.
     *
     * @param value The hexadecimal string.
     * @return The long value represented by the hexadecimal string.
     */
    public static long hexToLong(final String value) {
        return Long.parseLong(removeHexPrefix(value), 16);
    }

    /**
     * Converts a float value to its hexadecimal string representation.
     *
     * @param value the float value to convert
     * @return the hexadecimal string
     */
    public static String toHex(float value) {
        return Integer.toHexString(Float.floatToIntBits(value));
    }

    /**
     * Converts a hexadecimal string to a float value.
     *
     * @param value the hexadecimal string
     * @return the float value derived from the hex string
     */
    public static float hexToFloat(String value) {
        return Float.intBitsToFloat(Integer.parseUnsignedInt(removeHexPrefix(value), 16));
    }

    /**
     * Converts a double value to its hexadecimal string representation.
     *
     * @param value the double value to convert
     * @return the hexadecimal string
     */
    public static String toHex(double value) {
        return Long.toHexString(Double.doubleToLongBits(value));
    }

    /**
     * Converts a hexadecimal string to a double value.
     *
     * @param value the hexadecimal string
     * @return the double value derived from the hex string
     */
    public static double hexToDouble(String value) {
        return Double.longBitsToDouble(Long.parseUnsignedLong(removeHexPrefix(value), 16));
    }

    /**
     * Converts a byte value to its hexadecimal representation and appends it to a {@link StringBuilder}.
     *
     * @param builder     The {@link StringBuilder} to append to.
     * @param b           The byte value.
     * @param toLowerCase {@code true} to use lowercase hexadecimal characters, {@code false} for uppercase.
     */
    public static void appendHex(final StringBuilder builder, final byte b, final boolean toLowerCase) {
        (toLowerCase ? Base16Provider.CODEC_LOWER : Base16Provider.CODEC_UPPER).appendHex(builder, b);
    }

    /**
     * Converts a hexadecimal string to a {@link BigInteger}.
     *
     * @param text The hexadecimal string.
     * @return The {@link BigInteger} represented by the hexadecimal string, or {@code null} if the input string is
     *         {@code null}.
     */
    public static BigInteger toBigInteger(final String text) {
        if (null == text) {
            return null;
        }
        return new BigInteger(removeHexPrefix(text), 16);
    }

    /**
     * Formats a hexadecimal string by inserting a space every two characters. For example:
     *
     * <pre>
     * e8 8c 67 03 80 cb 22 00 95 26 8f
     * </pre>
     *
     * @param text The hexadecimal string to format.
     * @return The formatted string.
     */
    public static String format(final String text) {
        return format(text, Normal.EMPTY);
    }

    /**
     * Formats a hexadecimal string by inserting a space and a custom prefix every two characters. For example:
     *
     * <pre>
     * e8 8c 67 03 80 cb 22 00 95 26 8f
     * </pre>
     *
     * @param text   The hexadecimal string to format.
     * @param prefix The custom prefix to insert, such as "0x". If {@code null}, an empty string is used.
     * @return The formatted string.
     */
    public static String format(final String text, final String prefix) {
        return format(text, prefix, Symbol.SPACE);
    }

    /**
     * Formats a hexadecimal string by inserting a custom separator and prefix every two characters. For example:
     *
     * <pre>
     * e8 8c 67 03 80 cb 22 00 95 26 8f
     * </pre>
     *
     * @param text      The hexadecimal string to format.
     * @param prefix    The custom prefix, such as "0x".
     * @param separator The custom separator, such as a space.
     * @return The formatted string.
     */
    public static String format(final String text, String prefix, String separator) {
        if (StringKit.isEmpty(text)) {
            return Normal.EMPTY;
        }
        if (null == prefix) {
            prefix = Normal.EMPTY;
        }
        if (null == separator) {
            separator = Symbol.SPACE;
        }

        final int length = text.length();
        final StringBuilder builder = StringKit.builder(length + length / 2 + (length / 2 * prefix.length()));
        for (int i = 0; i < length; i++) {
            if (i % 2 == 0) {
                if (i != 0) {
                    builder.append(separator);
                }
                builder.append(prefix);
            }
            builder.append(text.charAt(i));
        }
        return builder.toString();
    }

    /**
     * Removes standard hexadecimal prefixes from a string. The supported prefixes are "0x", "0X", and "#".
     *
     * @param text The hexadecimal string.
     * @return The string with the prefix removed.
     */
    private static String removeHexPrefix(final String text) {
        if (StringKit.length(text) > 1) {
            final char c0 = text.charAt(0);
            switch (c0) {
                case '0':
                    if (text.charAt(1) == 'x' || text.charAt(1) == 'X') {
                        return text.substring(2);
                    }
                case '#':
                    return text.substring(1);
            }
        }
        return text;
    }

}
