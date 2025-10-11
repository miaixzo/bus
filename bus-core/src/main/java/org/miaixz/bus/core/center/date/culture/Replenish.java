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

import org.miaixz.bus.core.center.date.Almanac;

/**
 * An abstract class representing a traditional cultural element (like a festival) with an associated index, often used
 * for multi-day events.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class Replenish extends Tradition {

    /**
     * The underlying traditional element (e.g., the festival itself).
     */
    protected Tradition tradition;

    /**
     * The index of the day within the event (0-based).
     */
    protected int dayIndex;

    /**
     * Constructs a new {@code Replenish} instance.
     *
     * @param tradition The underlying traditional element.
     * @param dayIndex  The 0-based index of the day within the event.
     */
    public Replenish(Tradition tradition, int dayIndex) {
        this.tradition = tradition;
        this.dayIndex = dayIndex;
    }

    /**
     * Gets the day index within the event.
     *
     * @return The 0-based day index.
     */
    public int getDayIndex() {
        return dayIndex;
    }

    /**
     * Gets the underlying traditional element.
     *
     * @return The {@link Almanac} instance.
     */
    protected Almanac getTradition() {
        return tradition;
    }

    @Override
    public String toString() {
        return String.format("%s第%d天", tradition, dayIndex + 1);
    }

    /**
     * Gets the name of the underlying traditional element.
     *
     * @return The name of the tradition.
     */
    @Override
    public String getName() {
        return tradition.getName();
    }

}
