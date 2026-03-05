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
package org.miaixz.bus.core.center.date.culture.lunar;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents a quarter in the Lunar calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LunarQuarter extends Samsara {

    /**
     * Names of lunar quarters.
     */
    public static final String[] NAMES = { "孟春", "仲春", "季春", "孟夏", "仲夏", "季夏", "孟秋", "仲秋", "季秋", "孟冬", "仲冬", "季冬" };

    /**
     * Constructs a {@code LunarQuarter} with the given index.
     *
     * @param index The index of the quarter.
     */
    public LunarQuarter(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code LunarQuarter} with the given name.
     *
     * @param name The name of the quarter.
     */
    public LunarQuarter(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a LunarQuarter instance from the given index.
     *
     * @param index The index of the quarter.
     * @return A new {@link LunarQuarter} instance.
     */
    public static LunarQuarter fromIndex(int index) {
        return new LunarQuarter(index);
    }

    /**
     * Creates a {@code LunarQuarter} instance from the given name.
     *
     * @param name The name of the quarter.
     * @return A new {@link LunarQuarter} instance.
     */
    public static LunarQuarter fromName(String name) {
        return new LunarQuarter(name);
    }

    /**
     * Gets the next lunar quarter after a specified number of quarters.
     *
     * @param n The number of quarters to add.
     * @return The {@link LunarQuarter} after {@code n} quarters.
     */
    public LunarQuarter next(int n) {
        return fromIndex(nextIndex(n));
    }

}
