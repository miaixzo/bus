/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.starter.core.proxy;

import java.lang.reflect.Method;

/**
 * 拦截层
 *
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public interface ProxyChain {

    /**
     * 获取参数名称
     *
     * @return 参数
     */
    Object[] getNames();

    /**
     * 获取参数值
     *
     * @return 参数
     */
    Object[] getArgs();

    /**
     * 获取目标实例
     *
     * @return 目标实例
     */
    Object getTarget();

    /**
     * 获取方法
     *
     * @return Method
     */
    Method getMethod();

    /**
     * 执行方法
     *
     * @param arguments 参数
     * @return 执行结果
     * @throws Throwable Throwable
     */
    Object doProxyChain(Object[] arguments) throws Throwable;
}
