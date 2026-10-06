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
package org.miaixz.bus.core.center.date.culture.eightchar.provider.impl;

import org.miaixz.bus.core.center.date.culture.eightchar.ChildLimitInfo;
import org.miaixz.bus.core.center.date.culture.parts.SecondParts;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.center.date.culture.solar.SolarTime;

/**
 * Implementation of Child Limit calculation based on the "Yuan Heng Li Zhen" (元亨利贞) method.
 *
 * @author Kimi Liu
 */
public class China95ChildLimitProvider extends LunarSect2ChildLimitProvider {

    /**
     * Creates a new {@code China95ChildLimitProvider} instance.
     */
    public China95ChildLimitProvider() {
        // No initialization required.
    }

    /**
     * Calculates and returns the Child Limit information based on the "Yuan Heng Li Zhen" method.
     *
     * @param birthTime The Gregorian birth time.
     * @param term      The solar term (节令) relevant to the calculation.
     * @return The {@link ChildLimitInfo} containing details about the Child Limit.
     */
    @Override
    public ChildLimitInfo getInfo(SolarTime birthTime, SolarTerms term) {
        SecondParts t = compute(birthTime, term);
        return next(birthTime, t.getYear(), t.getMonth(), t.getDay(), 0, 0, 0);
    }

}
