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
package org.miaixz.bus.core.net;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

/**
 * 代理Socket工厂，用于创建代理Socket
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ProxySocketFactory extends SocketFactory {

    private final Proxy proxy;

    /**
     * 构造
     *
     * @param proxy Socket代理
     */
    public ProxySocketFactory(final Proxy proxy) {
        this.proxy = proxy;
    }

    /**
     * 创建代理SocketFactory
     *
     * @param proxy 代理对象
     * @return {@code ProxySocketFactory}
     */
    public static ProxySocketFactory of(final Proxy proxy) {
        return new ProxySocketFactory(proxy);
    }

    @Override
    public Socket createSocket() {
        if (proxy != null) {
            return new Socket(proxy);
        }
        return new Socket();
    }

    @Override
    public Socket createSocket(final InetAddress address, final int port) throws IOException {
        if (proxy != null) {
            final Socket s = new Socket(proxy);
            s.connect(new InetSocketAddress(address, port));
            return s;
        }
        return new Socket(address, port);
    }

    @Override
    public Socket createSocket(final InetAddress address, final int port, final InetAddress localAddr, final int localPort) throws IOException {
        if (proxy != null) {
            final Socket s = new Socket(proxy);
            s.bind(new InetSocketAddress(localAddr, localPort));
            s.connect(new InetSocketAddress(address, port));
            return s;
        }
        return new Socket(address, port, localAddr, localPort);
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        if (proxy != null) {
            final Socket s = new Socket(proxy);
            s.connect(new InetSocketAddress(host, port));
            return s;
        }
        return new Socket(host, port);
    }

    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localAddr, final int localPort) throws IOException {
        if (proxy != null) {
            final Socket s = new Socket(proxy);
            s.bind(new InetSocketAddress(localAddr, localPort));
            s.connect(new InetSocketAddress(host, port));
            return s;
        }
        return new Socket(host, port, localAddr, localPort);
    }

}
