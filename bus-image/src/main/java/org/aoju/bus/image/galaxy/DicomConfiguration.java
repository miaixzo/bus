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
package org.aoju.bus.image.galaxy;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.image.Device;
import org.aoju.bus.image.metric.*;

import java.io.Closeable;
import java.security.cert.X509Certificate;
import java.util.EnumSet;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public interface DicomConfiguration extends Closeable {

    WebApplicationInfo[] listWebApplicationInfos(WebApplicationInfo keys)
            throws InstrumentException;

    boolean configurationExists() throws InstrumentException;

    boolean purgeConfiguration() throws InstrumentException;

    boolean registerAETitle(String aet) throws InstrumentException;

    boolean registerWebAppName(String webAppName) throws InstrumentException;

    void unregisterAETitle(String aet) throws InstrumentException;

    void unregisterWebAppName(String webAppName) throws InstrumentException;

    ApplicationEntity findApplicationEntity(String aet) throws InstrumentException;

    WebApplication findWebApplication(String name) throws InstrumentException;

    Device findDevice(String name) throws InstrumentException;

    /**
     * Query for Devices with specified attributes.
     *
     * @param keys Device attributes which shall match or null to
     *             get information for all configured Devices
     * @return array of DeviceInfo objects for configured Devices
     * with matching attributes
     * @throws InstrumentException exception
     */
    DeviceInfo[] listDeviceInfos(DeviceInfo keys) throws InstrumentException;

    /**
     * Query for Application Entities with specified attributes.
     *
     * @param keys Application Entity attributes which shall match or null to
     *             get information for all configured Application Entities
     * @return array of ApplicationEntityInfo objects for configured Application Entity
     * with matching attributes
     * @throws InstrumentException exception
     */
    ApplicationEntityInfo[] listAETInfos(ApplicationEntityInfo keys) throws InstrumentException;

    String[] listDeviceNames() throws InstrumentException;

    String[] listRegisteredAETitles() throws InstrumentException;

    String[] listRegisteredWebAppNames() throws InstrumentException;

    ConfigurationChanges persist(Device device, EnumSet<Option> options) throws InstrumentException;

    ConfigurationChanges merge(Device device, EnumSet<Option> options) throws InstrumentException;

    ConfigurationChanges removeDevice(String name, EnumSet<Option> options) throws InstrumentException;

    byte[][] loadDeviceVendorData(String deviceName) throws InstrumentException;

    ConfigurationChanges updateDeviceVendorData(String deviceName, byte[]... vendorData) throws InstrumentException;

    String deviceRef(String name);

    void persistCertificates(String ref, X509Certificate... certs) throws InstrumentException;

    void removeCertificates(String ref) throws InstrumentException;

    X509Certificate[] findCertificates(String dn) throws InstrumentException;

    void close();

    void sync() throws InstrumentException;

    <T> T getDicomConfigurationExtension(Class<T> clazz);

    enum Option {
        REGISTER, PRESERVE_VENDOR_DATA, PRESERVE_CERTIFICATE, CONFIGURATION_CHANGES, CONFIGURATION_CHANGES_VERBOSE
    }

}
