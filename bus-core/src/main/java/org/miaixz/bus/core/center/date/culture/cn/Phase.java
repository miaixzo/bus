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

/**
 * Represents the phases of the moon (月相) in Chinese traditional culture. This class extends {@link Samsara} to manage a
 * cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Phase extends Samsara {

    /**
     * Array of names for the moon phases.
     */
    public static final String[] NAMES = { "朔月", "既朔月", "蛾眉新月", "蛾眉新月", "蛾眉月", "夕月", "上弦月", "上弦月", "九夜月", "宵月", "宵月",
            "宵月", "渐盈凸月", "小望月", "望月", "既望月", "立待月", "居待月", "寝待月", "更待月", "渐亏凸月", "下弦月", "下弦月", "有明月", "有明月", "蛾眉残月",
            "蛾眉残月", "残月", "晓月", "晦月" };

    /**
     * Constructs a {@code Phase} instance with the specified index.
     *
     * @param index The index of the moon phase in the {@link #NAMES} array.
     */
    public Phase(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Phase} instance with the specified name.
     *
     * @param name The name of the moon phase.
     */
    public Phase(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Phase} instance from its index.
     *
     * @param index The index of the moon phase.
     * @return A new {@code Phase} instance.
     */
    public static Phase fromIndex(int index) {
        return new Phase(index);
    }

    /**
     * Creates a {@code Phase} instance from its name.
     *
     * @param name The name of the moon phase.
     * @return A new {@code Phase} instance.
     */
    public static Phase fromName(String name) {
        return new Phase(name);
    }

    /**
     * Gets the next {@code Phase} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Phase} instance.
     */
    public Phase next(int n) {
        return fromIndex(nextIndex(n));
    }

}
