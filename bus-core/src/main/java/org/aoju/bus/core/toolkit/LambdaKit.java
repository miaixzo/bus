/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.toolkit;

import org.aoju.bus.core.lang.Func;
import org.aoju.bus.core.lang.SimpleCache;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;

/**
 * Lambda相关工具类
 *
 * @author Kimi Liu
 * @version 6.2.6
 * @since JDK 1.8+
 */
public class LambdaKit {

    private static final SimpleCache<String, SerializedLambda> cache = new SimpleCache<>();

    /**
     * 解析lambda表达式,加了缓存。
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param <T>  Lambda类型
     * @param func 需要解析的 lambda 对象（无参方法）
     * @return 返回解析后的结果
     */
    public static <T> SerializedLambda resolve(Func.Func1<T, ?> func) {
        return _resolve(func);
    }

    /**
     * 获取lambda表达式函数（方法）名称
     *
     * @param <T>  Lambda类型
     * @param func 函数（无参方法）
     * @return 函数名称
     */
    public static <T> String getMethodName(Func.Func1<T, ?> func) {
        return resolve(func).getImplMethodName();
    }

    /**
     * 解析lambda表达式,加了缓存。
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param func 需要解析的 lambda 对象
     * @return 返回解析后的结果
     */
    private static <T> SerializedLambda _resolve(Serializable func) {
        return cache.get(func.getClass().getName(), () -> ReflectKit.invoke(func, "writeReplace"));
    }

}
