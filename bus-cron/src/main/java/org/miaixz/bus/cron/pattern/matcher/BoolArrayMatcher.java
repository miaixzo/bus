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
package org.miaixz.bus.cron.pattern.matcher;

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
