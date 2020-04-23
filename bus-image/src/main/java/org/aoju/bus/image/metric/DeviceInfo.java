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

import java.io.Serializable;

/**
 * @author Kimi Liu
 * @version 5.0.8
 * @since JDK 1.8+
 */
public class DeviceInfo implements Serializable {

    private String deviceName;
    private String description;
    private String manufacturer;
    private String manufacturerModelName;
    private String stationName;
    private String[] softwareVersions = {};
    private String[] primaryDeviceTypes = {};
    private String[] institutionNames = {};
    private String[] institutionalDepartmentNames = {};
    private Boolean installed;
    private Boolean arcDevExt;

    public final String getDeviceName() {
        return deviceName;
    }

    public final void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public final String getDescription() {
        return description;
    }

    public final void setDescription(String description) {
        this.description = description;
    }

    public final String getManufacturer() {
        return manufacturer;
    }

    public final void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public final String getManufacturerModelName() {
        return manufacturerModelName;
    }

    public final void setManufacturerModelName(String manufacturerModelName) {
        this.manufacturerModelName = manufacturerModelName;
    }

    public final String getStationName() {
        return stationName;
    }

    public final void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public final String[] getSoftwareVersions() {
        return softwareVersions;
    }

    public final void setSoftwareVersions(String[] softwareVersions) {
        this.softwareVersions = softwareVersions;
    }

    public final String[] getPrimaryDeviceTypes() {
        return primaryDeviceTypes;
    }

    public final void setPrimaryDeviceTypes(String[] primaryDeviceTypes) {
        this.primaryDeviceTypes = primaryDeviceTypes;
    }

    public final String[] getInstitutionNames() {
        return institutionNames;
    }

    public final void setInstitutionNames(String[] institutionNames) {
        this.institutionNames = institutionNames;
    }

    public final String[] getInstitutionalDepartmentNames() {
        return institutionalDepartmentNames;
    }

    public final void setInstitutionalDepartmentNames(
            String[] institutionalDepartmentNames) {
        this.institutionalDepartmentNames = institutionalDepartmentNames;
    }

    public final Boolean getInstalled() {
        return installed;
    }

    public final void setInstalled(Boolean installed) {
        this.installed = installed;
    }

    public Boolean getArcDevExt() {
        return arcDevExt;
    }

    public void setArcDevExt(Boolean arcDevExt) {
        this.arcDevExt = arcDevExt;
    }

    @Override
    public String toString() {
        return "DeviceInfo[name=" + deviceName
                + ", installed=" + installed
                + "]";
    }

}
