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
package org.miaixz.bus.image.metric.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class TCPListener implements Listener {

    private final Connection conn;
    private final TCPProtocolHandler handler;
    private final ServerSocket ss;

    public TCPListener(Connection conn, TCPProtocolHandler handler) throws IOException, GeneralSecurityException {
        try {

            this.conn = conn;
            this.handler = handler;
            ss = conn.isTls() ? createTLSServerSocket(conn) : new ServerSocket();
            conn.setReceiveBufferSize(ss);
            ss.bind(conn.getBindPoint(), conn.getBacklog());
            conn.getDevice().execute(() -> listen());

        } catch (IOException e) {
            throw new IOException("Unable to start TCPListener on " + conn.getHostname() + ":" + conn.getPort(), e);
        }
    }

    public ServerSocket createTLSServerSocket(Connection conn) throws IOException, GeneralSecurityException {
        SSLContext sslContext = conn.getDevice().sslContext();
        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket();
        ss.setEnabledProtocols(conn.getTlsProtocols());
        ss.setEnabledCipherSuites(conn.getTlsCipherSuites());
        ss.setNeedClientAuth(conn.isTlsNeedClientAuth());
        return ss;
    }

    public void listen() {
        SocketAddress sockAddr = ss.getLocalSocketAddress();
        Logger.info("Start TCP Listener on {}", sockAddr);
        try {
            while (!ss.isClosed()) {
                Logger.debug("Wait for connection on {}", sockAddr);
                Socket s = ss.accept();
                ConnectionMonitor monitor = conn.getDevice() != null ? conn.getDevice().getConnectionMonitor() : null;
                if (conn.isBlackListed(s.getInetAddress())) {
                    if (monitor != null)
                        monitor.onConnectionRejectedBlacklisted(conn, s);
                    Logger.info("Reject blacklisted connection {}", s);
                    conn.close(s);
                } else {
                    try {
                        conn.setSocketSendOptions(s);
                    } catch (Throwable e) {
                        if (monitor != null)
                            monitor.onConnectionRejected(conn, s, e);
                        Logger.warn("Reject connection {}:", s, e);
                        conn.close(s);
                        continue;
                    }

                    if (monitor != null)
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
    public void close() throws IOException {
        try {
            ss.close();
        } catch (Throwable e) {
            Logger.error(e.getMessage());
            // 关闭服务器套接字时，请忽略错误
        }
    }

}
