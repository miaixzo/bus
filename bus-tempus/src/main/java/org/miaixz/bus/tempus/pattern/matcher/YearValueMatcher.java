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
