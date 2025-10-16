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
