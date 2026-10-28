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
package org.miaixz.bus.health.unix.freebsd.hardware;

import java.util.*;
import java.util.regex.Matcher;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.UsbDevice;
import org.miaixz.bus.health.builtin.hardware.common.AbstractUsbDevice;

/**
 * FreeBSD USB device implementation backed by {@code devinfo -v}.
 *
 * @author Kimi Liu
 */
@Immutable
public class FreeBsdUsbDevice extends AbstractUsbDevice {

    /** Pattern matching one indented {@code devinfo -v} device line. */
    private static final java.util.regex.Pattern DEVINFO_LINE = java.util.regex.Pattern
            .compile("( *)(\\S+)(?: <(.*?)>)?(.*)");

    /** Pattern matching a quoted or unquoted plug-and-play field. */
    private static final java.util.regex.Pattern PNP_FIELD = java.util.regex.Pattern
            .compile("(?<!\\w)(\\w++)=(\"[^\"]*+\"|\\S++)");

    /**
     * Creates a USB device.
     *
     * @param name             the device name
     * @param vendor           the vendor name
     * @param vendorId         the vendor identifier
     * @param productId        the product identifier
     * @param serialNumber     the serial number
     * @param uniqueDeviceId   the unique device identifier
     * @param connectedDevices the connected devices
     */
    public FreeBsdUsbDevice(String name, String vendor, String vendorId, String productId, String serialNumber,
            String uniqueDeviceId, List<UsbDevice> connectedDevices) {
        super(name, vendor, vendorId, productId, serialNumber, uniqueDeviceId, connectedDevices);
    }

    /**
     * Gets the USB device tree or a flattened list.
     *
     * @param tree whether to retain the controller tree
     * @return the USB devices
     */
    public static List<UsbDevice> getUsbDevices(boolean tree) {
        List<UsbDevice> devices = parseUsbDevices(Executor.runNative("devinfo -v"));
        if (tree) {
            return devices;
        }
        List<UsbDevice> flat = new ArrayList<>();
        for (UsbDevice device : devices) {
            addDevicesToList(flat, device.getConnectedDevices());
        }
        return flat;
    }

    /**
     * Parses {@code devinfo -v} into USB controller device trees.
     *
     * @param lines the command output
     * @return the USB controllers
     */
    static List<UsbDevice> parseUsbDevices(List<String> lines) {
        Map<String, String> names = new HashMap<>();
        Map<String, String> vendorIds = new HashMap<>();
        Map<String, String> productIds = new HashMap<>();
        Map<String, String> serials = new HashMap<>();
        Map<String, String> parents = new HashMap<>();
        Map<String, List<String>> children = new HashMap<>();
        List<String> buses = new ArrayList<>();
        Deque<Pair<Integer, String>> ancestors = new ArrayDeque<>();
        Set<String> usbNodes = new HashSet<>();
        Set<String> nonUsbNodes = new HashSet<>();

        for (String line : lines) {
            Matcher matcher = DEVINFO_LINE.matcher(line);
            if (!matcher.matches()) {
                continue;
            }
            int indent = matcher.group(Normal._1).length();
            Pair<Integer, String> ancestor = ancestors.peek();
            while (ancestor != null && ancestor.getLeft() >= indent) {
                ancestors.pop();
                ancestor = ancestors.peek();
            }
            String parent = ancestor == null ? null : ancestor.getRight();
            Map<String, String> fields = parseFields(matcher.group(Normal._4));
            String generatedName = fields.get("ugen");
            String key = generatedName == null ? matcher.group(Normal._2) : generatedName;
            if (generatedName == null && names.containsKey(key)) {
                key += Symbol.HASH + names.size();
            }
            ancestors.push(Pair.of(indent, key));
            if (parent != null && generatedName == null
                    && (usbNodes.contains(parent) || nonUsbNodes.contains(parent))) {
                nonUsbNodes.add(key);
                continue;
            }
            if (generatedName != null || parent != null && parent.startsWith("usbus")) {
                usbNodes.add(key);
            }
            if (names.containsKey(key)) {
                continue;
            }
            if (key.startsWith("usbus")) {
                buses.add(key);
            }
            String description = Parsing.getStringValueOrEmpty(matcher.group(Normal._3));
            int usbClass = description.indexOf(", class ");
            names.put(
                    key,
                    description.isEmpty() ? matcher.group(Normal._2)
                            : usbClass < Normal._0 ? description : description.substring(Normal._0, usbClass));
            putHexId(vendorIds, key, fields.get("vendor"));
            putHexId(productIds, key, fields.get(fields.containsKey("product") ? "product" : "device"));
            String serial = fields.get("sernum");
            if (serial != null && !serial.isEmpty()) {
                serials.put(key, serial);
            }
            if (parent != null) {
                parents.put(key, parent);
                children.computeIfAbsent(parent, unused -> new ArrayList<>()).add(key);
            }
        }

        List<UsbDevice> controllers = new ArrayList<>();
        for (String bus : buses) {
            String parent = parents.get(bus);
            if (parent != null) {
                children.put(parent, new ArrayList<>(children.getOrDefault(bus, Collections.emptyList())));
                controllers.add(
                        buildDeviceTree(
                                parent,
                                "0000",
                                "0000",
                                names,
                                Collections.emptyMap(),
                                vendorIds,
                                productIds,
                                serials,
                                children,
                                FreeBsdUsbDevice::new));
            }
        }
        return Collections.unmodifiableList(controllers);
    }

    /** Parses plug-and-play fields from one device line. */
    private static Map<String, String> parseFields(String fields) {
        Map<String, String> values = new HashMap<>();
        Matcher matcher = PNP_FIELD.matcher(fields);
        while (matcher.find()) {
            String value = matcher.group(Normal._2);
            if (value.length() >= Normal._2 && value.charAt(Normal._0) == '"'
                    && value.charAt(value.length() - Normal._1) == '"') {
                value = value.substring(Normal._1, value.length() - Normal._1);
            }
            values.put(matcher.group(Normal._1), value);
        }
        return values;
    }

    /** Adds a hexadecimal USB identifier when present. */
    private static void putHexId(Map<String, String> values, String key, String hex) {
        if (hex != null && hex.startsWith("0x")) {
            values.put(
                    key,
                    String.format(Locale.ROOT, "%04x", Parsing.hexStringToInt(hex.substring(Normal._2), Normal._0)));
        }
    }

}
