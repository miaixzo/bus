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
package org.miaixz.bus.health.linux.hardware;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.builtin.hardware.SoundCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHardwareAbstractionLayer;
import org.miaixz.bus.health.builtin.hardware.common.AbstractSoundCard;
import org.miaixz.bus.health.linux.ProcPath;
import org.miaixz.bus.logger.Logger;

/**
 * Sound card data obtained via /proc/asound directory
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class LinuxSoundCard extends AbstractSoundCard {

    private static final String CARD_FOLDER = "card";
    private static final String CARDS_FILE = "cards";
    private static final String ID_FILE = "id";

    /**
     * Constructor for LinuxSoundCard.
     *
     * @param kernelVersion The version
     * @param name          The name
     * @param codec         The codec
     */
    LinuxSoundCard(String kernelVersion, String name, String codec) {
        super(kernelVersion, name, codec);
    }

    /**
     * Method to find all the card folders contained in the <b>asound</b> folder denoting the cards currently contained
     * in our machine.
     *
     * @return : A list of files starting with 'card'
     */
    private static List<File> getCardFolders() {
        File cardsDirectory = new File(ProcPath.ASOUND);
        List<File> cardFolders = new ArrayList<>();
        File[] allContents = cardsDirectory.listFiles();
        if (allContents != null) {
            for (File card : allContents) {
                if (card.getName().startsWith(CARD_FOLDER) && card.isDirectory()) {
                    cardFolders.add(card);
                }
            }
        } else {
            Logger.warn("No Audio Cards Found");
        }
        return cardFolders;
    }

    /**
     * Reads the 'version' file in the asound folder that contains the complete name of the ALSA driver. Reads all the
     * lines of the file and retrieves the first line.
     *
     * @return The complete name of the ALSA driver currently residing in our machine
     */
    private static String getSoundCardVersion() {
        String driverVersion = Builder.getStringFromFile(ProcPath.ASOUND + "version");
        return driverVersion.isEmpty() ? "not available" : driverVersion;
    }

    /**
     * Retrieves the codec of the sound card contained in the <b>codec</b> file. The name of the codec is always the
     * first line of that file. <b>Working</b> This converts the codec file into key value pairs using the
     * {@link Builder} class and then returns the value of the <b>Codec</b> key.
     *
     * @param cardDir The sound card directory
     * @return The name of the codec
     */
    private static String getCardCodec(File cardDir) {
        String cardCodec = Normal.EMPTY;
        File[] cardFiles = cardDir.listFiles();
        if (cardFiles != null) {
            for (File file : cardFiles) {
                if (file.getName().startsWith("codec")) {
                    if (!file.isDirectory()) {
                        cardCodec = Builder.getKeyValueMapFromFile(file.getPath(), Symbol.COLON).get("Codec");
                    } else {
                        // on various centos environments, this is a subdirectory
                        // each file is usually named something like
                        // codec#0-0
                        // example : ac97#0-0
                        File[] codecs = file.listFiles();
                        if (codecs != null) {
                            for (File codec : codecs) {
                                if (!codec.isDirectory() && codec.getName().contains(Symbol.HASH)) {
                                    cardCodec = codec.getName().substring(0, codec.getName().indexOf(Symbol.C_HASH));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return cardCodec;
    }

    /**
     * Retrieves the name of the sound card by :
     * <ol>
     * <li>Reading the <b>id</b> file and comparing each id with the card id present in the <b>cards</b> file</li>
     * <li>If the id and the card name matches , then it assigns that name to {@literal cardName}</li>
     * </ol>
     *
     * @param file The sound card File.
     * @return The name of the sound card.
     */
    private static String getCardName(File file) {
        String cardName = "Not Found..";
        Map<String, String> cardNamePairs = Builder
                .getKeyValueMapFromFile(ProcPath.ASOUND + "/" + CARDS_FILE, Symbol.COLON);
        String cardId = Builder.getStringFromFile(file.getPath() + "/" + ID_FILE);
        for (Map.Entry<String, String> entry : cardNamePairs.entrySet()) {
            if (entry.getKey().contains(cardId)) {
                cardName = entry.getValue();
                return cardName;
            }
        }
        return cardName;
    }

    /**
     * public method used by {@link AbstractHardwareAbstractionLayer} to access the sound cards.
     *
     * @return List of {@link LinuxSoundCard} objects.
     */
    public static List<SoundCard> getSoundCards() {
        List<SoundCard> soundCards = new ArrayList<>();
        for (File cardFile : getCardFolders()) {
            soundCards.add(new LinuxSoundCard(getSoundCardVersion(), getCardName(cardFile), getCardCodec(cardFile)));
        }
        return soundCards;
    }

}
