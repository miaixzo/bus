/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.convert;

/**
 * 转换器接口,实现类型转换
 *
 * @author Kimi Liu
 * @version 6.5.0
 * @since Java 17+
 */
public interface Converter<T> {

    /**
     * 转换为指定类型
     * 如果类型无法确定,将读取默认值的类型做为目标类型
     *
     * @param value        原始值
     * @param defaultValue 默认值
     * @return 转换后的值
     * @throws IllegalArgumentException 无法确定目标类型,且默认值为{@code null},无法确定类型
     */
    T convert(Object value, T defaultValue) throws IllegalArgumentException;

    /**
     * 转换值为指定类型，可选是否不抛异常转换
     * 当转换失败时返回默认值
     *
     * @param value        值
     * @param defaultValue 默认值
     * @param quietly      是否静默转换，true不抛异常
     * @return 转换后的值
     * @see #convert(Object, Object)
     */
    default T convert(Object value, T defaultValue, boolean quietly) {
        try {
            return convert(value, defaultValue);
        } catch (Exception e) {
            if (quietly) {
                return defaultValue;
            }
            throw e;
        }
    }

}
