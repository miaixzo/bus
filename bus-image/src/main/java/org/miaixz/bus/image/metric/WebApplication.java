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
package org.miaixz.bus.image.metric;

import java.util.*;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.metric.net.KeycloakClient;

/**
 * Description of a Web Application provided by {@link Device}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class WebApplication {

    /**
     * The service classes value.
     */
    private final EnumSet<ServiceClass> serviceClasses = EnumSet.noneOf(ServiceClass.class);

    /**
     * The properties value.
     */
    private final Map<String, String> properties = new HashMap<>();

    /**
     * The conns value.
     */
    private final List<Connection> conns = new ArrayList<>(1);

    /**
     * The device value.
     */
    private Device device;

    /**
     * The application name value.
     */
    private String applicationName;

    /**
     * The description value.
     */
    private String description;

    /**
     * The service path value.
     */
    private String servicePath;

    /**
     * The ae title value.
     */
    private String aeTitle;

    /**
     * The application clusters value.
     */
    private String[] applicationClusters = {};

    /**
     * The keycloak client id value.
     */
    private String keycloakClientID;

    /**
     * The installed value.
     */
    private Boolean installed;

    /**
     * The device name value.
     */
    private String deviceName;

    /**
     * The keycloak client value.
     */
    private KeycloakClient keycloakClient;

    /**
     * Creates a new instance.
     */
    public WebApplication() {
        // No initialization required.
    }

    /**
     * Creates a new instance.
     *
     * @param applicationName the application name.
     */
    public WebApplication(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * Gets the device.
     *
     * @return the device.
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Sets the device.
     *
     * @param device the device.
     */
    public void setDevice(Device device) {
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
     * Gets the application name.
     *
     * @return the application name.
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Sets the application name.
     *
     * @param name the name.
     */
    public void setApplicationName(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("name cannot be empty");
        Device device = this.device;
        if (device != null)
            device.removeWebApplication(this.applicationName);
        this.applicationName = name;
        if (device != null)
            device.addWebApplication(this);
    }

    /**
     * Gets the description.
     *
     * @return the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the service path.
     *
     * @return the service path.
     */
    public String getServicePath() {
        return servicePath;
    }

    /**
     * Sets the service path.
     *
     * @param servicePath the service path.
     */
    public void setServicePath(String servicePath) {
        this.servicePath = servicePath.startsWith(Symbol.SLASH) ? servicePath : Symbol.C_SLASH + servicePath;
    }

    /**
     * Gets the ae title.
     *
     * @return the ae title.
     */
    public String getAETitle() {
        return aeTitle;
    }

    /**
     * Sets the ae title.
     *
     * @param aeTitle the ae title.
     */
    public void setAETitle(String aeTitle) {
        this.aeTitle = aeTitle;
    }

    /**
     * Gets the application clusters.
     *
     * @return the application clusters.
     */
    public String[] getApplicationClusters() {
        return applicationClusters;
    }

    /**
     * Sets the application clusters.
     *
     * @param applicationClusters the application clusters.
     */
    public void setApplicationClusters(String... applicationClusters) {
        this.applicationClusters = applicationClusters;
    }

    /**
     * Gets the keycloak client id.
     *
     * @return the keycloak client id.
     */
    public String getKeycloakClientID() {
        return keycloakClientID;
    }

    /**
     * Sets the keycloak client id.
     *
     * @param keycloakClientID the keycloak client id.
     */
    public void setKeycloakClientID(String keycloakClientID) {
        this.keycloakClientID = keycloakClientID;
    }

    /**
     * Determines whether installed.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isInstalled() {
        return device != null && device.isInstalled() && (installed == null || installed.booleanValue());
    }

    /**
     * Gets the installed.
     *
     * @return the installed.
     */
    public final Boolean getInstalled() {
        return installed;
    }

    /**
     * Sets the installed.
     *
     * @param installed the installed.
     */
    public void setInstalled(Boolean installed) {
        if (installed != null && installed.booleanValue() && device != null && !device.isInstalled())
            throw new IllegalStateException("owning device not installed");
        this.installed = installed;
    }

    /**
     * Gets the keycloak client.
     *
     * @return the keycloak client.
     */
    public KeycloakClient getKeycloakClient() {
        return keycloakClientID != null ? device.getKeycloakClient(keycloakClientID) : keycloakClient;
    }

    /**
     * Sets the keycloak client.
     *
     * @param keycloakClient the keycloak client.
     */
    public void setKeycloakClient(KeycloakClient keycloakClient) {
        this.keycloakClient = keycloakClient;
    }

    /**
     * Gets the device name.
     *
     * @return the device name.
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * Sets the device name.
     *
     * @param deviceName the device name.
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * Adds the connection.
     *
     * @param conn the conn.
     */
    public void addConnection(Connection conn) {
        if (conn.getProtocol() != Connection.Protocol.HTTP)
            throw new IllegalArgumentException("Web Application does not support protocol " + conn.getProtocol());
        if (device != null && device != conn.getDevice())
            throw new IllegalStateException(conn + " not contained by " + device.getDeviceName());
        conns.add(conn);
    }

    /**
     * Gets the service url.
     *
     * @return the service url.
     */
    public StringBuilder getServiceURL() {
        return getServiceURL(firstInstalledConnection());
    }

    /**
     * Executes the first installed connection operation.
     *
     * @return the operation result.
     */
    public Connection firstInstalledConnection() {
        for (Connection conn : conns) {
            if (conn.isInstalled())
                return conn;
        }
        throw new IllegalStateException("No installed Network Connection");
    }

    /**
     * Gets the service url.
     *
     * @param conn the conn.
     * @return the service url.
     */
    public StringBuilder getServiceURL(Connection conn) {
        return new StringBuilder(Normal._64).append(conn.isTls() ? Protocol.HTTPS_PREFIX : Protocol.HTTP_PREFIX)
                .append(conn.getHostname()).append(Symbol.C_COLON).append(conn.getPort()).append(servicePath);
    }

    /**
     * Removes the connection.
     *
     * @param conn the conn.
     * @return true if the condition is met; otherwise false.
     */
    public boolean removeConnection(Connection conn) {
        return conns.remove(conn);
    }

    /**
     * Gets the connections.
     *
     * @return the connections.
     */
    public List<Connection> getConnections() {
        return conns;
    }

    /**
     * Gets the service classes.
     *
     * @return the service classes.
     */
    public ServiceClass[] getServiceClasses() {
        return serviceClasses.toArray(new ServiceClass[0]);
    }

    /**
     * Sets the service classes.
     *
     * @param serviceClasses the service classes.
     */
    public void setServiceClasses(ServiceClass... serviceClasses) {
        this.serviceClasses.clear();
        this.serviceClasses.addAll(Arrays.asList(serviceClasses));
    }

    /**
     * Determines whether service class.
     *
     * @param serviceClass the service class.
     * @return true if the condition is met; otherwise false.
     */
    public boolean containsServiceClass(ServiceClass serviceClass) {
        return serviceClasses.contains(serviceClass);
    }

    /**
     * Sets the property.
     *
     * @param name  the name.
     * @param value the value.
     */
    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    /**
     * Gets the property.
     *
     * @param name     the name.
     * @param defValue the def value.
     * @return the property.
     */
    public String getProperty(String name, String defValue) {
        String value = properties.get(name);
        return value != null ? value : defValue;
    }

    /**
     * Gets the properties.
     *
     * @return the properties.
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Sets the properties.
     *
     * @param ss the ss.
     */
    public void setProperties(String[] ss) {
        properties.clear();
        for (String s : ss) {
            int index = s.indexOf('=');
            if (index < 0)
                throw new IllegalArgumentException("Property in incorrect format : " + s);
            setProperty(s.substring(0, index), s.substring(index + 1));
        }
    }

    /**
     * Executes the reconfigure operation.
     *
     * @param src the src.
     */
    public void reconfigure(WebApplication src) {
        description = src.description;
        servicePath = src.servicePath;
        aeTitle = src.aeTitle;
        applicationClusters = src.applicationClusters;
        keycloakClientID = src.keycloakClientID;
        installed = src.installed;
        serviceClasses.clear();
        serviceClasses.addAll(src.serviceClasses);
        properties.clear();
        properties.putAll(src.properties);
        device.reconfigureConnections(conns, src.conns);
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return "WebApplication[name=" + applicationName + ",serviceClasses=" + serviceClasses + ",path=" + servicePath
                + ",aet=" + aeTitle + ",applicationClusters=" + Arrays.toString(applicationClusters)
                + ",keycloakClientID=" + keycloakClientID + ",properties=" + properties + ",installed=" + installed
                + ']';
    }

    /**
     * Defines the ServiceClass values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ServiceClass {
        /**
         * Constant for the wado uri value.
         */
        WADO_URI,
        /**
         * Constant for the wado rs value.
         */
        WADO_RS,
        /**
         * Constant for the stow rs value.
         */
        STOW_RS,
        /**
         * Constant for the qido rs value.
         */
        QIDO_RS,
        /**
         * Constant for the ups rs value.
         */
        UPS_RS,
        /**
         * Constant for the mwl rs value.
         */
        MWL_RS,
        /**
         * Constant for the mpps rs value.
         */
        MPPS_RS,
        /**
         * Constant for the qido count value.
         */
        QIDO_COUNT,
        /**
         * Constant for the dcm arc value.
         */
        DCM_ARC,
        /**
         * Constant for the dcm arc aet value.
         */
        DCM_ARC_AET,
        /**
         * Constant for the dcm arc aet diff value.
         */
        DCM_ARC_AET_DIFF,
        /**
         * Constant for the pam value.
         */
        PAM,
        /**
         * Constant for the reject value.
         */
        REJECT,
        /**
         * Constant for the move value.
         */
        MOVE,
        /**
         * Constant for the move matching value.
         */
        MOVE_MATCHING,
        /**
         * Constant for the ups matching value.
         */
        UPS_MATCHING,
        /**
         * Constant for the elasticsearch value.
         */
        ELASTICSEARCH,
        /**
         * Constant for the prometheus value.
         */
        PROMETHEUS,
        /**
         * Constant for the grafana value.
         */
        GRAFANA,
        /**
         * Constant for the documentation value.
         */
        DOCUMENTATION,
        /**
         * Constant for the xds rs value.
         */
        XDS_RS,
        /**
         * Constant for the agfa blob value.
         */
        AGFA_BLOB,
        /**
         * Constant for the j4 c router value.
         */
        J4C_ROUTER,
        /**
         * Constant for the fhir value.
         */
        FHIR,
        /**
         * Constant for the ai chat value.
         */
        AI_CHAT,
        /**
         * Constant for the workflow manager value.
         */
        WORKFLOW_MANAGER

    }

}
