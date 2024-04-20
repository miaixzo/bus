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
package org.miaixz.bus.image.metric.internal.hl7;

import org.miaixz.bus.image.metric.acquire.HL7DeviceExtension;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL7Connection implements Closeable {

    private final HL7Application hl7Application;
    private final MLLPConnection mllpConnection;
    private final HL7ConnectionMonitor monitor;

    public HL7Connection(HL7Application hl7Application, MLLPConnection mllpConnection) {
        this.hl7Application = hl7Application;
        this.mllpConnection = mllpConnection;
        this.monitor = hl7Application.getDevice()
                .getDeviceExtensionNotNull(HL7DeviceExtension.class)
                .getHL7ConnectionMonitor();
    }

    public void writeMessage(UnparsedHL7Message msg) throws IOException {
        try {
            mllpConnection.writeMessage(msg.data());
            if (null != monitor)
                monitor.onMessageSent(hl7Application, mllpConnection.getSocket(), msg, null);
        } catch (IOException e) {
            monitor.onMessageSent(hl7Application, mllpConnection.getSocket(), msg, e);
            throw e;
        }
    }

    public UnparsedHL7Message readMessage(UnparsedHL7Message msg) throws IOException {
        try {
            byte[] b = mllpConnection.readMessage();
            UnparsedHL7Message rsp = null != b ? new UnparsedHL7Message(b) : null;
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
