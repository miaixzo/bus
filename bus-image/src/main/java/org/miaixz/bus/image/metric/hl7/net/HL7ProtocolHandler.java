/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.metric.hl7.net;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.hl7.HL7Exception;
import org.miaixz.bus.image.metric.hl7.HL7Message;
import org.miaixz.bus.image.metric.hl7.MLLPConnection;
import org.miaixz.bus.image.metric.hl7.MLLPRelease;
import org.miaixz.bus.image.metric.net.TCPProtocolHandler;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public enum HL7ProtocolHandler implements TCPProtocolHandler {
    INSTANCE;

    @Override
    public void onAccept(Connection conn, Socket s) {
        conn.getDevice().execute(new HL7Receiver(conn, s));
    }

    private static class HL7Receiver implements Runnable {

        final Connection conn;
        final Socket s;
        final HL7DeviceExtension hl7dev;

        HL7Receiver(Connection conn, Socket s) {
            this.conn = conn;
            this.s = s;
            this.hl7dev = conn.getDevice().getDeviceExtensionNotNull(HL7DeviceExtension.class);
        }

        public void run() {
            int messageCount = 0;
            try {
                s.setSoTimeout(conn.getIdleTimeout());
                MLLPConnection mllp = new MLLPConnection(s,
                        conn.getProtocol() == Connection.Protocol.HL7_MLLP2 ? MLLPRelease.MLLP2 : MLLPRelease.MLLP1);
                byte[] data;
                while ((data = mllp.readMessage()) != null) {
                    messageCount++;
                    HL7ConnectionMonitor monitor = hl7dev.getHL7ConnectionMonitor();
                    UnparsedHL7Message msg = new UnparsedHL7Message(data);
                    if (monitor != null)
                        monitor.onMessageReceived(conn, s, msg);
                    UnparsedHL7Message rsp;
                    try {
                        rsp = hl7dev.onMessage(conn, s, msg);
                        if (monitor != null)
                            monitor.onMessageProcessed(conn, s, msg, rsp, null);
                    } catch (HL7Exception e) {
                        Logger.info("{}: failed to process {}:\n", s, msg, e);
                        rsp = new UnparsedHL7Message(HL7Message.makeACK(msg.msh(), e).getBytes(null));
                        if (monitor != null)
                            monitor.onMessageProcessed(conn, s, msg, rsp, e);
                    }
                    mllp.writeMessage(rsp.data());
                }
            } catch (IOException e) {
                if (e instanceof SocketException && messageCount == 0)
                    Logger.info("Exception on accepted connection {}: {}", s, e.toString());
                else
                    Logger.warn("Exception on accepted connection {}:", s, e);
            } finally {
                conn.close(s);
            }
        }
    }

}
