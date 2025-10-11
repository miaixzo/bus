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
package org.miaixz.bus.core.center.date.culture.lunar;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents a season in the Lunar calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LunarSeason extends Samsara {

    /**
     * Names of lunar seasons.
     */
    public static final String[] NAMES = { "孟春", "仲春", "季春", "孟夏", "仲夏", "季夏", "孟秋", "仲秋", "季秋", "孟冬", "仲冬", "季冬" };

    /**
     * Constructs a {@code LunarSeason} with the given index.
     *
     * @param index The index of the season.
     */
    public LunarSeason(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code LunarSeason} with the given name.
     *
     * @param name The name of the season.
     */
    public LunarSeason(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code LunarSeason} instance from the given index.
     *
     * @param index The index of the season.
     * @return A new {@link LunarSeason} instance.
     */
    public static LunarSeason fromIndex(int index) {
        return new LunarSeason(index);
    }

    /**
     * Creates a {@code LunarSeason} instance from the given name.
     *
     * @param name The name of the season.
     * @return A new {@link LunarSeason} instance.
     */
    public static LunarSeason fromName(String name) {
        return new LunarSeason(name);
    }

    /**
     * Gets the next lunar season after a specified number of seasons.
     *
     * @param n The number of seasons to add.
     * @return The {@link LunarSeason} after {@code n} seasons.
     */
    public LunarSeason next(int n) {
        return fromIndex(nextIndex(n));
    }

}
