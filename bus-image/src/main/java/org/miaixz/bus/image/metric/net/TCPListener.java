/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * Represents the TCPListener type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TCPListener implements Listener {

    /**
     * The conn value.
     */
    private final Connection conn;

    /**
     * The handler value.
     */
    private final TCPProtocolHandler handler;

    /**
     * The ss value.
     */
    private final ServerSocket ss;

    /**
     * Creates a new instance.
     *
     * @param conn    the conn.
     * @param handler the handler.
     * @throws IOException              if the operation cannot be completed.
     * @throws GeneralSecurityException if the operation cannot be completed.
     */
    public TCPListener(Connection conn, TCPProtocolHandler handler) throws IOException, GeneralSecurityException {
        try {

            this.conn = conn;
            this.handler = handler;
            ss = conn.isTls() ? createTLSServerSocket(conn) : new ServerSocket();
            conn.setReceiveBufferSize(ss);
            ss.bind(conn.getBindPoint(), conn.getBacklog());
            conn.getDevice().execute(() -> listen());

        } catch (IOException e) {
            Logger.error(
                    false,
                    "Image",
                    e,
                    "TCP listener start failed: host={}, port={}, tls={}, backlog={}, exception={}",
                    conn.getHostname(),
                    conn.getPort(),
                    conn.isTls(),
                    conn.getBacklog(),
                    e.getClass().getSimpleName());
            throw new IOException("Unable to start TCPListener on " + conn.getHostname() + ":" + conn.getPort(), e);
        }
    }

    /**
     * Creates the tls server socket.
     *
     * @param conn the conn.
     * @return the operation result.
     * @throws IOException              if the operation cannot be completed.
     * @throws GeneralSecurityException if the operation cannot be completed.
     */
    public ServerSocket createTLSServerSocket(Connection conn) throws IOException, GeneralSecurityException {
        SSLContext sslContext = conn.getDevice().sslContext();
        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket();
        ss.setEnabledProtocols(conn.getTlsProtocols());
        ss.setEnabledCipherSuites(conn.getTlsCipherSuites());
        ss.setNeedClientAuth(conn.isTlsNeedClientAuth());
        return ss;
    }

    /**
     * Executes the listen operation.
     */
    public void listen() {
        SocketAddress sockAddr = ss.getLocalSocketAddress();
        Logger.info(true, "Image", "Start TCP Listener on {}", sockAddr);
        try {
            while (!ss.isClosed()) {
                Logger.debug(true, "Image", "Wait for connection on {}", sockAddr);
                Socket s = ss.accept();
                ConnectionMonitor monitor = conn.getDevice() != null ? conn.getDevice().getConnectionMonitor() : null;
                if (conn.isBlackListed(s.getInetAddress())) {
                    if (monitor != null)
                        monitor.onConnectionRejectedBlacklisted(conn, s);
                    Logger.info(true, "Image", "Reject blacklisted connection {}", s);
                    conn.close(s);
                } else {
                    try {
                        conn.setSocketSendOptions(s);
                    } catch (Throwable e) {
                        if (monitor != null)
                            monitor.onConnectionRejected(conn, s, e);
                        Logger.warn(
                                false,
                                "Image",
                                e,
                                "Connection rejected during socket option setup: socketPresent={}, exception={}",
                                s != null,
                                e.getClass().getSimpleName());
                        conn.close(s);
                        continue;
                    }

                    if (monitor != null)
                        monitor.onConnectionAccepted(conn, s);
                    Logger.info(true, "Image", "Accept connection {}", s);
                    try {
                        handler.onAccept(conn, s);
                    } catch (Throwable e) {
                        Logger.warn(
                                false,
                                "Image",
                                e,
                                "Accepted connection handling failed: socketPresent={}, exception={}",
                                s != null,
                                e.getClass().getSimpleName());
                        conn.close(s);
                    }
                }
            }
        } catch (Throwable e) {
            if (!ss.isClosed())
                Logger.error(false, "Image", "Exception on listing on {}:", sockAddr, e);
        }
        Logger.info(false, "Image", "Stop TCP Listener on {}", sockAddr);
    }

    /**
     * Gets the end point.
     *
     * @return the end point.
     */
    @Override
    public SocketAddress getEndPoint() {
        return ss.getLocalSocketAddress();
    }

    /**
     * Executes the close operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void close() throws IOException {
        try {
            ss.close();
        } catch (Throwable e) {
            Logger.error(
                    false,
                    "Image",
                    e,
                    "TCP listener close failed: endpoint={}, exception={}",
                    ss.getLocalSocketAddress(),
                    e.getClass().getSimpleName());
            // Ignore errors when closing server socket
        }
    }

}
