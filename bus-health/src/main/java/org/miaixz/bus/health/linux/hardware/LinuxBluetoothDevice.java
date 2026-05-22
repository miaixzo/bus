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
package org.miaixz.bus.health.linux.hardware;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.BluetoothDevice;
import org.miaixz.bus.health.builtin.hardware.common.AbstractBluetoothDevice;
import org.miaixz.bus.health.linux.SysPath;

/**
 * Linux Bluetooth device enumeration via BlueZ filesystem paths.
 * <p>
 * Adapters are discovered from {@code /sys/class/bluetooth/hciX}. Paired devices are read from
 * {@code /var/lib/bluetooth/<adapter-mac>/<device-mac>/info}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
public final class LinuxBluetoothDevice extends AbstractBluetoothDevice {

    /**
     * The SYS_BLUETOOTH constant.
     */
    private static final String SYS_BLUETOOTH = SysPath.SYS + "class/bluetooth/";

    /**
     * The VAR_LIB_BLUETOOTH constant.
     */
    private static final String VAR_LIB_BLUETOOTH = "/var/lib/bluetooth/";

    /**
     * The MAC_PATTERN constant.
     */
    private static final Pattern MAC_PATTERN = Pattern.compile("(?i)([0-9a-f]{2}:){5}[0-9a-f]{2}");

    /**
     * Creates a new LinuxBluetoothDevice instance.
     *
     * @param name             the device name
     * @param address          the MAC address
     * @param majorDeviceClass the major device class
     * @param connected        whether the device is connected
     * @param paired           whether the device is paired
     * @param batteryLevel     the battery level
     * @param adapterName      the adapter name
     */
    private LinuxBluetoothDevice(String name, String address, String majorDeviceClass, boolean connected,
            boolean paired, int batteryLevel, String adapterName) {
        super(name, address, majorDeviceClass, connected, paired, batteryLevel, adapterName);
    }

    /**
     * Gets Bluetooth devices known to the system.
     *
     * @return a list of {@link BluetoothDevice} objects
     */
    public static List<BluetoothDevice> getBluetoothDevices() {
        return queryBluetoothDevices(SYS_BLUETOOTH, VAR_LIB_BLUETOOTH);
    }

    /**
     * Queries Bluetooth devices from the specified paths.
     *
     * @param sysBluetoothPath path to sysfs bluetooth class
     * @param varLibPath       path to BlueZ state directory
     * @return a list of Bluetooth devices
     */
    static List<BluetoothDevice> queryBluetoothDevices(String sysBluetoothPath, String varLibPath) {
        File sysDir = new File(sysBluetoothPath);
        File[] adapterDirs = sysDir.listFiles();
        if (adapterDirs == null) {
            return Collections.emptyList();
        }

        List<BluetoothDevice> devices = new ArrayList<>();
        for (File adapterDir : adapterDirs) {
            String adapterName = adapterDir.getName();
            String adapterAddress = Builder.getStringFromFile(adapterDir.getAbsolutePath() + "/address").trim();
            if (adapterAddress.isEmpty()) {
                continue;
            }

            File adapterStateDir = new File(varLibPath + adapterAddress.toUpperCase(Locale.ROOT));
            if (!adapterStateDir.isDirectory()) {
                adapterStateDir = new File(varLibPath + adapterAddress.toLowerCase(Locale.ROOT));
            }
            if (!adapterStateDir.isDirectory()) {
                continue;
            }

            File[] deviceDirs = adapterStateDir.listFiles();
            if (deviceDirs == null) {
                continue;
            }

            for (File deviceDir : deviceDirs) {
                String dirName = deviceDir.getName();
                if (!MAC_PATTERN.matcher(dirName).matches()) {
                    continue;
                }
                File infoFile = new File(deviceDir, "info");
                if (!infoFile.exists()) {
                    continue;
                }

                Map<String, String> props = Builder.getKeyValueMapFromFile(infoFile.getAbsolutePath(), "=");
                String name = props.getOrDefault("Name", Normal.EMPTY);
                boolean paired = Boolean.parseBoolean(props.getOrDefault("Paired", "true"));
                boolean connected = Boolean.parseBoolean(props.getOrDefault("Connected", "false"));
                int batteryLevel = Parsing.parseIntOrDefault(props.get("Battery"), -1);
                int classOfDevice = Parsing.hexStringToInt(props.getOrDefault("Class", "0"), 0);
                String majorClass = parseMajorDeviceClass(classOfDevice);

                devices.add(
                        new LinuxBluetoothDevice(name, dirName, majorClass, connected, paired, batteryLevel,
                                adapterName));
            }
        }
        return Collections.unmodifiableList(devices);
    }

}
