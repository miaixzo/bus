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
package org.miaixz.bus.core.text.finder;

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.lang.Assert;

/**
 * Abstract class for text searching.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class TextFinder implements Finder, Serializable {

    @Serial
    private static final long serialVersionUID = 2852237708637L;

    /**
     * The text to be searched.
     */
    protected CharSequence text;
    /**
     * The end position for the search.
     */
    protected int endIndex = -1;
    /**
     * Flag indicating whether to search in reverse (from end to start).
     */
    protected boolean negative;

    /**
     * Sets the text to be searched.
     *
     * @param text The text to search within.
     * @return This TextFinder instance.
     */
    public TextFinder setText(final CharSequence text) {
        this.text = Assert.notNull(text, "Text must be not null!");
        return this;
    }

    /**
     * Sets the end position for the search. If searching forwards, the end position is at most {@code text.length()}.
     * If searching backwards, the end position is -1.
     *
     * @param endIndex The end position (exclusive).
     * @return This TextFinder instance.
     */
    public TextFinder setEndIndex(final int endIndex) {
        this.endIndex = endIndex;
        return this;
    }

    /**
     * Sets whether to search in reverse. {@code true} indicates searching from end to start.
     *
     * @param negative {@code true} to search in reverse, {@code false} otherwise.
     * @return This TextFinder instance.
     */
    public TextFinder setNegative(final boolean negative) {
        this.negative = negative;
        return this;
    }

    /**
     * Gets the valid end index for the search. If {@link #endIndex} is less than 0, it represents the beginning (-1) in
     * reverse mode, and the end ({@code text.length()}) in forward mode.
     *
     * @return The valid end index.
     */
    protected int getValidEndIndex() {
        if (negative && -1 == endIndex) {
            // In reverse search mode, -1 means the position before 0, i.e., the end of the string in reverse.
            return -1;
        }
        final int limit;
        if (endIndex < 0) {
            limit = endIndex + text.length() + 1;
        } else {
            limit = Math.min(endIndex, text.length());
        }
        return limit;
    }

}
