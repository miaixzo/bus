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

import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.logger.Logger;

import java.io.IOException;
import java.net.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class UDPListener implements Listener {

    private static final int MAX_PACKAGE_LEN = 0x10000;

    private final Connection conn;
    private final UDPProtocolHandler handler;
    private final DatagramSocket ds;

    public UDPListener(Connection conn, UDPProtocolHandler handler)
            throws IOException {
        this.conn = conn;
        this.handler = handler;
        try {
            ds = new DatagramSocket(conn.getBindPoint());
        } catch (BindException e) {
            throw new IOException("Cannot start UDP listener on " + conn.getBindPoint().getHostName() + ":" + conn.getBindPoint().getPort(), e);
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
                    Logger.info(
                            "Ignore UDP datagram package received from blacklisted {}", senderAddr);
                } else {
                    Logger.info(
                            "Received UDP datagram package from {}", senderAddr);
                    try {
                        handler.onReceive(conn, dp);
                    } catch (Throwable e) {
                        Logger.warn(
                                "Exception processing UDP received from {}:", senderAddr, e);
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
            // 关闭数据报套接字时忽略错误
            Logger.error(e.getMessage());
        }
    }

}
