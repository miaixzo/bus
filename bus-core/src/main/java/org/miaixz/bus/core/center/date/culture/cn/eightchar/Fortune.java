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
package org.miaixz.bus.core.center.date.culture.cn.eightchar;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycle;

/**
 * Represents a "Fortune" (小运) in Chinese astrology, often calculated based on a Child Limit (童限). This class extends
 * {@link Loops} for cyclical operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Fortune extends Loops {

    /**
     * The Child Limit (童限) associated with this Fortune.
     */
    protected ChildLimit childLimit;

    /**
     * The index of this Fortune within the Child Limit period.
     */
    protected int index;

    /**
     * Constructs a {@code Fortune} instance with the specified Child Limit and index.
     *
     * @param childLimit The {@link ChildLimit} instance.
     * @param index      The index of this Fortune.
     */
    public Fortune(ChildLimit childLimit, int index) {
        this.childLimit = childLimit;
        this.index = index;
    }

    /**
     * Creates a {@code Fortune} instance from a Child Limit and an index.
     *
     * @param childLimit The {@link ChildLimit} instance.
     * @param index      The index of this Fortune.
     * @return A new {@code Fortune} instance.
     */
    public static Fortune fromChildLimit(ChildLimit childLimit, int index) {
        return new Fortune(childLimit, index);
    }

    /**
     * Gets the age associated with this Fortune.
     *
     * @return The age.
     */
    public int getAge() {
        return childLimit.getEndTime().getYear() - childLimit.getStartTime().getYear() + 1 + index;
    }

    /**
     * Gets the Sixty-Year Cycle (干支) for this Fortune.
     *
     * @return The {@link SixtyCycle} instance.
     */
    public SixtyCycle getSixtyCycle() {
        int n = getAge();
        return childLimit.getEightChar().getHour().next(childLimit.isForward() ? n : -n);
    }

    /**
     * Gets the name of this Fortune, which is the name of its associated Sixty-Year Cycle.
     *
     * @return The name of the Fortune.
     */
    public String getName() {
        return getSixtyCycle().getName();
    }

    /**
     * Gets the next {@code Fortune} in the sequence.
     *
     * @param n The number of steps to move forward or backward.
     * @return The next {@code Fortune} instance.
     */
    public Fortune next(int n) {
        return fromChildLimit(childLimit, index + n);
    }

}
