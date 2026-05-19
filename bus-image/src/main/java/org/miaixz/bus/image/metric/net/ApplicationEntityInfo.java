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
 * Represents the ApplicationEntityInfo type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ApplicationEntityInfo implements Serializable {

    /**
     * Constructs a new ApplicationEntityInfo instance.
     */
    public ApplicationEntityInfo() {
        // No initialization required.
    }

    /**
     * The conns value.
     */
    private final List<Connection> conns = new ArrayList<>(1);

    /**
     * The device name value.
     */
    private String deviceName;

    /**
     * The description value.
     */
    private String description;

    /**
     * The aet value.
     */
    private String aet;

    /**
     * The application clusters value.
     */
    private String[] applicationClusters = {};

    /**
     * The association initiator value.
     */
    private Boolean associationInitiator;

    /**
     * The association acceptor value.
     */
    private Boolean associationAcceptor;

    /**
     * The installed value.
     */
    private Boolean installed;

    /**
     * The other ae title value.
     */
    private String[] otherAETitle;

    /**
     * The hl7 application name value.
     */
    private String hl7ApplicationName;

    /**
     * Gets the installed.
     *
     * @return the installed.
     */
    public Boolean getInstalled() {
        return installed;
    }

    /**
     * Sets the installed.
     *
     * @param installed the installed.
     */
    public void setInstalled(Boolean installed) {
        this.installed = installed;
    }

    /**
     * Gets the other ae title.
     *
     * @return the other ae title.
     */
    public String[] getOtherAETitle() {
        return otherAETitle;
    }

    /**
     * Sets the other ae title.
     *
     * @param otherAETitle the other ae title.
     */
    public void setOtherAETitle(String[] otherAETitle) {
        this.otherAETitle = otherAETitle;
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
     * Gets the ae title.
     *
     * @return the ae title.
     */
    public String getAETitle() {
        return aet;
    }

    /**
     * Sets the ae title.
     *
     * @param aet the aet.
     */
    public void setAETitle(String aet) {
        this.aet = aet;
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
    public void setApplicationClusters(String[] applicationClusters) {
        this.applicationClusters = applicationClusters;
    }

    /**
     * Gets the association initiator.
     *
     * @return the association initiator.
     */
    public Boolean getAssociationInitiator() {
        return associationInitiator;
    }

    /**
     * Sets the association initiator.
     *
     * @param associationInitiator the association initiator.
     */
    public void setAssociationInitiator(Boolean associationInitiator) {
        this.associationInitiator = associationInitiator;
    }

    /**
     * Gets the association acceptor.
     *
     * @return the association acceptor.
     */
    public Boolean getAssociationAcceptor() {
        return associationAcceptor;
    }

    /**
     * Sets the association acceptor.
     *
     * @param associationAcceptor the association acceptor.
     */
    public void setAssociationAcceptor(Boolean associationAcceptor) {
        this.associationAcceptor = associationAcceptor;
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
     * Gets the hl7 application name.
     *
     * @return the hl7 application name.
     */
    public String getHl7ApplicationName() {
        return hl7ApplicationName;
    }

    /**
     * Sets the hl7 application name.
     *
     * @param hl7ApplicationName the hl7 application name.
     */
    public void setHl7ApplicationName(String hl7ApplicationName) {
        this.hl7ApplicationName = hl7ApplicationName;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return "ApplicationEntityInfo[dicomAETitle=" + aet + "]";
    }

}
