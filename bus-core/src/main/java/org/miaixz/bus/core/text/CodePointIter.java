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
package org.miaixz.bus.core.text;

import java.util.Iterator;

/**
 * Unicode character iterator. Reference:
 * <a href="http://stackoverflow.com/a/21791059/6030888">http://stackoverflow.com/a/21791059/6030888</a>
 *
 * @param text the string to iterate over
 * @author Kimi Liu
 * @since Java 17+
 */
public record CodePointIter(String text) implements Iterable<Integer> {

    /**
     * Returns an iterator over Unicode code points in the text.
     *
     * @return An iterator that yields each Unicode code point in the text.
     */
    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<>() {

            /**
             * The total length of the string in characters.
             */
            private final int length = text.length();
            /**
             * The index of the next code point to be returned.
             */
            private int nextIndex = 0;

            /**
             * Returns {@code true} if there are more code points to iterate.
             *
             * @return {@code true} if the iteration has more elements, {@code false} otherwise.
             */
            @Override
            public boolean hasNext() {
                return this.nextIndex < this.length;
            }

            /**
             * Returns the next Unicode code point in the iteration.
             *
             * @return The next code point as an {@link Integer}.
             */
            @Override
            public Integer next() {
                final int result = text.codePointAt(this.nextIndex);
                this.nextIndex += Character.charCount(result);
                return result;
            }

            /**
             * This operation is not supported.
             *
             * @throws UnsupportedOperationException always.
             */
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

}
