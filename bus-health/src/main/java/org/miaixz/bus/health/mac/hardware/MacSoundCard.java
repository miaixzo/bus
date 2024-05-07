/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI Team and other contributors.          *
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
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.SoundCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHardwareAbstractionLayer;
import org.miaixz.bus.health.builtin.hardware.common.AbstractSoundCard;

import java.util.ArrayList;
import java.util.List;

/**
 * Sound card data obtained via AppleHDA kext
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class MacSoundCard extends AbstractSoundCard {

    private static final String APPLE = "Apple Inc.";

    /**
     * Constructor for MacSoundCard.
     *
     * @param kernelVersion The version
     * @param name          The name
     * @param codec         The codec
     */
    MacSoundCard(String kernelVersion, String name, String codec) {
        super(kernelVersion, name, codec);
    }

    /**
     * public method used by {@link AbstractHardwareAbstractionLayer} to access the sound cards.
     *
     * @return List of {@link MacSoundCard} objects.
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
