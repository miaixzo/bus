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
package org.miaixz.bus.http.accord;

import org.miaixz.bus.http.Address;

import java.util.concurrent.TimeUnit;

/**
 * 管理HTTP和HTTP/2连接的重用，以减少网络延迟。 共享相同的
 * {@link Address}的HTTP请求可能共享一个{@link Connection}
 * 该类实现了哪些连接保持开放以供将来使用的策略
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConnectionPool {

    public final RealConnectionPool delegate;

    /**
     * 使用适合于单用户应用程序的调优参数创建新的连接池。
     * 这个池中的调优参数可能在将来的Httpd版本中更改。
     * 目前这个池最多可以容纳5个空闲连接，这些连接将在5分钟不活动后被清除
     */
    public ConnectionPool() {
        this(5, 5, TimeUnit.MINUTES);
    }

    public ConnectionPool(int maxIdleConnections, long keepAliveDuration, TimeUnit timeUnit) {
        this.delegate = new RealConnectionPool(maxIdleConnections, keepAliveDuration, timeUnit);
    }

    /**
     * 返回池中空闲连接的数量
     *
     * @return 连接的数量
     */
    public int idleConnectionCount() {
        return delegate.idleConnectionCount();
    }

    /**
     * 返回池中的连接总数。注意，在Httpd 2.7之前，这只包括空闲连接 和HTTP/2连接
     * 因为Httpd 2.7包含了所有的连接，包括活动的和非活动的。
     * 使用{@link #idleConnectionCount()}来计数当前未使用的连接
     *
     * @return 连接总数
     */
    public int connectionCount() {
        return delegate.connectionCount();
    }

    /**
     * 关闭并删除池中的所有空闲连接.
     */
    public void evictAll() {
        delegate.evictAll();
    }

}
