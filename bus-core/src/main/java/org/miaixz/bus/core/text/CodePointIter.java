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
