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
 * Represents the Four Auspicious Beasts (神兽) in Chinese mythology, associated with cardinal directions. This class
 * extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Beast extends Samsara {

    /**
     * Array of names for the Four Auspicious Beasts.
     */
    public static final String[] NAMES = { "青龙", "玄武", "白虎", "朱雀" };

    /**
     * Constructs a {@code Beast} instance with the specified index.
     *
     * @param index The index of the Beast in the {@link #NAMES} array.
     */
    public Beast(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Beast} instance with the specified name.
     *
     * @param name The name of the Beast.
     */
    public Beast(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Beast} instance from its index.
     *
     * @param index The index of the Beast.
     * @return A new {@code Beast} instance.
     */
    public static Beast fromIndex(int index) {
        return new Beast(index);
    }

    /**
     * Creates a {@code Beast} instance from its name.
     *
     * @param name The name of the Beast.
     * @return A new {@code Beast} instance.
     */
    public static Beast fromName(String name) {
        return new Beast(name);
    }

    /**
     * Gets the next {@code Beast} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Beast} instance.
     */
    public Beast next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Gets the corresponding {@link Winds} (宫) for this Beast.
     *
     * @return The {@link Winds} associated with this Beast.
     */
    public Winds getZone() {
        return Winds.fromIndex(index);
    }

}
