/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.net.url;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

import org.miaixz.bus.core.io.stream.FastByteArrayOutputStream;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Decodes URL-encoded strings of the {@code application/x-www-form-urlencoded} type.
 * <p>
 * The decoding process includes:
 * 
 * <pre>
 * 1. Converting "%20" to a space.
 * 2. Converting "%xy" to its corresponding character, where "xy" is a two-digit hexadecimal number.
 * 3. Skipping any malformed "%" patterns and outputting them directly.
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UrlDecoder implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852231876163L;

    /**
     * Decodes a URL path segment, which does not decode the plus sign ({@code +}).
     * <ol>
     * <li>Converts "%20" to a space.</li>
     * <li>Converts "%xy" to its corresponding character, where "xy" is a two-digit hexadecimal number.</li>
     * <li>Skips any malformed "%" patterns and outputting them directly.</li>
     * </ol>
     *
     * @param text    The URL-encoded string.
     * @param charset The character set for decoding; if {@code null}, no decoding is performed.
     * @return The decoded string.
     */
    public static String decodeForPath(final String text, final java.nio.charset.Charset charset) {
        return decode(text, charset, false);
    }

    /**
     * Decodes a URL-encoded string according to the rules at
     * <a href="https://url.spec.whatwg.org/#urlencoded-parsing">https://url.spec.whatwg.org/#urlencoded-parsing</a>.
     * 
     * <pre>
     * 1. Converts "+" and "%20" to a space (" ").
     * 2. Converts "%xy" to its corresponding character, where "xy" is a two-digit hexadecimal number.
     * 3. Skips any malformed "%" patterns and outputting them directly.
     * </pre>
     *
     * @param text The URL-encoded string.
     * @return The decoded string.
     */
    public static String decode(final String text) {
        return decode(text, Charset.UTF_8);
    }

    /**
     * Decodes a URL-encoded string according to the rules at
     * <a href="https://url.spec.whatwg.org/#urlencoded-parsing">https://url.spec.whatwg.org/#urlencoded-parsing</a>.
     * 
     * <pre>
     * 1. Converts "+" and "%20" to a space (" ").
     * 2. Converts "%xy" to its corresponding character, where "xy" is a two-digit hexadecimal number.
     * 3. Skips any malformed "%" patterns and outputting them directly.
     * </pre>
     *
     * @param text    The URL-encoded string.
     * @param charset The character set for decoding.
     * @return The decoded string.
     */
    public static String decode(final String text, final java.nio.charset.Charset charset) {
        return decode(text, charset, true);
    }

    /**
     * Decodes a URL-encoded string.
     * 
     * <pre>
     * 1. Converts "%20" to a space.
     * 2. Converts "%xy" to its corresponding character, where "xy" is a two-digit hexadecimal number.
     * 3. Skips any malformed "%" patterns and outputting them directly.
     * </pre>
     *
     * @param text          The URL-encoded string.
     * @param isPlusToSpace If {@code true}, converts the plus sign ({@code +}) to a space.
     * @return The decoded string.
     */
    public static String decode(final String text, final boolean isPlusToSpace) {
        return decode(text, Charset.UTF_8, isPlusToSpace);
    }

    /**
     * Decodes a URL-encoded string.
     * 
     * <pre>
     * 1. Converts "%20" to a space.
     * 2. Converts "%xy" to its corresponding character, where "xy" is a two-digit hexadecimal number.
     * 3. Skips any malformed "%" patterns and outputting them directly.
     * </pre>
     *
     * @param text          The URL-encoded string.
     * @param charset       The character set for decoding; if {@code null}, no decoding is performed.
     * @param isPlusToSpace If {@code true}, converts the plus sign ({@code +}) to a space.
     * @return The decoded string.
     */
    public static String decode(
            final String text,
            final java.nio.charset.Charset charset,
            final boolean isPlusToSpace) {
        if (null == charset) {
            return text;
        }
        if (null == text) {
            return null;
        }
        final int length = text.length();
        if (0 == length) {
            return Normal.EMPTY;
        }

        final StringBuilder result = new StringBuilder(length / 3);

        int begin = 0;
        char c;
        for (int i = 0; i < length; i++) {
            c = text.charAt(i);
            if (Symbol.C_PERCENT == c || CharKit.isHexChar(c)) {
                continue;
            }

            if (i > begin) {
                result.append(decodeSub(text, begin, i, charset, isPlusToSpace));
            }

            if (Symbol.C_PLUS == c && isPlusToSpace) {
                c = Symbol.C_SPACE;
            }

            result.append(c);
            begin = i + 1;
        }

        if (begin < length) {
            result.append(decodeSub(text, begin, length, charset, isPlusToSpace));
        }

        return result.toString();
    }

    /**
     * Decodes URL-encoded bytes.
     * 
     * <pre>
     * 1. Converts "+" and "%20" to a space.
     * 2. Converts "%xy" to its corresponding character, where "xy" is a two-digit hexadecimal number.
     * 3. Skips any malformed "%" patterns and outputting them directly.
     * </pre>
     *
     * @param bytes The URL-encoded bytes.
     * @return The decoded bytes.
     */
    public static byte[] decode(final byte[] bytes) {
        return decode(bytes, true);
    }

    /**
     * Decodes URL-encoded bytes.
     * 
     * <pre>
     * 1. Converts "%20" to a space.
     * 2. Converts "%xy" to its corresponding character, where "xy" is a two-digit hexadecimal number.
     * 3. Skips any malformed "%" patterns and outputting them directly.
     * </pre>
     *
     * @param bytes         The URL-encoded bytes.
     * @param isPlusToSpace If {@code true}, converts the plus sign ({@code +}) to a space.
     * @return The decoded bytes.
     */
    public static byte[] decode(final byte[] bytes, final boolean isPlusToSpace) {
        if (bytes == null) {
            return null;
        }
        final FastByteArrayOutputStream buffer = new FastByteArrayOutputStream(bytes.length / 3);
        int b;
        for (int i = 0; i < bytes.length; i++) {
            b = bytes[i];
            if (b == Symbol.C_PLUS) {
                buffer.write(isPlusToSpace ? Symbol.C_SPACE : b);
            } else if (b == Symbol.C_PERCENT) {
                if (i + 1 < bytes.length) {
                    final int u = CharKit.digit16(bytes[i + 1]);
                    if (u >= 0 && i + 2 < bytes.length) {
                        final int l = CharKit.digit16(bytes[i + 2]);
                        if (l >= 0) {
                            buffer.write((char) ((u << 4) + l));
                            i += 2;
                            continue;
                        }
                    }
                }
                buffer.write(b);
            } else {
                buffer.write(b);
            }
        }
        return buffer.toByteArray();
    }

    /**
     * Decodes a substring.
     *
     * @param text          The string.
     * @param begin         The beginning index, inclusive.
     * @param end           The ending index, exclusive.
     * @param charset       The character set.
     * @param isPlusToSpace If {@code true}, converts the plus sign ({@code +}) to a space.
     * @return The decoded substring.
     */
    private static String decodeSub(
            final String text,
            final int begin,
            final int end,
            final java.nio.charset.Charset charset,
            final boolean isPlusToSpace) {
        return new String(decode(text.substring(begin, end).getBytes(Charset.ISO_8859_1), isPlusToSpace), charset);
    }

    /**
     * Parses URL parameters (or POST key-value pairs) into a map.
     *
     * @param params  The parameter string (or a path with parameters).
     * @param charset The character set.
     * @return A map of the parameters.
     */
    public static Map<String, String> decodeMap(String params, String charset) {
        final Map<String, List<String>> paramsMap = decodeObject(params, charset);
        final Map<String, String> result = MapKit.newHashMap(paramsMap.size());
        List<String> list;
        for (Map.Entry<String, List<String>> entry : paramsMap.entrySet()) {
            list = entry.getValue();
            result.put(entry.getKey(), CollKit.isEmpty(list) ? null : list.get(0));
        }
        return result;
    }

    /**
     * Parses URL parameters (or POST key-value pairs) into a map where values are lists of strings.
     *
     * @param params  The parameter string (or a path with parameters).
     * @param charset The character set.
     * @return A map of the parameters.
     */
    public static Map<String, List<String>> decodeObject(String params, String charset) {
        if (StringKit.isBlank(params)) {
            return Collections.emptyMap();
        }

        int pathEndPos = params.indexOf(Symbol.C_QUESTION_MARK);
        if (pathEndPos > -1) {
            params = StringKit.subSuf(params, pathEndPos + 1);
        }

        final Map<String, List<String>> map = new LinkedHashMap<>();
        final int len = params.length();
        String name = null;
        int pos = 0; // Start position of the unprocessed string
        int i; // End position of the unprocessed string
        char c; // Current character
        for (i = 0; i < len; i++) {
            c = params.charAt(i);
            if (c == Symbol.C_EQUAL) { // Delimiter for key-value pair
                if (null == name) {
                    name = params.substring(pos, i);
                }
                pos = i + 1;
            } else if (c == Symbol.C_AND) { // Delimiter for parameter pair
                if (null == name && pos != i) {
                    addParam(map, params.substring(pos, i), Normal.EMPTY, charset);
                } else if (null != name) {
                    addParam(map, name, params.substring(pos, i), charset);
                    name = null;
                }
                pos = i + 1;
            }
        }

        if (pos != i) {
            if (null == name) {
                addParam(map, params.substring(pos, i), Normal.EMPTY, charset);
            } else {
                addParam(map, name, params.substring(pos, i), charset);
            }
        } else if (null != name) {
            addParam(map, name, Normal.EMPTY, charset);
        }

        return map;
    }

    /**
     * Adds a key-value pair to a map where the values are lists of strings.
     *
     * @param params  The parameter map.
     * @param name    The key.
     * @param value   The value.
     * @param charset The character set.
     */
    private static void addParam(Map<String, List<String>> params, String name, String value, String charset) {
        name = decode(name, Charset.parse(charset));
        value = decode(value, Charset.parse(charset));
        List<String> values = params.computeIfAbsent(name, k -> new ArrayList<>(1));
        values.add(value);
    }

}
