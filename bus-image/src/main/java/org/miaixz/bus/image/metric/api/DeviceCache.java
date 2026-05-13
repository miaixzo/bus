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

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.NotFoundException;
import org.miaixz.bus.image.Device;

/**
 * Represents the DeviceCache type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DeviceCache extends ConfigurationCache<DicomConfiguration, Device> implements IDeviceCache {

    /**
     * Creates a new instance.
     *
     * @param conf the conf.
     */
    public DeviceCache(DicomConfiguration conf) {
        super(conf);
    }

    /**
     * Executes the find operation.
     *
     * @param conf the conf.
     * @param key  the key.
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    @Override
    protected Device find(DicomConfiguration conf, String key) throws InternalException {
        return conf.findDevice(key);
    }

    /**
     * Finds the device.
     *
     * @param deviceName the device name.
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    @Override
    public Device findDevice(String deviceName) throws InternalException {
        Device device = get(deviceName);
        if (device == null)
            throw new NotFoundException("Unknown Device: " + deviceName);
        if (!device.isInstalled())
            throw new NotFoundException("Device: " + deviceName + " not installed");
        return device;
    }

}
