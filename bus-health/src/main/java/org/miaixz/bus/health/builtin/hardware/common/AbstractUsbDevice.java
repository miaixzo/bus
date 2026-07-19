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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.builtin.hardware.UsbDevice;

/**
 * A USB device
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
public abstract class AbstractUsbDevice implements UsbDevice {

    /**
     * The name value.
     */
    private final String name;

    /**
     * The vendor value.
     */
    private final String vendor;

    /**
     * The vendorId value.
     */
    private final String vendorId;

    /**
     * The productId value.
     */
    private final String productId;

    /**
     * The serialNumber value.
     */
    private final String serialNumber;

    /**
     * The uniqueDeviceId value.
     */
    private final String uniqueDeviceId;

    /**
     * The connectedDevices value.
     */
    private final List<UsbDevice> connectedDevices;

    /**
     * Creates a new AbstractUsbDevice instance.
     *
     * @param name             the name
     * @param vendor           the vendor
     * @param vendorId         the vendor id
     * @param productId        the product id
     * @param serialNumber     the serial number
     * @param uniqueDeviceId   the unique device id
     * @param connectedDevices the connected devices
     */
    protected AbstractUsbDevice(String name, String vendor, String vendorId, String productId, String serialNumber,
            String uniqueDeviceId, List<UsbDevice> connectedDevices) {
        this.name = name;
        this.vendor = vendor;
        this.vendorId = vendorId;
        this.productId = productId;
        this.serialNumber = serialNumber;
        this.uniqueDeviceId = uniqueDeviceId;
        this.connectedDevices = Collections.unmodifiableList(connectedDevices);
    }

    /**
     * Helper method for indenting chained USB devices
     *
     * @param usbDevice A USB device to print
     * @param indent    number of spaces to indent
     * @return The device toString, indented
     */
    private static String indentUsb(UsbDevice usbDevice, int indent) {
        String indentFmt = indent > 4 ? String.format(Locale.ROOT, "%%%ds|-- ", indent - 4)
                : String.format(Locale.ROOT, "%%%ds", indent);
        StringBuilder sb = new StringBuilder(String.format(Locale.ROOT, indentFmt, Normal.EMPTY));
        sb.append(usbDevice.getName());
        if (!usbDevice.getVendor().isEmpty()) {
            sb.append(" (").append(usbDevice.getVendor()).append(Symbol.C_PARENTHESE_RIGHT);
        }
        if (!usbDevice.getSerialNumber().isEmpty()) {
            sb.append(" [s/n: ").append(usbDevice.getSerialNumber()).append(']');
        }
        for (UsbDevice connected : usbDevice.getConnectedDevices()) {
            sb.append('\n').append(indentUsb(connected, indent + 4));
        }
        return sb.toString();
    }

    /**
     * Returns the name.
     *
     * @return the get name result
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Returns the vendor.
     *
     * @return the get vendor result
     */
    @Override
    public String getVendor() {
        return this.vendor;
    }

    /**
     * Returns the vendor id.
     *
     * @return the get vendor id result
     */
    @Override
    public String getVendorId() {
        return this.vendorId;
    }

    /**
     * Returns the product id.
     *
     * @return the get product id result
     */
    @Override
    public String getProductId() {
        return this.productId;
    }

    /**
     * Returns the serial number.
     *
     * @return the get serial number result
     */
    @Override
    public String getSerialNumber() {
        return this.serialNumber;
    }

    /**
     * Returns the unique device id.
     *
     * @return the get unique device id result
     */
    @Override
    public String getUniqueDeviceId() {
        return this.uniqueDeviceId;
    }

    /**
     * Returns the connected devices.
     *
     * @return the get connected devices result
     */
    @Override
    public List<UsbDevice> getConnectedDevices() {
        return this.connectedDevices;
    }

    /**
     * Recursively adds USB devices from {@code list} to {@code deviceList}, depth-first.
     *
     * @param deviceList the target list to add devices to
     * @param list       the source list of devices and their children
     */
    protected static void addDevicesToList(List<UsbDevice> deviceList, List<UsbDevice> list) {
        for (UsbDevice device : list) {
            deviceList.add(device);
            addDevicesToList(deviceList, device.getConnectedDevices());
        }
    }

    /**
     * Recursively builds a USB device and its children from attribute maps keyed by device identifier.
     *
     * @param id           the identifier of the device to build
     * @param vid          the inherited vendor ID
     * @param pid          the inherited product ID
     * @param nameMap      id to product name
     * @param vendorMap    id to vendor name
     * @param vendorIdMap  id to vendor ID
     * @param productIdMap id to product ID
     * @param serialMap    id to serial number
     * @param hubMap       parent id to child ids
     * @param factory      platform-specific device factory
     * @return the assembled USB device
     */
    protected static UsbDevice buildDeviceTree(
            String id,
            String vid,
            String pid,
            Map<String, String> nameMap,
            Map<String, String> vendorMap,
            Map<String, String> vendorIdMap,
            Map<String, String> productIdMap,
            Map<String, String> serialMap,
            Map<String, List<String>> hubMap,
            UsbDeviceFactory factory) {
        String vendorId = vendorIdMap.getOrDefault(id, vid);
        String productId = productIdMap.getOrDefault(id, pid);
        List<UsbDevice> connectedDevices = new ArrayList<>();
        for (String childId : hubMap.getOrDefault(id, Collections.emptyList())) {
            connectedDevices.add(
                    buildDeviceTree(
                            childId,
                            vendorId,
                            productId,
                            nameMap,
                            vendorMap,
                            vendorIdMap,
                            productIdMap,
                            serialMap,
                            hubMap,
                            factory));
        }
        Collections.sort(connectedDevices);
        return factory.create(
                nameMap.getOrDefault(id, vendorId + Symbol.COLON + productId),
                vendorMap.getOrDefault(id, Normal.EMPTY),
                vendorId,
                productId,
                serialMap.getOrDefault(id, Normal.EMPTY),
                id,
                connectedDevices);
    }

    /**
     * Creates a platform-specific USB device from resolved fields.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FunctionalInterface
    public interface UsbDeviceFactory {

        /**
         * Creates a USB device.
         *
         * @param name             the device name
         * @param vendor           the vendor name
         * @param vendorId         the vendor ID
         * @param productId        the product ID
         * @param serialNumber     the serial number
         * @param uniqueDeviceId   the unique device ID
         * @param connectedDevices the connected child devices
         * @return the created USB device
         */
        UsbDevice create(
                String name,
                String vendor,
                String vendorId,
                String productId,
                String serialNumber,
                String uniqueDeviceId,
                List<UsbDevice> connectedDevices);

    }

    /**
     * Returns the to string result.
     *
     * @return the to string result
     */
    @Override
    public String toString() {
        return indentUsb(this, 1);
    }

    /**
     * Returns the compare to result.
     *
     * @param usb the usb
     * @return the compare to result
     */
    @Override
    public int compareTo(UsbDevice usb) {
        // Naturally sort by device name
        return getName().compareTo(usb.getName());
    }

}
