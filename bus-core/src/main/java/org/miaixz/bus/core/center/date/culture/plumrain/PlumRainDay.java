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
package org.miaixz.bus.core.center.date.culture.plumrain;

import org.miaixz.bus.core.center.date.culture.Replenish;

/**
 * Represents a specific day within the "Plum Rain" (梅雨) quarter. This class extends {@link Replenish} to associate a
 * specific day index with a {@link PlumRain} instance.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PlumRainDay extends Replenish {

    /**
     * Constructs a {@code PlumRainDay} instance with the specified {@link PlumRain} and day index.
     *
     * @param plumRain The {@link PlumRain} instance representing the Plum Rain period.
     * @param dayIndex The index of the day within the Plum Rain period.
     */
    public PlumRainDay(PlumRain plumRain, int dayIndex) {
        super(plumRain, dayIndex);
    }

    /**
     * Gets the {@link PlumRain} instance associated with this Plum Rain Day.
     *
     * @return The {@link PlumRain} instance.
     */
    public PlumRain getPlumRain() {
        return (PlumRain) tradition;
    }

    /**
     * Returns a string representation of this Plum Rain Day. If it's the "Entering Plum Rain" period (index 0), it
     * returns the superclass's string representation. Otherwise, it returns the name of the Plum Rain period.
     *
     * @return A string representation of the Plum Rain Day.
     */
    @Override
    public String toString() {
        return getPlumRain().getIndex() == 0 ? super.toString() : tradition.getName();
    }

}
