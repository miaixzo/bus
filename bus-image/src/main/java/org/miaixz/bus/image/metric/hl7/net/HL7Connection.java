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
 * @author Kimi Liu
 * @since Java 21+
 */
public class HL7Connection implements Closeable {

    private final HL7Application hl7Application;
    private final MLLPConnection mllpConnection;
    private final HL7ConnectionMonitor monitor;

    public HL7Connection(HL7Application hl7Application, MLLPConnection mllpConnection) {
        this.hl7Application = hl7Application;
        this.mllpConnection = mllpConnection;
        this.monitor = hl7Application.getDevice().getDeviceExtensionNotNull(HL7DeviceExtension.class)
                .getHL7ConnectionMonitor();
    }

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

    @Override
    public void close() throws IOException {
        mllpConnection.close();
    }

}
