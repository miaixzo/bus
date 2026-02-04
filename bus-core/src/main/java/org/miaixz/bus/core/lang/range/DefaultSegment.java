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
