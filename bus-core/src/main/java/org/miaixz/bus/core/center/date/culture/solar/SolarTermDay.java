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
package org.miaixz.bus.core.center.date.culture.solar;

import org.miaixz.bus.core.center.date.culture.Replenish;

/**
 * Represents a day within a solar term.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarTermDay extends Replenish {

    /**
     * Constructs a {@code SolarTermDay} with the given solar term and day index.
     *
     * @param solarTerm The {@link SolarTerms} this day belongs to.
     * @param dayIndex  The index of the day within the solar term.
     */
    public SolarTermDay(SolarTerms solarTerm, int dayIndex) {
        super(solarTerm, dayIndex);
    }

    /**
     * Gets the solar term this day belongs to.
     *
     * @return The {@link SolarTerms} of this day.
     */
    public SolarTerms getSolarTerm() {
        return (SolarTerms) tradition;
    }

}
