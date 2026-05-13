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
package org.miaixz.bus.image.metric.hl7.net;

import java.io.Closeable;
import java.io.IOException;

import org.miaixz.bus.image.metric.hl7.MLLPConnection;

/**
 * Represents the HL7Connection type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HL7Connection implements Closeable {

    /**
     * The hl7 application value.
     */
    private final HL7Application hl7Application;

    /**
     * The mllp connection value.
     */
    private final MLLPConnection mllpConnection;

    /**
     * The monitor value.
     */
    private final HL7ConnectionMonitor monitor;

    /**
     * Creates a new instance.
     *
     * @param hl7Application the hl7 application.
     * @param mllpConnection the mllp connection.
     */
    public HL7Connection(HL7Application hl7Application, MLLPConnection mllpConnection) {
        this.hl7Application = hl7Application;
        this.mllpConnection = mllpConnection;
        this.monitor = hl7Application.getDevice().getDeviceExtensionNotNull(HL7DeviceExtension.class)
                .getHL7ConnectionMonitor();
    }

    /**
     * Writes the message.
     *
     * @param msg the msg.
     * @throws IOException if the operation cannot be completed.
     */
    public void writeMessage(UnparsedHL7Message msg) throws IOException {
        try {
            mllpConnection.writeMessage(msg.data());
            if (monitor != null)
                monitor.onMessageSent(hl7Application, mllpConnection.getSocket(), msg, null);
        } catch (IOException e) {
            monitor.onMessageSent(hl7Application, mllpConnection.getSocket(), msg, e);
            throw e;
        }
    }

    /**
     * Reads the message.
     *
     * @param msg the msg.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public UnparsedHL7Message readMessage(UnparsedHL7Message msg) throws IOException {
        try {
            byte[] b = mllpConnection.readMessage();
            UnparsedHL7Message rsp = b != null ? new UnparsedHL7Message(b) : null;
            monitor.onMessageResponse(hl7Application, mllpConnection.getSocket(), msg, rsp, null);
            return rsp;
        } catch (IOException e) {
            monitor.onMessageResponse(hl7Application, mllpConnection.getSocket(), msg, null, e);
            throw e;
        }
    }

    /**
     * Executes the close operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void close() throws IOException {
        mllpConnection.close();
    }

}
