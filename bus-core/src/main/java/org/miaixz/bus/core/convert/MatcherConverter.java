/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.convert;

import org.miaixz.bus.core.xyz.TypeKit;

import java.lang.reflect.Type;

/**
 * 带有匹配的转换器 判断目标对象是否满足条件，满足则转换，否则跳过 实现此接口同样可以不判断断言而直接转换
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface MatcherConverter extends Converter {

    /**
     * 判断需要转换的对象是否匹配当前转换器，满足则转换，否则跳过
     *
     * @param targetType 转换的目标类型，不能为{@code null}
     * @param rawType    目标原始类型，当targetType为Class时，和此参数一致，不能为{@code null}
     * @param value      需要转换的值
     * @return 是否匹配
     */
    boolean match(Type targetType, Class<?> rawType, Object value);

    /**
     * 判断需要转换的对象是否匹配当前转换器，满足则转换，否则跳过
     *
     * @param targetType 转换的目标类型
     * @param value      需要转换的值
     * @return 是否匹配
     */
    default boolean match(final Type targetType, final Object value) {
        return match(targetType, TypeKit.getClass(targetType), value);
    }

}
