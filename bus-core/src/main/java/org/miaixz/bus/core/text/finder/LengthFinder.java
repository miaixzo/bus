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
