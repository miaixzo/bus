/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.date.culture.eightchar;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.sixty.SixtyCycle;

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
