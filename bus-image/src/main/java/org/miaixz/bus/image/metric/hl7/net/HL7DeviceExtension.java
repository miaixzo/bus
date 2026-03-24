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

import java.io.Serial;
import java.net.Socket;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.hl7.ERRSegment;
import org.miaixz.bus.image.metric.hl7.HL7Exception;
import org.miaixz.bus.image.metric.net.DeviceExtension;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class HL7DeviceExtension extends DeviceExtension {

    @Serial
    private static final long serialVersionUID = 2852267701967L;

    static {
        Connection.registerTCPProtocolHandler(Connection.Protocol.HL7, HL7ProtocolHandler.INSTANCE);
        Connection.registerTCPProtocolHandler(Connection.Protocol.HL7_MLLP2, HL7ProtocolHandler.INSTANCE);
    }

    private final LinkedHashMap<String, HL7Application> hl7apps = new LinkedHashMap<>();

    private transient HL7MessageListener hl7MessageListener;
    private transient HL7ConnectionMonitor hl7ConnectionMonitor;

    @Override
    public void verifyNotUsed(Connection conn) {
        for (HL7Application app : hl7apps.values())
            if (app.getConnections().contains(conn))
                throw new IllegalStateException(conn + " used by HL7 Application: " + app.getApplicationName());
    }

    public void addHL7Application(HL7Application hl7App) {
        hl7App.setDevice(device);
        hl7apps.put(hl7App.getApplicationName(), hl7App);
    }

    public HL7Application removeHL7Application(String name) {
        HL7Application hl7App = hl7apps.remove(name);
        if (hl7App != null)
            hl7App.setDevice(null);

        return hl7App;
    }

    public boolean removeHL7Application(HL7Application hl7App) {
        return removeHL7Application(hl7App.getApplicationName()) != null;
    }

    public HL7Application getHL7Application(String name) {
        return hl7apps.get(name);
    }

    public HL7Application getHL7Application(String name, boolean matchOtherAppNames) {
        HL7Application app = hl7apps.get(name);
        if (app == null)
            app = hl7apps.get("*");
        if (app == null && matchOtherAppNames)
            for (HL7Application app1 : getHL7Applications())
                if (app1.isOtherApplicationName(name))
                    return app1;
        return app;
    }

    public boolean containsHL7Application(String name) {
        return hl7apps.containsKey(name);
    }

    public Collection<String> getHL7ApplicationNames() {
        return hl7apps.keySet();
    }

    public Collection<HL7Application> getHL7Applications() {
        return hl7apps.values();
    }

    public final HL7MessageListener getHL7MessageListener() {
        return hl7MessageListener;
    }

    public final void setHL7MessageListener(HL7MessageListener listener) {
        this.hl7MessageListener = listener;
    }

    public HL7ConnectionMonitor getHL7ConnectionMonitor() {
        return hl7ConnectionMonitor;
    }

    public void setHL7ConnectionMonitor(HL7ConnectionMonitor hl7ConnectionMonitor) {
        this.hl7ConnectionMonitor = hl7ConnectionMonitor;
    }

    UnparsedHL7Message onMessage(Connection conn, Socket s, UnparsedHL7Message msg) throws HL7Exception {
        HL7Application hl7App = getHL7Application(msg.msh().getReceivingApplicationWithFacility(), true);
        if (hl7App == null || !hl7App.isInstalled() || !hl7App.getConnections().contains(conn))
            throw new HL7Exception(new ERRSegment(msg.msh()).setHL7ErrorCode(ERRSegment.TABLE_VALUE_NOT_FOUND)
                    .setErrorLocation(ERRSegment.RECEIVING_APPLICATION)
                    .setUserMessage("Receiving Application and/or Facility not recognized"));
        return hl7App.onMessage(conn, s, msg);
    }

    @Override
    public void reconfigure(DeviceExtension from) {
        reconfigureHL7Applications((HL7DeviceExtension) from);
    }

    private void reconfigureHL7Applications(HL7DeviceExtension from) {
        hl7apps.keySet().retainAll(from.hl7apps.keySet());
        for (HL7Application src : from.hl7apps.values()) {
            HL7Application hl7app = hl7apps.get(src.getApplicationName());
            if (hl7app == null)
                addHL7Application(hl7app = new HL7Application(src.getApplicationName()));
            hl7app.reconfigure(src);
        }
    }

}
