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
package org.miaixz.bus.core.center.date.culture.star.seven;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.Week;

/**
 * Represents the Seven Luminaries (七曜), also known as Qizheng (七政) or Qiwei (七纬), which correspond to the days of the
 * week. This class extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SevenStar extends Samsara {

    /**
     * Array of names for the Seven Luminaries.
     */
    public static final String[] NAMES = { "日", "月", "火", "水", "木", "金", "土" };

    /**
     * Constructs a {@code SevenStar} instance with the specified index.
     *
     * @param index The index of the luminary in the {@link #NAMES} array.
     */
    public SevenStar(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code SevenStar} instance with the specified name.
     *
     * @param name The name of the luminary.
     */
    public SevenStar(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code SevenStar} instance from its index.
     *
     * @param index The index of the luminary.
     * @return A new {@code SevenStar} instance.
     */
    public static SevenStar fromIndex(int index) {
        return new SevenStar(index);
    }

    /**
     * Creates a {@code SevenStar} instance from its name.
     *
     * @param name The name of the luminary.
     * @return A new {@code SevenStar} instance.
     */
    public static SevenStar fromName(String name) {
        return new SevenStar(name);
    }

    /**
     * Gets the next {@code SevenStar} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code SevenStar} instance.
     */
    public SevenStar next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Gets the corresponding {@link Week} (星期) for this Seven Luminary.
     *
     * @return The {@link Week} associated with this Seven Luminary.
     */
    public Week getWeek() {
        return Week.fromIndex(index);
    }

}
