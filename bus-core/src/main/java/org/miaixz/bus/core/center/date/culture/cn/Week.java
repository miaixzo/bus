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
package org.miaixz.bus.core.center.date.culture.cn;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.cn.star.seven.SevenStar;

/**
 * Represents the days of the week (星期) in Chinese culture. This class extends {@link Samsara} to manage a cyclical list
 * of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Week extends Samsara {

    /**
     * Array of names for the days of the week, retrieved from the English Week enumeration.
     */
    public static final String[] NAMES = org.miaixz.bus.core.center.date.culture.en.Week.get("name");

    /**
     * Array representing the ordinal number of the week (e.g., "First Week", "Second Week").
     */
    public static final String[] WHICH = { "第一周", "第二周", "第三周", "第四周", "第五周", "第六周" };

    /**
     * Constructs a {@code Week} instance with the specified index.
     *
     * @param index The index of the day of the week in the {@link #NAMES} array.
     */
    public Week(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Week} instance with the specified name.
     *
     * @param name The name of the day of the week.
     */
    public Week(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Week} instance from its index.
     *
     * @param index The index of the day of the week.
     * @return A new {@code Week} instance.
     */
    public static Week fromIndex(int index) {
        return new Week(index);
    }

    /**
     * Creates a {@code Week} instance from its name.
     *
     * @param name The name of the day of the week.
     * @return A new {@code Week} instance.
     */
    public static Week fromName(String name) {
        return new Week(name);
    }

    /**
     * Gets the next {@code Week} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Week} instance.
     */
    public Week next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Gets the corresponding {@link SevenStar} (七曜) for this day of the week.
     *
     * @return The {@link SevenStar} associated with this day.
     */
    public SevenStar getSevenStar() {
        return SevenStar.fromIndex(index);
    }

}
