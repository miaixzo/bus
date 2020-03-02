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
package org.aoju.bus.health.hardware;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;

import java.util.Arrays;

/**
 * A USB device
 *
 * @author Kimi Liu
 * @version 5.6.5
 * @since JDK 1.8+
 */
public abstract class AbstractUsbDevice implements UsbDevice {

    protected String name;

    protected String vendor;

    protected String vendorId;

    protected String productId;

    protected String serialNumber;

    protected String uniqueDeviceId;

    protected UsbDevice[] connectedDevices;

    /**
     * <p>
     * Constructor for AbstractUsbDevice.
     * </p>
     *
     * @param name             a {@link java.lang.String} object.
     * @param vendor           a {@link java.lang.String} object.
     * @param vendorId         a {@link java.lang.String} object.
     * @param productId        a {@link java.lang.String} object.
     * @param serialNumber     a {@link java.lang.String} object.
     * @param uniqueDeviceId   a {@link java.lang.String} object.
     * @param connectedDevices an array of {@link UsbDevice} objects.
     */
    public AbstractUsbDevice(String name, String vendor, String vendorId, String productId, String serialNumber,
                             String uniqueDeviceId, UsbDevice[] connectedDevices) {
        this.name = name;
        this.vendor = vendor;
        this.vendorId = vendorId;
        this.productId = productId;
        this.serialNumber = serialNumber;
        this.uniqueDeviceId = uniqueDeviceId;
        this.connectedDevices = Arrays.copyOf(connectedDevices, connectedDevices.length);
    }

    /**
     * Helper method for indenting chained USB devices
     *
     * @param usbDevice A USB device to print
     * @param indent    number of spaces to indent
     */
    private static String indentUsb(UsbDevice usbDevice, int indent) {
        String indentFmt = indent > 2 ? String.format("%%%ds|-- ", indent - 4) : String.format("%%%ds", indent);
        StringBuilder sb = new StringBuilder(String.format(indentFmt, Normal.EMPTY));
        sb.append(usbDevice.getName());
        if (usbDevice.getVendor().length() > 0) {
            sb.append(" (").append(usbDevice.getVendor()).append(Symbol.C_PARENTHESE_RIGHT);
        }
        if (usbDevice.getSerialNumber().length() > 0) {
            sb.append(" [s/n: ").append(usbDevice.getSerialNumber()).append(Symbol.C_BRACKET_RIGHT);
        }
        for (UsbDevice connected : usbDevice.getConnectedDevices()) {
            sb.append(Symbol.C_LF).append(indentUsb(connected, indent + 4));
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
    public UsbDevice[] getConnectedDevices() {
        return Arrays.copyOf(this.connectedDevices, this.connectedDevices.length);
    }

    @Override
    public int compareTo(UsbDevice usb) {
        // Naturally sort by device name
        return getName().compareTo(usb.getName());
    }

    @Override
    public String toString() {
        return indentUsb(this, 1);
    }
}
