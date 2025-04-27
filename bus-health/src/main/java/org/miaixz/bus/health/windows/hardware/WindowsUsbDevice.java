/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.windows.hardware;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.UsbDevice;
import org.miaixz.bus.health.builtin.hardware.common.AbstractUsbDevice;
import org.miaixz.bus.health.windows.driver.DeviceTree;

import com.sun.jna.platform.win32.Guid.GUID;

/**
 * Windows Usb Device
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
public class WindowsUsbDevice extends AbstractUsbDevice {

    private static final GUID GUID_DEVINTERFACE_USB_HOST_CONTROLLER = new GUID(
            "{3ABF6F2D-71C4-462A-8A92-1E6861E6AF27}");

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

    private static void addDevicesToList(List<UsbDevice> deviceList, List<UsbDevice> list) {
        for (UsbDevice device : list) {
            deviceList.add(new WindowsUsbDevice(device.getName(), device.getVendor(), device.getVendorId(),
                    device.getProductId(), device.getSerialNumber(), device.getUniqueDeviceId(),
                    Collections.emptyList()));
            addDevicesToList(deviceList, device.getConnectedDevices());
        }
    }

    private static List<UsbDevice> queryUsbDevices() {
        Tuple controllerDevices = DeviceTree.queryDeviceTree(GUID_DEVINTERFACE_USB_HOST_CONTROLLER);
        Map<Integer, Integer> parentMap = controllerDevices.get(1);
        Map<Integer, String> nameMap = controllerDevices.get(2);
        Map<Integer, String> deviceIdMap = controllerDevices.get(3);
        Map<Integer, String> mfgMap = controllerDevices.get(4);

        List<UsbDevice> usbDevices = new ArrayList<>();
        // recursively build results
        for (Integer controllerDevice : (Set<Integer>) controllerDevices.get(0)) {
            WindowsUsbDevice deviceAndChildren = queryDeviceAndChildren(controllerDevice, parentMap, nameMap,
                    deviceIdMap, mfgMap, "0000", "0000", Normal.EMPTY);
            if (deviceAndChildren != null) {
                usbDevices.add(deviceAndChildren);
            }
        }
        return usbDevices;
    }

    private static WindowsUsbDevice queryDeviceAndChildren(Integer device, Map<Integer, Integer> parentMap,
            Map<Integer, String> nameMap, Map<Integer, String> deviceIdMap, Map<Integer, String> mfgMap, String vid,
            String pid, String parentSerial) {
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
            WindowsUsbDevice deviceAndChildren = queryDeviceAndChildren(child, parentMap, nameMap, deviceIdMap, mfgMap,
                    vendorId, productId, serial);
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
