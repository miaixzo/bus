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

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.hl7.HL7Exception;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL7ServiceRegistry extends DefaultHL7MessageListener {

    private final List<HL7Service> services = new ArrayList<>();
    private final Map<String, HL7MessageListener> listeners = new HashMap<>();

    public synchronized void addHL7Service(HL7Service service) {
        services.add(service);
        for (String messageType : service.getMessageTypes())
            listeners.put(messageType, service);
    }

    public synchronized boolean removeHL7Service(HL7Service service) {
        if (!services.remove(service))
            return false;

        for (String messageType : service.getMessageTypes())
            listeners.remove(messageType);

        return true;
    }

    @Override
    public UnparsedHL7Message onMessage(HL7Application hl7App, Connection conn, Socket s, UnparsedHL7Message msg)
            throws HL7Exception {
        HL7MessageListener listener = listeners.get(msg.msh().getMessageType());
        if (listener == null) {
            listener = listeners.get(Symbol.STAR);
            if (listener == null)
                return super.onMessage(hl7App, conn, s, msg);
        }
        return listener.onMessage(hl7App, conn, s, msg);
    }

}
