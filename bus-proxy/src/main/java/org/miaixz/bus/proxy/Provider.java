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
package org.miaixz.bus.proxy;

import org.miaixz.bus.core.xyz.ReflectKit;

/**
 * 动态代理引擎接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Provider {

    /**
     * 创建代理
     *
     * @param <T>    代理对象类型
     * @param target 被代理对象
     * @param aspect 切面实现
     * @return 代理对象
     */
    <T> T proxy(T target, Aspect aspect);

    /**
     * 创建代理
     *
     * @param <T>         代理对象类型
     * @param target      被代理对象
     * @param aspectClass 切面实现类，自动实例化
     * @return 代理对象
     */
    default <T> T proxy(final T target, final Class<? extends Aspect> aspectClass) {
        return proxy(target, ReflectKit.newInstanceIfPossible(aspectClass));
    }

}
