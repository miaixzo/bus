/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.date.culture.sixty;

import org.miaixz.bus.core.center.date.culture.Replenish;

/**
 * Represents a specific day within the period governed by a Hidden Stem (人元司令分野). This class associates a
 * {@link HiddenStem} with a day index.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HiddenStemDay extends Replenish {

    /**
     * Constructs a {@code HiddenStemDay} instance with the specified {@link HiddenStem} and day index.
     *
     * @param hiddenStem The {@link HiddenStem} instance.
     * @param dayIndex   The index of the day within the Hidden Stem's period.
     */
    public HiddenStemDay(HiddenStem hiddenStem, int dayIndex) {
        super(hiddenStem, dayIndex);
    }

    /**
     * Gets the {@link HiddenStem} associated with this Hidden Stem Day.
     *
     * @return The {@link HiddenStem} instance.
     */
    public HiddenStem getHideHeavenStem() {
        return (HiddenStem) tradition;
    }

    /**
     * Gets the name of the Heavenly Stem and its associated element for this Hidden Stem Day.
     *
     * @return The name of the Heavenly Stem and its element.
     */
    @Override
    public String getName() {
        HeavenStem heavenStem = getHideHeavenStem().getHeavenStem();
        return heavenStem.getName() + heavenStem.getElement().getName();
    }

    /**
     * Returns a string representation of this Hidden Stem Day, including its name and day index.
     *
     * @return A string representation of the Hidden Stem Day.
     */
    @Override
    public String toString() {
        return String.format("%s第%d天", getName(), dayIndex + 1);
    }

}
