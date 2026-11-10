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
package org.miaixz.bus.health.linux.driver;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.linux.SysPath;

/**
 * Reads board identity from the device tree used by ARM, RISC-V, and POWER systems in place of PC DMI tables.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class Devicetree {

    /**
     * Constructs a new Devicetree instance.
     */
    public Devicetree() {
        // No initialization required.
    }

    /**
     * Query the model from the devicetree
     *
     * @return The model if available, null otherwise
     */
    public static String queryModel() {
        return queryModel(SysPath.DEVICETREE);
    }

    /**
     * Queries the model from a selected device-tree directory.
     *
     * @param base the device-tree directory
     * @return the model, or {@code null} if unavailable
     */
    static String queryModel(String base) {
        List<String> model = readStrings(base + "model");
        return model.isEmpty() ? null : model.get(Normal._0).replace("Machine: ", Normal.EMPTY);
    }

    /**
     * Queries the board serial number.
     *
     * @return the serial number, or {@code null} if unavailable
     */
    public static String querySerialNumber() {
        return querySerialNumber(SysPath.DEVICETREE);
    }

    /**
     * Queries the serial number from a selected device-tree directory.
     *
     * @param base the device-tree directory
     * @return the serial number, or {@code null} if unavailable
     */
    static String querySerialNumber(String base) {
        List<String> serial = readStrings(base + "serial-number");
        return serial.isEmpty() ? null : serial.get(Normal._0);
    }

    /**
     * Queries the board manufacturer from its first compatible string.
     *
     * @return the manufacturer, or {@code null} if unavailable
     */
    public static String queryManufacturer() {
        return queryManufacturer(SysPath.DEVICETREE);
    }

    /**
     * Queries the manufacturer from a selected device-tree directory.
     *
     * @param base the device-tree directory
     * @return the manufacturer, or {@code null} if unavailable
     */
    static String queryManufacturer(String base) {
        List<String> compatible = readStrings(base + "compatible");
        return compatible.isEmpty() ? null : queryVendor(compatible.get(Normal._0));
    }

    /**
     * Resolves a device-tree compatible-string vendor prefix.
     *
     * @param compatible the compatible string
     * @return the vendor name, unrecognized prefix, or {@code null} when no vendor prefix exists
     */
    public static String queryVendor(String compatible) {
        int comma = compatible.indexOf(Symbol.C_COMMA);
        if (comma <= Normal._0) {
            return null;
        }
        String vendor = compatible.substring(Normal._0, comma);
        if ("linux".equals(vendor)) {
            return null;
        }
        String name = Builder.readProperties(Builder._DEVICETREE_PROPERTIES).getProperty(vendor);
        return StringKit.isBlank(name) ? vendor : name;
    }

    /**
     * Reads non-empty NUL-separated strings from a device-tree property.
     *
     * @param path the property path
     * @return the property strings
     */
    private static List<String> readStrings(String path) {
        List<String> strings = new ArrayList<>();
        for (String value : new String(Builder.readAllBytes(path, false), Charset.UTF_8).split("\0", -1)) {
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                strings.add(trimmed);
            }
        }
        return strings;
    }

}
