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
package org.miaixz.bus.health.windows.hardware;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.sun.jna.platform.win32.Guid.GUID;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.UsbDevice;
import org.miaixz.bus.health.builtin.hardware.common.AbstractUsbDevice;
import org.miaixz.bus.health.windows.driver.DeviceTree;

/**
 * Windows Usb Device
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
public class WindowsUsbDevice extends AbstractUsbDevice {

    /**
     * The GUID_DEVINTERFACE_USB_HOST_CONTROLLER constant.
     */
    private static final GUID GUID_DEVINTERFACE_USB_HOST_CONTROLLER = new GUID(
            "{3ABF6F2D-71C4-462A-8A92-1E6861E6AF27}");

    /**
     * Creates a new WindowsUsbDevice instance.
     *
     * @param name             the name
     * @param vendor           the vendor
     * @param vendorId         the vendor id
     * @param productId        the product id
     * @param serialNumber     the serial number
     * @param uniqueDeviceId   the unique device id
     * @param connectedDevices the connected devices
     */
    public WindowsUsbDevice(String name, String vendor, String vendorId, String productId, String serialNumber,
            String uniqueDeviceId, List<UsbDevice> connectedDevices) {
        super(name, vendor, vendorId, productId, serialNumber, uniqueDeviceId, connectedDevices);
    }

    /**
     * Instantiates a list of {@link UsbDevice} objects, representing devices connected via a usb port (including
     * internal devices). If the value of {@code tree} is true, the top level devices returned from this method are the
     * USB Controllers; connected hubs and devices in its device tree share that controller's bandwidth. If the value of
     * {@code tree} is false, USB devices (not controllers) are listed in a single flat list.
     *
     * @param tree If true, returns a list of controllers, which requires recursive iteration of connected devices. If
     *             false, returns a flat list of devices excluding controllers.
     * @return a list of {@link UsbDevice} objects.
     */
    public static List<UsbDevice> getUsbDevices(boolean tree) {
        List<UsbDevice> devices = queryUsbDevices();
        if (tree) {
            return devices;
        }
        List<UsbDevice> deviceList = new ArrayList<>();
        // Top level is controllers; they won't be added to the list, but all
        // their connected devices will be
        for (UsbDevice device : devices) {
            // Recursively add all child devices
            addDevicesToList(deviceList, device.getConnectedDevices());
        }
        return deviceList;
    }

    /**
     * Queries the usb devices.
     *
     * @return the query usb devices result
     */
    private static List<UsbDevice> queryUsbDevices() {
        Tuple controllerDevices = DeviceTree.queryDeviceTree(GUID_DEVINTERFACE_USB_HOST_CONTROLLER);
        Map<Integer, Integer> parentMap = controllerDevices.get(1);
        Map<Integer, String> nameMap = controllerDevices.get(2);
        Map<Integer, String> deviceIdMap = controllerDevices.get(3);
        Map<Integer, String> mfgMap = controllerDevices.get(4);

        List<UsbDevice> usbDevices = new ArrayList<>();
        // recursively build results
        for (Integer controllerDevice : (Set<Integer>) controllerDevices.get(0)) {
            WindowsUsbDevice deviceAndChildren = queryDeviceAndChildren(
                    controllerDevice,
                    parentMap,
                    nameMap,
                    deviceIdMap,
                    mfgMap,
                    "0000",
                    "0000",
                    Normal.EMPTY);
            if (deviceAndChildren != null) {
                usbDevices.add(deviceAndChildren);
            }
        }
        return usbDevices;
    }

    /**
     * Queries the device and children.
     *
     * @param device       the device
     * @param parentMap    the parent map
     * @param nameMap      the name map
     * @param deviceIdMap  the device id map
     * @param mfgMap       the mfg map
     * @param vid          the vid
     * @param pid          the pid
     * @param parentSerial the parent serial
     * @return the query device and children result
     */
    private static WindowsUsbDevice queryDeviceAndChildren(
            Integer device,
            Map<Integer, Integer> parentMap,
            Map<Integer, String> nameMap,
            Map<Integer, String> deviceIdMap,
            Map<Integer, String> mfgMap,
            String vid,
            String pid,
            String parentSerial) {
        // Parse vendor and product IDs from the device ID
        // If this doesn't work, use the IDs from the parent
        String vendorId = vid;
        String productId = pid;
        String serial = parentSerial;
        Triplet<String, String, String> idsAndSerial = Parsing
                .parseDeviceIdToVendorProductSerial(deviceIdMap.get(device));
        if (idsAndSerial != null) {
            vendorId = idsAndSerial.getLeft();
            productId = idsAndSerial.getMiddle();
            serial = idsAndSerial.getRight();
            if (serial.isEmpty() && vendorId.equals(vid) && productId.equals(pid)) {
                serial = parentSerial;
            }
        }
        // Iterate the parent map looking for children
        Set<Integer> childDeviceSet = parentMap.entrySet().stream().filter(e -> e.getValue().equals(device))
                .map(Entry::getKey).collect(Collectors.toSet());
        // Recursively find those children and put in a list
        List<UsbDevice> childDevices = new ArrayList<>();
        for (Integer child : childDeviceSet) {
            WindowsUsbDevice deviceAndChildren = queryDeviceAndChildren(
                    child,
                    parentMap,
                    nameMap,
                    deviceIdMap,
                    mfgMap,
                    vendorId,
                    productId,
                    serial);
            if (deviceAndChildren != null) {
                childDevices.add(deviceAndChildren);
            }
        }
        Collections.sort(childDevices);
        // Finally construct the object and return
        if (nameMap.containsKey(device)) {
            String name = nameMap.get(device);
            if (name.isEmpty()) {
                name = vendorId + Symbol.COLON + productId;
            }
            String deviceId = deviceIdMap.get(device);
            String mfg = mfgMap.get(device);
            return new WindowsUsbDevice(name, mfg, vendorId, productId, serial, deviceId, childDevices);
        }
        return null;
    }

}
