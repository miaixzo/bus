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
package org.miaixz.bus.health.unix.freebsd.hardware;

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
 */
@Immutable
final class FreeBsdGraphicsCard extends AbstractGraphicsCard {

    /**
     * The PCI_CLASS_DISPLAY constant.
     */
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
        return parsePciconf(Executor.runNative("pciconf -lv"));
    }

    /**
     * Parses display-class devices from {@code pciconf -lv} output.
     *
     * @param devices the command output
     * @return the graphics cards
     */
    static List<GraphicsCard> parsePciconf(List<String> devices) {
        List<GraphicsCard> cardList = new ArrayList<>();
        PciDevice device = null;
        for (String line : devices) {
            if (line.contains("class=0x") && !Character.isWhitespace(line.charAt(Normal._0))) {
                addIfDisplay(cardList, device);
                device = new PciDevice();
                String[] split = Pattern.SPACES_PATTERN.split(line);
                for (String s : split) {
                    String[] keyValue = s.split(Symbol.EQUAL, Normal._2);
                    if (keyValue.length < Normal._2) {
                        continue;
                    }
                    switch (keyValue[Normal._0]) {
                        case "class":
                            device.display = keyValue[Normal._1].startsWith(PCI_CLASS_DISPLAY);
                            break;

                        case "chip":
                            if (keyValue[Normal._1].length() >= Normal._10) {
                                device.deviceId = keyValue[Normal._1].substring(Normal._0, Normal._6);
                                device.vendorId = "0x" + keyValue[Normal._1].substring(Normal._6, Normal._10);
                            }
                            break;

                        case "vendor":
                            device.vendorId = keyValue[Normal._1];
                            break;

                        case "device":
                            device.deviceId = keyValue[Normal._1];
                            break;

                        case "rev":
                            device.versionInfo = s;
                            break;

                        default:
                            break;
                    }
                }
            } else if (device != null) {
                String[] split = line.trim().split(Symbol.EQUAL, Normal._2);
                if (split.length == Normal._2) {
                    String key = split[Normal._0].trim();
                    if (key.equals("vendor")) {
                        device.vendorName = Parsing.getSingleQuoteStringValue(line);
                    } else if (key.equals("device")) {
                        device.name = Parsing.getSingleQuoteStringValue(line);
                    }
                }
            }
        }
        addIfDisplay(cardList, device);
        return Collections.unmodifiableList(cardList);
    }

    /** Adds one parsed PCI device when it is a display controller. */
    private static void addIfDisplay(List<GraphicsCard> cards, PciDevice device) {
        if (device != null && device.display) {
            String vendor = device.vendorName.isEmpty() ? device.vendorId
                    : Normal.UNKNOWN.equals(device.vendorId) ? device.vendorName
                            : device.vendorName + " (" + device.vendorId + Symbol.PARENTHESE_RIGHT;
            cards.add(
                    new FreeBsdGraphicsCard(Parsing.getStringValueOrUnknown(device.name), device.deviceId, vendor,
                            device.versionInfo, 0L));
        }
    }

    /**
     * Fields collected for one PCI device.
     *
     * @author Kimi Liu
     */
    private static final class PciDevice {

        /** Whether the device belongs to the display class. */
        private boolean display;
        /** Vendor identifier. */
        private String vendorId = Normal.UNKNOWN;
        /** Device identifier. */
        private String deviceId = Normal.UNKNOWN;
        /** Revision information. */
        private String versionInfo = Normal.UNKNOWN;
        /** Vendor name. */
        private String vendorName = Normal.EMPTY;
        /** Device name. */
        private String name = Normal.EMPTY;

        /**
         * Creates an empty PCI device record.
         */
        private PciDevice() {
        }
    }

}
