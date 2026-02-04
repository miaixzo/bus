/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.range;

import java.lang.reflect.Type;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.MathKit;

/**
 * Represents a segment or a range within a data structure, such as text or collections. This interface defines methods
 * to retrieve the beginning and ending indices of the segment, and to calculate its length.
 *
 * @param <T> the numeric type used to represent the indices (e.g., Integer, Long)
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Segment<T extends Number> {

    /**
     * Retrieves the beginning index of this segment.
     *
     * @return the beginning index
     */
    T getBeginIndex();

    /**
     * Retrieves the ending index of this segment.
     *
     * @return the ending index
     */
    T getEndIndex();

    /**
     * Calculates the length of this segment. The default calculation is the absolute difference between the
     * {@link #getEndIndex()} and {@link #getBeginIndex()}.
     *
     * @return the length of the segment
     * @throws NullPointerException if either the beginning or ending index is {@code null}
     */
    default T length() {
        final T start = Assert.notNull(getBeginIndex(), "Start index must be not null!");
        final T end = Assert.notNull(getEndIndex(), "End index must be not null!");
        return Convert.convert((Type) start.getClass(), MathKit.sub(end, start).abs());
    }

}
