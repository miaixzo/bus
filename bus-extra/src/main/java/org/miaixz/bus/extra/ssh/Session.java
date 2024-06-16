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
package org.miaixz.bus.extra.ssh;

import org.miaixz.bus.core.lang.Wrapper;
import org.miaixz.bus.core.lang.exception.InternalException;

import java.io.Closeable;
import java.net.InetSocketAddress;

/**
 * SSH Session抽象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Session extends Wrapper<Object>, Closeable {

    /**
     * 是否连接状态
     *
     * @return 是否连接状态
     */
    boolean isConnected();

    /**
     * 绑定端口到本地。 一个会话可绑定多个端口
     * 当请求localHost:localPort时，通过SSH到服务器，转发请求到remoteHost:remotePort
     * 此方法用于访问本地无法访问但是服务器可以访问的地址，如只有服务器能访问的内网数据库等
     *
     * @param localPort     本地端口
     * @param remoteAddress 远程主机和端口
     */
    default void bindLocalPort(final int localPort, final InetSocketAddress remoteAddress) {
        bindLocalPort(new InetSocketAddress(localPort), remoteAddress);
    }

    /**
     * 绑定端口到本地。 一个会话可绑定多个端口
     * 当请求localHost:localPort时，通过SSH到服务器，转发请求到remoteHost:remotePort
     * 此方法用于访问本地无法访问但是服务器可以访问的地址，如只有服务器能访问的内网数据库等
     *
     * @param localAddress  本地主机和端口
     * @param remoteAddress 远程主机和端口
     */
    void bindLocalPort(final InetSocketAddress localAddress, final InetSocketAddress remoteAddress);

    /**
     * 解除本地端口映射
     *
     * @param localPort 需要解除的本地端口
     * @throws InternalException 端口解绑失败异常
     */
    default void unBindLocalPort(final int localPort) {
        unBindLocalPort(new InetSocketAddress(localPort));
    }

    /**
     * 解除本地端口映射
     *
     * @param localAddress 需要解除的本地地址
     */
    void unBindLocalPort(final InetSocketAddress localAddress);

    /**
     * 绑定ssh服务端的serverPort端口, 到本地主机的port端口上.
     * 即数据从ssh服务端的serverPort端口, 流经ssh客户端, 达到host:port上.
     * 此方法用于在服务端访问本地资源，如服务器访问本机所在的数据库等。
     *
     * @param remoteAddress ssh服务端上要被绑定的地址
     * @param localAddress  转发到的本地地址
     * @throws InternalException 端口绑定失败异常
     */
    void bindRemotePort(final InetSocketAddress remoteAddress, final InetSocketAddress localAddress);

    /**
     * 解除远程端口映射
     *
     * @param remoteAddress 需要解除的远程地址和端口
     */
    void unBindRemotePort(final InetSocketAddress remoteAddress);

}
