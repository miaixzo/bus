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
package org.miaixz.bus.health.mac.hardware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.BluetoothDevice;
import org.miaixz.bus.health.builtin.hardware.common.AbstractBluetoothDevice;

/**
 * macOS Bluetooth device enumeration via {@code system_profiler SPBluetoothDataType}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
public final class MacBluetoothDevice extends AbstractBluetoothDevice {

    /**
     * Creates a new MacBluetoothDevice instance.
     *
     * @param name             the device name
     * @param address          the MAC address
     * @param majorDeviceClass the major device class
     * @param connected        whether the device is connected
     * @param paired           whether the device is paired
     * @param batteryLevel     the battery level
     * @param adapterName      the adapter name
     */
    private MacBluetoothDevice(
            String name,
            String address,
            String majorDeviceClass,
            boolean connected,
            boolean paired,
            int batteryLevel,
            String adapterName) {
        super(name, address, majorDeviceClass, connected, paired, batteryLevel, adapterName);
    }

    /**
     * Gets Bluetooth devices known to the system.
     *
     * @return a list of {@link BluetoothDevice} objects
     */
    public static List<BluetoothDevice> getBluetoothDevices() {
        return parseSystemProfiler(Executor.runNative("system_profiler SPBluetoothDataType"));
    }

    /**
     * Parses the output of {@code system_profiler SPBluetoothDataType}.
     *
     * @param lines the output lines
     * @return a list of Bluetooth devices
     */
    static List<BluetoothDevice> parseSystemProfiler(List<String> lines) {
        List<BluetoothDevice> devices = new ArrayList<>();
        boolean inConnected = false;
        boolean inNotConnected = false;
        boolean inDevice = false;
        String name = Normal.EMPTY;
        String address = Normal.EMPTY;
        String majorClass = Normal.EMPTY;
        int batteryLevel = -1;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("Connected:") || trimmed.equals("Devices (Paired, Configured, & Connected):")) {
                if (inDevice) {
                    devices.add(new MacBluetoothDevice(name, address, majorClass, inConnected, true, batteryLevel,
                            Normal.EMPTY));
                }
                inConnected = true;
                inNotConnected = false;
                inDevice = false;
                continue;
            }
            if (trimmed.startsWith("Not Connected:") || trimmed.equals("Devices (Paired, Not Connected):")) {
                if (inDevice) {
                    devices.add(new MacBluetoothDevice(name, address, majorClass, inConnected, true, batteryLevel,
                            Normal.EMPTY));
                }
                inConnected = false;
                inNotConnected = true;
                inDevice = false;
                continue;
            }
            if (!inConnected && !inNotConnected) {
                continue;
            }

            String[] parts = trimmed.split(":", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();

                if (value.isEmpty() && !key.startsWith("Address") && !key.startsWith("Minor")) {
                    if (inDevice) {
                        devices.add(new MacBluetoothDevice(name, address, majorClass, inConnected, true, batteryLevel,
                                Normal.EMPTY));
                    }
                    inDevice = true;
                    name = key;
                    address = Normal.EMPTY;
                    majorClass = Normal.EMPTY;
                    batteryLevel = -1;
                } else if (inDevice) {
                    switch (key.toLowerCase(Locale.ROOT)) {
                        case "address":
                            address = value.toUpperCase(Locale.ROOT);
                            break;

                        case "major type":
                        case "minor type":
                            majorClass = value;
                            break;

                        case "battery level":
                            batteryLevel = Parsing.parseIntOrDefault(value.replace("%", "").trim(), -1);
                            break;

                        default:
                            break;
                    }
                }
            }
        }
        if (inDevice) {
            devices.add(new MacBluetoothDevice(name, address, majorClass, inConnected, true, batteryLevel,
                    Normal.EMPTY));
        }
        return Collections.unmodifiableList(devices);
    }

}
