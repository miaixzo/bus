/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.date.culture.cn.star.seven;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.cn.Week;

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
