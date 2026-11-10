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
package org.miaixz.bus.health.linux;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;

/**
 * Reads device properties from the world-readable udev database under {@code /run/udev/data}. The records expose the
 * same {@code E:} properties returned by libudev, allowing sysfs fallbacks to retain udev-provided metadata.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class UdevData {

    /**
     * The default udev database directory.
     */
    public static final String RUN_UDEV_DATA = "/run/udev/data/";

    /**
     * Constructs a new UdevData instance.
     */
    public UdevData() {
        // No initialization required.
    }

    /**
     * Reads properties for a block device.
     *
     * @param major the major device number
     * @param minor the minor device number
     * @return the device properties
     */
    public static Map<String, String> queryBlockDevice(int major, int minor) {
        return queryBlockDevice(RUN_UDEV_DATA, major, minor);
    }

    /**
     * Reads properties for a block device from a selected database directory.
     *
     * @param dataDir the database directory
     * @param major   the major device number
     * @param minor   the minor device number
     * @return the device properties
     */
    public static Map<String, String> queryBlockDevice(String dataDir, int major, int minor) {
        if (major <= Normal._0) {
            return Collections.emptyMap();
        }
        return parseProperties(Builder.readFile(dataDir + "b" + major + Symbol.COLON + minor, false));
    }

    /**
     * Reads properties for a network interface.
     *
     * @param ifindex the interface index
     * @return the interface properties
     */
    public static Map<String, String> queryNetworkInterface(int ifindex) {
        return queryNetworkInterface(RUN_UDEV_DATA, ifindex);
    }

    /**
     * Reads properties for a network interface from a selected database directory.
     *
     * @param dataDir the database directory
     * @param ifindex the interface index
     * @return the interface properties
     */
    public static Map<String, String> queryNetworkInterface(String dataDir, int ifindex) {
        if (ifindex <= Normal._0) {
            return Collections.emptyMap();
        }
        return parseProperties(Builder.readFile(dataDir + "n" + ifindex, false));
    }

    /**
     * Reads properties for a device without a device node, such as a PCI function.
     *
     * @param subsystem the device subsystem
     * @param sysname   the device name within the subsystem
     * @return the device properties
     */
    public static Map<String, String> queryDevice(String subsystem, String sysname) {
        return queryDevice(RUN_UDEV_DATA, subsystem, sysname);
    }

    /**
     * Reads properties for a device without a device node from a selected database directory.
     *
     * @param dataDir   the database directory
     * @param subsystem the device subsystem
     * @param sysname   the device name within the subsystem
     * @return the device properties
     */
    public static Map<String, String> queryDevice(String dataDir, String subsystem, String sysname) {
        if (subsystem.isEmpty() || sysname.isEmpty()) {
            return Collections.emptyMap();
        }
        return parseProperties(Builder.readFile(dataDir + Symbol.PLUS + subsystem + Symbol.COLON + sysname, false));
    }

    /**
     * Parses property records from a udev database file.
     *
     * @param lines the database records
     * @return properties keyed by name
     */
    static Map<String, String> parseProperties(List<String> lines) {
        Map<String, String> properties = new HashMap<>();
        for (String line : lines) {
            if (line.startsWith("E:")) {
                int equals = line.indexOf(Symbol.C_EQUAL);
                if (equals > Normal._2) {
                    properties.put(line.substring(Normal._2, equals), line.substring(equals + Normal._1));
                }
            }
        }
        return properties;
    }

}
