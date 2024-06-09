/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.unix.platform.aix.hardware;

import org.miaixz.bus.core.annotation.Immutable;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.health.builtin.hardware.UsbDevice;
import org.miaixz.bus.health.builtin.hardware.common.AbstractUsbDevice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * AIX Usb Device
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
public class AixUsbDevice extends AbstractUsbDevice {

    public AixUsbDevice(String name, String vendor, String vendorId, String productId, String serialNumber,
                        String uniqueDeviceId, List<UsbDevice> connectedDevices) {
        super(name, vendor, vendorId, productId, serialNumber, uniqueDeviceId, connectedDevices);
    }

    /**
     * Instantiates a list of {@link UsbDevice} objects, representing devices connected via a usb port
     * (including internal devices).
     * <p>
     * If the value of {@code tree} is true, the top level devices returned from this method are the USB Controllers;
     * connected hubs and devices in its device tree share that controller's bandwidth. If the value of {@code tree} is
     * false, USB devices (not controllers) are listed in a single flat list.
     *
     * @param tree  If true, returns a list of controllers, which requires recursive iteration of connected devices. If
     *              false, returns a flat list of devices excluding controllers.
     * @param lscfg A memoized lscfg list
     * @return a list of {@link UsbDevice} objects.
     */
    public static List<UsbDevice> getUsbDevices(boolean tree, Supplier<List<String>> lscfg) {
        List<UsbDevice> deviceList = new ArrayList<>();
        for (String line : lscfg.get()) {
            String s = line.trim();
            if (s.startsWith("usb")) {
                String[] split = Pattern.SPACES_PATTERN.split(s, 3);
                if (split.length == 3) {
                    deviceList.add(new AixUsbDevice(split[2], Normal.UNKNOWN, Normal.UNKNOWN, Normal.UNKNOWN,
                            Normal.UNKNOWN, split[0], Collections.emptyList()));
                }
            }
        }
        if (tree) {
            return List.of(new AixUsbDevice("USB Controller", Normal.EMPTY, "0000", "0000", Normal.EMPTY, Normal.EMPTY, deviceList));
        }
        return deviceList;
    }

}
