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
package org.miaixz.bus.core.center.date.culture.parts;

import org.miaixz.bus.core.center.date.culture.Direction;
import org.miaixz.bus.core.center.date.culture.Twenty;
import org.miaixz.bus.core.center.date.culture.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.star.nine.NineStar;

/**
 * Abstract base class for traditional years.
 *
 * @author Kimi Liu
 */
public abstract class AbstractTraditionalYear extends AbstractYear {

    /**
     * Constructs a traditional year.
     *
     * @param year the year value
     */
    public AbstractTraditionalYear(final int year) {
        super(year);
    }

    /**
     * Gets the sixty-cycle year.
     *
     * @return the sixty-cycle year
     */
    public SixtyCycle getSixtyCycle() {
        return SixtyCycle.fromIndex(year - 4);
    }

    /**
     * Gets the twenty-year fortune cycle.
     *
     * @return the twenty-year cycle
     */
    public Twenty getTwenty() {
        return Twenty.fromIndex((int) Math.floor((year - 1864) / 20D));
    }

    /**
     * Gets the nine star.
     *
     * @return the nine star
     */
    public NineStar getNineStar() {
        return NineStar.fromIndex(63 + getTwenty().getSixty().getIndex() * 3 - getSixtyCycle().getIndex());
    }

    /**
     * Gets the Jupiter direction.
     *
     * @return the direction
     */
    public Direction getJupiterDirection() {
        return Direction.fromIndex(
                new int[] { 0, 7, 7, 2, 3, 3, 8, 1, 1, 6, 0, 0 }[getSixtyCycle().getEarthBranch().getIndex()]);
    }

}
