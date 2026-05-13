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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.image.metric.Connection;

/**
 * Represents the HL7ApplicationInfo type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HL7ApplicationInfo implements Serializable {

    /**
     * The conns value.
     */
    private final List<Connection> conns = new ArrayList<>(1);

    /**
     * The device name value.
     */
    private String deviceName;

    /**
     * The hl7 application name value.
     */
    private String hl7ApplicationName;

    /**
     * The hl7 other application name value.
     */
    private String[] hl7OtherApplicationName;

    /**
     * The description value.
     */
    private String description;

    /**
     * The application clusters value.
     */
    private String[] applicationClusters = {};

    /**
     * The installed value.
     */
    private Boolean installed;

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
     * Gets the hl7 other application name.
     *
     * @return the hl7 other application name.
     */
    public String[] getHl7OtherApplicationName() {
        return hl7OtherApplicationName;
    }

    /**
     * Sets the hl7 other application name.
     *
     * @param hl7OtherApplicationName the hl7 other application name.
     */
    public void setHl7OtherApplicationName(String[] hl7OtherApplicationName) {
        this.hl7OtherApplicationName = hl7OtherApplicationName;
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
     * Gets the connections.
     *
     * @return the connections.
     */
    public List<Connection> getConnections() {
        return conns;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return "HL7ApplicationInfo[hl7ApplicationName=" + hl7ApplicationName + "]";
    }

}
