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
package org.miaixz.bus.health.windows.hardware;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.builtin.hardware.SoundCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractSoundCard;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinReg;

/**
 * Sound Card data obtained from registry
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
final class WindowsSoundCard extends AbstractSoundCard {

    private static final String REGISTRY_SOUNDCARDS = "SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e96c-e325-11ce-bfc1-08002be10318}\\";

    /**
     * Constructor for WindowsSoundCard.
     *
     * @param kernelVersion The version
     * @param name          The name
     * @param codec         The codec
     */
    WindowsSoundCard(String kernelVersion, String name, String codec) {
        super(kernelVersion, name, codec);
    }

    /**
     * Returns Windows audio device driver information, which represents the closest proxy we have to sound cards. NOTE
     * : The reason why the codec name is same as the card name is because windows does not provide the name of the
     * codec chip but sometimes the name of the card returned is infact the name of the codec chip also. Example :
     * Realtek ALC887 HD Audio Device
     *
     * @return List of sound cards
     */
    public static List<SoundCard> getSoundCards() {
        List<SoundCard> soundCards = new ArrayList<>();
        String[] keys = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, REGISTRY_SOUNDCARDS);
        for (String key : keys) {
            String fullKey = REGISTRY_SOUNDCARDS + key;
            try {
                if (Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, fullKey, "Driver")) {
                    soundCards.add(
                            new WindowsSoundCard(
                                    Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, fullKey, "Driver")
                                            + Symbol.SPACE + Advapi32Util.registryGetStringValue(
                                                    WinReg.HKEY_LOCAL_MACHINE,
                                                    fullKey,
                                                    "DriverVersion"),
                                    Advapi32Util
                                            .registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, fullKey, "ProviderName")
                                            + Symbol.SPACE
                                            + Advapi32Util.registryGetStringValue(
                                                    WinReg.HKEY_LOCAL_MACHINE,
                                                    fullKey,
                                                    "DriverDesc"),
                                    Advapi32Util
                                            .registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, fullKey, "DriverDesc")));
                }
            } catch (Win32Exception e) {
                if (e.getErrorCode() != WinError.ERROR_ACCESS_DENIED) {
                    // Ignore access denied errors, re-throw others
                    throw e;
                }
            }
        }
        return soundCards;
    }

}
