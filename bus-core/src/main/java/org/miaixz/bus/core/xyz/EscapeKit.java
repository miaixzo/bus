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

import java.util.function.Predicate;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.escape.Html4Escape;
import org.miaixz.bus.core.text.escape.Html4Unescape;
import org.miaixz.bus.core.text.escape.XmlEscape;
import org.miaixz.bus.core.text.escape.XmlUnescape;

/**
 * Utility class for escaping and unescaping strings. This is equivalent to JavaScript's `escape()` and `unescape()`
 * functions, which encode strings using the ISO Latin character set. All spaces, punctuation, special characters, and
 * other non-ASCII characters are converted to %xx format.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EscapeKit {

    /**
     * Characters that should not be escaped for JS compatibility.
     */
    private static final String NOT_ESCAPE_CHARS = "*@-_+./";
    private static final Predicate<Character> JS_ESCAPE_FILTER = c -> !(Character.isDigit(c) || Character.isLowerCase(c)
            || Character.isUpperCase(c) || StringKit.contains(NOT_ESCAPE_CHARS, c));

    /**
     * Escapes special characters in XML.
     * 
     * <pre>
     * &amp; (ampersand) becomes &amp;amp;
     * &lt; (less than) becomes &amp;lt;
     * &gt; (greater than) becomes &amp;gt;
     * &quot; (double quote) becomes &amp;quot;
     * ' (single quote / apostrophe) becomes &amp;apos;
     * </pre>
     *
     * @param xml The XML text.
     * @return The escaped text.
     */
    public static String escapeXml(final CharSequence xml) {
        final XmlEscape escape = new XmlEscape();
        return escape.apply(xml).toString();
    }

    /**
     * Unescapes special characters in XML.
     *
     * @param xml The XML text.
     * @return The unescaped text.
     */
    public static String unescapeXml(final CharSequence xml) {
        final XmlUnescape unescape = new XmlUnescape();
        return unescape.apply(xml).toString();
    }

    /**
     * Escapes special characters in HTML4.
     *
     * @param html The HTML text.
     * @return The escaped text.
     */
    public static String escapeHtml4(final CharSequence html) {
        final Html4Escape escape = new Html4Escape();
        return escape.apply(html).toString();
    }

    /**
     * Unescapes special characters in HTML4.
     *
     * @param html The HTML text.
     * @return The unescaped text.
     */
    public static String unescapeHtml4(final CharSequence html) {
        final Html4Unescape unescape = new Html4Unescape();
        return unescape.apply(html).toString();
    }

    /**
     * Escapes a string using JavaScript's `escape()` style (Unicode). This method does not encode ASCII letters,
     * digits, or the following symbols: * @ - _ + . /
     *
     * @param content The content to be escaped.
     * @return The escaped string.
     */
    public static String escape(final CharSequence content) {
        return escape(content, JS_ESCAPE_FILTER);
    }

    /**
     * Escapes a string using JavaScript's `escape()` style (Unicode), escaping all non-alphanumeric characters.
     *
     * @param content The content to be escaped.
     * @return The escaped string.
     */
    public static String escapeAll(final CharSequence content) {
        return escape(content, c -> true);
    }

    /**
     * Escapes a string using JavaScript's `escape()` style (Unicode) with a custom filter.
     *
     * @param content The content to be escaped.
     * @param filter  A predicate that returns `true` for characters that should be escaped.
     * @return The escaped string.
     */
    public static String escape(final CharSequence content, final Predicate<Character> filter) {
        if (StringKit.isEmpty(content)) {
            return StringKit.toStringOrNull(content);
        }

        final StringBuilder tmp = new StringBuilder(content.length() * 6);
        char c;
        for (int i = 0; i < content.length(); i++) {
            c = content.charAt(i);
            if (!filter.test(c)) {
                tmp.append(c);
            } else if (c < 256) {
                tmp.append(Symbol.PERCENT);
                if (c < 16) {
                    tmp.append(Symbol.ZERO);
                }
                tmp.append(Integer.toString(c, 16));
            } else {
                tmp.append("%u");
                if (c <= 0xfff) {
                    // Pad with a leading zero if necessary
                    tmp.append(Symbol.ZERO);
                }
                tmp.append(Integer.toString(c, 16));
            }
        }
        return tmp.toString();
    }

    /**
     * Decodes a string using JavaScript's `unescape()` style.
     *
     * @param content The escaped content.
     * @return The decoded string.
     */
    public static String unescape(final String content) {
        if (StringKit.isBlank(content)) {
            return content;
        }

        final StringBuilder tmp = new StringBuilder(content.length());
        int lastPos = 0;
        int pos;
        char ch;
        while (lastPos < content.length()) {
            pos = content.indexOf(Symbol.PERCENT, lastPos);
            if (pos == lastPos) {
                if (content.charAt(pos + 1) == 'u') {
                    ch = (char) Integer.parseInt(content.substring(pos + 2, pos + 6), 16);
                    tmp.append(ch);
                    lastPos = pos + 6;
                } else {
                    ch = (char) Integer.parseInt(content.substring(pos + 1, pos + 3), 16);
                    tmp.append(ch);
                    lastPos = pos + 3;
                }
            } else {
                if (pos == -1) {
                    tmp.append(content.substring(lastPos));
                    lastPos = content.length();
                } else {
                    tmp.append(content, lastPos, pos);
                    lastPos = pos;
                }
            }
        }
        return tmp.toString();
    }

    /**
     * Safely unescapes a string. If the string is not in a valid escaped format, the original string is returned.
     *
     * @param content The content.
     * @return The decoded string, or the original string on failure.
     */
    public static String safeUnescape(final String content) {
        try {
            return unescape(content);
        } catch (final Exception e) {
            // Ignore Exception
        }
        return content;
    }

}
