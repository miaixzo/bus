/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.health.mac.hardware;

import org.miaixz.bus.core.annotation.Immutable;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.builtin.hardware.AbstractGraphicsCard;
import org.miaixz.bus.health.builtin.hardware.AbstractHardwareAbstractionLayer;
import org.miaixz.bus.health.builtin.hardware.GraphicsCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Graphics card info obtained by system_profiler SPDisplaysDataType.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class MacGraphicsCard extends AbstractGraphicsCard {

    /**
     * Constructor for MacGraphicsCard
     *
     * @param name        The name
     * @param deviceId    The device ID
     * @param vendor      The vendor
     * @param versionInfo The version info
     * @param vram        The VRAM
     */
    MacGraphicsCard(String name, String deviceId, String vendor, String versionInfo, long vram) {
        super(name, deviceId, vendor, versionInfo, vram);
    }

    /**
     * public method used by
     * {@link AbstractHardwareAbstractionLayer} to access the
     * graphics cards.
     *
     * @return List of {@link MacGraphicsCard} objects.
     */
    public static List<GraphicsCard> getGraphicsCards() {
        List<GraphicsCard> cardList = new ArrayList<>();
        List<String> sp = Executor.runNative("system_profiler SPDisplaysDataType");
        String name = Normal.UNKNOWN;
        String deviceId = Normal.UNKNOWN;
        String vendor = Normal.UNKNOWN;
        List<String> versionInfoList = new ArrayList<>();
        long vram = 0;
        int cardNum = 0;
        for (String line : sp) {
            String[] split = line.trim().split(":", 2);
            if (split.length == 2) {
                String prefix = split[0].toLowerCase(Locale.ROOT);
                if (prefix.equals("chipset model")) {
                    // Save previous card
                    if (cardNum++ > 0) {
                        cardList.add(new MacGraphicsCard(name, deviceId, vendor,
                                versionInfoList.isEmpty() ? Normal.UNKNOWN : String.join(", ", versionInfoList),
                                vram));
                        versionInfoList.clear();
                    }
                    name = split[1].trim();
                } else if (prefix.equals("device id")) {
                    deviceId = split[1].trim();
                } else if (prefix.equals("vendor")) {
                    vendor = split[1].trim();
                } else if (prefix.contains("version") || prefix.contains("revision")) {
                    versionInfoList.add(line.trim());
                } else if (prefix.startsWith("vram")) {
                    vram = Builder.parseDecimalMemorySizeToBinary(split[1].trim());
                }
            }
        }
        cardList.add(new MacGraphicsCard(name, deviceId, vendor,
                versionInfoList.isEmpty() ? Normal.UNKNOWN : String.join(", ", versionInfoList), vram));
        return cardList;
    }

}
