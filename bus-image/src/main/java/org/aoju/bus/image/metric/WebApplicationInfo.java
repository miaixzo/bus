/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.image.metric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class WebApplicationInfo {

    private final List<Connection> conns = new ArrayList<>(1);
    private String deviceName;
    private String applicationName;
    private String description;
    private String servicePath;
    private String aeTitle;
    private String[] applicationClusters = {};
    private String keycloakClientID;
    private Boolean installed;
    private EnumSet<WebApplication.ServiceClass> serviceClasses = EnumSet.noneOf(WebApplication.ServiceClass.class);

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String name) {
        this.applicationName = name;
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
        this.servicePath = servicePath;
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

    public void setApplicationClusters(String[] applicationClusters) {
        this.applicationClusters = applicationClusters;
    }

    public String getKeycloakClientID() {
        return keycloakClientID;
    }

    public void setKeycloakClientID(String keycloakClientID) {
        this.keycloakClientID = keycloakClientID;
    }

    public Boolean getInstalled() {
        return installed;
    }

    public void setInstalled(Boolean installed) {
        this.installed = installed;
    }

    public List<Connection> getConnections() {
        return conns;
    }

    public WebApplication.ServiceClass[] getServiceClasses() {
        return serviceClasses.toArray(new WebApplication.ServiceClass[0]);
    }

    public void setServiceClasses(WebApplication.ServiceClass... serviceClasses) {
        this.serviceClasses.clear();
        this.serviceClasses.addAll(Arrays.asList(serviceClasses));
    }

    @Override
    public String toString() {
        return "WebApplicationInfo[name=" + applicationName
                + ",classes=" + serviceClasses
                + ",path=" + servicePath
                + ",aet=" + aeTitle
                + ']';
    }

}
