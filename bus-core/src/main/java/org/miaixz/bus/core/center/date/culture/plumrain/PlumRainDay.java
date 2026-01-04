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
package org.miaixz.bus.core.center.date.culture.plumrain;

import org.miaixz.bus.core.center.date.culture.Replenish;

/**
 * Represents a specific day within the "Plum Rain" (梅雨) quarter. This class extends {@link Replenish} to associate a
 * specific day index with a {@link PlumRain} instance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PlumRainDay extends Replenish {

    /**
     * Constructs a {@code PlumRainDay} instance with the specified {@link PlumRain} and day index.
     *
     * @param plumRain The {@link PlumRain} instance representing the Plum Rain period.
     * @param dayIndex The index of the day within the Plum Rain period.
     */
    public PlumRainDay(PlumRain plumRain, int dayIndex) {
        super(plumRain, dayIndex);
    }

    /**
     * Gets the {@link PlumRain} instance associated with this Plum Rain Day.
     *
     * @return The {@link PlumRain} instance.
     */
    public PlumRain getPlumRain() {
        return (PlumRain) tradition;
    }

    /**
     * Returns a string representation of this Plum Rain Day. If it's the "Entering Plum Rain" period (index 0), it
     * returns the superclass's string representation. Otherwise, it returns the name of the Plum Rain period.
     *
     * @return A string representation of the Plum Rain Day.
     */
    @Override
    public String toString() {
        return getPlumRain().getIndex() == 0 ? super.toString() : tradition.getName();
    }

}
