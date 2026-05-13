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
package org.miaixz.bus.image.nimble.codec;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.imageio.spi.ImageReaderWriterSpi;

/**
 * Represents the FormatNameFilterIterator type.
 *
 * @param <T> the t type.
 * @author Kimi Liu
 * @since Java 21+
 */
final class FormatNameFilterIterator<T extends ImageReaderWriterSpi> implements Iterator<T> {

    /**
     * The iter value.
     */
    private final Iterator<T> iter;

    /**
     * The format name value.
     */
    private final String formatName;

    /**
     * The next value.
     */
    private T next = null;

    /**
     * Creates a new instance.
     *
     * @param iter       the iter.
     * @param formatName the format name.
     */
    FormatNameFilterIterator(Iterator<T> iter, String formatName) {
        this.iter = iter;
        this.formatName = formatName;
        advance();
    }

    /**
     * Executes the contains operation.
     *
     * @param names the names.
     * @param name  the name.
     * @return true if the condition is met; otherwise false.
     */
    private static boolean contains(String[] names, String name) {
        for (int i = 0; i < names.length; i++) {
            if (name.equalsIgnoreCase(names[i])) {
                return true;
            }
        }

        return false;
    }

    /**
     * Executes the advance operation.
     */
    private void advance() {
        while (iter.hasNext()) {
            T elt = iter.next();
            if (contains(elt.getFormatNames(), formatName)) {
                next = elt;
                return;
            }
        }

        next = null;
    }

    /**
     * Determines whether next.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean hasNext() {
        return next != null;
    }

    /**
     * Executes the next operation.
     *
     * @return the operation result.
     */
    @Override
    public T next() {
        if (next == null) {
            throw new NoSuchElementException();
        }
        T o = next;
        advance();
        return o;
    }

    /**
     * Executes the remove operation.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
