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
package org.miaixz.bus.health.builtin.hardware;

import java.util.List;
import java.util.SortedSet;

import org.miaixz.bus.core.lang.annotation.Immutable;

/**
 * A USB device is a device connected via a USB port, possibly internally/permanently. Hubs may contain ports to which
 * other devices connect in a recursive fashion.
 *
 * @author Kimi Liu
 */
@Immutable
public interface UsbDevice extends Comparable<UsbDevice> {

    /**
     * Name of the USB device
     *
     * @return The device name
     */
    String getName();

    /**
     * Vendor that manufactured the USB device
     *
     * @return The vendor name
     */
    String getVendor();

    /**
     * ID of the vendor that manufactured the USB device
     *
     * @return The vendor ID, a 4-digit hex string
     */
    String getVendorId();

    /**
     * Product ID of the USB device
     *
     * @return The product ID, a 4-digit hex string
     */
    String getProductId();

    /**
     * Serial number of the USB device
     *
     * @return The serial number, if known
     */
    String getSerialNumber();

    /**
     * A Unique Device ID of the USB device, such as the PnPDeviceID (Windows), Device Node Path (Linux), Registry Entry
     * ID (macOS), or Device Node number (Unix)
     *
     * @return The Unique Device ID
     */
    String getUniqueDeviceId();

    /**
     * Other devices connected to this hub
     *
     * @return An {@code UnmodifiableList} of other devices connected to this hub, if any, or an empty list if none
     */
    List<UsbDevice> getConnectedDevices();

    /**
     * Compares this device to another, ordering by name and then, for devices sharing a name, by unique device ID,
     * vendor ID, product ID and serial number in that order.
     * <p>
     * The ordering is defined here rather than in each implementation so that every {@code UsbDevice} agrees on it.
     * That matters because {@link Comparable} requires {@code sgn(a.compareTo(b)) == -sgn(b.compareTo(a))} for every
     * pair: were one implementation to order by name alone while another broke ties, two devices sharing a name would
     * compare as equal in one direction and not the other, and a {@link SortedSet} of them would keep a different
     * number of elements depending on insertion order. Implementations should not override this method.
     * <p>
     * Because the tie-breakers are the fields that identify a device, this returns zero only for devices that carry the
     * same identity. Note that a class implementing this interface without also overriding
     * {@link Object#equals(Object)} is ordered consistently but is still not {@code equals} to anything but itself.
     *
     * @param usb the device to compare with
     * @return a negative integer, zero, or a positive integer as this device sorts before, with, or after the argument
     */
    @Override
    default int compareTo(UsbDevice usb) {
        int cmp = getName().compareTo(usb.getName());
        if (cmp == 0) {
            cmp = getUniqueDeviceId().compareTo(usb.getUniqueDeviceId());
        }
        if (cmp == 0) {
            cmp = getVendorId().compareTo(usb.getVendorId());
        }
        if (cmp == 0) {
            cmp = getProductId().compareTo(usb.getProductId());
        }
        if (cmp == 0) {
            cmp = getSerialNumber().compareTo(usb.getSerialNumber());
        }
        return cmp;
    }

}
