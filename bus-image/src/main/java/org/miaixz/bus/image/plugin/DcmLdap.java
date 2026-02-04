/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.builtin.ldap.LdapDicomConfiguration;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.api.DicomConfiguration;
import org.miaixz.bus.image.metric.net.ApplicationEntity;

import java.io.Closeable;
import java.util.EnumSet;
import java.util.Hashtable;

/**
 * The {@code DcmLdap} class provides a high-level API for managing DICOM configuration data stored in an LDAP
 * directory. It allows creating, adding, and removing DICOM devices and application entities from the LDAP-based
 * configuration.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DcmLdap implements Closeable {

    /**
     * Default LDAP URI if not specified.
     */
    private static final String DEFAULT_LDAP_URI = "ldap://localhost:389/dc=miaixz,dc=org";
    /**
     * Default Bind DN for LDAP authentication.
     */
    private static final String DEFAULT_BIND_DN = "cn=admin,dc=miaixz,dc=org";
    /**
     * Default password for LDAP authentication.
     */
    private static final String DEFAULT_PASSWORD = "secret";
    /**
     * The LDAP DICOM configuration handler.
     */
    private final LdapDicomConfiguration conf;
    /**
     * The name of the device.
     */
    private String deviceName;
    /**
     * The description of the device.
     */
    private String deviceDesc;
    /**
     * The type of the device.
     */
    private String deviceType;
    /**
     * The Application Entity title.
     */
    private String aeTitle;
    /**
     * The description of the Application Entity.
     */
    private String aeDesc;
    /**
     * The network connection details.
     */
    private Connection conn;

    /**
     * Constructs a new {@code DcmLdap} instance with the given LDAP environment settings.
     *
     * @param env A {@link Hashtable} containing the JNDI environment properties for the LDAP connection.
     * @throws InternalException if the LDAP configuration cannot be initialized.
     */
    public DcmLdap(Hashtable<?, ?> env) throws InternalException {
        conf = new LdapDicomConfiguration(env);
    }

    /**
     * Sets the name of the device to be managed.
     *
     * @param deviceName The device name.
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * Sets the description for the device.
     *
     * @param deviceDesc The device description.
     */
    public void setDeviceDescription(String deviceDesc) {
        this.deviceDesc = deviceDesc;
    }

    /**
     * Sets the primary device type.
     *
     * @param deviceType The device type string.
     */
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    /**
     * Sets the description for the Application Entity.
     *
     * @param aeDesc The Application Entity description.
     */
    public void setAEDescription(String aeDesc) {
        this.aeDesc = aeDesc;
    }

    /**
     * Sets the title of the Application Entity to be managed.
     *
     * @param aeTitle The AE Title.
     */
    public void setAETitle(String aeTitle) {
        this.aeTitle = aeTitle;
    }

    /**
     * Sets the network connection details for the Application Entity.
     *
     * @param conn The connection object.
     */
    public void setConnection(Connection conn) {
        this.conn = conn;
    }

    /**
     * Closes the underlying LDAP connection.
     */
    @Override
    public void close() {
        conf.close();
    }

    /**
     * Creates a new network Application Entity and its containing device in the LDAP directory. If the device already
     * exists, this operation might fail depending on the LDAP server configuration.
     *
     * @throws InternalException if an error occurs during the LDAP operation.
     */
    public void createNetworkAE() throws InternalException {
        Device device = new Device(deviceName != null ? deviceName : aeTitle.toLowerCase());
        device.setDescription(deviceDesc);
        if (deviceType != null) {
            device.setPrimaryDeviceTypes(deviceType);
        }
        device.addConnection(conn);
        ApplicationEntity ae = new ApplicationEntity(aeTitle);
        device.addApplicationEntity(ae);
        ae.setDescription(aeDesc);
        ae.addConnection(conn);
        conf.persist(device, EnumSet.of(DicomConfiguration.Option.REGISTER));
    }

    /**
     * Adds a new network Application Entity to an existing device in the LDAP directory. The device is identified by
     * the device name set via {@link #setDeviceName(String)}.
     *
     * @throws InternalException if the device is not found or an error occurs during the LDAP operation.
     */
    public void addNetworkAE() throws InternalException {
        Device device = conf.findDevice(deviceName);
        device.addConnection(conn);
        ApplicationEntity ae = new ApplicationEntity(aeTitle);
        device.addApplicationEntity(ae);
        ae.setDescription(aeDesc);
        ae.addConnection(conn);
        conf.merge(device, EnumSet.of(DicomConfiguration.Option.REGISTER));
    }

    /**
     * Removes a network Application Entity from the LDAP directory. If the AE is the last one on its device, the device
     * itself is also removed. The AE is identified by the AE title set via {@link #setAETitle(String)}.
     *
     * @throws InternalException if the AE is not found or an error occurs during the LDAP operation.
     */
    public void removeNetworkAE() throws InternalException {
        ApplicationEntity ae = conf.findApplicationEntity(aeTitle);
        Device device = ae.getDevice();
        device.removeApplicationEntity(aeTitle);
        for (Connection conn : ae.getConnections()) {
            device.removeConnection(conn);
        }
        if (device.getApplicationAETitles().isEmpty())
            conf.removeDevice(device.getDeviceName(), EnumSet.of(DicomConfiguration.Option.REGISTER));
        else
            conf.merge(device, EnumSet.of(DicomConfiguration.Option.REGISTER));
    }

}
