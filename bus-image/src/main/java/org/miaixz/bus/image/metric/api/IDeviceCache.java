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
import org.miaixz.bus.image.Device;

/**
 * Defines the IDeviceCache contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface IDeviceCache {

    /**
     * Gets the stale timeout.
     *
     * @return the stale timeout.
     */
    int getStaleTimeout();

    /**
     * Sets the stale timeout.
     *
     * @param staleTimeout the stale timeout.
     */
    void setStaleTimeout(int staleTimeout);

    /**
     * Executes the clear operation.
     */
    void clear();

    /**
     * Executes the get operation.
     *
     * @param deviceName the device name.
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    Device get(String deviceName) throws InternalException;

    /**
     * Finds the device.
     *
     * @param deviceName the device name.
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    Device findDevice(String deviceName) throws InternalException;

}
