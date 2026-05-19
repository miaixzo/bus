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
 * Represents the HL7ServiceRegistry type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HL7ServiceRegistry extends DefaultHL7MessageListener {

    /**
     * Constructs a new HL7ServiceRegistry instance.
     */
    public HL7ServiceRegistry() {
        // No initialization required.
    }

    /**
     * The services value.
     */
    private final List<HL7Service> services = new ArrayList<>();

    /**
     * The listeners value.
     */
    private final Map<String, HL7MessageListener> listeners = new HashMap<>();

    /**
     * Adds the hl7 service.
     *
     * @param service the service.
     */
    public synchronized void addHL7Service(HL7Service service) {
        services.add(service);
        for (String messageType : service.getMessageTypes())
            listeners.put(messageType, service);
    }

    /**
     * Removes the hl7 service.
     *
     * @param service the service.
     * @return true if the condition is met; otherwise false.
     */
    public synchronized boolean removeHL7Service(HL7Service service) {
        if (!services.remove(service))
            return false;

        for (String messageType : service.getMessageTypes())
            listeners.remove(messageType);

        return true;
    }

    /**
     * Executes the on message operation.
     *
     * @param hl7App the hl7 app.
     * @param conn   the conn.
     * @param s      the s.
     * @param msg    the msg.
     * @return the operation result.
     * @throws HL7Exception if the operation cannot be completed.
     */
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
