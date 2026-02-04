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
package org.miaixz.bus.health.mac.hardware;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.SoundCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHardwareAbstractionLayer;
import org.miaixz.bus.health.builtin.hardware.common.AbstractSoundCard;

/**
 * <p>
 * MacSoundCard class.
 * </p>
 * Sound card data obtained via AppleHDA kext.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class MacSoundCard extends AbstractSoundCard {

    /**
     * The manufacturer name for Apple sound cards.
     */
    private static final String APPLE = "Apple Inc.";

    /**
     * Constructor for MacSoundCard.
     *
     * @param kernelVersion The kernel version of the sound card driver.
     * @param name          The name of the sound card.
     * @param codec         The codec used by the sound card.
     */
    MacSoundCard(String kernelVersion, String name, String codec) {
        super(kernelVersion, name, codec);
    }

    /**
     * Public method used by {@link AbstractHardwareAbstractionLayer} to access the sound cards.
     *
     * @return A list of {@link MacSoundCard} objects representing the sound cards.
     */
    public static List<SoundCard> getSoundCards() {
        List<SoundCard> soundCards = new ArrayList<>();

        // /System/Library/Extensions/AppleHDA.kext/Contents/Info.plist

        // ..... snip ....
        // <dict>
        // <key>com.apple.driver.AppleHDAController</key>
        // <string>1.7.2a1</string>

        String manufacturer = APPLE;
        String kernelVersion = "AppleHDAController";
        String codec = "AppleHDACodec";

        boolean version = false;
        String versionMarker = "<key>com.apple.driver.AppleHDAController</key>";

        for (final String checkLine : Builder
                .readFile("/System/Library/Extensions/AppleHDA.kext/Contents/Info.plist")) {
            if (checkLine.contains(versionMarker)) {
                version = true;
                continue;
            }
            if (version) {
                kernelVersion = "AppleHDAController "
                        + Parsing.getTextBetweenStrings(checkLine, "<string>", "</string>");
                version = false;
            }
        }
        soundCards.add(new MacSoundCard(kernelVersion, manufacturer, codec));

        return soundCards;
    }

}
