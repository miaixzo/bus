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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.metric.Compatible;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.acquire.HL7ApplicationExtension;
import org.miaixz.bus.image.metric.acquire.HL7DeviceExtension;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL7Application implements Serializable {

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

    public void setDevice(Device device) {
        if (null != device) {
            if (null != this.device)
                throw new IllegalStateException("already owned by " +
                        this.device.getDeviceName());
            for (Connection conn : conns)
                if (conn.getDevice() != device)
                    throw new IllegalStateException(conn + " not owned by " +
                            device.getDeviceName());
        }
        this.device = device;
    }

    public String getApplicationName() {
        return name;
    }

    public void setApplicationName(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("name cannot be empty");
        HL7DeviceExtension ext = null != device
                ? device.getDeviceExtension(HL7DeviceExtension.class)
                : null;
        if (null != ext)
            ext.removeHL7Application(this.name);
        this.name = name;
        if (null != ext)
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
        return acceptedSendingApplications.toArray(
                new String[acceptedSendingApplications.size()]);
    }

    public void setAcceptedSendingApplications(String... names) {
        acceptedSendingApplications.clear();
        for (String name : names)
            acceptedSendingApplications.add(name);
    }

    public String[] getOtherApplicationNames() {
        return otherApplicationNames.toArray(new String[otherApplicationNames.size()]);
    }

    public void setOtherApplicationNames(String... names) {
        otherApplicationNames.clear();
        for (String name : names)
            otherApplicationNames.add(name);
    }

    public boolean isOtherApplicationName(String name) {
        return otherApplicationNames.contains(name);
    }

    public String[] getAcceptedMessageTypes() {
        return acceptedMessageTypes.toArray(
                new String[acceptedMessageTypes.size()]);
    }

    public void setAcceptedMessageTypes(String... types) {
        acceptedMessageTypes.clear();
        for (String name : types)
            acceptedMessageTypes.add(name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getApplicationClusters() {
        return applicationClusters;
    }

    public void setApplicationClusters(String[] applicationClusters) {
        this.applicationClusters = applicationClusters;
    }

    public boolean isInstalled() {
        return null != device && device.isInstalled()
                && (null == installed || installed.booleanValue());
    }

    public final Boolean getInstalled() {
        return installed;
    }

    public void setInstalled(Boolean installed) {
        if (null != installed && installed.booleanValue()
                && null != device && !device.isInstalled())
            throw new IllegalStateException("owning device not installed");
        this.installed = installed;
    }

    public HL7MessageListener getHL7MessageListener() {
        HL7MessageListener listener = hl7MessageListener;
        if (null != listener) {
            return listener;
        }

        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);
        return null != hl7Ext ? hl7Ext.getHL7MessageListener() : null;
    }

    public final void setHL7MessageListener(HL7MessageListener listener) {
        this.hl7MessageListener = listener;
    }

    public void addConnection(Connection conn) {
        if (conn.getProtocol() != Connection.Protocol.HL7)
            throw new IllegalArgumentException(
                    "protocol != HL7 - " + conn.getProtocol());

        if (null != device && device != conn.getDevice())
            throw new IllegalStateException(conn + " not contained by " +
                    device.getDeviceName());
        conns.add(conn);
    }

    public boolean removeConnection(Connection conn) {
        return conns.remove(conn);
    }

    public List<Connection> getConnections() {
        return conns;
    }

    public UnparsedHL7Message onMessage(Connection conn, Socket s, UnparsedHL7Message msg) throws HL7Exception {
        HL7Segment msh = msg.msh();
        if (!(isInstalled() && conns.contains(conn)))
            throw new HL7Exception(
                    new ERRSegment(msh)
                            .setHL7ErrorCode(Builder.TableValueNotFound)
                            .setErrorLocation(Builder.UnknownReceivingApplication)
                            .setUserMessage("Receiving Application not recognized"));
        if (!(acceptedSendingApplications.isEmpty()
                || acceptedSendingApplications.contains(msh.getSendingApplicationWithFacility())))
            throw new HL7Exception(
                    new ERRSegment(msh)
                            .setHL7ErrorCode(Builder.TableValueNotFound)
                            .setErrorLocation(Builder.UnknownSendingApplication)
                            .setUserMessage("Sending Application not recognized"));
        String messageType = msh.getMessageType();
        if (!(acceptedMessageTypes.contains(Symbol.STAR)
                || acceptedMessageTypes.contains(messageType)))
            throw new HL7Exception(
                    new ERRSegment(msh)
                            .setHL7ErrorCode(unsupportedMessageTypeOrEventCode(messageType.substring(0, 3)))
                            .setUserMessage("Message Type not supported"));

        HL7MessageListener listener = getHL7MessageListener();
        if (null == listener)
            throw new HL7Exception(new ERRSegment(msh)
                    .setHL7ErrorCode(Builder.ApplicationInternalError)
                    .setUserMessage("No HL7 Message Listener configured"));

        return listener.onMessage(this, conn, s, msg);
    }

    private String unsupportedMessageTypeOrEventCode(String messageType) {
        for (String acceptedMessageType : acceptedMessageTypes) {
            if (acceptedMessageType.startsWith(messageType))
                return Builder.UnsupportedEventCode;
        }
        return Builder.UnsupportedMessageType;
    }

    public MLLPConnection connect(Connection remote)
            throws IOException, InternalException, GeneralSecurityException {
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

    public HL7Connection open(Connection remote)
            throws IOException, InternalException, GeneralSecurityException {
        return new HL7Connection(this, connect(remote));
    }

    public HL7Connection open(HL7Application remote)
            throws IOException, InternalException, GeneralSecurityException {
        return new HL7Connection(this, connect(remote));
    }

    public HL7Connection open(Connection local, Connection remote)
            throws IOException, InternalException, GeneralSecurityException {
        return new HL7Connection(this, connect(local, remote));
    }

    public Compatible findCompatibleConnection(HL7Application remote)
            throws InternalException {
        for (Connection remoteConn : remote.conns)
            if (remoteConn.isInstalled() && remoteConn.isServer())
                for (Connection conn : conns)
                    if (conn.isInstalled() && conn.isCompatible(remoteConn))
                        return new Compatible(conn, remoteConn);
        throw new InternalException(
                "No compatible connection to " + remote.getApplicationName() + " available on " + name);
    }

    public Connection findCompatibleConnection(Connection remoteConn)
            throws InternalException {
        for (Connection conn : conns)
            if (conn.isInstalled() && conn.isCompatible(remoteConn))
                return conn;
        throw new InternalException(
                "No compatible connection to " + remoteConn + " available on " + name);
    }

    private void checkInstalled() {
        if (!isInstalled())
            throw new IllegalStateException("Not installed");
    }

    private void checkDevice() {
        if (null == device)
            throw new IllegalStateException("Not attached to Device");
    }

    public void reconfigure(HL7Application src) {
        setHL7ApplicationAttributes(src);
        device.reconfigureConnections(conns, src.conns);
        reconfigureHL7ApplicationExtensions(src);
    }

    private void reconfigureHL7ApplicationExtensions(HL7Application from) {
        for (Iterator<Class<? extends HL7ApplicationExtension>> it =
             extensions.keySet().iterator(); it.hasNext(); ) {
            if (!from.extensions.containsKey(it.next()))
                it.remove();
        }
        for (HL7ApplicationExtension src : from.extensions.values()) {
            Class<? extends HL7ApplicationExtension> clazz = src.getClass();
            HL7ApplicationExtension ext = extensions.get(clazz);
            if (null == ext)
                try {
                    addHL7ApplicationExtension(ext = clazz.newInstance());
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to instantiate " + clazz.getName(), e);
                }
            ext.reconfigure(src);
        }
    }

    protected void setHL7ApplicationAttributes(HL7Application src) {
        description = src.description;
        applicationClusters = src.applicationClusters;
        hl7DefaultCharacterSet = src.hl7DefaultCharacterSet;
        hl7SendingCharacterSet = src.hl7SendingCharacterSet;
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
            throw new IllegalStateException(
                    "already contains AE Extension:" + clazz);

        ext.setHL7Application(this);
        extensions.put(clazz, ext);
    }

    public boolean removeHL7ApplicationExtension(HL7ApplicationExtension ext) {
        if (null == extensions.remove(ext.getClass()))
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
        if (null == hl7AppExt)
            throw new IllegalStateException("No " + clazz.getName()
                    + " configured for HL7 Application: " + name);
        return hl7AppExt;
    }

}
