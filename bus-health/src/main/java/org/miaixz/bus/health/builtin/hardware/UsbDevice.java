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
package org.miaixz.bus.health.builtin.hardware;

import java.util.List;

import org.miaixz.bus.core.lang.annotation.Immutable;

/**
 * A USB device is a device connected via a USB port, possibly internally/permanently. Hubs may contain ports to which
 * other devices connect in a recursive fashion.
 *
 * @author Kimi Liu
 * @since Java 21+
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

}
