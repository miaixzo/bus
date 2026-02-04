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
package org.miaixz.bus.core.center.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.miaixz.bus.core.lang.Assert;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An {@link Iterator} wrapper for {@link NodeList}. This iterator allows iterating over the nodes in a
 * {@link NodeList}. The {@link #remove()} method is not supported by this iterator.
 *
 * @author Kimi Liu
 * @see NodeList
 * @since Java 17+
 */
public class NodeListIterator implements ResettableIterator<Node> {

    /**
     * The underlying {@link NodeList} to iterate over.
     */
    private final NodeList nodeList;
    /**
     * The current index of the iterator within the {@link NodeList}.
     */
    private int index = 0;

    /**
     * Constructs a {@code NodeListIterator} from the given {@link NodeList}.
     *
     * @param nodeList the {@link NodeList} to iterate over, must not be {@code null}
     * @throws NullPointerException if {@code nodeList} is {@code null}
     */
    public NodeListIterator(final NodeList nodeList) {
        this.nodeList = Assert.notNull(nodeList, "NodeList must not be null.");
    }

    /**
     * Returns {@code true} if the iteration has more elements. (In other words, returns {@code true} if {@link #next}
     * would return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return nodeList != null && index < nodeList.getLength();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public Node next() {
        if (nodeList != null && index < nodeList.getLength()) {
            return nodeList.item(index++);
        }
        throw new NoSuchElementException("underlying nodeList has no more elements");
    }

    /**
     * This operation is not supported by this iterator.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() method not supported for a NodeListIterator.");
    }

    /**
     * Resets the iterator to its initial state, allowing it to iterate from the beginning again.
     */
    @Override
    public void reset() {
        this.index = 0;
    }

}
