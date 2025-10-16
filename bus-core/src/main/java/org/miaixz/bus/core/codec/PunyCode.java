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
package org.miaixz.bus.core.codec;

import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Punycode is an encoding system defined by RFC 3492, primarily used to convert domain names from Unicode (used by
 * local languages) into an ASCII-compatible encoding suitable for the DNS system. Reference: <a href=
 * "https://blog.csdn.net/a19881029/article/details/18262671">https://blog.csdn.net/a19881029/article/details/18262671</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PunyCode {

    /**
     * Minimum value for T.
     */
    private static final int TMIN = 1;
    /**
     * Maximum value for T.
     */
    private static final int TMAX = 26;
    /**
     * Base for Punycode encoding, typically 36 (26 letters + 10 digits).
     */
    private static final int BASE = 36;
    /**
     * Initial value for n, the smallest code point that has not yet been seen in the output.
     */
    private static final int INITIAL_N = 128;
    /**
     * Initial bias value for the Punycode algorithm.
     */
    private static final int INITIAL_BIAS = 72;
    /**
     * Dampening factor used in the adapt function.
     */
    private static final int DAMP = 700;
    /**
     * Skew factor used in the adapt function.
     */
    private static final int SKEW = 38;
    /**
     * The prefix for Punycode encoded domain names.
     */
    private static final String PUNY_CODE_PREFIX = "xn--";

    /**
     * Encodes a domain name to Punycode. This method handles domain names with multiple parts separated by dots,
     * encoding each part individually. The dot separators themselves are not encoded.
     *
     * @param domain The domain name to encode. Must not be null.
     * @return The Punycode encoded domain name.
     * @throws InternalException If an error occurs during encoding, such as an overflow.
     */
    public static String encodeDomain(final String domain) throws InternalException {
        Assert.notNull(domain, "domain must not be null!");
        final List<String> split = CharsBacker.split(domain, Symbol.DOT);
        final StringBuilder result = new StringBuilder(domain.length() * 4);
        for (final String text : split) {
            if (result.length() != 0) {
                result.append(Symbol.C_DOT);
            }
            result.append(encode(text, true));
        }

        return result.toString();
    }

    /**
     * Encodes the given character sequence to Punycode without the "xn--" prefix.
     *
     * @param input The character sequence to encode. Must not be null.
     * @return The Punycode encoded string.
     * @throws InternalException If an error occurs during encoding, such as an overflow.
     */
    public static String encode(final CharSequence input) throws InternalException {
        return encode(input, false);
    }

    /**
     * Encodes the given character sequence to Punycode.
     *
     * @param input      The character sequence to encode. Must not be null.
     * @param withPrefix A boolean indicating whether to prepend the "xn--" prefix to the encoded string.
     * @return The Punycode encoded string.
     * @throws InternalException If an error occurs during encoding, such as an overflow.
     */
    public static String encode(final CharSequence input, final boolean withPrefix) throws InternalException {
        Assert.notNull(input, "input must not be null!");
        int n = INITIAL_N;
        int delta = 0;
        int bias = INITIAL_BIAS;
        final int length = input.length();
        final StringBuilder output = new StringBuilder(length * 4);
        // Copy all basic code points to the output
        int b = 0;
        for (int i = 0; i < length; i++) {
            final char c = input.charAt(i);
            if (isBasic(c)) {
                output.append(c);
                b++;
            }
        }
        // Append delimiter
        if (b > 0) {
            if (b == length) {
                // No characters need to be encoded
                return output.toString();
            }
            output.append(Symbol.C_MINUS);
        }
        int h = b;
        while (h < length) {
            int m = Integer.MAX_VALUE;
            // Find the minimum code point >= n
            for (int i = 0; i < length; i++) {
                final char c = input.charAt(i);
                if (c >= n && c < m) {
                    m = c;
                }
            }
            if (m - n > (Integer.MAX_VALUE - delta) / (h + 1)) {
                throw new InternalException("OVERFLOW");
            }
            delta = delta + (m - n) * (h + 1);
            n = m;
            for (int j = 0; j < length; j++) {
                final int c = input.charAt(j);
                if (c < n) {
                    delta++;
                    if (0 == delta) {
                        throw new InternalException("OVERFLOW");
                    }
                }
                if (c == n) {
                    int q = delta;
                    for (int k = BASE;; k += BASE) {
                        final int t;
                        if (k <= bias) {
                            t = TMIN;
                        } else if (k >= bias + TMAX) {
                            t = TMAX;
                        } else {
                            t = k - bias;
                        }
                        if (q < t) {
                            break;
                        }
                        output.append((char) digit2codepoint(t + (q - t) % (BASE - t)));
                        q = (q - t) / (BASE - t);
                    }
                    output.append((char) digit2codepoint(q));
                    bias = adapt(delta, h + 1, h == b);
                    delta = 0;
                    h++;
                }
            }
            delta++;
            n++;
        }

        if (withPrefix) {
            output.insert(0, PUNY_CODE_PREFIX);
        }
        return output.toString();
    }

    /**
     * Decodes a Punycode encoded domain name back to its original form. This method handles domain names with multiple
     * parts separated by dots.
     *
     * @param domain The Punycode encoded domain name to decode. Must not be null.
     * @return The decoded domain name.
     * @throws InternalException If an error occurs during decoding, such as an invalid input format.
     */
    public static String decodeDomain(final String domain) throws InternalException {
        Assert.notNull(domain, "domain must not be null!");
        final List<String> split = CharsBacker.split(domain, Symbol.DOT);
        final StringBuilder result = new StringBuilder(domain.length() / 4 + 1);
        for (final String text : split) {
            if (result.length() != 0) {
                result.append(Symbol.C_DOT);
            }
            result.append(StringKit.startWithIgnoreEquals(text, PUNY_CODE_PREFIX) ? decode(text) : text);
        }

        return result.toString();
    }

    /**
     * Decodes a Punycode encoded string back to its original character sequence. This method automatically handles the
     * "xn--" prefix if present.
     *
     * @param input The Punycode encoded string to decode. Must not be null.
     * @return The decoded character sequence.
     * @throws InternalException If an error occurs during decoding, such as an invalid input format or overflow.
     */
    public static String decode(String input) throws InternalException {
        Assert.notNull(input, "input must not be null!");
        input = StringKit.removePrefixIgnoreCase(input, PUNY_CODE_PREFIX);

        int n = INITIAL_N;
        int i = 0;
        int bias = INITIAL_BIAS;
        final int length = input.length();
        final StringBuilder output = new StringBuilder(length / 4 + 1);
        int d = input.lastIndexOf(Symbol.C_MINUS);
        if (d > 0) {
            for (int j = 0; j < d; j++) {
                final char c = input.charAt(j);
                if (isBasic(c)) {
                    output.append(c);
                }
            }
            d++;
        } else {
            d = 0;
        }
        while (d < length) {
            final int oldi = i;
            int w = 1;
            for (int k = BASE;; k += BASE) {
                if (d == length) {
                    throw new InternalException("BAD_INPUT");
                }
                final int c = input.charAt(d++);
                final int digit = codepoint2digit(c);
                if (digit > (Integer.MAX_VALUE - i) / w) {
                    throw new InternalException("OVERFLOW");
                }
                i = i + digit * w;
                final int t;
                if (k <= bias) {
                    t = TMIN;
                } else if (k >= bias + TMAX) {
                    t = TMAX;
                } else {
                    t = k - bias;
                }
                if (digit < t) {
                    break;
                }
                w = w * (BASE - t);
            }
            bias = adapt(i - oldi, output.length() + 1, oldi == 0);
            if (i / (output.length() + 1) > Integer.MAX_VALUE - n) {
                throw new InternalException("OVERFLOW");
            }
            n = n + i / (output.length() + 1);
            i = i % (output.length() + 1);
            output.insert(i, (char) n);
            i++;
        }

        return output.toString();
    }

    /**
     * Adapts the bias value based on the delta, number of points, and whether it's the first adaptation. This function
     * is a core part of the Punycode algorithm's variable-length integer encoding.
     *
     * @param delta     The delta value from the encoding/decoding process.
     * @param numpoints The number of code points processed so far.
     * @param first     A boolean indicating if this is the first adaptation.
     * @return The new bias value.
     */
    private static int adapt(int delta, final int numpoints, final boolean first) {
        if (first) {
            delta = delta / DAMP;
        } else {
            delta = delta / 2;
        }
        delta = delta + (delta / numpoints);
        int k = 0;
        while (delta > ((BASE - TMIN) * TMAX) / 2) {
            delta = delta / (BASE - TMIN);
            k = k + BASE;
        }
        return k + ((BASE - TMIN + 1) * delta) / (delta + SKEW);
    }

    /**
     * Checks if a given character is a "basic" code point (ASCII character). Basic code points are those with a value
     * less than 0x80 (128).
     *
     * @param c The character to check.
     * @return {@code true} if the character is a basic code point, {@code false} otherwise.
     */
    private static boolean isBasic(final char c) {
        return c < 0x80;
    }

    /**
     * Converts a digit (0-35) to its corresponding character representation used in Punycode. The mapping is as
     * follows:
     * 
     * <pre>
     *     0-25  -> 'a'-'z'
     *     26-35 -> '0'-'9'
     * </pre>
     *
     * @param d The digit to convert (must be between 0 and 35, inclusive).
     * @return The character representation of the digit.
     * @throws InternalException If the input digit is out of the valid range.
     */
    private static int digit2codepoint(final int d) throws InternalException {
        Assert.checkBetween(d, 0, 35);
        if (d < 26) {
            // 0..25 : 'a'..'z'
            return d + 'a';
        } else if (d < 36) {
            // 26..35 : '0'..'9';
            return d - 26 + '0';
        } else {
            throw new InternalException("BAD_INPUT");
        }
    }

    /**
     * Converts a character (representing a Punycode digit) to its corresponding integer digit value. The mapping is as
     * follows:
     * 
     * <pre>
     *     'a'-'z' -> 0-25
     *     '0'-'9' -> 26-35
     * </pre>
     *
     * @param c The character to convert.
     * @return The integer digit value.
     * @throws InternalException If the input character is not a valid Punycode digit character.
     */
    private static int codepoint2digit(final int c) throws InternalException {
        if (c - '0' < 10) {
            // '0'..'9' : 26..35
            return c - '0' + 26;
        } else if (c - 'a' < 26) {
            // 'a'..'z' : 0..25
            return c - 'a';
        } else {
            throw new InternalException("BAD_INPUT");
        }
    }

}
