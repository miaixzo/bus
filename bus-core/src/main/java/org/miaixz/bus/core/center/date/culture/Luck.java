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

/**
 * Represents the concept of auspiciousness (吉) and inauspiciousness (凶) in Chinese traditional culture. This class
 * extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Luck extends Samsara {

    /**
     * Array of names for Luck, typically "Auspicious" (吉) and "Inauspicious" (凶).
     */
    public static final String[] NAMES = { "吉", "凶" };

    /**
     * Constructs a {@code Luck} instance with the specified index.
     *
     * @param index The index of the Luck in the {@link #NAMES} array.
     */
    public Luck(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Luck} instance with the specified name.
     *
     * @param name The name of the Luck.
     */
    public Luck(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Luck} instance from its index.
     *
     * @param index The index of the Luck.
     * @return A new {@code Luck} instance.
     */
    public static Luck fromIndex(int index) {
        return new Luck(index);
    }

    /**
     * Creates a {@code Luck} instance from its name.
     *
     * @param name The name of the Luck.
     * @return A new {@code Luck} instance.
     */
    public static Luck fromName(String name) {
        return new Luck(name);
    }

    /**
     * Gets the next {@code Luck} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Luck} instance.
     */
    public Luck next(int n) {
        return fromIndex(nextIndex(n));
    }

}
