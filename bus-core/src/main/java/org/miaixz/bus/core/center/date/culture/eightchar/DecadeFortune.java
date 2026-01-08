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
package org.miaixz.bus.core.center.date.culture.eightchar;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.sixty.SixtyCycleYear;

/**
 * Represents a "Decade Fortune" (大运) in Chinese astrology, which is a ten-year period of fortune. This class extends
 * {@link Loops} for cyclical operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DecadeFortune extends Loops {

    /**
     * The Child Limit (童限) associated with this Decade Fortune.
     */
    protected ChildLimit childLimit;

    /**
     * The index of this Decade Fortune within the sequence of fortunes.
     */
    protected int index;

    /**
     * Constructs a {@code DecadeFortune} instance with the specified Child Limit and index.
     *
     * @param childLimit The {@link ChildLimit} instance.
     * @param index      The index of this Decade Fortune.
     */
    public DecadeFortune(ChildLimit childLimit, int index) {
        this.childLimit = childLimit;
        this.index = index;
    }

    /**
     * Creates a {@code DecadeFortune} instance from a Child Limit and an index.
     *
     * @param childLimit The {@link ChildLimit} instance.
     * @param index      The index of this Decade Fortune.
     * @return A new {@code DecadeFortune} instance.
     */
    public static DecadeFortune fromChildLimit(ChildLimit childLimit, int index) {
        return new DecadeFortune(childLimit, index);
    }

    /**
     * Gets the starting age for this Decade Fortune.
     *
     * @return The starting age.
     */
    public int getStartAge() {
        return childLimit.getEndSixtyCycleYear().getYear() - childLimit.getStartSixtyCycleYear().getYear() + 1
                + index * 10;
    }

    /**
     * Gets the ending age for this Decade Fortune.
     *
     * @return The ending age.
     */
    public int getEndAge() {
        return getStartAge() + 9;
    }

    /**
     * Gets the Sixty-Year Cycle (干支) for this Decade Fortune.
     *
     * @return The {@link SixtyCycle} instance.
     */
    public SixtyCycle getSixtyCycle() {
        return childLimit.getEightChar().getMonth().next(childLimit.isForward() ? index + 1 : -index - 1);
    }

    /**
     * Gets the name of this Decade Fortune, which is the name of its associated Sixty-Year Cycle.
     *
     * @return The name of the Decade Fortune.
     */
    public String getName() {
        return getSixtyCycle().getName();
    }

    /**
     * Gets the next {@code DecadeFortune} in the sequence.
     *
     * @param n The number of steps to move forward or backward.
     * @return The next {@code DecadeFortune} instance.
     */
    public DecadeFortune next(int n) {
        return fromChildLimit(childLimit, index + n);
    }

    /**
     * Gets the Sixty-Year Cycle Year (干支年) when this Decade Fortune starts.
     *
     * @return The {@link SixtyCycleYear} instance representing the start year.
     */
    public SixtyCycleYear getStartSixtyCycleYear() {
        return childLimit.getEndSixtyCycleYear().next(index * 10);
    }

    /**
     * Gets the Sixty-Year Cycle Year (干支年) when this Decade Fortune ends.
     *
     * @return The {@link SixtyCycleYear} instance representing the end year.
     */
    public SixtyCycleYear getEndSixtyCycleYear() {
        return getStartSixtyCycleYear().next(9);
    }

    /**
     * Gets the starting Fortune (小运) for this Decade Fortune.
     *
     * @return The {@link Fortune} instance.
     */
    public Fortune getStartFortune() {
        return Fortune.fromChildLimit(childLimit, index * 10);
    }

}
