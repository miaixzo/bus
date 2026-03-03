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
package org.miaixz.bus.core.tree;

import java.io.Serial;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a generic node in a tree structure. Each property of this node can be renamed using {@link NodeConfig}. In
 * your application, this could be a department entity, a region entity, or any other class that can be structured as a
 * tree node (i.e., it has a key and a parent key).
 *
 * @param <T> The type of the identifier for the node.
 * @author Kimi Liu
 * @since Java 17+
 */
public class TreeNode<T> implements Node<T> {

    @Serial
    private static final long serialVersionUID = 2852250559730L;

    /**
     * The unique identifier of the node.
     */
    private T id;

    /**
     * The identifier of the parent node.
     */
    private T parentId;

    /**
     * The name or label of the node.
     */
    private CharSequence name;

    /**
     * The order or weight of the node. A smaller value indicates a higher priority. Defaults to 0.
     */
    private Comparable<?> weight = 0;

    /**
     * A map to hold extended properties.
     */
    private Map<String, Object> extra;

    /**
     * Default constructor.
     */
    public TreeNode() {
    }

    /**
     * Constructs a new TreeNode with the specified details.
     *
     * @param id       The unique identifier of the node.
     * @param parentId The identifier of the parent node.
     * @param name     The name of the node.
     * @param weight   The weight for ordering.
     */
    public TreeNode(final T id, final T parentId, final String name, final Comparable<?> weight) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        if (weight != null) {
            this.weight = weight;
        }
    }

    /**
     * Gets the unique identifier of the node.
     *
     * @return The node ID.
     */
    @Override
    public T getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the node.
     *
     * @param id The new node ID.
     * @return This node instance for method chaining.
     */
    @Override
    public TreeNode<T> setId(final T id) {
        this.id = id;
        return this;
    }

    /**
     * Gets the identifier of the parent node.
     *
     * @return The parent node ID.
     */
    @Override
    public T getParentId() {
        return this.parentId;
    }

    /**
     * Sets the identifier of the parent node.
     *
     * @param parentId The new parent node ID.
     * @return This node instance for method chaining.
     */
    @Override
    public TreeNode<T> setParentId(final T parentId) {
        this.parentId = parentId;
        return this;
    }

    /**
     * Gets the name or label of the node.
     *
     * @return The node name.
     */
    @Override
    public CharSequence getName() {
        return name;
    }

    /**
     * Sets the name or label of the node.
     *
     * @param name The new node name.
     * @return This node instance for method chaining.
     */
    @Override
    public TreeNode<T> setName(final CharSequence name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the weight or order priority of the node.
     *
     * @return The node weight. A smaller value indicates a higher priority.
     */
    @Override
    public Comparable<?> getWeight() {
        return weight;
    }

    /**
     * Sets the weight or order priority of the node.
     *
     * @param weight The new node weight. A smaller value indicates a higher priority.
     * @return This node instance for method chaining.
     */
    @Override
    public TreeNode<T> setWeight(final Comparable<?> weight) {
        this.weight = weight;
        return this;
    }

    /**
     * Gets the map of extended properties.
     *
     * @return The map of extended properties.
     */
    public Map<String, Object> getExtra() {
        return extra;
    }

    /**
     * Sets the map of extended properties.
     *
     * @param extra The map of extended properties.
     * @return this
     */
    public TreeNode<T> setExtra(final Map<String, Object> extra) {
        this.extra = extra;
        return this;
    }

    /**
     * Checks if this node is equal to another object. Two nodes are considered equal if they have the same ID.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TreeNode<?> treeNode = (TreeNode<?>) o;
        return Objects.equals(id, treeNode.id);
    }

    /**
     * Returns the hash code of this node based on its ID.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
