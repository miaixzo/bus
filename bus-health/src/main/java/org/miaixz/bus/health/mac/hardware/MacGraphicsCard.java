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
package org.miaixz.bus.health.mac.hardware;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.GraphicsCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractGraphicsCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHardwareAbstractionLayer;

/**
 * Graphics card information obtained by system_profiler SPDisplaysDataType.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class MacGraphicsCard extends AbstractGraphicsCard {

    /**
     * Constructor for MacGraphicsCard.
     *
     * @param name        The name of the graphics card.
     * @param deviceId    The device ID of the graphics card.
     * @param vendor      The vendor of the graphics card.
     * @param versionInfo The version information of the graphics card.
     * @param vram        The VRAM (Video Random Access Memory) of the graphics card in bytes.
     */
    MacGraphicsCard(String name, String deviceId, String vendor, String versionInfo, long vram) {
        super(name, deviceId, vendor, versionInfo, vram);
    }

    /**
     * Public method used by {@link AbstractHardwareAbstractionLayer} to access the graphics cards.
     *
     * @return A list of {@link GraphicsCard} objects.
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
            String[] split = line.trim().split(Symbol.COLON, 2);
            if (split.length == 2) {
                String prefix = split[0].toLowerCase(Locale.ROOT);
                if (prefix.equals("chipset model")) {
                    // Save previous card
                    if (cardNum++ > 0) {
                        cardList.add(
                                new MacGraphicsCard(name, deviceId, vendor,
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
                    vram = Parsing.parseDecimalMemorySizeToBinary(split[1].trim());
                }
            }
        }
        cardList.add(
                new MacGraphicsCard(name, deviceId, vendor,
                        versionInfoList.isEmpty() ? Normal.UNKNOWN : String.join(", ", versionInfoList), vram));
        return cardList;
    }

}
