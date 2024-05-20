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
package org.miaixz.bus.core.convert;

import org.miaixz.bus.core.xyz.ClassKit;

/**
 * 类转换器
 * 将类名转换为类，默认初始化这个类（执行static块）
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ClassConverter extends AbstractConverter {

    private static final long serialVersionUID = -1L;

    /**
     * 单例
     */
    public static ClassConverter INSTANCE = new ClassConverter();

    private final boolean isInitialized;

    /**
     * 构造
     */
    public ClassConverter() {
        this(true);
    }

    /**
     * 构造
     *
     * @param isInitialized 是否初始化类（调用static模块内容和初始化static属性）
     */
    public ClassConverter(final boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    @Override
    protected Class<?> convertInternal(final Class<?> targetClass, final Object value) {
        return ClassKit.loadClass(convertToString(value), isInitialized);
    }

}
