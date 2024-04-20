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
package org.miaixz.bus.image.metric.internal.net;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.Monitoring;
import org.miaixz.bus.image.metric.SocketListener;
import org.miaixz.bus.logger.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class TCPListener implements SocketListener {

    private final Connection conn;
    private final TCPHandler handler;
    private final ServerSocket ss;

    public TCPListener(Connection conn, TCPHandler handler)
            throws IOException, GeneralSecurityException {
        try {

            this.conn = conn;
            this.handler = handler;
            ss = conn.isTls() ? createTLSServerSocket(conn) : new ServerSocket();
            conn.setReceiveBufferSize(ss);
            ss.bind(conn.getBindPoint(), conn.getBacklog());
            conn.getDevice().execute(() -> listen());

        } catch (IOException e) {
            throw new IOException("Unable to start TCPListener on " + conn.getHostname() + Symbol.COLON + conn.getPort(), e);
        }
    }

    private ServerSocket createTLSServerSocket(Connection conn)
            throws IOException, GeneralSecurityException {
        SSLContext sslContext = conn.getDevice().sslContext();
        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket();
        ss.setEnabledProtocols(conn.getTlsProtocols());
        ss.setEnabledCipherSuites(conn.getTlsCipherSuites());
        ss.setNeedClientAuth(conn.isTlsNeedClientAuth());
        return ss;
    }

    private void listen() {
        SocketAddress sockAddr = ss.getLocalSocketAddress();
        Logger.info("Start TCP Listener on {}", sockAddr);
        try {
            while (!ss.isClosed()) {
                Logger.debug("Wait for connection on {}", sockAddr);
                Socket s = ss.accept();
                Monitoring monitor = null != conn.getDevice()
                        ? conn.getDevice().getMonitoring()
                        : null;
                if (conn.isBlackListed(s.getInetAddress())) {
                    if (null != monitor)
                        monitor.onConnectionRejectedBlacklisted(conn, s);
                    Logger.info("Reject blacklisted connection {}", s);
                    conn.close(s);
                } else {
                    try {
                        conn.setSocketSendOptions(s);
                        if (s instanceof SSLSocket) {
                            ((SSLSocket) s).startHandshake();
                        }
                    } catch (Throwable e) {
                        if (null != monitor)
                            monitor.onConnectionRejected(conn, s, e);
                        Logger.warn("Reject connection {}:", s, e);
                        conn.close(s);
                        continue;
                    }

                    if (null != monitor)
                        monitor.onConnectionAccepted(conn, s);
                    Logger.info("Accept connection {}", s);
                    try {
                        handler.onAccept(conn, s);
                    } catch (Throwable e) {
                        Logger.warn("Exception on accepted connection {}:", s, e);
                        conn.close(s);
                    }
                }
            }
        } catch (Throwable e) {
            if (!ss.isClosed())
                Logger.error("Exception on listing on {}:", sockAddr, e);
        }
        Logger.info("Stop TCP Listener on {}", sockAddr);
    }


    @Override
    public SocketAddress getEndPoint() {
        return ss.getLocalSocketAddress();
    }

    @Override
    public void close() {
        try {
            ss.close();
        } catch (Throwable e) {
            Logger.error(e.getMessage());
            // 关闭服务器套接字时，请忽略错误
        }
    }

}
