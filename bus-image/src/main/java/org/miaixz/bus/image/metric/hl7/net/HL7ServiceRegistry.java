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
