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
import java.net.*;

import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class UDPListener implements Listener {

    private static final int MAX_PACKAGE_LEN = 0x10000;

    private final Connection conn;
    private final UDPProtocolHandler handler;
    private final DatagramSocket ds;

    public UDPListener(Connection conn, UDPProtocolHandler handler) throws IOException {
        this.conn = conn;
        this.handler = handler;
        try {
            ds = new DatagramSocket(conn.getBindPoint());
        } catch (BindException e) {
            throw new IOException("Cannot start UDP listener on " + conn.getBindPoint().getHostName() + ":"
                    + conn.getBindPoint().getPort(), e);
        }
        conn.setReceiveBufferSize(ds);
        conn.getDevice().execute(() -> listen());
    }

    public void listen() {
        SocketAddress sockAddr = ds.getLocalSocketAddress();
        Logger.info("Start UDP listener on {}", sockAddr);
        byte[] data = new byte[MAX_PACKAGE_LEN];
        try {
            while (!ds.isClosed()) {
                Logger.debug("Wait for UDP datagram package on {}", sockAddr);
                DatagramPacket dp = new DatagramPacket(data, MAX_PACKAGE_LEN);
                ds.receive(dp);
                InetAddress senderAddr = dp.getAddress();
                if (conn.isBlackListed(dp.getAddress())) {
                    Logger.info("Ignore UDP datagram package received from blacklisted {}", senderAddr);
                } else {
                    Logger.info("Received UDP datagram package from {}", senderAddr);
                    try {
                        handler.onReceive(conn, dp);
                    } catch (Throwable e) {
                        Logger.warn("Exception processing UDP received from {}:", senderAddr, e);
                    }
                }
            }
        } catch (Throwable e) {
            if (!ds.isClosed()) // ignore exception caused by close()
                Logger.error("Exception on listing on {}:", sockAddr, e);
        }
        Logger.info("Stop UDP listener on {}", sockAddr);
    }

    @Override
    public SocketAddress getEndPoint() {
        return ds.getLocalSocketAddress();
    }

    @Override
    public void close() throws IOException {
        try {
            ds.close();
        } catch (Throwable e) {
            // Ignore errors when closing datagram socket
            Logger.error(e.getMessage());
        }
    }

}
