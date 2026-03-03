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
package org.miaixz.bus.health.unix.platform.aix.hardware;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.GraphicsCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractGraphicsCard;

/**
 * Graphics Card info obtained from lscfg
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class AixGraphicsCard extends AbstractGraphicsCard {

    /**
     * Constructor for AixGraphicsCard
     *
     * @param name        The name
     * @param deviceId    The device ID
     * @param vendor      The vendor
     * @param versionInfo The version info
     * @param vram        The VRAM
     */
    AixGraphicsCard(String name, String deviceId, String vendor, String versionInfo, long vram) {
        super(name, deviceId, vendor, versionInfo, vram);
    }

    /**
     * Gets graphics cards
     *
     * @param lscfg A memoized lscfg list
     * @return List of graphics cards
     */
    public static List<GraphicsCard> getGraphicsCards(Supplier<List<String>> lscfg) {
        List<GraphicsCard> cardList = new ArrayList<>();
        boolean display = false;
        String name = null;
        String vendor = null;
        List<String> versionInfo = new ArrayList<>();
        for (String line : lscfg.get()) {
            String s = line.trim();
            if (s.startsWith("Name:") && s.contains("display")) {
                display = true;
            } else if (display && s.toLowerCase(Locale.ROOT).contains("graphics")) {
                name = s;
            } else if (display && name != null) {
                if (s.startsWith("Manufacture ID")) {
                    vendor = Parsing.removeLeadingDots(s.substring(14));
                } else if (s.contains("Level")) {
                    versionInfo.add(s.replaceAll("\\.\\.+", Symbol.EQUAL));
                } else if (s.startsWith("Hardware Location Code")) {
                    cardList.add(
                            new AixGraphicsCard(name, Normal.UNKNOWN,
                                    StringKit.isBlank(vendor) ? Normal.UNKNOWN : vendor,
                                    versionInfo.isEmpty() ? Normal.UNKNOWN : String.join(Symbol.COMMA, versionInfo),
                                    0L));
                    display = false;
                }
            }
        }
        return cardList;
    }

}
