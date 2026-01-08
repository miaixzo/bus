/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.tree;

import java.io.Serializable;

import org.miaixz.bus.core.xyz.CompareKit;

/**
 * Represents a node in a tree structure, providing definitions for node-related methods.
 *
 * @param <T> The type of the node's identifier.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Node<T> extends Comparable<Node<T>>, Serializable {

    /**
     * Retrieves the unique identifier of the node.
     *
     * @return The ID of the node.
     */
    T getId();

    /**
     * Sets the unique identifier of the node.
     *
     * @param id The ID to be set for the node.
     * @return The current node instance for chaining.
     */
    Node<T> setId(T id);

    /**
     * Retrieves the identifier of the parent node.
     *
     * @return The parent node's ID.
     */
    T getParentId();

    /**
     * Sets the identifier of the parent node.
     *
     * @param parentId The ID of the parent node.
     * @return The current node instance for chaining.
     */
    Node<T> setParentId(T parentId);

    /**
     * Retrieves the name or label of the node.
     *
     * @return The name of the node.
     */
    CharSequence getName();

    /**
     * Sets the name or label of the node.
     *
     * @param name The name to be set for the node.
     * @return The current node instance for chaining.
     */
    Node<T> setName(CharSequence name);

    /**
     * Retrieves the weight of the node, used for ordering.
     *
     * @return The weight of the node.
     */
    Comparable<?> getWeight();

    /**
     * Sets the weight of the node, used for ordering.
     *
     * @param weight The weight to be set for the node.
     * @return The current node instance for chaining.
     */
    Node<T> setWeight(Comparable<?> weight);

    /**
     * Compares this node with another node for ordering. The comparison is based on the nodes' weights.
     *
     * @param node The node to be compared.
     * @return A negative integer, zero, or a positive integer as this node is less than, equal to, or greater than the
     *         specified node.
     */
    @Override
    default int compareTo(final Node<T> node) {
        if (null == node) {
            return 1;
        }
        final Comparable weight = this.getWeight();
        final Comparable weightOther = node.getWeight();
        return CompareKit.compare(weight, weightOther);
    }

}
