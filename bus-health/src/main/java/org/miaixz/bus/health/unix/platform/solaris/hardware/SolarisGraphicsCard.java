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
package org.miaixz.bus.health.unix.platform.solaris.hardware;

import org.miaixz.bus.core.annotation.Immutable;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.GraphicsCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractGraphicsCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHardwareAbstractionLayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Graphics Card info obtained from prtconf
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class SolarisGraphicsCard extends AbstractGraphicsCard {

    private static final String PCI_CLASS_DISPLAY = "0003";

    /**
     * Constructor for SolarisGraphicsCard
     *
     * @param name        The name
     * @param deviceId    The device ID
     * @param vendor      The vendor
     * @param versionInfo The version info
     * @param vram        The VRAM
     */
    SolarisGraphicsCard(String name, String deviceId, String vendor, String versionInfo, long vram) {
        super(name, deviceId, vendor, versionInfo, vram);
    }

    /**
     * public method used by {@link AbstractHardwareAbstractionLayer} to access the graphics cards.
     *
     * @return List of {@link SolarisGraphicsCard} objects.
     */
    public static List<GraphicsCard> getGraphicsCards() {
        List<GraphicsCard> cardList = new ArrayList<>();
        // Enumerate all devices and add if required
        List<String> devices = Executor.runNative("prtconf -pv");
        if (devices.isEmpty()) {
            return cardList;
        }
        String name = Normal.EMPTY;
        String vendorId = Normal.EMPTY;
        String productId = Normal.EMPTY;
        String classCode = Normal.EMPTY;
        List<String> versionInfoList = new ArrayList<>();
        for (String line : devices) {
            // Node 0x... identifies start of a new device. Save previous if it's a graphics
            // card
            if (line.contains("Node 0x")) {
                if (PCI_CLASS_DISPLAY.equals(classCode)) {
                    cardList.add(new SolarisGraphicsCard(name.isEmpty() ? Normal.UNKNOWN : name,
                            productId.isEmpty() ? Normal.UNKNOWN : productId,
                            vendorId.isEmpty() ? Normal.UNKNOWN : vendorId,
                            versionInfoList.isEmpty() ? Normal.UNKNOWN : String.join(", ", versionInfoList), 0L));
                }
                // Reset strings
                name = Normal.EMPTY;
                vendorId = Normal.UNKNOWN;
                productId = Normal.UNKNOWN;
                classCode = Normal.EMPTY;
                versionInfoList.clear();
            } else {
                String[] split = line.trim().split(Symbol.COLON, 2);
                if (split.length == 2) {
                    if (split[0].equals("model")) {
                        // This is preferred, always set it
                        name = Parsing.getSingleQuoteStringValue(line);
                    } else if (split[0].equals("name")) {
                        // Name is backup for model if model doesn't exist, so only
                        // put if name blank
                        if (name.isEmpty()) {
                            name = Parsing.getSingleQuoteStringValue(line);
                        }
                    } else if (split[0].equals("vendor-id")) {
                        // Format: vendor-id: 00008086
                        vendorId = "0x" + line.substring(line.length() - 4);
                    } else if (split[0].equals("device-id")) {
                        // Format: device-id: 00002440
                        productId = "0x" + line.substring(line.length() - 4);
                    } else if (split[0].equals("revision-id")) {
                        // Format: revision-id: 00000002
                        versionInfoList.add(line.trim());
                    } else if (split[0].equals("class-code")) {
                        // Format: 00030000
                        // Display class is 0003xx, first 6 bytes of this code
                        classCode = line.substring(line.length() - 8, line.length() - 4);
                    }
                }
            }
        }
        // In case we reached end before saving
        if (PCI_CLASS_DISPLAY.equals(classCode)) {
            cardList.add(new SolarisGraphicsCard(name.isEmpty() ? Normal.UNKNOWN : name,
                    productId.isEmpty() ? Normal.UNKNOWN : productId,
                    vendorId.isEmpty() ? Normal.UNKNOWN : vendorId,
                    versionInfoList.isEmpty() ? Normal.UNKNOWN : String.join(", ", versionInfoList), 0L));
        }
        return cardList;
    }

}
