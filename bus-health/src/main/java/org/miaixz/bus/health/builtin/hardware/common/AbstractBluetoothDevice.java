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
package org.miaixz.bus.health.builtin.hardware.common;

import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.builtin.hardware.BluetoothDevice;

/**
 * Abstract base class for Bluetooth device implementations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
public abstract class AbstractBluetoothDevice implements BluetoothDevice {

    /**
     * The name value.
     */
    private final String name;

    /**
     * The address value.
     */
    private final String address;

    /**
     * The majorDeviceClass value.
     */
    private final String majorDeviceClass;

    /**
     * The connected value.
     */
    private final boolean connected;

    /**
     * The paired value.
     */
    private final boolean paired;

    /**
     * The batteryLevel value.
     */
    private final int batteryLevel;

    /**
     * The adapterName value.
     */
    private final String adapterName;

    /**
     * Creates an AbstractBluetoothDevice with the given parameters.
     *
     * @param name             the device name
     * @param address          the MAC address
     * @param majorDeviceClass the major device class
     * @param connected        whether the device is connected
     * @param paired           whether the device is paired
     * @param batteryLevel     the battery level, or {@code -1} if unavailable
     * @param adapterName      the adapter name
     */
    protected AbstractBluetoothDevice(
            String name,
            String address,
            String majorDeviceClass,
            boolean connected,
            boolean paired,
            int batteryLevel,
            String adapterName) {
        this.name = name;
        this.address = address;
        this.majorDeviceClass = majorDeviceClass;
        this.connected = connected;
        this.paired = paired;
        this.batteryLevel = batteryLevel < 0 ? -1 : Math.min(100, batteryLevel);
        this.adapterName = adapterName;
    }

    /**
     * Returns the name.
     *
     * @return the get name result
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the address.
     *
     * @return the get address result
     */
    @Override
    public String getAddress() {
        return address;
    }

    /**
     * Returns the major device class.
     *
     * @return the get major device class result
     */
    @Override
    public String getMajorDeviceClass() {
        return majorDeviceClass;
    }

    /**
     * Returns the connected state.
     *
     * @return the is connected result
     */
    @Override
    public boolean isConnected() {
        return connected;
    }

    /**
     * Returns the paired state.
     *
     * @return the is paired result
     */
    @Override
    public boolean isPaired() {
        return paired;
    }

    /**
     * Returns the battery level.
     *
     * @return the get battery level result
     */
    @Override
    public int getBatteryLevel() {
        return batteryLevel;
    }

    /**
     * Returns the adapter name.
     *
     * @return the get adapter name result
     */
    @Override
    public String getAdapterName() {
        return adapterName;
    }

    /**
     * Returns the string representation.
     *
     * @return the to string result
     */
    @Override
    public String toString() {
        return "BluetoothDevice [name=" + name + ", address=" + address + ", class=" + majorDeviceClass
                + ", connected=" + connected + ", paired=" + paired + ", battery=" + batteryLevel + ", adapter="
                + adapterName + "]";
    }

    /**
     * Parses the major device class from the Bluetooth Class of Device integer.
     *
     * @param cod the Class of Device integer
     * @return a human-readable major device class string
     */
    public static String parseMajorDeviceClass(int cod) {
        int major = (cod >> 8) & 0x1F;
        switch (major) {
            case 0:
                return "Miscellaneous";
            case 1:
                return "Computer";
            case 2:
                return "Phone";
            case 3:
                return "Networking";
            case 4:
                return "Audio/Video";
            case 5:
                return "Peripheral";
            case 6:
                return "Imaging";
            case 7:
                return "Wearable";
            case 8:
                return "Toy";
            case 9:
                return "Health";
            case 31:
                return "Uncategorized";
            default:
                return "";
        }
    }

}
