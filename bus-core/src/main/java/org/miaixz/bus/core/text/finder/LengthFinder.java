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
package org.miaixz.bus.core.text.finder;

import java.io.Serial;

import org.miaixz.bus.core.lang.Assert;

/**
 * A {@link TextFinder} that finds a position at a fixed-length offset from the starting point. This is typically used
 * for segmenting a string into fixed-size chunks.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LengthFinder extends TextFinder {

    @Serial
    private static final long serialVersionUID = 2852236815139L;

    /**
     * The fixed length of the segment.
     */
    private final int length;

    /**
     * Constructs a new {@code LengthFinder}.
     *
     * @param length The fixed length, which must be greater than 0.
     */
    public LengthFinder(final int length) {
        Assert.isTrue(length > 0, "Length must be greater than 0");
        this.length = length;
    }

    /**
     * Calculates the start position based on the fixed length offset.
     *
     * @param from The starting position for the calculation.
     * @return The calculated start position, or -1 if out of bounds.
     */
    @Override
    public int start(final int from) {
        Assert.notNull(this.text, "Text to find must not be null!");
        final int limit = getValidEndIndex();
        final int result;
        if (negative) {
            result = from - length;
            if (result > limit) {
                return result;
            }
        } else {
            result = from + length;
            if (result < limit) {
                return result;
            }
        }
        return -1;
    }

    /**
     * Returns the end position, which is the same as the start position for fixed-length segments.
     *
     * @param start The start position.
     * @return The end position (same as start).
     */
    @Override
    public int end(final int start) {
        return start;
    }

}
