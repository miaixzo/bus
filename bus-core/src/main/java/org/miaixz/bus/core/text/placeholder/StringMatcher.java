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
package org.miaixz.bus.core.text.placeholder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * String pattern matching utility that uses {@code ${XXXXX}} as variables. This class provides functionality to match a
 * given text against a predefined pattern containing placeholders and extract the corresponding values. For example:
 *
 * <pre>
 *     pattern: ${name}-${age}-${gender}-${country}-${province}-${city}-${status}
 *     text:    "John-19-Male-USA-California-LosAngeles-Married"
 *     result:  {name=John, age=19, gender=Male, country=USA, province=California, city=LosAngeles, status=Married}
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringMatcher {

    /**
     * The list of parsed patterns, where each element is either a literal string or a variable placeholder. Variable
     * placeholders are represented as `${variableName}`.
     */
    private final List<String> patterns;

    /**
     * Constructs a {@code StringMatcher} with the given pattern. Variables are denoted by {@code ${XXX}} placeholders.
     *
     * @param pattern The pattern string with {@code ${XXX}} placeholders.
     */
    public StringMatcher(final String pattern) {
        this.patterns = parse(pattern);
    }

    /**
     * Parses the given pattern string into a list of literal strings and variable placeholders. Variables are denoted
     * by {@code ${XXXX}} placeholders. For example, a pattern like "Hello ${name}! Your age is ${age}." would be parsed
     * into a list like ["Hello ", "${name}", "! Your age is ", "${age}", "."].
     *
     * @param pattern The pattern string to parse.
     * @return A list of strings representing the parsed pattern segments, including literal parts and variable
     *         placeholders.
     */
    private static List<String> parse(final String pattern) {
        final List<String> patterns = new ArrayList<>();
        final int length = pattern.length();
        char c = 0;
        char pre;
        boolean inVar = false;
        final StringBuilder part = StringKit.builder();
        for (int i = 0; i < length; i++) {
            pre = c;
            c = pattern.charAt(i);
            if (inVar) {
                part.append(c);
                if ('}' == c) {
                    // Variable end
                    inVar = false;
                    patterns.add(part.toString());
                    part.setLength(0);
                }
            } else if ('{' == c && Symbol.C_DOLLAR == pre) {
                // Variable start
                inVar = true;
                final String preText = part.substring(0, part.length() - 1);
                if (StringKit.isNotEmpty(preText)) {
                    patterns.add(preText);
                }
                part.setLength(0);
                part.append(pre).append(c);
            } else {
                // Normal character
                part.append(c);
            }
        }

        if (part.length() > 0) {
            patterns.add(part.toString());
        }
        return patterns;
    }

    /**
     * Matches the given text against the pattern and extracts the matched content for each variable. The method
     * iterates through the parsed pattern segments and attempts to find corresponding literal strings and variable
     * values within the input text.
     *
     * @param text The text to be matched against the pattern.
     * @return A map where keys are variable names (e.g., "name", "age") and values are the matched content from the
     *         text. Returns an empty map if the pattern does not match the text or if no variables are found.
     */
    public Map<String, String> match(final String text) {
        final HashMap<String, String> result = MapKit.newHashMap(true);
        int from = 0;
        String key = null;
        int to;
        for (final String part : patterns) {
            if (StringKit.isWrap(part, "${", "}")) {
                // Variable
                key = StringKit.sub(part, 2, part.length() - 1);
            } else {
                to = text.indexOf(part, from);
                if (to < 0) {
                    // If a literal string is not found, the entire pattern does not match.
                    return MapKit.empty();
                }
                if (null != key && to > from) {
                    // Content for the variable part exists.
                    result.put(key, text.substring(from, to));
                }
                // The next starting point is the end of the literal string.
                from = to + part.length();
                key = null;
            }
        }

        if (null != key && from < text.length()) {
            // Content for the variable part exists until the end of the text.
            result.put(key, text.substring(from));
        }

        return result;
    }

}
