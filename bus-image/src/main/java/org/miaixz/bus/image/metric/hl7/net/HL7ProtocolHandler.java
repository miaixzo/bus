/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
