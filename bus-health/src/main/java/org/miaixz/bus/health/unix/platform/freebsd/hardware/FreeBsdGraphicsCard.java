/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.unix.platform.freebsd.hardware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.GraphicsCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractGraphicsCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHardwareAbstractionLayer;

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
                    cardList.add(
                            new FreeBsdGraphicsCard(name.isEmpty() ? Normal.UNKNOWN : name,
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
                                + (vendorId.equals(Normal.UNKNOWN) ? Normal.EMPTY
                                        : " (" + vendorId + Symbol.PARENTHESE_RIGHT);
                    } else if (key.equals("device")) {
                        name = Parsing.getSingleQuoteStringValue(line);
                    }
                }
            }
        }
        // In case we reached end before saving
        if (PCI_CLASS_DISPLAY.equals(classCode)) {
            cardList.add(
                    new FreeBsdGraphicsCard(name.isEmpty() ? Normal.UNKNOWN : name,
                            productId.isEmpty() ? Normal.UNKNOWN : productId,
                            vendorId.isEmpty() ? Normal.UNKNOWN : vendorId,
                            versionInfo.isEmpty() ? Normal.UNKNOWN : versionInfo, 0L));
        }
        return cardList;
    }

}
