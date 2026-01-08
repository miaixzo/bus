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
package org.miaixz.bus.core.bean.path.node;

import java.util.List;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Represents a range node in a Bean path expression, following the pattern {@code [start:end:step]}. This node is used
 * to specify a sub-range of elements within a collection or array.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RangeNode implements Node {

    /**
     * The starting index of the range (inclusive).
     */
    private final int start;
    /**
     * The ending index of the range (exclusive).
     */
    private final int end;
    /**
     * The step value for iterating through the range.
     */
    private final int step;

    /**
     * Constructs a {@code RangeNode} by parsing the given expression string. The expression should be in the format
     * {@code start:end} or {@code start:end:step}.
     *
     * @param expression The expression string (e.g., "0:5" or "0:10:2").
     */
    public RangeNode(final String expression) {
        final List<String> parts = CharsBacker.splitTrim(expression, Symbol.COLON);
        this.start = Integer.parseInt(parts.get(0));
        this.end = Integer.parseInt(parts.get(1));
        int step = 1;
        if (3 == parts.size()) {
            step = Integer.parseInt(parts.get(2));
        }
        this.step = step;
    }

    /**
     * Retrieves the starting index of the range.
     *
     * @return The starting index (inclusive).
     */
    public int getStart() {
        return start;
    }

    /**
     * Retrieves the ending index of the range.
     *
     * @return The ending index (exclusive).
     */
    public int getEnd() {
        return end;
    }

    /**
     * Retrieves the step value for iterating through the range.
     *
     * @return The step value.
     */
    public int getStep() {
        return step;
    }

    /**
     * Returns a string representation of this range node in the format {@code [start:end:step]}.
     *
     * @return A string representation of the object.
     */
    @Override
    public String toString() {
        return StringKit.format("[{}:{}:{}]", this.start, this.end, this.step);
    }

}
