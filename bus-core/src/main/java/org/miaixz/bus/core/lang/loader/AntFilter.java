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
package org.miaixz.bus.core.lang.loader;

import org.miaixz.bus.core.lang.Symbol;

/**
 * An ANT-style path filter that extends {@link RegexFilter} to provide filtering based on ANT-style path expressions.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AntFilter extends RegexFilter implements Filter {

    /**
     * Special characters in ANT-style paths that need to be escaped when converting to regular expressions.
     */
    public static final String[] SYMBOLS = { Symbol.BACKSLASH, Symbol.DOLLAR, Symbol.PARENTHESE_LEFT,
            Symbol.PARENTHESE_RIGHT, Symbol.PLUS, Symbol.DOT, Symbol.BRACKET_LEFT, Symbol.BRACKET_RIGHT, Symbol.CARET,
            Symbol.BRACE_LEFT, Symbol.BRACE_RIGHT, Symbol.OR };

    /**
     * Constructs an {@code AntFilter} with the given ANT-style path expression.
     *
     * @param ant The ANT-style path expression.
     */
    public AntFilter(String ant) {
        super(convert(ant));
    }

    /**
     * Converts an ANT-style path expression into a regular expression.
     *
     * @param ant The ANT-style path expression to convert.
     * @return The corresponding regular expression string.
     */
    private static String convert(String ant) {
        String regex = ant;
        for (String symbol : SYMBOLS)
            regex = regex.replace(symbol, Symbol.C_BACKSLASH + symbol);
        regex = regex.replace(Symbol.QUESTION_MARK, ".{1}");
        regex = regex.replace(Symbol.STAR + Symbol.STAR + Symbol.SLASH, "(.{0,}?/){0,}?");
        regex = regex.replace(Symbol.STAR + Symbol.STAR, ".{0,}?");
        regex = regex.replace(Symbol.STAR, "[^/]{0,}?");
        while (regex.startsWith(Symbol.SLASH))
            regex = regex.substring(1);
        while (regex.endsWith(Symbol.SLASH))
            regex = regex.substring(0, regex.length() - 1);
        return regex;
    }

}
