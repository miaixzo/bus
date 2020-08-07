/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.metric.manual;

import java.lang.reflect.Method;

/**
 * api信息
 *
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8++
 */
public interface ApiMeta {

    /**
     * 获取ApiServce对象
     *
     * @return 返回ApiService对象，即业务对象
     */
    Object getHandler();

    /**
     * 获取接口对应的方法
     *
     * @return 返回方法
     */
    Method getMethod();

    /**
     * 获取方法参数类型
     *
     * @return 返回方法参数类型
     */
    Class<?> getMethodArguClass();

    /**
     * 获取接口名
     *
     * @return 返回接口名
     */
    String getName();

    /**
     * 获取接口版本号
     *
     * @return 返回版本号
     */
    String getVersion();

    /**
     * 是否忽略签名
     *
     * @return true，是
     */
    boolean isIgnoreSign();

    /**
     * 是否忽略验证
     *
     * @return true，是
     */
    boolean isIgnoreValidate();

    /**
     * 是否对返回结果进行包装
     *
     * @return true，是
     */
    boolean isWrapResult();

    /**
     * 是否返回结果到客户端，返回true，表示不输出结果到客户端。可用在导出功能上
     *
     * @return 返回true，表示不输出结果到客户端
     */
    boolean noReturn();

    /**
     * 是否忽略jwt认证
     *
     * @return 返回true，忽略认证
     */
    boolean isIgnoreJWT();

    /**
     * @return 是否忽略token，true 忽略
     * 注意：框架本身不会校验token，需要自己实现，可用拦截器实现，这个属性主要配合拦截器使用。
     */
    boolean isIgnoreToken();

}
