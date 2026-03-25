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
package org.miaixz.bus.image.metric.net;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.image.metric.Connection;

/**
 * @author Kimi Liu
 * @since Java 21+
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
