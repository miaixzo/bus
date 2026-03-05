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
 * @since Java 17+
 */
public class WebApplication {

    private final EnumSet<ServiceClass> serviceClasses = EnumSet.noneOf(ServiceClass.class);
    private final Map<String, String> properties = new HashMap<>();
    private final List<Connection> conns = new ArrayList<>(1);
    private Device device;
    private String applicationName;
    private String description;
    private String servicePath;
    private String aeTitle;
    private String[] applicationClusters = {};
    private String keycloakClientID;
    private Boolean installed;
    private String deviceName;
    private KeycloakClient keycloakClient;

    public WebApplication() {
    }

    public WebApplication(String applicationName) {
        this.applicationName = applicationName;
    }

    public Device getDevice() {
        return device;
    }

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

    public String getApplicationName() {
        return applicationName;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getServicePath() {
        return servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath.startsWith(Symbol.SLASH) ? servicePath : Symbol.C_SLASH + servicePath;
    }

    public String getAETitle() {
        return aeTitle;
    }

    public void setAETitle(String aeTitle) {
        this.aeTitle = aeTitle;
    }

    public String[] getApplicationClusters() {
        return applicationClusters;
    }

    public void setApplicationClusters(String... applicationClusters) {
        this.applicationClusters = applicationClusters;
    }

    public String getKeycloakClientID() {
        return keycloakClientID;
    }

    public void setKeycloakClientID(String keycloakClientID) {
        this.keycloakClientID = keycloakClientID;
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

    public KeycloakClient getKeycloakClient() {
        return keycloakClientID != null ? device.getKeycloakClient(keycloakClientID) : keycloakClient;
    }

    public void setKeycloakClient(KeycloakClient keycloakClient) {
        this.keycloakClient = keycloakClient;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void addConnection(Connection conn) {
        if (conn.getProtocol() != Connection.Protocol.HTTP)
            throw new IllegalArgumentException("Web Application does not support protocol " + conn.getProtocol());
        if (device != null && device != conn.getDevice())
            throw new IllegalStateException(conn + " not contained by " + device.getDeviceName());
        conns.add(conn);
    }

    public StringBuilder getServiceURL() {
        return getServiceURL(firstInstalledConnection());
    }

    public Connection firstInstalledConnection() {
        for (Connection conn : conns) {
            if (conn.isInstalled())
                return conn;
        }
        throw new IllegalStateException("No installed Network Connection");
    }

    public StringBuilder getServiceURL(Connection conn) {
        return new StringBuilder(Normal._64).append(conn.isTls() ? Protocol.HTTPS_PREFIX : Protocol.HTTP_PREFIX)
                .append(conn.getHostname()).append(Symbol.C_COLON).append(conn.getPort()).append(servicePath);
    }

    public boolean removeConnection(Connection conn) {
        return conns.remove(conn);
    }

    public List<Connection> getConnections() {
        return conns;
    }

    public ServiceClass[] getServiceClasses() {
        return serviceClasses.toArray(new ServiceClass[0]);
    }

    public void setServiceClasses(ServiceClass... serviceClasses) {
        this.serviceClasses.clear();
        this.serviceClasses.addAll(Arrays.asList(serviceClasses));
    }

    public boolean containsServiceClass(ServiceClass serviceClass) {
        return serviceClasses.contains(serviceClass);
    }

    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    public String getProperty(String name, String defValue) {
        String value = properties.get(name);
        return value != null ? value : defValue;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(String[] ss) {
        properties.clear();
        for (String s : ss) {
            int index = s.indexOf('=');
            if (index < 0)
                throw new IllegalArgumentException("Property in incorrect format : " + s);
            setProperty(s.substring(0, index), s.substring(index + 1));
        }
    }

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

    @Override
    public String toString() {
        return "WebApplication[name=" + applicationName + ",serviceClasses=" + serviceClasses + ",path=" + servicePath
                + ",aet=" + aeTitle + ",applicationClusters=" + Arrays.toString(applicationClusters)
                + ",keycloakClientID=" + keycloakClientID + ",properties=" + properties + ",installed=" + installed
                + ']';
    }

    public enum ServiceClass {
        WADO_URI, WADO_RS, STOW_RS, QIDO_RS, UPS_RS, MWL_RS, MPPS_RS, QIDO_COUNT, DCM_ARC, DCM_ARC_AET,
        DCM_ARC_AET_DIFF, PAM, REJECT, MOVE, MOVE_MATCHING, UPS_MATCHING, ELASTICSEARCH, PROMETHEUS, GRAFANA,
        DOCUMENTATION, XDS_RS, AGFA_BLOB, J4C_ROUTER, FHIR, AI_CHAT, WORKFLOW_MANAGER
    }

}
