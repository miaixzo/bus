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
package org.miaixz.bus.shade.safety.complex;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.loader.AntFilter;
import org.miaixz.bus.shade.safety.Complex;

/**
 * An abstract {@link Complex} implementation that filters entries based on Ant-style path matching. This class converts
 * Ant-style patterns into regular expressions for underlying matching.
 *
 * @param <E> The type of entry to be filtered.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AntComplex<E> extends RegexComplex<E> implements Complex<E> {

    /**
     * Constructs a new {@code AntComplex} with the specified Ant-style pattern.
     *
     * @param ant The Ant-style pattern to use for filtering.
     */
    protected AntComplex(String ant) {
        super(convert(ant));
    }

    /**
     * Converts an Ant-style path expression into a regular expression. This method handles wildcards like '*' and '**'
     * and escapes special regex characters.
     *
     * @param ant The Ant-style path expression.
     * @return The corresponding regular expression string.
     */
    private static String convert(String ant) {
        String regex = ant;
        for (String symbol : AntFilter.SYMBOLS)
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
