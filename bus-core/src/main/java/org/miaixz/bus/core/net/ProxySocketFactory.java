/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

import javax.net.SocketFactory;

/**
 * A {@link SocketFactory} for creating sockets that connect through a proxy.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ProxySocketFactory extends SocketFactory {

    /**
     * The proxy to be used for new sockets.
     */
    private final Proxy proxy;

    /**
     * Constructs a new {@code ProxySocketFactory}.
     *
     * @param proxy The socket proxy.
     */
    public ProxySocketFactory(final Proxy proxy) {
        this.proxy = proxy;
    }

    /**
     * Creates a new {@code ProxySocketFactory}.
     *
     * @param proxy The proxy object.
     * @return A new {@code ProxySocketFactory} instance.
     */
    public static ProxySocketFactory of(final Proxy proxy) {
        return new ProxySocketFactory(proxy);
    }

    /**
     * Creates an unconnected socket. If a proxy is configured, the socket will be created with the proxy.
     *
     * @return An unconnected socket.
     */
    @Override
    public Socket createSocket() {
        if (proxy != null) {
            return new Socket(proxy);
        }
        return new Socket();
    }

    /**
     * Creates a socket and connects it to the specified port at the specified address. If a proxy is configured, the
     * socket will be created with the proxy.
     *
     * @param address The address to connect to.
     * @param port    The port number to connect to.
     * @return The connected socket.
     * @throws IOException If an I/O error occurs when creating the socket.
     */
    @Override
    public Socket createSocket(final InetAddress address, final int port) throws IOException {
        if (proxy != null) {
            final Socket s = new Socket(proxy);
            s.connect(new InetSocketAddress(address, port));
            return s;
        }
        return new Socket(address, port);
    }

    /**
     * Creates a socket, connects it to the specified port at the specified address, and binds it to the specified local
     * address and port. If a proxy is configured, the socket will be created with the proxy.
     *
     * @param address   The remote address to connect to.
     * @param port      The remote port number to connect to.
     * @param localAddr The local address to bind to.
     * @param localPort The local port number to bind to.
     * @return The connected socket bound to the local address and port.
     * @throws IOException If an I/O error occurs when creating the socket.
     */
    @Override
    public Socket createSocket(
            final InetAddress address,
            final int port,
            final InetAddress localAddr,
            final int localPort) throws IOException {
        if (proxy != null) {
            final Socket s = new Socket(proxy);
            s.bind(new InetSocketAddress(localAddr, localPort));
            s.connect(new InetSocketAddress(address, port));
            return s;
        }
        return new Socket(address, port, localAddr, localPort);
    }

    /**
     * Creates a socket and connects it to the specified port on the specified host. If a proxy is configured, the
     * socket will be created with the proxy.
     *
     * @param host The host name to connect to.
     * @param port The port number to connect to.
     * @return The connected socket.
     * @throws IOException If an I/O error occurs when creating the socket.
     */
    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        if (proxy != null) {
            final Socket s = new Socket(proxy);
            s.connect(new InetSocketAddress(host, port));
            return s;
        }
        return new Socket(host, port);
    }

    /**
     * Creates a socket, connects it to the specified port on the specified host, and binds it to the specified local
     * address and port. If a proxy is configured, the socket will be created with the proxy.
     *
     * @param host      The host name to connect to.
     * @param port      The remote port number to connect to.
     * @param localAddr The local address to bind to.
     * @param localPort The local port number to bind to.
     * @return The connected socket bound to the local address and port.
     * @throws IOException If an I/O error occurs when creating the socket.
     */
    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localAddr, final int localPort)
            throws IOException {
        if (proxy != null) {
            final Socket s = new Socket(proxy);
            s.bind(new InetSocketAddress(localAddr, localPort));
            s.connect(new InetSocketAddress(host, port));
            return s;
        }
        return new Socket(host, port, localAddr, localPort);
    }

}
