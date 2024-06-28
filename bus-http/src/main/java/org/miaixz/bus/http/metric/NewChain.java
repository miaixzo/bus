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
package org.miaixz.bus.http.metric;

import org.miaixz.bus.http.NewCall;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.accord.Connection;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 网络调用链
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface NewChain {

    /**
     * @return 网络请求
     */
    Request request();

    /**
     * @param request 网络请求
     * @return {@link Response}
     * @throws IOException 异常
     */
    Response proceed(Request request) throws IOException;

    /**
     * 返回将执行请求的连接。这只在网络拦截器链中可用;
     * 对于应用程序拦截器，这总是null
     *
     * @return 连接信息
     */
    Connection connection();

    /**
     * 实际调用准备执行的请求
     *
     * @return {@link NewCall}
     */
    NewCall call();

    /**
     * 连接超时时间
     *
     * @return the int
     */
    int connectTimeoutMillis();

    /**
     * 设置连接超时时间
     *
     * @param timeout 超时时间
     * @param unit    单位
     * @return {@link NewChain}
     */
    NewChain withConnectTimeout(int timeout, TimeUnit unit);

    /**
     * 读操作超时时间
     *
     * @return the int
     */
    int readTimeoutMillis();

    /**
     * 配置读操作超时时间
     *
     * @param timeout 超时时间
     * @param unit    单位
     * @return {@link NewChain}
     */
    NewChain withReadTimeout(int timeout, TimeUnit unit);

    /**
     * 写操作超时时间
     *
     * @return the int
     */
    int writeTimeoutMillis();

    /**
     * 配置写操作超时时间
     *
     * @param timeout 超时时间
     * @param unit    单位
     * @return {@link NewChain}
     */
    NewChain withWriteTimeout(int timeout, TimeUnit unit);

}
