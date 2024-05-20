/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.lang.intern.Intern;
import org.miaixz.bus.core.lang.intern.StringIntern;
import org.miaixz.bus.core.lang.intern.WeakIntern;

/**
 * 规范化对象生成工具
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class InternKit {

    /**
     * 创建WeakHshMap实现的字符串规范化器
     *
     * @param <T> 规范对象的类型
     * @return {@link Intern}
     */
    public static <T> Intern<T> ofWeak() {
        return new WeakIntern<>();
    }

    /**
     * 创建JDK默认实现的字符串规范化器
     *
     * @return {@link Intern}
     * @see String#intern()
     */
    public static Intern<String> ofString() {
        return new StringIntern();
    }

    /**
     * 创建字符串规范化器
     *
     * @param isWeak 是否创建使用WeakHashMap实现的Interner
     * @return {@link Intern}
     */
    public static Intern<String> of(final boolean isWeak) {
        return isWeak ? ofWeak() : ofString();
    }

}
