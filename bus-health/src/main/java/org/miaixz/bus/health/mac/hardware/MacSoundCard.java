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
package org.miaixz.bus.health.mac.hardware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sun.jna.platform.mac.IOKit.IOIterator;
import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
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
 */
@Immutable
final class MacSoundCard extends AbstractSoundCard {

    /**
     * The manufacturer name for Apple sound cards.
     */
    private static final String APPLE = "Apple Inc.";

    /**
     * Intel Mac audio-driver property list.
     */
    private static final String APPLE_HDA_PLIST = "/System/Library/Extensions/AppleHDA.kext/Contents/Info.plist";

    /**
     * Apple Silicon embedded-audio driver property list.
     */
    private static final String EMBEDDED_AUDIO_PLIST = "/System/Library/Extensions/AppleEmbeddedAudio.kext/Contents/Info.plist";

    /**
     * Device-tree compatible prefix identifying an audio control chip.
     */
    private static final String AUDIO_CONTROL_PREFIX = "audio-control,";

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
        List<String> hdaPlist = Builder.readFile(APPLE_HDA_PLIST, false);
        if (!hdaPlist.isEmpty()) {
            String version = plistValue(hdaPlist, "com.apple.driver.AppleHDAController");
            String kernelVersion = version.isEmpty() ? "AppleHDAController" : "AppleHDAController " + version;
            return Collections.singletonList(new MacSoundCard(kernelVersion, APPLE, "AppleHDACodec"));
        }
        String version = plistValue(Builder.readFile(EMBEDDED_AUDIO_PLIST, false), "CFBundleShortVersionString");
        String kernelVersion = version.isEmpty() ? "AppleEmbeddedAudio" : "AppleEmbeddedAudio " + version;
        return queryAudioControlChips(kernelVersion);
    }

    /**
     * Lists distinct Apple Silicon audio chips from IOKit's I2C device registry.
     *
     * @param kernelVersion the embedded-audio driver version
     * @return sound cards representing distinct audio chip parts
     */
    private static List<SoundCard> queryAudioControlChips(String kernelVersion) {
        Map<String, String> nameByPart = new LinkedHashMap<>();
        IOIterator iterator = IOKitUtil.getMatchingServices("AppleARMIICDevice");
        if (iterator == null) {
            return Collections.emptyList();
        }
        try {
            IORegistryEntry entry = iterator.next();
            while (entry != null) {
                try {
                    byte[] data = entry.getByteArrayProperty("compatible");
                    String compatible = data == null ? Normal.EMPTY : Parsing.decodeNulTerminated(data, Charset.UTF_8);
                    if (compatible.startsWith(AUDIO_CONTROL_PREFIX)) {
                        String part = compatible.substring(AUDIO_CONTROL_PREFIX.length());
                        String name = Parsing.getStringValueOrUnknown(entry.getName());
                        String known = nameByPart.get(part);
                        if (!part.isEmpty() && (known == null || name.length() < known.length())) {
                            nameByPart.put(part, name);
                        }
                    }
                } finally {
                    entry.release();
                }
                entry = iterator.next();
            }
        } finally {
            iterator.release();
        }
        List<SoundCard> soundCards = new ArrayList<>(nameByPart.size());
        for (Map.Entry<String, String> entry : nameByPart.entrySet()) {
            soundCards.add(new MacSoundCard(kernelVersion, entry.getValue(), entry.getKey()));
        }
        return soundCards;
    }

    /**
     * Reads the string value following a key in an XML property list.
     *
     * @param plist the property-list lines
     * @param key   the key whose value to read
     * @return the value, or an empty string when the key is absent
     */
    static String plistValue(List<String> plist, String key) {
        String marker = "<key>" + key + "</key>";
        for (int index = Normal._0; index < plist.size() - Normal._1; index++) {
            if (plist.get(index).contains(marker)) {
                return Parsing.getTextBetweenStrings(plist.get(index + Normal._1), "<string>", "</string>").trim();
            }
        }
        return Normal.EMPTY;
    }

}
