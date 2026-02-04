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
package org.miaixz.bus.health.unix.platform.openbsd.hardware;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.builtin.hardware.SoundCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractSoundCard;

/**
 * OpenBSD soundcard.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class OpenBsdSoundCard extends AbstractSoundCard {

    private static final Pattern AUDIO_AT = Pattern.compile("audio\\d+ at (.+)");
    private static final Pattern PCI_AT = Pattern
            .compile("(.+) at pci\\d+ dev \\d+ function \\d+ \"(.*)\" (rev .+):.*");

    /**
     * Constructor for OpenBsdSoundCard.
     *
     * @param kernelVersion The version
     * @param name          The name
     * @param codec         The codec
     */
    OpenBsdSoundCard(String kernelVersion, String name, String codec) {
        super(kernelVersion, name, codec);
    }

    /**
     * <p>
     * getSoundCards.
     * </p>
     *
     * @return a {@link java.util.List} object.
     */
    public static List<SoundCard> getSoundCards() {
        List<String> dmesg = Executor.runNative("dmesg");
        // Iterate dmesg once to collect location of audioN
        Set<String> names = new HashSet<>();
        for (String line : dmesg) {
            Matcher m = AUDIO_AT.matcher(line);
            if (m.matches()) {
                names.add(m.group(1));
            }
        }
        // Iterate again and add cards when they match the name
        Map<String, String> nameMap = new HashMap<>();
        Map<String, String> codecMap = new HashMap<>();
        Map<String, String> versionMap = new HashMap<>();
        String key = Normal.EMPTY;
        for (String line : dmesg) {
            Matcher m = PCI_AT.matcher(line);
            if (m.matches() && names.contains(m.group(1))) {
                key = m.group(1);
                nameMap.put(key, m.group(2));
                versionMap.put(key, m.group(3));
            } else if (!key.isEmpty()) {
                // Codec is on the next line
                int idx = line.indexOf("codec");
                if (idx >= 0) {
                    idx = line.indexOf(Symbol.C_COLON);
                    codecMap.put(key, line.substring(idx + 1).trim());
                }
                // clear key so we don't keep looking
                key = Normal.EMPTY;
            }
        }
        List<SoundCard> soundCards = new ArrayList<>();
        for (Entry<String, String> entry : nameMap.entrySet()) {
            soundCards.add(
                    new OpenBsdSoundCard(versionMap.get(entry.getKey()), entry.getValue(),
                            codecMap.get(entry.getKey())));
        }
        return soundCards;
    }

}
