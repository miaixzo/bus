/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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

import java.util.Collections;
import java.util.List;
import java.util.Locale;

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

    private final String name;
    private final String vendor;
    private final String vendorId;
    private final String productId;
    private final String serialNumber;
    private final String uniqueDeviceId;
    private final List<UsbDevice> connectedDevices;

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

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getVendor() {
        return this.vendor;
    }

    @Override
    public String getVendorId() {
        return this.vendorId;
    }

    @Override
    public String getProductId() {
        return this.productId;
    }

    @Override
    public String getSerialNumber() {
        return this.serialNumber;
    }

    @Override
    public String getUniqueDeviceId() {
        return this.uniqueDeviceId;
    }

    @Override
    public List<UsbDevice> getConnectedDevices() {
        return this.connectedDevices;
    }

    @Override
    public String toString() {
        return indentUsb(this, 1);
    }

    @Override
    public int compareTo(UsbDevice usb) {
        // Naturally sort by device name
        return getName().compareTo(usb.getName());
    }

}
