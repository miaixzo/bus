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

import java.io.IOException;
import java.io.Serial;
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
 * HL7 application used for message communication, connection management, message validation, and character set
 * handling.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HL7Application implements Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852267135868L;

    /**
     * Accepted sending applications.
     */
    private final LinkedHashSet<String> acceptedSendingApplications = new LinkedHashSet<>();

    /**
     * Alternate application names.
     */
    private final LinkedHashSet<String> otherApplicationNames = new LinkedHashSet<>();

    /**
     * Accepted message types.
     */
    private final LinkedHashSet<String> acceptedMessageTypes = new LinkedHashSet<>();

    /**
     * Network connections.
     */
    private final List<Connection> conns = new ArrayList<>(1);

    /**
     * HL7 application extensions.
     */
    private final Map<Class<? extends HL7ApplicationExtension>, HL7ApplicationExtension> extensions = new HashMap<>();

    /**
     * Owning device.
     */
    private Device device;

    /**
     * Application name.
     */
    private String name;

    /**
     * Default HL7 character set.
     */
    private String hl7DefaultCharacterSet = "ASCII";

    /**
     * Outbound HL7 character set.
     */
    private String hl7SendingCharacterSet = "ASCII";

    /**
     * Installation flag.
     */
    private Boolean installed;

    /**
     * Application description.
     */
    private String description;

    /**
     * Optional MSH fields.
     */
    private int[] optionalMSHFields = {};

    /**
     * Application clusters.
     */
    private String[] applicationClusters = {};

    /**
     * HL7 message listener.
     */
    private transient HL7MessageListener hl7MessageListener;

    /**
     * Creates an empty HL7 application.
     */
    public HL7Application() {

    }

    /**
     * Creates an HL7 application with the specified name.
     *
     * @param name application name
     */
    public HL7Application(String name) {
        setApplicationName(name);
    }

    /**
     * Returns the owning device.
     *
     * @return owning device
     */
    public final Device getDevice() {
        return device;
    }

    /**
     * Sets the owning device.
     *
     * @param device owning device
     * @throws IllegalStateException if the application is already owned or a connection belongs to another device
     */
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

    /**
     * Returns the application name.
     *
     * @return application name
     */
    public String getApplicationName() {
        return name;
    }

    /**
     * Sets the application name.
     *
     * @param name application name
     * @throws IllegalArgumentException if the name is empty
     */
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

    /**
     * Returns the default HL7 character set.
     *
     * @return default HL7 character set
     */
    public final String getHL7DefaultCharacterSet() {
        return hl7DefaultCharacterSet;
    }

    /**
     * Sets the default HL7 character set.
     *
     * @param hl7DefaultCharacterSet default HL7 character set
     */
    public final void setHL7DefaultCharacterSet(String hl7DefaultCharacterSet) {
        this.hl7DefaultCharacterSet = hl7DefaultCharacterSet;
    }

    /**
     * Returns the outbound HL7 character set.
     *
     * @return outbound HL7 character set
     */
    public String getHL7SendingCharacterSet() {
        return hl7SendingCharacterSet;
    }

    /**
     * Sets the outbound HL7 character set.
     *
     * @param hl7SendingCharacterSet outbound HL7 character set
     */
    public void setHL7SendingCharacterSet(String hl7SendingCharacterSet) {
        this.hl7SendingCharacterSet = hl7SendingCharacterSet;
    }

    /**
     * Returns accepted sending applications.
     *
     * @return accepted sending applications
     */
    public String[] getAcceptedSendingApplications() {
        return acceptedSendingApplications.toArray(new String[acceptedSendingApplications.size()]);
    }

    /**
     * Sets accepted sending applications.
     *
     * @param names sending application names
     */
    public void setAcceptedSendingApplications(String... names) {
        acceptedSendingApplications.clear();
        Collections.addAll(acceptedSendingApplications, names);
    }

    /**
     * Returns alternate application names.
     *
     * @return alternate application names
     */
    public String[] getOtherApplicationNames() {
        return otherApplicationNames.toArray(new String[otherApplicationNames.size()]);
    }

    /**
     * Sets alternate application names.
     *
     * @param names alternate application names
     */
    public void setOtherApplicationNames(String... names) {
        otherApplicationNames.clear();
        Collections.addAll(otherApplicationNames, names);
    }

    /**
     * Checks whether the specified name is configured as an alternate application name.
     *
     * @param name name to check
     * @return {@code true} if the name is configured
     */
    public boolean isOtherApplicationName(String name) {
        return otherApplicationNames.contains(name);
    }

    /**
     * Returns accepted message types.
     *
     * @return accepted message types
     */
    public String[] getAcceptedMessageTypes() {
        return acceptedMessageTypes.toArray(new String[acceptedMessageTypes.size()]);
    }

    /**
     * Sets accepted message types.
     *
     * @param types message types
     */
    public void setAcceptedMessageTypes(String... types) {
        acceptedMessageTypes.clear();
        Collections.addAll(acceptedMessageTypes, types);
    }

    /**
     * Returns the application description.
     *
     * @return application description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the application description.
     *
     * @param description application description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns optional MSH fields.
     *
     * @return optional MSH fields
     */
    public int[] getOptionalMSHFields() {
        return optionalMSHFields;
    }

    /**
     * Sets optional MSH fields.
     *
     * @param optionalMSHFields optional MSH fields
     */
    public void setOptionalMSHFields(int... optionalMSHFields) {
        this.optionalMSHFields = optionalMSHFields;
    }

    /**
     * Returns application clusters.
     *
     * @return application clusters
     */
    public String[] getApplicationClusters() {
        return applicationClusters;
    }

    /**
     * Sets application clusters.
     *
     * @param applicationClusters application clusters
     */
    public void setApplicationClusters(String[] applicationClusters) {
        this.applicationClusters = applicationClusters;
    }

    /**
     * Checks whether the application is installed.
     *
     * @return {@code true} if the application is installed
     */
    public boolean isInstalled() {
        return device != null && device.isInstalled() && (installed == null || installed.booleanValue());
    }

    /**
     * Returns the configured installation state.
     *
     * @return installation state, or {@code null} to inherit the device state
     */
    public final Boolean getInstalled() {
        return installed;
    }

    /**
     * Sets the installation state.
     *
     * @param installed installation state
     * @throws IllegalStateException if the application is installed while the owning device is not installed
     */
    public void setInstalled(Boolean installed) {
        if (installed != null && installed.booleanValue() && device != null && !device.isInstalled())
            throw new IllegalStateException("owning device not installed");
        this.installed = installed;
    }

    /**
     * Returns the HL7 message listener.
     *
     * @return HL7 message listener
     */
    public HL7MessageListener getHL7MessageListener() {
        HL7MessageListener listener = hl7MessageListener;
        if (listener != null)
            return listener;
        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);
        return hl7Ext != null ? hl7Ext.getHL7MessageListener() : null;
    }

    /**
     * Sets the HL7 message listener.
     *
     * @param listener HL7 message listener
     */
    public final void setHL7MessageListener(HL7MessageListener listener) {
        this.hl7MessageListener = listener;
    }

    /**
     * Adds a network connection to this HL7 application.
     *
     * @param conn network connection to add
     * @throws IllegalArgumentException if the connection protocol is not HL7
     * @throws IllegalStateException    if the connection belongs to another device
     */
    public void addConnection(Connection conn) {
        if (!conn.getProtocol().isHL7())
            throw new IllegalArgumentException("protocol != HL7 - " + conn.getProtocol());
        if (device != null && device != conn.getDevice())
            throw new IllegalStateException(conn + " not contained by " + device.getDeviceName());
        conns.add(conn);
    }

    /**
     * Removes a network connection from this HL7 application.
     *
     * @param conn network connection to remove
     * @return {@code true} if the connection was removed
     */
    public boolean removeConnection(Connection conn) {
        return conns.remove(conn);
    }

    /**
     * Returns all network connections for this HL7 application.
     *
     * @return network connections
     */
    public List<Connection> getConnections() {
        return conns;
    }

    /**
     * Handles a received HL7 message.
     *
     * @param conn network connection
     * @param s    socket
     * @param msg  unparsed HL7 message
     * @return processed unparsed HL7 message
     * @throws HL7Exception if message handling fails
     */
    UnparsedHL7Message onMessage(Connection conn, Socket s, UnparsedHL7Message msg) throws HL7Exception {
        HL7Segment msh = msg.msh();
        validateMSH(msh);
        HL7MessageListener listener = getHL7MessageListener();
        if (listener == null)
            throw new HL7Exception(new ERRSegment(msh).setHL7ErrorCode(ERRSegment.APPLICATION_INTERNAL_ERROR)
                    .setUserMessage("No HL7 Message Listener configured"));
        return listener.onMessage(this, conn, s, msg);
    }

    /**
     * Validates the MSH segment.
     *
     * @param msh MSH segment
     * @throws HL7Exception if validation fails
     */
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

    /**
     * Checks whether a message code is unsupported.
     *
     * @param messageType message type
     * @return {@code true} if the message code is unsupported
     */
    private boolean unsupportedMessageCode(String messageType) {
        for (String acceptedMessageType : acceptedMessageTypes) {
            if (acceptedMessageType.startsWith(messageType))
                return false;
        }
        return true;
    }

    /**
     * Connects to a remote connection.
     *
     * @param remote remote connection
     * @return MLLP connection
     * @throws IOException              if an I/O error occurs
     * @throws InternalException        if no compatible connection is available
     * @throws GeneralSecurityException if a security error occurs
     */
    public MLLPConnection connect(Connection remote) throws IOException, InternalException, GeneralSecurityException {
        return connect(findCompatibleConnection(remote), remote);
    }

    /**
     * Connects to a remote HL7 application.
     *
     * @param remote remote HL7 application
     * @return MLLP connection
     * @throws IOException              if an I/O error occurs
     * @throws InternalException        if no compatible connection is available
     * @throws GeneralSecurityException if a security error occurs
     */
    public MLLPConnection connect(HL7Application remote)
            throws IOException, InternalException, GeneralSecurityException {
        Compatible cc = findCompatibleConnection(remote);
        return connect(cc.getLocalConnection(), cc.getRemoteConnection());
    }

    /**
     * Opens an MLLP connection using local and remote connections.
     *
     * @param local  local connection
     * @param remote remote connection
     * @return MLLP connection
     * @throws IOException              if an I/O error occurs
     * @throws InternalException        if no compatible connection is available
     * @throws GeneralSecurityException if a security error occurs
     */
    public MLLPConnection connect(Connection local, Connection remote)
            throws IOException, InternalException, GeneralSecurityException {
        checkDevice();
        checkInstalled();
        Socket sock = local.connect(remote);
        sock.setSoTimeout(local.getResponseTimeout());
        return new MLLPConnection(sock);
    }

    /**
     * Opens an HL7 connection to a remote connection.
     *
     * @param remote remote connection
     * @return HL7 connection
     * @throws IOException              if an I/O error occurs
     * @throws InternalException        if no compatible connection is available
     * @throws GeneralSecurityException if a security error occurs
     */
    public HL7Connection open(Connection remote) throws IOException, InternalException, GeneralSecurityException {
        return new HL7Connection(this, connect(remote));
    }

    /**
     * Opens an HL7 connection to a remote HL7 application.
     *
     * @param remote remote HL7 application
     * @return HL7 connection
     * @throws IOException              if an I/O error occurs
     * @throws InternalException        if no compatible connection is available
     * @throws GeneralSecurityException if a security error occurs
     */
    public HL7Connection open(HL7Application remote) throws IOException, InternalException, GeneralSecurityException {
        return new HL7Connection(this, connect(remote));
    }

    /**
     * Opens an HL7 connection using local and remote connections.
     *
     * @param local  local connection
     * @param remote remote connection
     * @return HL7 connection
     * @throws IOException              if an I/O error occurs
     * @throws InternalException        if no compatible connection is available
     * @throws GeneralSecurityException if a security error occurs
     */
    public HL7Connection open(Connection local, Connection remote)
            throws IOException, InternalException, GeneralSecurityException {
        return new HL7Connection(this, connect(local, remote));
    }

    /**
     * Finds compatible local and remote connections for the remote HL7 application.
     *
     * @param remote remote HL7 application
     * @return compatible local and remote connections
     * @throws InternalException if no compatible connection is available
     */
    public Compatible findCompatibleConnection(HL7Application remote) throws InternalException {
        for (Connection remoteConn : remote.conns)
            if (remoteConn.isInstalled() && remoteConn.isServer())
                for (Connection conn : conns)
                    if (conn.isInstalled() && conn.isCompatible(remoteConn))
                        return new Compatible(conn, remoteConn);
        throw new InternalException(
                "No compatible connection to " + remote.getApplicationName() + " available on " + name);
    }

    /**
     * Finds a local connection compatible with the specified remote connection.
     *
     * @param remoteConn remote connection
     * @return compatible local connection
     * @throws InternalException if no compatible connection is available
     */
    public Connection findCompatibleConnection(Connection remoteConn) throws InternalException {
        for (Connection conn : conns)
            if (conn.isInstalled() && conn.isCompatible(remoteConn))
                return conn;
        throw new InternalException("No compatible connection to " + remoteConn + " available on " + name);
    }

    /**
     * Checks that the application is installed.
     *
     * @throws IllegalStateException if the application is not installed
     */
    private void checkInstalled() {
        if (!isInstalled())
            throw new IllegalStateException("Not installed");
    }

    /**
     * Checks that the application is attached to a device.
     *
     * @throws IllegalStateException if the application is not attached to a device
     */
    private void checkDevice() {
        if (device == null)
            throw new IllegalStateException("Not attached to Device");
    }

    /**
     * Reconfigures this application from the source HL7 application.
     *
     * @param src source HL7 application
     */
    void reconfigure(HL7Application src) {
        setHL7ApplicationAttributes(src);
        device.reconfigureConnections(conns, src.conns);
        reconfigureHL7ApplicationExtensions(src);
    }

    /**
     * Reconfigures HL7 application extensions from the source application.
     *
     * @param from source HL7 application
     */
    private void reconfigureHL7ApplicationExtensions(HL7Application from) {
        extensions.keySet().removeIf(aClass -> !from.extensions.containsKey(aClass));
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

    /**
     * Copies HL7 application attributes from the source application.
     *
     * @param src source HL7 application
     */
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

    /**
     * Adds an HL7 application extension.
     *
     * @param ext HL7 application extension to add
     * @throws IllegalStateException if an extension of the same type already exists
     */
    public void addHL7ApplicationExtension(HL7ApplicationExtension ext) {
        Class<? extends HL7ApplicationExtension> clazz = ext.getClass();
        if (extensions.containsKey(clazz))
            throw new IllegalStateException("already contains AE Extension:" + clazz);
        ext.setHL7Application(this);
        extensions.put(clazz, ext);
    }

    /**
     * Removes an HL7 application extension.
     *
     * @param ext HL7 application extension to remove
     * @return {@code true} if the extension was removed
     */
    public boolean removeHL7ApplicationExtension(HL7ApplicationExtension ext) {
        if (extensions.remove(ext.getClass()) == null)
            return false;
        ext.setHL7Application(null);
        return true;
    }

    /**
     * Lists all HL7 application extensions.
     *
     * @return HL7 application extensions
     */
    public Collection<HL7ApplicationExtension> listHL7ApplicationExtensions() {
        return extensions.values();
    }

    /**
     * Returns the HL7 application extension for the specified type.
     *
     * @param <T>   extension type
     * @param clazz extension class
     * @return matching HL7 application extension, or {@code null}
     */
    public <T extends HL7ApplicationExtension> T getHL7ApplicationExtension(Class<T> clazz) {
        return (T) extensions.get(clazz);
    }

    /**
     * Returns the HL7 application extension for the specified type.
     *
     * @param <T>   extension type
     * @param clazz extension class
     * @return matching HL7 application extension
     * @throws IllegalStateException if no extension of the specified type exists
     */
    public <T extends HL7ApplicationExtension> T getHL7AppExtensionNotNull(Class<T> clazz) {
        T hl7AppExt = getHL7ApplicationExtension(clazz);
        if (hl7AppExt == null)
            throw new IllegalStateException("No " + clazz.getName() + " configured for HL7 Application: " + name);
        return hl7AppExt;
    }

}
