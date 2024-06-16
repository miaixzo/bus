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
package org.miaixz.bus.health.unix.platform.freebsd.hardware;

import org.miaixz.bus.core.annotation.Immutable;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.GraphicsCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractGraphicsCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHardwareAbstractionLayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Graphics Card info obtained from pciconf
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class FreeBsdGraphicsCard extends AbstractGraphicsCard {

    private static final String PCI_CLASS_DISPLAY = "0x03";

    /**
     * Constructor for FreeBsdGraphicsCard
     *
     * @param name        The name
     * @param deviceId    The device ID
     * @param vendor      The vendor
     * @param versionInfo The version info
     * @param vram        The VRAM
     */
    FreeBsdGraphicsCard(String name, String deviceId, String vendor, String versionInfo, long vram) {
        super(name, deviceId, vendor, versionInfo, vram);
    }

    /**
     * public method used by {@link AbstractHardwareAbstractionLayer} to access the graphics cards.
     *
     * @return List of {@link FreeBsdGraphicsCard} objects.
     */
    public static List<GraphicsCard> getGraphicsCards() {
        List<GraphicsCard> cardList = new ArrayList<>();
        // Enumerate all devices and add if required
        List<String> devices = Executor.runNative("pciconf -lv");
        if (devices.isEmpty()) {
            return Collections.emptyList();
        }
        String name = Normal.UNKNOWN;
        String vendorId = Normal.UNKNOWN;
        String productId = Normal.UNKNOWN;
        String classCode = Normal.EMPTY;
        String versionInfo = Normal.UNKNOWN;
        for (String line : devices) {
            if (line.contains("class=0x")) {
                // Identifies start of a new device. Save previous if it's a graphics card
                if (PCI_CLASS_DISPLAY.equals(classCode)) {
                    cardList.add(new FreeBsdGraphicsCard(name.isEmpty() ? Normal.UNKNOWN : name,
                            productId.isEmpty() ? Normal.UNKNOWN : productId,
                            vendorId.isEmpty() ? Normal.UNKNOWN : vendorId,
                            versionInfo.isEmpty() ? Normal.UNKNOWN : versionInfo, 0L));
                }
                // Parse this line
                String[] split = Pattern.SPACES_PATTERN.split(line);
                for (String s : split) {
                    String[] keyVal = s.split(Symbol.EQUAL);
                    if (keyVal.length > 1) {
                        if (keyVal[0].equals("class") && keyVal[1].length() >= 4) {
                            // class=0x030000
                            classCode = keyVal[1].substring(0, 4);
                        } else if (keyVal[0].equals("chip") && keyVal[1].length() >= 10) {
                            // chip=0x3ea08086
                            productId = keyVal[1].substring(0, 6);
                            vendorId = "0x" + keyVal[1].substring(6, 10);
                        } else if (keyVal[0].contains("rev")) {
                            // rev=0x00
                            versionInfo = s;
                        }
                    }
                }
                // Reset name
                name = Normal.UNKNOWN;
            } else {
                String[] split = line.trim().split(Symbol.EQUAL, 2);
                if (split.length == 2) {
                    String key = split[0].trim();
                    if (key.equals("vendor")) {
                        vendorId = Parsing.getSingleQuoteStringValue(line)
                                + (vendorId.equals(Normal.UNKNOWN) ? Normal.EMPTY : " (" + vendorId + Symbol.PARENTHESE_RIGHT);
                    } else if (key.equals("device")) {
                        name = Parsing.getSingleQuoteStringValue(line);
                    }
                }
            }
        }
        // In case we reached end before saving
        if (PCI_CLASS_DISPLAY.equals(classCode)) {
            cardList.add(new FreeBsdGraphicsCard(name.isEmpty() ? Normal.UNKNOWN : name,
                    productId.isEmpty() ? Normal.UNKNOWN : productId,
                    vendorId.isEmpty() ? Normal.UNKNOWN : vendorId,
                    versionInfo.isEmpty() ? Normal.UNKNOWN : versionInfo, 0L));
        }
        return cardList;
    }

}
