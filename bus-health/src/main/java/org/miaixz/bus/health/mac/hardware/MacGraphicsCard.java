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
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.GpuStats;
import org.miaixz.bus.health.builtin.hardware.GraphicsCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractGraphicsCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHardwareAbstractionLayer;
import org.miaixz.bus.health.mac.SysctlKit;

/**
 * Graphics card information obtained by system_profiler SPDisplaysDataType.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
final class MacGraphicsCard extends AbstractGraphicsCard {

    private static final boolean IS_APPLE_SILICON = "aarch64".equals(System.getProperty("os.arch"));

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

    @Override
    public GpuStats createStatsSession() {
        return new MacGpuStats(IS_APPLE_SILICON, getName());
    }

    /**
     * public method used by {@link AbstractHardwareAbstractionLayer} to access the graphics cards.
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
                    if (cardNum++ > 0) {
                        cardList.add(
                                new MacGraphicsCard(name, deviceId, vendor,
                                        versionInfoList.isEmpty() ? Normal.UNKNOWN : String.join(", ", versionInfoList),
                                        resolveVram(vram, name)));
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
        if (cardNum > 0) {
            cardList.add(
                    new MacGraphicsCard(name, deviceId, vendor,
                            versionInfoList.isEmpty() ? Normal.UNKNOWN : String.join(", ", versionInfoList),
                            resolveVram(vram, name)));
        }
        return cardList;
    }

    private static long resolveVram(long parsedVram, String chipsetName) {
        if (parsedVram > 0) {
            return parsedVram;
        }
        if (chipsetName.contains("Apple")) {
            return SysctlKit.sysctl("hw.memsize", 0L);
        }
        return parsedVram;
    }

}
