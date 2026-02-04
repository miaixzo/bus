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
