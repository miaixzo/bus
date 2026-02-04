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
package org.miaixz.bus.core.lang.selector;

import java.io.Serial;
import java.util.ArrayList;

/**
 * A simple round-robin selector.
 *
 * @param <T> the type of the elements
 * @author Kimi Liu
 * @since Java 17+
 */
public class IncrementSelector<T> extends ArrayList<T> implements Selector<T> {

    @Serial
    private static final long serialVersionUID = 2852277613270L;

    /**
     * Index position
     */
    private int position;

    /**
     * Constructs an empty {@code IncrementSelector}.
     */
    public IncrementSelector() {
        super();
    }

    /**
     * Constructs a new {@code IncrementSelector} and initializes it with the elements from the given iterable.
     *
     * @param objList the iterable of objects to add
     */
    public IncrementSelector(final Iterable<T> objList) {
        this();
        for (final T object : objList) {
            add(object);
        }
    }

    /**
     * Selects the next element in a round-robin fashion.
     *
     * @return the next element in the sequence
     */
    @Override
    public T select() {
        final T result = get(position);
        if (++position >= size()) {
            position = 0;
        }
        return result;
    }

}
