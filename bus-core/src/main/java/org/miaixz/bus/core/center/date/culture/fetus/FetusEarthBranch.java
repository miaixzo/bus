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
package org.miaixz.bus.core.center.date.culture.fetus;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents the Fetus God associated with Earthly Branches (地支六甲胎神), a concept in Chinese traditional culture related
 * to pregnancy. The names indicate the location where the Fetus God resides based on the Earthly Branch of the day.
 * (Reference: 《地支六甲胎神歌》子午二日碓须忌，丑未厕道莫修移。寅申火炉休要动，卯酉大门修当避。辰戌鸡栖巳亥床，犯着六甲身堕胎。) This class extends {@link Samsara} to manage a
 * cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class FetusEarthBranch extends Samsara {

    /**
     * Array of names for the Fetus God locations based on Earthly Branches.
     */
    public static final String[] NAMES = { "碓", "厕", "炉", "门", "栖", "床" };

    /**
     * Constructs a {@code FetusEarthBranch} instance with the specified index.
     *
     * @param index The index of the Fetus God location in the {@link #NAMES} array.
     */
    public FetusEarthBranch(int index) {
        super(NAMES, index);
    }

    /**
     * Gets the next {@code FetusEarthBranch} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code FetusEarthBranch} instance.
     */
    public FetusEarthBranch next(int n) {
        return new FetusEarthBranch(nextIndex(n));
    }

}
