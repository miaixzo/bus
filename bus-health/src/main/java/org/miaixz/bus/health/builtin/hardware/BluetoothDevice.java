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
package org.miaixz.bus.health.builtin.hardware;

import org.miaixz.bus.core.lang.annotation.Immutable;

/**
 * Represents a Bluetooth device, paired or connected, known to the system.
 * <p>
 * Bluetooth devices are enumerated per adapter. Each device reports its name, MAC address, major device class,
 * connection state, pairing state, and battery level when available.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
public interface BluetoothDevice {

    /**
     * Returns the user-visible name of the Bluetooth device.
     *
     * @return the device name, or an empty string if unknown
     */
    String getName();

    /**
     * Returns the MAC address of the Bluetooth device in colon-separated format.
     *
     * @return the device MAC address
     */
    String getAddress();

    /**
     * Returns the major Bluetooth device class.
     * <p>
     * This value is derived from the Class of Device field when available.
     *
     * @return the major device class, or an empty string if unknown
     */
    String getMajorDeviceClass();

    /**
     * Returns whether the device is currently connected to this system.
     *
     * @return {@code true} if connected, {@code false} otherwise
     */
    boolean isConnected();

    /**
     * Returns whether the device is paired with this system.
     *
     * @return {@code true} if paired, {@code false} otherwise
     */
    boolean isPaired();

    /**
     * Returns the battery level of the device as a percentage.
     *
     * @return the battery percentage, or {@code -1} if not available
     */
    int getBatteryLevel();

    /**
     * Returns the name of the adapter through which this device is known.
     *
     * @return the adapter name
     */
    String getAdapterName();

}
