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
package org.miaixz.bus.core.center.date.culture.climate;

import org.miaixz.bus.core.center.date.culture.Replenish;

/**
 * Represents a specific day within a "Hou" (候) or Pentad period, part of the Seventy-Two Pentads (七十二候). This class
 * extends {@link Replenish} to associate a specific day index with a {@link Climate} instance.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ClimateDay extends Replenish {

    /**
     * Constructs a {@code ClimateDay} instance with the specified {@link Climate} and day index.
     *
     * @param climate  The {@link Climate} instance representing the Pentad.
     * @param dayIndex The index of the day within the Pentad.
     */
    public ClimateDay(Climate climate, int dayIndex) {
        super(climate, dayIndex);
    }

    /**
     * Gets the {@link Climate} instance associated with this Climate Day.
     *
     * @return The {@link Climate} instance.
     */
    public Climate getClimate() {
        return (Climate) tradition;
    }

}
