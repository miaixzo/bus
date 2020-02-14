/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.health.hardware.unix.freebsd;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Command;
import org.aoju.bus.health.hardware.AbstractUsbDevice;
import org.aoju.bus.health.hardware.UsbDevice;

import java.util.*;

/**
 * <p>
 * FreeBsdUsbDevice class.
 * </p>
 *
 * @author Kimi Liu
 * @version 5.6.0
 * @since JDK 1.8+
 */
public class FreeBsdUsbDevice extends AbstractUsbDevice {

    public FreeBsdUsbDevice(String name, String vendor, String vendorId, String productId, String serialNumber,
                            String uniqueDeviceId, UsbDevice[] connectedDevices) {
        super(name, vendor, vendorId, productId, serialNumber, uniqueDeviceId, connectedDevices);
    }

    /**
     * {@inheritDoc}
     *
     * @param tree a boolean.
     * @return an array of {@link UsbDevice} objects.
     */
    public static UsbDevice[] getUsbDevices(boolean tree) {
        UsbDevice[] devices = getUsbDevices();
        if (tree) {
            return devices;
        }
        List<UsbDevice> deviceList = new ArrayList<>();
        // Top level is controllers; they won't be added to the list, but all
        // their connected devices will be
        for (UsbDevice device : devices) {
            deviceList.add(new FreeBsdUsbDevice(device.getName(), device.getVendor(), device.getVendorId(),
                    device.getProductId(), device.getSerialNumber(), device.getUniqueDeviceId(),
                    new FreeBsdUsbDevice[0]));
            addDevicesToList(deviceList, device.getConnectedDevices());
        }
        return deviceList.toArray(new UsbDevice[0]);
    }

    private static UsbDevice[] getUsbDevices() {
        // Maps to store information using node # as the key
        Map<String, String> nameMap = new HashMap<>();
        Map<String, String> vendorMap = new HashMap<>();
        Map<String, String> vendorIdMap = new HashMap<>();
        Map<String, String> productIdMap = new HashMap<>();
        Map<String, String> serialMap = new HashMap<>();
        Map<String, String> parentMap = new HashMap<>();
        Map<String, List<String>> hubMap = new HashMap<>();

        // Enumerate all devices and build information maps. This will build the
        // entire device tree; we will identify the controllers as the parents
        // of the usbus entries and eventually only populate the returned
        // results with those
        List<String> devices = Command.runNative("lshal");
        if (devices.isEmpty()) {
            return new FreeBsdUsbDevice[0];
        }
        // For each item enumerated, store information in the maps
        String key = Normal.EMPTY;
        List<String> usBuses = new ArrayList<>();
        for (String line : devices) {
            // udi = ... identifies start of a new tree
            if (line.startsWith("udi =")) {
                // Remove indent for key
                key = Builder.getSingleQuoteStringValue(line);
            } else if (!key.isEmpty()) {
                // We are currently processing for node identified by key. Save
                // approrpriate variables to maps.
                line = line.trim();
                if (!line.isEmpty()) {
                    if (line.startsWith("freebsd.driver =")
                            && "usbus".equals(Builder.getSingleQuoteStringValue(line))) {
                        usBuses.add(key);
                    } else if (line.contains(".parent =")) {
                        String parent = Builder.getSingleQuoteStringValue(line);
                        // If this is interface of parent, skip
                        if (key.replace(parent, Normal.EMPTY).startsWith("_if")) {
                            continue;
                        }
                        // Store parent for later usbus-skipping
                        parentMap.put(key, parent);
                        // Add this key to the parent's hubmap list
                        hubMap.computeIfAbsent(parent, x -> new ArrayList<>()).add(key);
                    } else if (line.contains(".product =")) {
                        nameMap.put(key, Builder.getSingleQuoteStringValue(line));
                    } else if (line.contains(".vendor =")) {
                        vendorMap.put(key, Builder.getSingleQuoteStringValue(line));
                    } else if (line.contains(".serial =")) {
                        String serial = Builder.getSingleQuoteStringValue(line);
                        serialMap.put(key,
                                serial.startsWith("0x") ? Builder.hexStringToString(serial.replace("0x", Normal.EMPTY))
                                        : serial);
                    } else if (line.contains(".vendor_id =")) {
                        vendorIdMap.put(key, String.format("%04x", Builder.getFirstIntValue(line)));
                    } else if (line.contains(".product_id =")) {
                        productIdMap.put(key, String.format("%04x", Builder.getFirstIntValue(line)));
                    }
                }
            }
        }

        // Build tree and return
        List<UsbDevice> controllerDevices = new ArrayList<>();
        for (String usbus : usBuses) {
            // Skip the usbuses: make their parents the controllers and replace
            // parents' children with the buses' children
            String parent = parentMap.get(usbus);
            hubMap.put(parent, hubMap.get(usbus));
            controllerDevices.add(getDeviceAndChildren(parent, "0000", "0000", nameMap, vendorMap, vendorIdMap,
                    productIdMap, serialMap, hubMap));
        }
        return controllerDevices.toArray(new UsbDevice[0]);
    }

    private static void addDevicesToList(List<UsbDevice> deviceList, UsbDevice[] connectedDevices) {
        for (UsbDevice device : connectedDevices) {
            deviceList.add(device);
            addDevicesToList(deviceList, device.getConnectedDevices());
        }
    }

    /**
     * Recursively creates SolarisUsbDevices by fetching information from maps to
     * populate fields
     *
     * @param devPath      The device node path.
     * @param vid          The default (parent) vendor ID
     * @param pid          The default (parent) product ID
     * @param nameMap      The nameMap
     * @param vendorMap    The vendorMap
     * @param vendorIdMap  The vendorIdMap
     * @param productIdMap The productIdMap
     * @param hubMap       The hubMap
     * @return A SolarisUsbDevice corresponding to this device
     */
    private static FreeBsdUsbDevice getDeviceAndChildren(String devPath, String vid, String pid,
                                                         Map<String, String> nameMap, Map<String, String> vendorMap, Map<String, String> vendorIdMap,
                                                         Map<String, String> productIdMap, Map<String, String> serialMap, Map<String, List<String>> hubMap) {
        String vendorId = vendorIdMap.getOrDefault(devPath, vid);
        String productId = productIdMap.getOrDefault(devPath, pid);
        List<String> childPaths = hubMap.getOrDefault(devPath, new ArrayList<>());
        List<FreeBsdUsbDevice> usbDevices = new ArrayList<>();
        for (String path : childPaths) {
            usbDevices.add(getDeviceAndChildren(path, vendorId, productId, nameMap, vendorMap, vendorIdMap,
                    productIdMap, serialMap, hubMap));
        }
        Collections.sort(usbDevices);
        return new FreeBsdUsbDevice(nameMap.getOrDefault(devPath, vendorId + Symbol.COLON + productId),
                vendorMap.getOrDefault(devPath, Normal.EMPTY), vendorId, productId, serialMap.getOrDefault(devPath, Normal.EMPTY), devPath,
                usbDevices.toArray(new UsbDevice[0]));
    }

}
