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
package org.miaixz.bus.tempus.pattern.matcher;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.StringKit;

import java.util.Collections;
import java.util.List;

/**
 * A {@link PartMatcher} that uses a boolean array to represent a list of integer values from a cron expression.
 * Matching is performed by checking the corresponding index in the array.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BoolArrayMatcher implements PartMatcher {

    /**
     * The minimum value defined in the cron expression for this part.
     */
    protected final int minValue;
    /**
     * A boolean array where the index represents the value and {@code true} indicates a match.
     */
    protected final boolean[] bValues;

    /**
     * Constructs a new BoolArrayMatcher.
     *
     * @param intValueList The list of integer values to match.
     */
    public BoolArrayMatcher(final List<Integer> intValueList) {
        Assert.isTrue(CollKit.isNotEmpty(intValueList), "Values must be not empty!");
        bValues = new boolean[Collections.max(intValueList) + 1];
        int min = Integer.MAX_VALUE;
        for (final Integer value : intValueList) {
            min = Math.min(min, value);
            bValues[value] = true;
        }
        this.minValue = min;
    }

    @Override
    public boolean test(final Integer value) {
        final boolean[] bValues = this.bValues;
        if (null == value || value >= bValues.length) {
            return false;
        }
        return bValues[value];
    }

    @Override
    public int nextAfter(int value) {
        final int minValue = this.minValue;
        if (value > minValue) {
            final boolean[] bValues = this.bValues;
            while (value < bValues.length) {
                if (bValues[value]) {
                    return value;
                }
                value++;
            }
        }

        // Returns the minimum value in two cases:
        // 1. The given value is less than the minimum value, so the next match is the minimum value.
        // 2. The given value is greater than the maximum value in the array, so the next match is the minimum value of
        // the next cycle.
        return minValue;
    }

    /**
     * Gets the minimum value defined in the expression.
     *
     * @return The minimum value.
     */
    public int getMinValue() {
        return this.minValue;
    }

    @Override
    public String toString() {
        return StringKit.format("Matcher:{}", new Object[] { this.bValues });
    }

}
