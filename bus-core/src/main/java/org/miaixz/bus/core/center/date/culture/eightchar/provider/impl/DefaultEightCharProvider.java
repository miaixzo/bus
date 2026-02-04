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
package org.miaixz.bus.core.center.date.culture.eightchar.provider.impl;

import org.miaixz.bus.core.center.date.culture.eightchar.EightChar;
import org.miaixz.bus.core.center.date.culture.eightchar.provider.EightCharProvider;
import org.miaixz.bus.core.center.date.culture.lunar.LunarHour;

/**
 * Default implementation for calculating the Eight Characters (八字). This implementation considers the day pillar for
 * the late Zi hour (子时) to be the next day.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultEightCharProvider implements EightCharProvider {

    /**
     * Constructs a new DefaultEightCharProvider. Utility class constructor for static access.
     */
    public DefaultEightCharProvider() {
    }

    /**
     * Calculates the Eight Characters (八字) from a Lunar Hour.
     *
     * @param hour The Lunar Hour.
     * @return The {@link EightChar} instance.
     */
    @Override
    public EightChar getEightChar(LunarHour hour) {
        return hour.getSixtyCycleHour().getEightChar();
    }

}
