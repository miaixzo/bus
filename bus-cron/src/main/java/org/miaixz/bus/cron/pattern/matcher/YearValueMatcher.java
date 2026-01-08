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

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * A matcher for the year field of a cron expression.
 * <p>
 * Considering that year values are typically large, using a boolean array for storage and matching is not suitable.
 * Therefore, this implementation uses a {@link LinkedHashSet} for matching.
 * </p>
 * <p>
 * This class implements the {@link PartMatcher} interface, providing a specific implementation for year matching.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class YearValueMatcher implements PartMatcher {

    /**
     * A collection to store the year values.
     */
    private final LinkedHashSet<Integer> valueList;

    /**
     * Constructs a new YearValueMatcher.
     *
     * @param intValueList A collection of integer values representing the years. Must not be null.
     * @throws IllegalArgumentException if intValueList is null.
     */
    public YearValueMatcher(final Collection<Integer> intValueList) {
        if (intValueList == null) {
            throw new IllegalArgumentException("Year value list cannot be null");
        }
        this.valueList = new LinkedHashSet<>(intValueList);
    }

    /**
     * Tests if the given year is matched by this matcher.
     *
     * @param t The year value to test.
     * @return {@code true} if the given year is in the matcher's value list, {@code false} otherwise.
     * @throws NullPointerException if the argument t is null.
     */
    @Override
    public boolean test(final Integer t) {
        if (t == null) {
            throw new NullPointerException("Year value to test cannot be null");
        }
        return valueList.contains(t);
    }

    /**
     * Gets the next matching year that is greater than or equal to the given value.
     *
     * @param value The current year value to start the search from.
     * @return The next matching year that is greater than or equal to the given value, or -1 if no such year is found.
     */
    @Override
    public int nextAfter(final int value) {
        for (final Integer year : valueList) {
            if (year >= value) {
                return year;
            }
        }
        // If no year greater than or equal to the current value is found, the year is considered invalid,
        // making the entire expression invalid for future matches.
        return -1;
    }

    /**
     * Returns a string representation of this YearValueMatcher.
     *
     * @return A string representation of the matcher.
     */
    @Override
    public String toString() {
        return "YearValueMatcher{" + "valueList=" + valueList + '}';
    }

}
