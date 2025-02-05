/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.unix.platform.freebsd.hardware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.SoundCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractSoundCard;

/**
 * FreeBSD soundcard.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class FreeBsdSoundCard extends AbstractSoundCard {

    private static final String LSHAL = "lshal";

    /**
     * Constructor for FreeBsdSoundCard.
     *
     * @param kernelVersion The version
     * @param name          The name
     * @param codec         The codec
     */
    FreeBsdSoundCard(String kernelVersion, String name, String codec) {
        super(kernelVersion, name, codec);
    }

    /**
     * getSoundCards.
     *
     * @return a {@link java.util.List} object.
     */
    public static List<SoundCard> getSoundCards() {
        Map<String, String> vendorMap = new HashMap<>();
        Map<String, String> productMap = new HashMap<>();
        vendorMap.clear();
        productMap.clear();
        List<String> sounds = new ArrayList<>();
        String key = Normal.EMPTY;
        for (String line : Executor.runNative(LSHAL)) {
            line = line.trim();
            if (line.startsWith("udi =")) {
                // we have the key.
                key = Parsing.getSingleQuoteStringValue(line);
            } else if (!key.isEmpty() && !line.isEmpty()) {
                if (line.contains("freebsd.driver =") && "pcm".equals(Parsing.getSingleQuoteStringValue(line))) {
                    sounds.add(key);
                } else if (line.contains("info.product")) {
                    productMap.put(key, Parsing.getStringBetween(line, '\''));
                } else if (line.contains("info.vendor")) {
                    vendorMap.put(key, Parsing.getStringBetween(line, '\''));
                }
            }
        }
        List<SoundCard> soundCards = new ArrayList<>();
        for (String s : sounds) {
            soundCards.add(new FreeBsdSoundCard(productMap.get(s), vendorMap.get(s) + Symbol.SPACE + productMap.get(s),
                    productMap.get(s)));
        }
        return soundCards;
    }

}
