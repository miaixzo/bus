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
package org.miaixz.bus.core.center.map;

import java.util.Map;
import java.util.function.Consumer;

import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Represents a node within a tree structure, extending {@link Map.Entry} to hold a key-value pair. Each
 * {@code TreeEntry} can have a single parent and multiple children, allowing for hierarchical data representation. This
 * interface provides methods for navigating the tree, querying relationships, and accessing node properties.
 *
 * @param <K> The type of the key that uniquely identifies this node.
 * @param <V> The type of the value associated with this node.
 * @author Kimi Liu
 * @see ForestMap
 * @since Java 17+
 */
public interface TreeEntry<K, V> extends Map.Entry<K, V> {

    /**
     * Retrieves the depth or level of this node within its tree structure. The root node typically has a weight of 0.
     *
     * @return The integer weight (depth) of the current node from the root.
     */
    int getWeight();

    /**
     * Retrieves the root node of the tree to which this node belongs. The root node is the topmost node in the
     * hierarchy, having no parent.
     *
     * @return The root {@link TreeEntry} of the tree.
     */
    TreeEntry<K, V> getRoot();

    /**
     * Checks if this node has a directly declared parent.
     *
     * @return {@code true} if this node has an immediate parent, {@code false} otherwise (e.g., if it's a root node).
     */
    default boolean hasParent() {
        return ObjectKit.isNotNull(getDeclaredParent());
    }

    /**
     * Retrieves the directly declared parent node of this node.
     *
     * @return The immediate parent {@link TreeEntry}, or {@code null} if this node is a root node or has no declared
     *         parent.
     */
    TreeEntry<K, V> getDeclaredParent();

    /**
     * Retrieves a specific ancestor node by its key within the tree structure. This method traverses up the hierarchy
     * from the current node to find the ancestor.
     *
     * @param key The key of the specific parent (ancestor) node to retrieve.
     * @return The specified parent {@link TreeEntry}, or {@code null} if no such ancestor is found.
     */
    TreeEntry<K, V> getParent(K key);

    /**
     * Checks if a node with the given key is an ancestor of this node.
     *
     * @param key The key of the potential ancestor node.
     * @return {@code true} if the node with the given key is an ancestor of this node, {@code false} otherwise.
     */
    default boolean containsParent(final K key) {
        return ObjectKit.isNotNull(getParent(key));
    }

    /**
     * Traverses all descendant nodes (including itself if {@code includeSelf} is true) in a depth-first manner,
     * applying the given consumer to each node.
     *
     * @param includeSelf  If {@code true}, the current node is included in the traversal; otherwise, traversal starts
     *                     from its children.
     * @param nodeConsumer A {@link Consumer} to apply to each traversed {@link TreeEntry}.
     */
    void forEachChild(boolean includeSelf, Consumer<TreeEntry<K, V>> nodeConsumer);

    /**
     * Retrieves a map of the direct children of this node, where keys are the children's keys.
     *
     * @return A {@link Map} containing the direct child {@link TreeEntry} objects, keyed by their keys.
     */
    Map<K, TreeEntry<K, V>> getDeclaredChildren();

    /**
     * Retrieves a map of all descendant nodes (direct and indirect children) of this node, keyed by their keys.
     *
     * @return A {@link Map} containing all descendant {@link TreeEntry} objects, keyed by their keys.
     */
    Map<K, TreeEntry<K, V>> getChildren();

    /**
     * Checks if this node has any direct child nodes.
     *
     * @return {@code true} if this node has one or more direct children, {@code false} otherwise.
     */
    default boolean hasChildren() {
        return CollKit.isNotEmpty(getDeclaredChildren());
    }

    /**
     * Retrieves a specific child node by its key from the direct children of this node.
     *
     * @param key The key of the specific child node to retrieve.
     * @return The specified child {@link TreeEntry}, or {@code null} if no direct child with that key is found.
     */
    TreeEntry<K, V> getChild(K key);

    /**
     * Checks if this node has a direct child with the specified key.
     *
     * @param key The key of the child node to check for.
     * @return {@code true} if a direct child with the given key exists, {@code false} otherwise.
     */
    default boolean containsChild(final K key) {
        return ObjectKit.isNotNull(getChild(key));
    }

}
