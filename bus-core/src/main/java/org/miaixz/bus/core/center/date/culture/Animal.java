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
package org.miaixz.bus.core.center.date.culture;

import org.miaixz.bus.core.center.date.culture.star.twentyeight.TwentyEightStar;

/**
 * Represents an animal (动物) in Chinese traditional culture, often associated with the Twenty-Eight Mansions. This class
 * extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Animal extends Samsara {

    /**
     * Array of names for the animals.
     */
    public static final String[] NAMES = { "蛟", "龙", "貉", "兔", "狐", "虎", "豹", "獬", "牛", "蝠", "鼠", "燕", "猪", "獝", "狼",
            "狗", "彘", "鸡", "乌", "猴", "猿", "犴", "羊", "獐", "马", "鹿", "蛇", "蚓" };

    /**
     * Constructs an {@code Animal} instance with the specified index.
     *
     * @param index The index of the animal in the {@link #NAMES} array.
     */
    public Animal(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs an {@code Animal} instance with the specified name.
     *
     * @param name The name of the animal.
     */
    public Animal(String name) {
        super(NAMES, name);
    }

    /**
     * Creates an {@code Animal} instance from its index.
     *
     * @param index The index of the animal.
     * @return A new {@code Animal} instance.
     */
    public static Animal fromIndex(int index) {
        return new Animal(index);
    }

    /**
     * Creates an {@code Animal} instance from its name.
     *
     * @param name The name of the animal.
     * @return A new {@code Animal} instance.
     */
    public static Animal fromName(String name) {
        return new Animal(name);
    }

    /**
     * Gets the next {@code Animal} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Animal} instance.
     */
    public Animal next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Gets the corresponding {@link TwentyEightStar} (二十八宿) for this animal.
     *
     * @return The {@link TwentyEightStar} associated with this animal.
     */
    public TwentyEightStar getTwentyEightStar() {
        return TwentyEightStar.fromIndex(index);
    }

}
