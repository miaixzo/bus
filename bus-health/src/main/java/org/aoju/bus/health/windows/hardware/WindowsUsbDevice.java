/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.health.windows.hardware;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import com.sun.jna.platform.win32.Cfgmgr32;
import com.sun.jna.platform.win32.Cfgmgr32Util;
import com.sun.jna.ptr.IntByReference;
import org.aoju.bus.core.annotation.Immutable;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.core.lang.tuple.Triple;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.builtin.hardware.AbstractUsbDevice;
import org.aoju.bus.health.builtin.hardware.UsbDevice;
import org.aoju.bus.health.windows.WmiKit;
import org.aoju.bus.health.windows.drivers.Win32DiskDrive;
import org.aoju.bus.health.windows.drivers.Win32PnPEntity;
import org.aoju.bus.health.windows.drivers.Win32USBController;
import org.aoju.bus.logger.Logger;

import java.util.*;

/**
 * Windows Usb Device
 *
 * @author Kimi Liu
 * @version 6.0.0
 * @since JDK 1.8+
 */
@Immutable
public class WindowsUsbDevice extends AbstractUsbDevice {

    public WindowsUsbDevice(String name, String vendor, String vendorId, String productId, String serialNumber,
                            String uniqueDeviceId, List<UsbDevice> connectedDevices) {
        super(name, vendor, vendorId, productId, serialNumber, uniqueDeviceId, connectedDevices);
    }

    /**
     * {@inheritDoc}
     *
     * @param tree a boolean.
     * @return an array of {@link UsbDevice} objects.
     */
    public static List<UsbDevice> getUsbDevices(boolean tree) {
        List<UsbDevice> devices = getUsbDevices();
        if (tree) {
            return devices;
        }
        List<UsbDevice> deviceList = new ArrayList<>();
        // Top level is controllers; they won't be added to the list, but all
        // their connected devices will be
        for (UsbDevice device : devices) {
            addDevicesToList(deviceList, device.getConnectedDevices());
        }
        return deviceList;
    }

    private static List<UsbDevice> getUsbDevices() {
        // Map to build the recursive tree structure
        Map<String, List<String>> deviceTreeMap = new HashMap<>();
        // Track devices seen in the process
        Set<String> devicesSeen = new HashSet<>();

        // Navigate the device tree to track what devices are present
        List<UsbDevice> controllerDevices = new ArrayList<>();
        List<String> controllerDeviceIdList = getControllerDeviceIdList();
        for (String controllerDeviceId : controllerDeviceIdList) {
            putChildrenInDeviceTree(controllerDeviceId, 0, deviceTreeMap, devicesSeen);
        }
        // Map to store information using PNPDeviceID as the key.
        Map<String, Triple<String, String, String>> deviceStringMap = queryDeviceStringsMap(devicesSeen);
        // recursively build results
        for (String controllerDeviceId : controllerDeviceIdList) {
            WindowsUsbDevice deviceAndChildren = getDeviceAndChildren(controllerDeviceId, "0000", "0000", deviceTreeMap,
                    deviceStringMap);
            if (deviceAndChildren != null) {
                controllerDevices.add(deviceAndChildren);
            }
        }
        return controllerDevices;
    }

    private static void addDevicesToList(List<UsbDevice> deviceList, List<UsbDevice> list) {
        for (UsbDevice device : list) {
            deviceList.add(new WindowsUsbDevice(device.getName(), device.getVendor(), device.getVendorId(),
                    device.getProductId(), device.getSerialNumber(), device.getUniqueDeviceId(),
                    Collections.emptyList()));
            addDevicesToList(deviceList, device.getConnectedDevices());
        }
    }

    private static Map<String, Triple<String, String, String>> queryDeviceStringsMap(Set<String> devicesToAdd) {
        Map<String, Triple<String, String, String>> deviceStringCache = new HashMap<>();
        // Add devices not in the tree
        if (!devicesToAdd.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (String deviceID : devicesToAdd) {
                if (first) {
                    sb.append(" WHERE (PnPDeviceID=\"");
                    first = false;
                } else {
                    sb.append(" OR (PnPDeviceID=\"");
                }
                sb.append(deviceID).append("\")");
            }
            String whereClause = sb.toString();
            // Get serial # for disk drives or other physical media
            Map<String, String> pnpToSerialMap = new HashMap<>();
            WmiResult<Win32DiskDrive.DeviceIdProperty> serialNumbers = Win32DiskDrive.queryDiskDriveId(whereClause);
            for (int i = 0; i < serialNumbers.getResultCount(); i++) {
                String pnpDeviceID = WmiKit.getString(serialNumbers, Win32DiskDrive.DeviceIdProperty.PNPDEVICEID, i);
                if (deviceStringCache.containsKey(pnpDeviceID)) {
                    pnpToSerialMap.put(pnpDeviceID, Builder
                            .hexStringToString(WmiKit.getString(serialNumbers, Win32DiskDrive.DeviceIdProperty.SERIALNUMBER, i)));
                }
            }
            // Query Win32_PnPEntity to populate the maps
            WmiResult<Win32PnPEntity.PnPEntityProperty> pnpEntity = Win32PnPEntity.queryDeviceId(whereClause);
            for (int i = 0; i < pnpEntity.getResultCount(); i++) {
                String pnpDeviceID = WmiKit.getString(pnpEntity, Win32PnPEntity.PnPEntityProperty.PNPDEVICEID, i);
                String name = WmiKit.getString(pnpEntity, Win32PnPEntity.PnPEntityProperty.NAME, i);
                String vendor = WmiKit.getString(pnpEntity, Win32PnPEntity.PnPEntityProperty.MANUFACTURER, i);
                deviceStringCache.put(pnpDeviceID,
                        Triple.of(name, vendor, pnpToSerialMap.getOrDefault(pnpDeviceID, "")));
                Logger.debug("Adding {} to USB device cache.", pnpDeviceID);
            }
        }
        return deviceStringCache;
    }

    /**
     * Navigates the Device Tree to place all children PNPDeviceIDs into the map for
     * the specified deviceID. Recursively adds children's children, etc.
     *
     * @param deviceId       The device to add respective children to the map
     * @param deviceInstance The device instance (devnode handle), if known. If set to 0, the
     *                       code will search for a match.
     * @param deviceTreeMap  The overall device tree map that starts at the controllers
     * @param devicesSeen    Devices we've seen so we can add/remove from cache later
     */
    private static void putChildrenInDeviceTree(String deviceId, int deviceInstance,
                                                Map<String, List<String>> deviceTreeMap, Set<String> devicesSeen) {
        devicesSeen.add(deviceId);
        // If no devInst provided, find it
        int devInst = deviceInstance;
        if (devInst == 0) {
            IntByReference pdnDevInst = new IntByReference();
            Cfgmgr32.INSTANCE.CM_Locate_DevNode(pdnDevInst, deviceId, 0);
            devInst = pdnDevInst.getValue();
        }
        // Now iterate the children. Call CM_Get_Child to get first child
        IntByReference child = new IntByReference();
        if (0 == Cfgmgr32.INSTANCE.CM_Get_Child(child, devInst, 0)) {
            // Add first child to a list
            List<String> childList = new ArrayList<>();
            String childId = Cfgmgr32Util.CM_Get_Device_ID(child.getValue());
            childList.add(childId);
            deviceTreeMap.put(deviceId, childList);
            putChildrenInDeviceTree(childId, child.getValue(), deviceTreeMap, devicesSeen);
            // Find any other children
            IntByReference sibling = new IntByReference();
            while (0 == Cfgmgr32.INSTANCE.CM_Get_Sibling(sibling, child.getValue(), 0)) {
                // Add to the list
                String siblingId = Cfgmgr32Util.CM_Get_Device_ID(sibling.getValue());
                deviceTreeMap.get(deviceId).add(siblingId);
                putChildrenInDeviceTree(siblingId, sibling.getValue(), deviceTreeMap, devicesSeen);
                // Make this sibling the new child to find other siblings
                child = sibling;
            }
        }
    }

    private static WindowsUsbDevice getDeviceAndChildren(String hubDeviceId, String vid, String pid,
                                                         Map<String, List<String>> deviceTreeMap, Map<String, Triple<String, String, String>> deviceStringMap) {
        String vendorId = vid;
        String productId = pid;
        Pair<String, String> idPair = Builder.parsePnPDeviceIdToVendorProductId(hubDeviceId);
        if (idPair != null) {
            vendorId = idPair.getLeft();
            productId = idPair.getRight();
        }
        List<String> pnpDeviceIds = deviceTreeMap.getOrDefault(hubDeviceId, new ArrayList<>());
        List<UsbDevice> usbDevices = new ArrayList<>();
        for (String pnpDeviceId : pnpDeviceIds) {
            WindowsUsbDevice deviceAndChildren = getDeviceAndChildren(pnpDeviceId, vendorId, productId, deviceTreeMap,
                    deviceStringMap);
            if (deviceAndChildren != null) {
                usbDevices.add(deviceAndChildren);
            }
        }
        Collections.sort(usbDevices);
        if (deviceStringMap.containsKey(hubDeviceId)) {
            // name, vendor, serial
            Triple<String, String, String> device = deviceStringMap.get(hubDeviceId);
            String name = device.getLeft();
            if (name.isEmpty()) {
                name = vendorId + Symbol.COLON + productId;
            }
            return new WindowsUsbDevice(name, device.getMiddle(), vendorId, productId, device.getRight(), hubDeviceId,
                    usbDevices);
        }
        return null;
    }

    /**
     * Queries the USB Controller list
     *
     * @return A list of Strings of USB Controller PNPDeviceIDs
     */
    private static List<String> getControllerDeviceIdList() {
        List<String> controllerDeviceIdsList = new ArrayList<>();
        // One time lookup of USB Controller PnP Device IDs which don't
        // change
        WmiResult<Win32USBController.USBControllerProperty> usbController = Win32USBController.queryUSBControllers();
        for (int i = 0; i < usbController.getResultCount(); i++) {
            controllerDeviceIdsList.add(WmiKit.getString(usbController, Win32USBController.USBControllerProperty.PNPDEVICEID, i));
        }
        return controllerDeviceIdsList;
    }

}
