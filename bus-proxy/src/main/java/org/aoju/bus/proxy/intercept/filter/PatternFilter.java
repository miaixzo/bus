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
package org.aoju.bus.proxy.intercept.filter;

import org.aoju.bus.proxy.intercept.MethodFilter;

import java.lang.reflect.Method;

/**
 * 如果方法的名称与提供的正则表达式(JDK regex)模式字符串匹配，则方法筛选器实现返回true
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PatternFilter implements MethodFilter {

    public static String GETTER_SETTER_PATTERN = "get\\w+|set\\w+";
    private final String pattern;

    public PatternFilter(String pattern) {
        this.pattern = pattern;
    }

    public static MethodFilter getterSetterFilter() {
        return new PatternFilter(GETTER_SETTER_PATTERN);
    }

    public boolean accepts(Method method) {
        return method.getName().matches(pattern);
    }

}

