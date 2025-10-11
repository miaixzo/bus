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

import org.miaixz.bus.core.xyz.StringKit;

/**
 * Default implementation of the {@link Segment} interface. This class represents a segment defined by a beginning and
 * an ending index.
 *
 * @param <T> the numeric type used to represent the indices (e.g., Integer, Long)
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultSegment<T extends Number> implements Segment<T> {

    /**
     * The beginning index of the segment.
     */
    protected T beginIndex;
    /**
     * The ending index of the segment.
     */
    protected T endIndex;

    /**
     * Constructs a new {@code DefaultSegment} with the specified beginning and ending indices.
     *
     * @param beginIndex the beginning index of the segment
     * @param endIndex   the ending index of the segment
     */
    public DefaultSegment(final T beginIndex, final T endIndex) {
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
    }

    /**
     * Retrieves the beginning index of this segment.
     *
     * @return the beginning index
     */
    @Override
    public T getBeginIndex() {
        return this.beginIndex;
    }

    /**
     * Retrieves the ending index of this segment.
     *
     * @return the ending index
     */
    @Override
    public T getEndIndex() {
        return this.endIndex;
    }

    /**
     * Returns a string representation of this segment in the format "[beginIndex, endIndex]".
     *
     * @return a string representation of the segment
     */
    @Override
    public String toString() {
        return StringKit.format("[{}, {}]", beginIndex, endIndex);
    }

}
