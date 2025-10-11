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
package org.miaixz.bus.core.center.map;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.SetKit;

/**
 * Represents a forest (a collection of trees) where each node is a {@link TreeEntry}. This interface extends
 * {@link Map} and provides specialized methods for building and navigating hierarchical tree structures. Each key in
 * the map corresponds to a unique node in the forest.
 *
 * @param <K> The type of the keys (node identifiers).
 * @param <V> The type of the values stored in the nodes.
 * @author Kimi Liu
 * @see TreeEntry
 * @since Java 17+
 */
public interface ForestMap<K, V> extends Map<K, TreeEntry<K, V>> {

    /**
     * Adds or updates a node in the forest using the provided {@link TreeEntry}. If a node with the same key already
     * exists, its value is updated. This is a convenience method that delegates to {@link #putNode(Object, Object)}.
     *
     * @param key  The key of the node.
     * @param node The {@link TreeEntry} containing the value to associate with the key.
     * @return The previous {@link TreeEntry} associated with the key, or {@code null} if there was no mapping.
     * @see #putNode(Object, Object)
     */
    @Override
    default TreeEntry<K, V> put(final K key, final TreeEntry<K, V> node) {
        return putNode(key, node.getValue());
    }

    /**
     * Adds all nodes from the specified map into this forest, preserving their parent-child relationships.
     *
     * @param treeEntryMap A map of keys to {@link TreeEntry} objects to be added.
     */
    @Override
    default void putAll(final Map<? extends K, ? extends TreeEntry<K, V>> treeEntryMap) {
        if (CollKit.isEmpty(treeEntryMap)) {
            return;
        }
        treeEntryMap.forEach((k, v) -> {
            if (v.hasParent()) {
                final TreeEntry<K, V> parent = v.getDeclaredParent();
                putLinkedNodes(parent.getKey(), parent.getValue(), v.getKey(), v.getValue());
            } else {
                putNode(v.getKey(), v.getValue());
            }
        });
    }

    /**
     * Populates the forest from a collection of value objects, using functions to extract keys and parent keys.
     *
     * @param <C>                The type of the collection.
     * @param values             The collection of value objects to add as nodes.
     * @param keyGenerator       A function to extract the key for a node from its value object.
     * @param parentKeyGenerator A function to extract the parent's key from a node's value object.
     * @param ignoreNullNode     If {@code true}, entries where the key or parent key is {@code null} will be skipped.
     */
    default <C extends Collection<V>> void putAllNode(
            final C values,
            final Function<V, K> keyGenerator,
            final Function<V, K> parentKeyGenerator,
            final boolean ignoreNullNode) {
        if (CollKit.isEmpty(values)) {
            return;
        }
        values.forEach(v -> {
            final K key = keyGenerator.apply(v);
            final K parentKey = parentKeyGenerator.apply(v);

            final boolean hasKey = ObjectKit.isNotNull(key);
            final boolean hasParentKey = ObjectKit.isNotNull(parentKey);

            if (!ignoreNullNode || (hasKey && hasParentKey)) {
                linkNodes(parentKey, key);
                get(key).setValue(v);
            } else if (hasKey) {
                putNode(key, v);
            } else if (hasParentKey) {
                // Ensure parent node exists even if child key is null
                putNode(parentKey, null);
            }
        });
    }

    /**
     * Adds or updates a single node in the forest. If a node with the given key does not exist, a new one is created.
     * If a node with the given key already exists, its value is updated.
     *
     * @param key   The key of the node.
     * @param value The value of the node.
     * @return The {@link TreeEntry} associated with the key (either existing or newly created).
     */
    TreeEntry<K, V> putNode(K key, V value);

    /**
     * Adds or updates both a parent and a child node and establishes a parent-child link between them. This is a
     * convenience method equivalent to:
     * 
     * <pre>{@code
     * putNode(parentKey, parentValue);
     * putNode(childKey, childValue);
     * linkNodes(parentKey, childKey);
     * }</pre>
     *
     * @param parentKey   The key of the parent node.
     * @param parentValue The value of the parent node.
     * @param childKey    The key of the child node.
     * @param childValue  The value of the child node.
     */
    default void putLinkedNodes(final K parentKey, final V parentValue, final K childKey, final V childValue) {
        putNode(parentKey, parentValue);
        putNode(childKey, childValue);
        linkNodes(parentKey, childKey);
    }

    /**
     * Adds or updates a child node and links it to a parent node. If the parent or child nodes do not exist, they are
     * created.
     *
     * @param parentKey  The key of the parent node.
     * @param childKey   The key of the child node.
     * @param childValue The value of the child node.
     */
    void putLinkedNodes(K parentKey, K childKey, V childValue);

    /**
     * Establishes a parent-child relationship between two nodes that are presumed to exist in the forest.
     *
     * @param parentKey The key of the parent node.
     * @param childKey  The key of the child node.
     */
    default void linkNodes(final K parentKey, final K childKey) {
        linkNodes(parentKey, childKey, null);
    }

    /**
     * Establishes a parent-child relationship between two nodes and provides a consumer to perform actions on them.
     *
     * @param parentKey The key of the parent node.
     * @param childKey  The key of the child node.
     * @param consumer  An optional {@link BiConsumer} to process the parent and child nodes after linking.
     */
    void linkNodes(K parentKey, K childKey, BiConsumer<TreeEntry<K, V>, TreeEntry<K, V>> consumer);

    /**
     * Removes the direct link between a parent and its immediate child node.
     *
     * @param parentKey The key of the parent node.
     * @param childKey  The key of the child node to unlink.
     */
    void unlinkNode(K parentKey, K childKey);

    /**
     * Retrieves all nodes belonging to the same tree as the node with the specified key. For example, in a tree
     * {@code a -> b -> c}, providing {@code a}, {@code b}, or {@code c} will return all three nodes.
     *
     * @param key The key of a node within the desired tree.
     * @return A {@link Set} of all {@link TreeEntry} objects in the tree, or an empty set if the key is not found.
     */
    default Set<TreeEntry<K, V>> getTreeNodes(final K key) {
        final TreeEntry<K, V> target = get(key);
        if (ObjectKit.isNull(target)) {
            return Collections.emptySet();
        }
        final Set<TreeEntry<K, V>> results = SetKit.ofLinked(target.getRoot());
        CollKit.addAll(results, target.getRoot().getChildren().values());
        return results;
    }

    /**
     * Retrieves the root node of the tree to which the specified node belongs. For example, in a tree
     * {@code a -> b -> c}, providing {@code b} or {@code c} will return node {@code a}.
     *
     * @param key The key of a node within the tree.
     * @return The root {@link TreeEntry} of the tree, or {@code null} if the key is not found.
     */
    default TreeEntry<K, V> getRootNode(final K key) {
        return Optional.ofNullable(get(key)).map(TreeEntry::getRoot).orElse(null);
    }

    /**
     * Retrieves the direct parent of the specified node. For example, in a tree {@code a -> b -> c}, providing
     * {@code c} will return node {@code b}.
     *
     * @param key The key of the node whose parent is to be retrieved.
     * @return The direct parent {@link TreeEntry}, or {@code null} if the node has no parent or is not found.
     */
    default TreeEntry<K, V> getDeclaredParentNode(final K key) {
        return Optional.ofNullable(get(key)).map(TreeEntry::getDeclaredParent).orElse(null);
    }

    /**
     * Retrieves a specific ancestor node from the hierarchy above the specified node.
     *
     * @param key       The key of the starting node.
     * @param parentKey The key of the ancestor node to find.
     * @return The ancestor {@link TreeEntry}, or {@code null} if it is not an ancestor or is not found.
     */
    default TreeEntry<K, V> getParentNode(final K key, final K parentKey) {
        return Optional.ofNullable(get(key)).map(t -> t.getParent(parentKey)).orElse(null);
    }

    /**
     * Checks if a node is an ancestor of another node.
     *
     * @param key       The key of the potential descendant node.
     * @param parentKey The key of the potential ancestor node.
     * @return {@code true} if the node identified by {@code parentKey} is an ancestor of the node identified by
     *         {@code key}.
     */
    default boolean containsParentNode(final K key, final K parentKey) {
        return Optional.ofNullable(get(key)).map(m -> m.containsParent(parentKey)).orElse(false);
    }

    /**
     * Retrieves the value of the specified node.
     *
     * @param key The key of the node.
     * @return The value of the node, or {@code null} if the node does not exist or its value is {@code null}.
     */
    default V getNodeValue(final K key) {
        return Optional.ofNullable(get(key)).map(TreeEntry::getValue).getOrNull();
    }

    /**
     * Checks if a node is a descendant of another node.
     *
     * @param parentKey The key of the potential ancestor node.
     * @param childKey  The key of the potential descendant node.
     * @return {@code true} if the node identified by {@code childKey} is a descendant of the node identified by
     *         {@code parentKey}.
     */
    default boolean containsChildNode(final K parentKey, final K childKey) {
        return Optional.ofNullable(get(parentKey)).map(m -> m.containsChild(childKey)).orElse(false);
    }

    /**
     * Retrieves all direct children of the specified node. For example, in a tree {@code a -> b -> c}, providing
     * {@code a} will return a collection containing only node {@code b}.
     *
     * @param key The key of the parent node.
     * @return A {@link Collection} of the direct child {@link TreeEntry} objects, or an empty collection if none exist.
     */
    default Collection<TreeEntry<K, V>> getDeclaredChildNodes(final K key) {
        return Optional.ofNullable(get(key)).map(TreeEntry::getDeclaredChildren).map(Map::values)
                .orElseGet(Collections::emptyList);
    }

    /**
     * Retrieves all descendant nodes (children, grandchildren, etc.) of the specified node. For example, in a tree
     * {@code a -> b -> c}, providing {@code a} will return a collection containing nodes {@code b} and {@code c}.
     *
     * @param key The key of the parent node.
     * @return A {@link Collection} of all descendant {@link TreeEntry} objects, or an empty collection if none exist.
     */
    default Collection<TreeEntry<K, V>> getChildNodes(final K key) {
        return Optional.ofNullable(get(key)).map(TreeEntry::getChildren).map(Map::values)
                .orElseGet(Collections::emptyList);
    }

}
