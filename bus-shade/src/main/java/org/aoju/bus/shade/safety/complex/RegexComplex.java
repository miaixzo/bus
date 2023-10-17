/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.shade.safety.complex;

import org.aoju.bus.shade.safety.Complex;

import java.util.regex.Pattern;

/**
 * 正则表达式规则
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class RegexComplex<E> implements Complex<E> {

    protected final Pattern pattern;

    protected RegexComplex(String regex) {
        this(Pattern.compile(regex));
    }

    protected RegexComplex(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean on(E entry) {
        String text = toText(entry);
        return pattern.matcher(text).matches();
    }

    /**
     * 将记录转换成字符串形式,用于模式匹配
     *
     * @param entry 记录
     * @return 记录的字符串表达形式
     */
    protected abstract String toText(E entry);

}
