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
package org.miaixz.bus.core.lang.selector;

import java.io.Serial;
import java.util.ArrayList;

import org.miaixz.bus.core.xyz.RandomKit;

/**
 * A selector that randomly picks an element from a list.
 *
 * @param <T> the type of the elements
 * @author Kimi Liu
 * @since Java 21+
 */
public class RandomSelector<T> extends ArrayList<T> implements Selector<T> {

    @Serial
    private static final long serialVersionUID = 2852277695036L;

    /**
     * Constructs an empty {@code RandomSelector}.
     */
    public RandomSelector() {
        super();
    }

    /**
     * Constructs a new {@code RandomSelector} and initializes it with the elements from the given iterable.
     *
     * @param objList the iterable of objects to add
     */
    public RandomSelector(final Iterable<T> objList) {
        this();
        for (final T object : objList) {
            add(object);
        }
    }

    /**
     * Selects an element randomly from the list.
     *
     * @return a randomly selected element
     * @throws IndexOutOfBoundsException if the selector is empty
     */
    @Override
    public T select() {
        return get(RandomKit.randomInt(0, size()));
    }

}
