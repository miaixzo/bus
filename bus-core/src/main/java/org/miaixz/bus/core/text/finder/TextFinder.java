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

    /**
     * The serial version UID.
     */
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
