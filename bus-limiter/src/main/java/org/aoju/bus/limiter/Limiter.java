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
package org.aoju.bus.limiter;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 限制器件的定义
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Limiter<T extends Annotation> {

    /**
     * 该限制器的名字 方便定位哪一个限制器被应用
     *
     * @return the string
     */
    String getLimiterName();

    /**
     * 对一个键值进行限制操作,并使用 args 参数
     * 例如实现一个速率限制器,则 args 通常为速率参数
     *
     * @param key  键
     * @param args 参数
     * @return true/false
     */
    boolean limit(Object key, Map<String, Object> args);

    /**
     * 对于一个键值释放限制,例如locker 对应于locker 的unlock 操作
     * 某些种类的没有对应的释放操作 例如速率限制器 这是该方法应该是空实现
     *
     * @param key  键
     * @param args 参数
     */
    void release(Object key, Map<String, Object> args);

}
