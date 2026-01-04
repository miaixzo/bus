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
package org.miaixz.bus.core.center.date.culture;

/**
 * Represents the Twelve Stages of Life (地势/长生十二神), a concept in Chinese metaphysics describing the life cycle of
 * elements. This class extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Terrain extends Samsara {

    /**
     * Array of names for the Twelve Stages of Life.
     */
    public static final String[] NAMES = { "长生", "沐浴", "冠带", "临官", "帝旺", "衰", "病", "死", "墓", "绝", "胎", "养" };

    /**
     * Constructs a {@code Terrain} instance with the specified index.
     *
     * @param index The index of the stage in the {@link #NAMES} array.
     */
    public Terrain(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Terrain} instance with the specified name.
     *
     * @param name The name of the stage.
     */
    public Terrain(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Terrain} instance from its index.
     *
     * @param index The index of the stage.
     * @return A new {@code Terrain} instance.
     */
    public static Terrain fromIndex(int index) {
        return new Terrain(index);
    }

    /**
     * Creates a {@code Terrain} instance from its name.
     *
     * @param name The name of the stage.
     * @return A new {@code Terrain} instance.
     */
    public static Terrain fromName(String name) {
        return new Terrain(name);
    }

    /**
     * Gets the next {@code Terrain} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Terrain} instance.
     */
    public Terrain next(int n) {
        return fromIndex(nextIndex(n));
    }

}
