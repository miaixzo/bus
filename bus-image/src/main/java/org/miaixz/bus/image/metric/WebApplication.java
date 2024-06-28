/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.metric;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.image.Device;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class WebApplication {

    private final List<Connection> conns = new ArrayList<>(1);
    private final EnumSet<ServiceClass> serviceClasses = EnumSet.noneOf(ServiceClass.class);
    private Device device;
    private String applicationName;
    private String description;
    private String servicePath;
    private String aeTitle;
    private String[] applicationClusters = {};
    private String keycloakClientID;
    private Boolean installed;

    public WebApplication() {

    }

    public WebApplication(String applicationName) {
        this.applicationName = applicationName;
    }

    public Device getDevice() {
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
        return applicationName;
    }

    public void setApplicationName(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("name cannot be empty");
        Device device = this.device;
        if (null != device)
            device.removeWebApplication(this.applicationName);
        this.applicationName = name;
        if (null != device)
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

    public KeycloakClient getKeycloakClient() {
        return null != keycloakClientID ? device.getKeycloakClient(keycloakClientID) : null;
    }

    public void addConnection(Connection conn) {
        if (conn.getProtocol() != Protocol.HTTP)
            throw new IllegalArgumentException(
                    "Web Application does not support protocol " + conn.getProtocol());
        if (null != device && device != conn.getDevice())
            throw new IllegalStateException(conn + " not contained by " +
                    device.getDeviceName());
        conns.add(conn);
    }

    public StringBuilder getServiceURL() {
        return getServiceURL(firstInstalledConnection());
    }

    private Connection firstInstalledConnection() {
        for (Connection conn : conns) {
            if (conn.isInstalled())
                return conn;
        }
        throw new IllegalStateException("No installed Network Connection");
    }

    public StringBuilder getServiceURL(Connection conn) {
        return new StringBuilder(Normal._64)
                .append(conn.isTls() ? Protocol.HTTPS_PREFIX : Protocol.HTTP_PREFIX)
                .append(conn.getHostname())
                .append(Symbol.C_COLON)
                .append(conn.getPort())
                .append(servicePath);
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

    public void reconfigure(WebApplication src) {
        description = src.description;
        servicePath = src.servicePath;
        aeTitle = src.aeTitle;
        applicationClusters = src.applicationClusters;
        keycloakClientID = src.keycloakClientID;
        installed = src.installed;
        serviceClasses.clear();
        serviceClasses.addAll(src.serviceClasses);
        device.reconfigureConnections(conns, src.conns);
    }

    @Override
    public String toString() {
        return "WebApplication[name=" + applicationName
                + ",classes=" + serviceClasses
                + ",path=" + servicePath
                + ",aet=" + aeTitle
                + ']';
    }

    public enum ServiceClass {
        WADO_URI,
        WADO_RS,
        STOW_RS,
        QIDO_RS,
        UPS_RS,
        M_ARC,
        M_ARC_AET
    }

}
