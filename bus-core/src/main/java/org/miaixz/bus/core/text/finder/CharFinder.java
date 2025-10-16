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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.CharKit;

/**
 * Character finder. Finds the position information of a specified character within a string.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CharFinder extends TextFinder {

    /**
     * The serial version UID
     */
    @Serial
    private static final long serialVersionUID = 2852236682062L;

    /**
     * The character to be searched.
     */
    private final char c;
    /**
     * Whether to ignore case during the search.
     */
    private final boolean caseInsensitive;

    /**
     * Constructor, does not ignore character case.
     *
     * @param c The character to be searched.
     */
    public CharFinder(final char c) {
        this(c, false);
    }

    /**
     * Constructor.
     *
     * @param c               The character to be searched.
     * @param caseInsensitive Whether to ignore case.
     */
    public CharFinder(final char c, final boolean caseInsensitive) {
        this.c = c;
        this.caseInsensitive = caseInsensitive;
    }

    @Override
    public int start(final int from) {
        Assert.notNull(this.text, "Text to find must be not null!");
        final int limit = getValidEndIndex();
        if (negative) {
            for (int i = from; i > limit; i--) {
                if (CharKit.equals(c, text.charAt(i), caseInsensitive)) {
                    return i;
                }
            }
        } else {
            for (int i = from; i < limit; i++) {
                if (CharKit.equals(c, text.charAt(i), caseInsensitive)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public int end(final int start) {
        if (start < 0) {
            return -1;
        }
        return start + 1;
    }

}
