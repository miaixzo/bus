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
 * Represents the Twelve Day Officers (建除十二值神) in Chinese traditional calendar. These officers indicate the
 * auspiciousness of a day for various activities. This class extends {@link Samsara} to manage a cyclical list of these
 * entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Duty extends Samsara {

    /**
     * Array of names for the Twelve Day Officers.
     */
    public static final String[] NAMES = { "建", "除", "满", "平", "定", "执", "破", "危", "成", "收", "开", "闭" };

    /**
     * Constructs a {@code Duty} instance with the specified index.
     *
     * @param index The index of the Duty Officer in the {@link #NAMES} array.
     */
    public Duty(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Duty} instance with the specified name.
     *
     * @param name The name of the Duty Officer.
     */
    public Duty(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Duty} instance from its index.
     *
     * @param index The index of the Duty Officer.
     * @return A new {@code Duty} instance.
     */
    public static Duty fromIndex(int index) {
        return new Duty(index);
    }

    /**
     * Creates a {@code Duty} instance from its name.
     *
     * @param name The name of the Duty Officer.
     * @return A new {@code Duty} instance.
     */
    public static Duty fromName(String name) {
        return new Duty(name);
    }

    /**
     * Gets the next {@code Duty} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Duty} instance.
     */
    public Duty next(int n) {
        return fromIndex(nextIndex(n));
    }

}
