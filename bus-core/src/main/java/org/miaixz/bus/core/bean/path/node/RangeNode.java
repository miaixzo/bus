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
 * @since Java 21+
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
