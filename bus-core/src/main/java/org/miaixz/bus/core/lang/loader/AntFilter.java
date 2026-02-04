/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
