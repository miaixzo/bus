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
import java.io.Serializable;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.*;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.metric.Compatible;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.hl7.ERRSegment;
import org.miaixz.bus.image.metric.hl7.HL7Exception;
import org.miaixz.bus.image.metric.hl7.HL7Segment;
import org.miaixz.bus.image.metric.hl7.MLLPConnection;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL7Application implements Serializable {

    private static final long serialVersionUID = -1L;
    private final LinkedHashSet<String> acceptedSendingApplications = new LinkedHashSet<>();
    private final LinkedHashSet<String> otherApplicationNames = new LinkedHashSet<>();
    private final LinkedHashSet<String> acceptedMessageTypes = new LinkedHashSet<>();
    private final List<Connection> conns = new ArrayList<>(1);
    private final Map<Class<? extends HL7ApplicationExtension>, HL7ApplicationExtension> extensions = new HashMap<>();
    private Device device;
    private String name;
    private String hl7DefaultCharacterSet = "ASCII";
    private String hl7SendingCharacterSet = "ASCII";
    private Boolean installed;
    private String description;
    private int[] optionalMSHFields = {};
    private String[] applicationClusters = {};
    private transient HL7MessageListener hl7MessageListener;

    public HL7Application() {
    }

    public HL7Application(String name) {
        setApplicationName(name);
    }

    public final Device getDevice() {
        return device;
    }

    void setDevice(Device device) {
        if (device != null) {
            if (this.device != null)
                throw new IllegalStateException("already owned by " + this.device.getDeviceName());
            for (Connection conn : conns)
                if (conn.getDevice() != device)
                    throw new IllegalStateException(conn + " not owned by " + device.getDeviceName());
        }
        this.device = device;
    }

    public String getApplicationName() {
        return name;
    }

    public void setApplicationName(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("name cannot be empty");
        HL7DeviceExtension ext = device != null ? device.getDeviceExtension(HL7DeviceExtension.class) : null;
        if (ext != null)
            ext.removeHL7Application(this.name);
        this.name = name;
        if (ext != null)
            ext.addHL7Application(this);
    }

    public final String getHL7DefaultCharacterSet() {
        return hl7DefaultCharacterSet;
    }

    public final void setHL7DefaultCharacterSet(String hl7DefaultCharacterSet) {
        this.hl7DefaultCharacterSet = hl7DefaultCharacterSet;
    }

    public String getHL7SendingCharacterSet() {
        return hl7SendingCharacterSet;
    }

    public void setHL7SendingCharacterSet(String hl7SendingCharacterSet) {
        this.hl7SendingCharacterSet = hl7SendingCharacterSet;
    }

    public String[] getAcceptedSendingApplications() {
        return acceptedSendingApplications.toArray(new String[acceptedSendingApplications.size()]);
    }

    public void setAcceptedSendingApplications(String... names) {
        acceptedSendingApplications.clear();
        Collections.addAll(acceptedSendingApplications, names);
    }

    public String[] getOtherApplicationNames() {
        return otherApplicationNames.toArray(new String[otherApplicationNames.size()]);
    }

    public void setOtherApplicationNames(String... names) {
        otherApplicationNames.clear();
        Collections.addAll(otherApplicationNames, names);
    }

    public boolean isOtherApplicationName(String name) {
        return otherApplicationNames.contains(name);
    }

    public String[] getAcceptedMessageTypes() {
        return acceptedMessageTypes.toArray(new String[acceptedMessageTypes.size()]);
    }

    public void setAcceptedMessageTypes(String... types) {
        acceptedMessageTypes.clear();
        Collections.addAll(acceptedMessageTypes, types);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int[] getOptionalMSHFields() {
        return optionalMSHFields;
    }

    public void setOptionalMSHFields(int... optionalMSHFields) {
        this.optionalMSHFields = optionalMSHFields;
    }

    public String[] getApplicationClusters() {
        return applicationClusters;
    }

    public void setApplicationClusters(String[] applicationClusters) {
        this.applicationClusters = applicationClusters;
    }

    public boolean isInstalled() {
        return device != null && device.isInstalled() && (installed == null || installed.booleanValue());
    }

    public final Boolean getInstalled() {
        return installed;
    }

    public void setInstalled(Boolean installed) {
        if (installed != null && installed.booleanValue() && device != null && !device.isInstalled())
            throw new IllegalStateException("owning device not installed");
        this.installed = installed;
    }

    public HL7MessageListener getHL7MessageListener() {
        HL7MessageListener listener = hl7MessageListener;
        if (listener != null)
            return listener;

        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);
        return hl7Ext != null ? hl7Ext.getHL7MessageListener() : null;
    }

    public final void setHL7MessageListener(HL7MessageListener listener) {
        this.hl7MessageListener = listener;
    }

    public void addConnection(Connection conn) {
        if (!conn.getProtocol().isHL7())
            throw new IllegalArgumentException("protocol != HL7 - " + conn.getProtocol());

        if (device != null && device != conn.getDevice())
            throw new IllegalStateException(conn + " not contained by " + device.getDeviceName());
        conns.add(conn);
    }

    public boolean removeConnection(Connection conn) {
        return conns.remove(conn);
    }

    public List<Connection> getConnections() {
        return conns;
    }

    UnparsedHL7Message onMessage(Connection conn, Socket s, UnparsedHL7Message msg) throws HL7Exception {
        HL7Segment msh = msg.msh();
        validateMSH(msh);
        HL7MessageListener listener = getHL7MessageListener();
        if (listener == null)
            throw new HL7Exception(new ERRSegment(msh).setHL7ErrorCode(ERRSegment.APPLICATION_INTERNAL_ERROR)
                    .setUserMessage("No HL7 Message Listener configured"));
        return listener.onMessage(this, conn, s, msg);
    }

    private void validateMSH(HL7Segment msh) throws HL7Exception {
        String[] errorLocations = { ERRSegment.SENDING_APPLICATION, // MSH-3
                ERRSegment.SENDING_FACILITY, // MSH-4
                ERRSegment.RECEIVING_APPLICATION, // MSH-5
                ERRSegment.RECEIVING_FACILITY, // MSH-6
                ERRSegment.MESSAGE_DATETIME, // MSH-7
                null, // MSH-8
                ERRSegment.MESSAGE_CODE, // MSH-9
                ERRSegment.MESSAGE_CONTROL_ID, // MSH-10
                ERRSegment.MESSAGE_PROCESSING_ID, // MSH-11
                ERRSegment.MESSAGE_VERSION_ID, // MSH-12
        };
        String[] userMsg = { "Missing Sending Application", "Missing Sending Facility", "Missing Receiving Application",
                "Missing Receiving Facility", "Missing Date/Time of Message", null, "Missing Message Type",
                "Missing Message Control ID", "Missing Processing ID", "Missing Version ID" };
        for (int hl7OptionalMSHField : optionalMSHFields) {
            try {
                errorLocations[hl7OptionalMSHField - 3] = null;
            } catch (IndexOutOfBoundsException ignore) {
            }
        }
        errorLocations[6] = ERRSegment.MESSAGE_CODE; // never optional
        for (int i = 0; i < errorLocations.length; i++) {
            if (errorLocations[i] != null)
                if (msh.getField(i + 2, null) == null)
                    throw new HL7Exception(new ERRSegment(msh).setHL7ErrorCode(ERRSegment.REQUIRED_FIELD_MISSING)
                            .setErrorLocation(errorLocations[i]).setUserMessage(userMsg[i]));
        }
        if (!(acceptedSendingApplications.isEmpty()
                || acceptedSendingApplications.contains(msh.getSendingApplicationWithFacility())))
            throw new HL7Exception(new ERRSegment(msh).setHL7ErrorCode(ERRSegment.TABLE_VALUE_NOT_FOUND)
                    .setErrorLocation(ERRSegment.SENDING_APPLICATION)
                    .setUserMessage("Sending Application and/or Facility not recognized"));
        String messageType = msh.getMessageType();
        if (!(acceptedMessageTypes.contains("*") || acceptedMessageTypes.contains(messageType))) {
            if (unsupportedMessageCode(messageType.substring(0, 3)))
                throw new HL7Exception(new ERRSegment(msh).setHL7ErrorCode(ERRSegment.UNSUPPORTED_MESSAGE_TYPE)
                        .setErrorLocation(ERRSegment.MESSAGE_CODE)
                        .setUserMessage("Message Type - Message Code not supported"));

            throw new HL7Exception(new ERRSegment(msh).setHL7ErrorCode(ERRSegment.UNSUPPORTED_EVENT_CODE)
                    .setErrorLocation(ERRSegment.TRIGGER_EVENT)
                    .setUserMessage("Message Type - Trigger Event not supported"));
        }
    }

    private boolean unsupportedMessageCode(String messageType) {
        for (String acceptedMessageType : acceptedMessageTypes) {
            if (acceptedMessageType.startsWith(messageType))
                return false;
        }
        return true;
    }

    public MLLPConnection connect(Connection remote) throws IOException, InternalException, GeneralSecurityException {
        return connect(findCompatibleConnection(remote), remote);
    }

    public MLLPConnection connect(HL7Application remote)
            throws IOException, InternalException, GeneralSecurityException {
        Compatible cc = findCompatibleConnection(remote);
        return connect(cc.getLocalConnection(), cc.getRemoteConnection());
    }

    public MLLPConnection connect(Connection local, Connection remote)
            throws IOException, InternalException, GeneralSecurityException {
        checkDevice();
        checkInstalled();
        Socket sock = local.connect(remote);
        sock.setSoTimeout(local.getResponseTimeout());
        return new MLLPConnection(sock);
    }

    public HL7Connection open(Connection remote) throws IOException, InternalException, GeneralSecurityException {
        return new HL7Connection(this, connect(remote));
    }

    public HL7Connection open(HL7Application remote) throws IOException, InternalException, GeneralSecurityException {
        return new HL7Connection(this, connect(remote));
    }

    public HL7Connection open(Connection local, Connection remote)
            throws IOException, InternalException, GeneralSecurityException {
        return new HL7Connection(this, connect(local, remote));
    }

    public Compatible findCompatibleConnection(HL7Application remote) throws InternalException {
        for (Connection remoteConn : remote.conns)
            if (remoteConn.isInstalled() && remoteConn.isServer())
                for (Connection conn : conns)
                    if (conn.isInstalled() && conn.isCompatible(remoteConn))
                        return new Compatible(conn, remoteConn);
        throw new InternalException(
                "No compatible connection to " + remote.getApplicationName() + " available on " + name);
    }

    public Connection findCompatibleConnection(Connection remoteConn) throws InternalException {
        for (Connection conn : conns)
            if (conn.isInstalled() && conn.isCompatible(remoteConn))
                return conn;
        throw new InternalException("No compatible connection to " + remoteConn + " available on " + name);
    }

    private void checkInstalled() {
        if (!isInstalled())
            throw new IllegalStateException("Not installed");
    }

    private void checkDevice() {
        if (device == null)
            throw new IllegalStateException("Not attached to Device");
    }

    void reconfigure(HL7Application src) {
        setHL7ApplicationAttributes(src);
        device.reconfigureConnections(conns, src.conns);
        reconfigureHL7ApplicationExtensions(src);
    }

    private void reconfigureHL7ApplicationExtensions(HL7Application from) {
        for (Iterator<Class<? extends HL7ApplicationExtension>> it = extensions.keySet().iterator(); it.hasNext();) {
            if (!from.extensions.containsKey(it.next()))
                it.remove();
        }
        for (HL7ApplicationExtension src : from.extensions.values()) {
            Class<? extends HL7ApplicationExtension> clazz = src.getClass();
            HL7ApplicationExtension ext = extensions.get(clazz);
            if (ext == null)
                try {
                    addHL7ApplicationExtension(ext = clazz.newInstance());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate " + clazz.getName(), e);
                }
            ext.reconfigure(src);
        }
    }

    protected void setHL7ApplicationAttributes(HL7Application src) {
        description = src.description;
        applicationClusters = src.applicationClusters;
        hl7DefaultCharacterSet = src.hl7DefaultCharacterSet;
        hl7SendingCharacterSet = src.hl7SendingCharacterSet;
        optionalMSHFields = src.optionalMSHFields;
        acceptedSendingApplications.clear();
        acceptedSendingApplications.addAll(src.acceptedSendingApplications);
        otherApplicationNames.clear();
        otherApplicationNames.addAll(src.otherApplicationNames);
        acceptedMessageTypes.clear();
        acceptedMessageTypes.addAll(src.acceptedMessageTypes);
        installed = src.installed;
    }

    public void addHL7ApplicationExtension(HL7ApplicationExtension ext) {
        Class<? extends HL7ApplicationExtension> clazz = ext.getClass();
        if (extensions.containsKey(clazz))
            throw new IllegalStateException("already contains AE Extension:" + clazz);

        ext.setHL7Application(this);
        extensions.put(clazz, ext);
    }

    public boolean removeHL7ApplicationExtension(HL7ApplicationExtension ext) {
        if (extensions.remove(ext.getClass()) == null)
            return false;

        ext.setHL7Application(null);
        return true;
    }

    public Collection<HL7ApplicationExtension> listHL7ApplicationExtensions() {
        return extensions.values();
    }

    public <T extends HL7ApplicationExtension> T getHL7ApplicationExtension(Class<T> clazz) {
        return (T) extensions.get(clazz);
    }

    public <T extends HL7ApplicationExtension> T getHL7AppExtensionNotNull(Class<T> clazz) {
        T hl7AppExt = getHL7ApplicationExtension(clazz);
        if (hl7AppExt == null)
            throw new IllegalStateException("No " + clazz.getName() + " configured for HL7 Application: " + name);
        return hl7AppExt;
    }

}
