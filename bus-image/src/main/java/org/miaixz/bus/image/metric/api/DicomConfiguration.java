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
package org.miaixz.bus.image.metric.api;

import java.io.Closeable;
import java.security.cert.X509Certificate;
import java.util.EnumSet;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.metric.WebApplication;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.net.ApplicationEntityInfo;

/**
 * Defines the DicomConfiguration contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface DicomConfiguration extends Closeable {

    /**
     * Executes the list web application infos operation.
     *
     * @param keys the keys.
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    WebApplication[] listWebApplicationInfos(WebApplication keys) throws InternalException;

    /**
     * Executes the configuration exists operation.
     *
     * @return true if the condition is met; otherwise false.
     * @throws InternalException if the operation cannot be completed.
     */
    boolean configurationExists() throws InternalException;

    /**
     * Executes the purge configuration operation.
     *
     * @return true if the condition is met; otherwise false.
     * @throws InternalException if the operation cannot be completed.
     */
    boolean purgeConfiguration() throws InternalException;

    /**
     * Executes the register ae title operation.
     *
     * @param aet the aet.
     * @return true if the condition is met; otherwise false.
     * @throws InternalException if the operation cannot be completed.
     */
    boolean registerAETitle(String aet) throws InternalException;

    /**
     * Executes the register web app name operation.
     *
     * @param webAppName the web app name.
     * @return true if the condition is met; otherwise false.
     * @throws InternalException if the operation cannot be completed.
     */
    boolean registerWebAppName(String webAppName) throws InternalException;

    /**
     * Executes the unregister ae title operation.
     *
     * @param aet the aet.
     * @throws InternalException if the operation cannot be completed.
     */
    void unregisterAETitle(String aet) throws InternalException;

    /**
     * Executes the unregister web app name operation.
     *
     * @param webAppName the web app name.
     * @throws InternalException if the operation cannot be completed.
     */
    void unregisterWebAppName(String webAppName) throws InternalException;

    /**
     * Finds the application entity.
     *
     * @param aet the aet.
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    ApplicationEntity findApplicationEntity(String aet) throws InternalException;

    /**
     * Finds the web application.
     *
     * @param name the name.
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    WebApplication findWebApplication(String name) throws InternalException;

    /**
     * Finds the device.
     *
     * @param name the name.
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    Device findDevice(String name) throws InternalException;

    /**
     * Provides DICOM processing details.
     *
     * @param keys the keys.
     * @return the result.
     * @throws InternalException if the operation cannot be completed.
     */
    Device[] listDeviceInfos(Device keys) throws InternalException;

    /**
     * Provides DICOM processing details.
     *
     * @param keys the keys.
     * @return the result.
     * @throws InternalException if the operation cannot be completed.
     */
    ApplicationEntityInfo[] listAETInfos(ApplicationEntityInfo keys) throws InternalException;

    /**
     * Executes the list device names operation.
     *
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    String[] listDeviceNames() throws InternalException;

    /**
     * Executes the list registered ae titles operation.
     *
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    String[] listRegisteredAETitles() throws InternalException;

    /**
     * Executes the list registered web app names operation.
     *
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    String[] listRegisteredWebAppNames() throws InternalException;

    /**
     * Executes the persist operation.
     *
     * @param device  the device.
     * @param options the options.
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    ConfigurationChanges persist(Device device, EnumSet<Option> options) throws InternalException;

    /**
     * Executes the merge operation.
     *
     * @param device  the device.
     * @param options the options.
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    ConfigurationChanges merge(Device device, EnumSet<Option> options) throws InternalException;

    /**
     * Removes the device.
     *
     * @param name    the name.
     * @param options the options.
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    ConfigurationChanges removeDevice(String name, EnumSet<Option> options) throws InternalException;

    /**
     * Loads the device vendor data.
     *
     * @param deviceName the device name.
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    byte[][] loadDeviceVendorData(String deviceName) throws InternalException;

    /**
     * Updates the device vendor data.
     *
     * @param deviceName the device name.
     * @param vendorData the vendor data.
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    ConfigurationChanges updateDeviceVendorData(String deviceName, byte[]... vendorData) throws InternalException;

    /**
     * Executes the device ref operation.
     *
     * @param name the name.
     * @return the operation result.
     */
    String deviceRef(String name);

    /**
     * Executes the persist certificates operation.
     *
     * @param ref   the ref.
     * @param certs the certs.
     * @throws InternalException if the operation cannot be completed.
     */
    void persistCertificates(String ref, X509Certificate... certs) throws InternalException;

    /**
     * Removes the certificates.
     *
     * @param ref the ref.
     * @throws InternalException if the operation cannot be completed.
     */
    void removeCertificates(String ref) throws InternalException;

    /**
     * Finds the certificates.
     *
     * @param dn the dn.
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    X509Certificate[] findCertificates(String dn) throws InternalException;

    /**
     * Executes the close operation.
     */
    void close();

    /**
     * Executes the sync operation.
     *
     * @throws InternalException if the operation cannot be completed.
     */
    void sync() throws InternalException;

    /**
     * Gets the dicom configuration extension.
     *
     * @param clazz the clazz.
     * @return the dicom configuration extension.
     */
    <T> T getDicomConfigurationExtension(Class<T> clazz);

    /**
     * Defines the Option values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    enum Option {
        /**
         * Constant for the register value.
         */
        REGISTER,
        /**
         * Constant for the preserve vendor data value.
         */
        PRESERVE_VENDOR_DATA,
        /**
         * Constant for the preserve certificate value.
         */
        PRESERVE_CERTIFICATE,
        /**
         * Constant for the configuration changes value.
         */
        CONFIGURATION_CHANGES,
        /**
         * Constant for the configuration changes verbose value.
         */
        CONFIGURATION_CHANGES_VERBOSE

    }

}
