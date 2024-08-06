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
package org.miaixz.bus.image.metric.net;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.image.metric.Connection;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ApplicationEntityInfo implements Serializable {

    private final List<Connection> conns = new ArrayList<>(1);
    private String deviceName;
    private String description;
    private String aet;
    private String[] applicationClusters = {};
    private Boolean associationInitiator;
    private Boolean associationAcceptor;
    private Boolean installed;
    private String[] otherAETitle;
    private String hl7ApplicationName;

    public Boolean getInstalled() {
        return installed;
    }

    public void setInstalled(Boolean installed) {
        this.installed = installed;
    }

    public String[] getOtherAETitle() {
        return otherAETitle;
    }

    public void setOtherAETitle(String[] otherAETitle) {
        this.otherAETitle = otherAETitle;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAETitle() {
        return aet;
    }

    public void setAETitle(String aet) {
        this.aet = aet;
    }

    public String[] getApplicationClusters() {
        return applicationClusters;
    }

    public void setApplicationClusters(String[] applicationClusters) {
        this.applicationClusters = applicationClusters;
    }

    public Boolean getAssociationInitiator() {
        return associationInitiator;
    }

    public void setAssociationInitiator(Boolean associationInitiator) {
        this.associationInitiator = associationInitiator;
    }

    public Boolean getAssociationAcceptor() {
        return associationAcceptor;
    }

    public void setAssociationAcceptor(Boolean associationAcceptor) {
        this.associationAcceptor = associationAcceptor;
    }

    public List<Connection> getConnections() {
        return conns;
    }

    public String getHl7ApplicationName() {
        return hl7ApplicationName;
    }

    public void setHl7ApplicationName(String hl7ApplicationName) {
        this.hl7ApplicationName = hl7ApplicationName;
    }

    @Override
    public String toString() {
        return "ApplicationEntityInfo[dicomAETitle=" + aet + "]";
    }

}
