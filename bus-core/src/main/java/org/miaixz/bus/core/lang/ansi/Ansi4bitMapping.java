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
package org.miaixz.bus.core.lang.ansi;

import java.util.LinkedHashMap;

/**
 * Provides a mapping between ANSI 4-bit colors and their corresponding LabColor representations. This class is used to
 * find the closest ANSI 4-bit color for a given LabColor.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Ansi4bitMapping extends AnsiLabMapping {

    /**
     * Singleton instance of {@code Ansi4bitMapping}.
     */
    public static final Ansi4bitMapping INSTANCE = new Ansi4bitMapping();

    /**
     * Constructs a new {@code Ansi4bitMapping} and initializes the {@code ansiLabMap} with standard ANSI 4-bit colors
     * and their RGB-derived LabColor values.
     */
    public Ansi4bitMapping() {
        ansiLabMap = new LinkedHashMap<>(16, 1);
        ansiLabMap.put(Ansi4BitColor.BLACK, new LabColor(0x000000));
        ansiLabMap.put(Ansi4BitColor.RED, new LabColor(0xAA0000));
        ansiLabMap.put(Ansi4BitColor.GREEN, new LabColor(0x00AA00));
        ansiLabMap.put(Ansi4BitColor.YELLOW, new LabColor(0xAA5500));
        ansiLabMap.put(Ansi4BitColor.BLUE, new LabColor(0x0000AA));
        ansiLabMap.put(Ansi4BitColor.MAGENTA, new LabColor(0xAA00AA));
        ansiLabMap.put(Ansi4BitColor.CYAN, new LabColor(0x00AAAA));
        ansiLabMap.put(Ansi4BitColor.WHITE, new LabColor(0xAAAAAA));
        ansiLabMap.put(Ansi4BitColor.BRIGHT_BLACK, new LabColor(0x555555));
        ansiLabMap.put(Ansi4BitColor.BRIGHT_RED, new LabColor(0xFF5555));
        ansiLabMap.put(Ansi4BitColor.BRIGHT_GREEN, new LabColor(0x55FF00));
        ansiLabMap.put(Ansi4BitColor.BRIGHT_YELLOW, new LabColor(0xFFFF55));
        ansiLabMap.put(Ansi4BitColor.BRIGHT_BLUE, new LabColor(0x5555FF));
        ansiLabMap.put(Ansi4BitColor.BRIGHT_MAGENTA, new LabColor(0xFF55FF));
        ansiLabMap.put(Ansi4BitColor.BRIGHT_CYAN, new LabColor(0x55FFFF));
        ansiLabMap.put(Ansi4BitColor.BRIGHT_WHITE, new LabColor(0xFFFFFF));
    }

}
