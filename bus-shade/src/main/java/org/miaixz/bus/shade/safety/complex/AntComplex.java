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
package org.miaixz.bus.shade.safety.complex;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.loader.AntFilter;
import org.miaixz.bus.shade.safety.Complex;

/**
 * Ant表达式过规则
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AntComplex<E> extends RegexComplex<E> implements Complex<E> {

    protected AntComplex(String ant) {
        super(convert(ant));
    }

    /**
     * 将ANT风格路径表达式转换成正则表达式
     *
     * @param ant ANT风格路径表达式
     * @return 正则表达式
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
