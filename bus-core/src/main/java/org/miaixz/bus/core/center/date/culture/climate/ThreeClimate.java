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
package org.miaixz.bus.core.center.date.culture.climate;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents a "Sanhou" (三候) or Three Climates, which are three five-day periods within a solar term. This class
 * extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ThreeClimate extends Samsara {

    /**
     * Array of names for the Three Climates.
     */
    public static final String[] NAMES = { "初候", "二候", "三候" };

    /**
     * Constructs a {@code ThreeClimate} instance with the specified name.
     *
     * @param name The name of the Climate.
     */
    public ThreeClimate(String name) {
        super(NAMES, name);
    }

    /**
     * Constructs a {@code ThreeClimate} instance with the specified index.
     *
     * @param index The index of the Climate in the {@link #NAMES} array.
     */
    public ThreeClimate(int index) {
        super(NAMES, index);
    }

    /**
     * Creates a {@code ThreeClimate} instance from its name.
     *
     * @param name The name of the Climate.
     * @return A new {@code ThreeClimate} instance.
     */
    public static ThreeClimate fromName(String name) {
        return new ThreeClimate(name);
    }

    /**
     * Creates a {@code ThreeClimate} instance from its index.
     *
     * @param index The index of the Climate.
     * @return A new {@code ThreeClimate} instance.
     */
    public static ThreeClimate fromIndex(int index) {
        return new ThreeClimate(index);
    }

    /**
     * Gets the next {@code ThreeClimate} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code ThreeClimate} instance.
     */
    public ThreeClimate next(int n) {
        return fromIndex(nextIndex(n));
    }

}
