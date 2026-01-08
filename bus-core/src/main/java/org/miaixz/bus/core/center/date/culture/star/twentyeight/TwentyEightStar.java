/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.date.culture.star.twentyeight;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.Animal;
import org.miaixz.bus.core.center.date.culture.Land;
import org.miaixz.bus.core.center.date.culture.Luck;
import org.miaixz.bus.core.center.date.culture.Winds;
import org.miaixz.bus.core.center.date.culture.star.seven.SevenStar;

/**
 * Represents the Twenty-Eight Mansions (二十八宿), a system of lunar mansions used in Chinese astronomy and astrology. This
 * class extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TwentyEightStar extends Samsara {

    /**
     * Array of names for the Twenty-Eight Mansions.
     */
    public static final String[] NAMES = { "角", "亢", "氐", "房", "心", "尾", "箕", "斗", "牛", "女", "虚", "危", "室", "壁", "奎",
            "娄", "胃", "昴", "毕", "觜", "参", "井", "鬼", "柳", "星", "张", "翼", "轸" };

    /**
     * Constructs a {@code TwentyEightStar} instance with the specified index.
     *
     * @param index The index of the mansion in the {@link #NAMES} array.
     */
    public TwentyEightStar(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code TwentyEightStar} instance with the specified name.
     *
     * @param name The name of the mansion.
     */
    public TwentyEightStar(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code TwentyEightStar} instance from its index.
     *
     * @param index The index of the mansion.
     * @return A new {@code TwentyEightStar} instance.
     */
    public static TwentyEightStar fromIndex(int index) {
        return new TwentyEightStar(index);
    }

    /**
     * Creates a {@code TwentyEightStar} instance from its name.
     *
     * @param name The name of the mansion.
     * @return A new {@code TwentyEightStar} instance.
     */
    public static TwentyEightStar fromName(String name) {
        return new TwentyEightStar(name);
    }

    /**
     * Gets the next {@code TwentyEightStar} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code TwentyEightStar} instance.
     */
    public TwentyEightStar next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Gets the corresponding {@link SevenStar} (七曜) for this Twenty-Eight Mansion.
     *
     * @return The {@link SevenStar} associated with this mansion.
     */
    public SevenStar getSevenStar() {
        return SevenStar.fromIndex(index % 7 + 4);
    }

    /**
     * Gets the corresponding {@link Land} (九野) for this Twenty-Eight Mansion.
     *
     * @return The {@link Land} associated with this mansion.
     */
    public Land getLand() {
        return Land.fromIndex(
                new int[] { 4, 4, 4, 2, 2, 2, 7, 7, 7, 0, 0, 0, 0, 5, 5, 5, 6, 6, 6, 1, 1, 1, 8, 8, 8, 3, 3,
                        3 }[index]);
    }

    /**
     * Gets the corresponding {@link Winds} (宫) for this Twenty-Eight Mansion.
     *
     * @return The {@link Winds} associated with this mansion.
     */
    public Winds getZone() {
        return Winds.fromIndex(index / 7);
    }

    /**
     * Gets the corresponding {@link Animal} (动物) for this Twenty-Eight Mansion.
     *
     * @return The {@link Animal} associated with this mansion.
     */
    public Animal getAnimal() {
        return Animal.fromIndex(index);
    }

    /**
     * Determines the auspiciousness (luck) of the current Twenty-Eight Mansion.
     *
     * @return The {@link Luck} associated with this mansion.
     */
    public Luck getLuck() {
        return Luck.fromIndex(
                new int[] { 0, 1, 1, 0, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 1, 1, 0, 1,
                        0 }[index]);
    }

}
