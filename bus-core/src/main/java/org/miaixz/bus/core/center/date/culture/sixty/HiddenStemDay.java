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
package org.miaixz.bus.core.center.date.culture.sixty;

import org.miaixz.bus.core.center.date.culture.Replenish;

/**
 * Represents a specific day within the period governed by a Hidden Stem (人元司令分野). This class associates a
 * {@link HiddenStem} with a day index.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HiddenStemDay extends Replenish {

    /**
     * Constructs a {@code HiddenStemDay} instance with the specified {@link HiddenStem} and day index.
     *
     * @param hiddenStem The {@link HiddenStem} instance.
     * @param dayIndex   The index of the day within the Hidden Stem's period.
     */
    public HiddenStemDay(HiddenStem hiddenStem, int dayIndex) {
        super(hiddenStem, dayIndex);
    }

    /**
     * Gets the {@link HiddenStem} associated with this Hidden Stem Day.
     *
     * @return The {@link HiddenStem} instance.
     */
    public HiddenStem getHideHeavenStem() {
        return (HiddenStem) tradition;
    }

    /**
     * Gets the name of the Heavenly Stem and its associated element for this Hidden Stem Day.
     *
     * @return The name of the Heavenly Stem and its element.
     */
    @Override
    public String getName() {
        HeavenStem heavenStem = getHideHeavenStem().getHeavenStem();
        return heavenStem.getName() + heavenStem.getElement().getName();
    }

    /**
     * Returns a string representation of this Hidden Stem Day, including its name and day index.
     *
     * @return A string representation of the Hidden Stem Day.
     */
    @Override
    public String toString() {
        return String.format("%s第%d天", getName(), dayIndex + 1);
    }

}
