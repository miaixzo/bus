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

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Wrapper;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A basic implementation of {@link ForestMap}.
 *
 * <p>
 * This collection can be viewed as a {@link LinkedHashMap} where the key is {@link TreeEntryNode#getKey()} and the
 * value is a {@link TreeEntryNode} instance. Each key-value pair is treated as a {@link TreeEntryNode}, and using the
 * same key will always access the same node.
 * 
 *
 * <p>
 * Nodes form parent-child relationships through their keys, ultimately creating a multi-way tree structure. Multiple
 * parallel multi-way trees constitute a forest within this collection. Users can manipulate or access the forest
 * through the methods of {@link ForestMap} itself, or by obtaining a {@link TreeEntry} and using the node's own
 * methods.
 * 
 *
 * @param <K> The type of the keys.
 * @param <V> The type of the values.
 * @author Kimi Liu
 * @since Java 17+
 */
public class LinkedForestMap<K, V> implements ForestMap<K, V> {

    /**
     * The collection of all nodes in the forest, keyed by their unique key.
     */
    private final Map<K, TreeEntryNode<K, V>> nodes;

    /**
     * Whether to allow forcibly changing a node's parent if it already has one.
     */
    private final boolean allowOverrideParent;

    /**
     * Constructs a {@code LinkedForestMap}.
     *
     * @param allowOverrideParent If true, allows a node's parent to be forcibly changed.
     */
    public LinkedForestMap(final boolean allowOverrideParent) {
        this.allowOverrideParent = allowOverrideParent;
        this.nodes = new LinkedHashMap<>();
    }

    /**
     * Gets the number of nodes in the current instance.
     *
     * @return The number of nodes.
     */
    @Override
    public int size() {
        return nodes.size();
    }

    /**
     * Checks if the current instance is empty.
     *
     * @return {@code true} if there are no nodes.
     */
    @Override
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    /**
     * Checks if a node with the specified key exists in the current instance.
     *
     * @param key The key of the node.
     * @return {@code true} if the key exists.
     */
    @Override
    public boolean containsKey(final Object key) {
        return nodes.containsKey(key);
    }

    /**
     * Checks if the specified {@link TreeEntry} instance exists in the current instance.
     *
     * @param value The {@link TreeEntry} instance.
     * @return {@code true} if the value exists.
     */
    @Override
    public boolean containsValue(final Object value) {
        return nodes.containsValue(value);
    }

    /**
     * Gets the node corresponding to the specified key.
     *
     * @param key The key of the node.
     * @return The node, or null if not found.
     */
    @Override
    public TreeEntry<K, V> get(final Object key) {
        return nodes.get(key);
    }

    /**
     * Removes the specified node from the current {@link Map}.
     * <ul>
     * <li>If the node has a parent or children, its references to them are disconnected.</li>
     * <li>If it has both a parent and children, the children are re-parented to the node's parent. For example, in a
     * relationship a -&gt; b -&gt; c, removing b results in a -&gt; c.</li>
     * </ul>
     *
     * @param key The key of the node to remove.
     * @return The removed node with its references updated, or null if the key was not found.
     */
    @Override
    public TreeEntry<K, V> remove(final Object key) {
        final TreeEntryNode<K, V> target = nodes.remove(key);
        if (ObjectKit.isNull(target)) {
            return null;
        }
        // If a parent exists, remove the target from its children and re-parent the target's children.
        if (target.hasParent()) {
            final TreeEntryNode<K, V> parent = target.getDeclaredParent();
            final Map<K, TreeEntry<K, V>> targetChildren = target.getChildren();
            parent.removeDeclaredChild(target.getKey());
            target.clear();
            targetChildren.forEach((k, c) -> parent.addChild((TreeEntryNode<K, V>) c));
        }
        return target;
    }

    /**
     * Clears the entire collection and removes all references between nodes.
     */
    @Override
    public void clear() {
        nodes.values().forEach(TreeEntryNode::clear);
        nodes.clear();
    }

    /**
     * Returns a {@link Set} of all keys in the current instance.
     *
     * @return The set of keys.
     */
    @Override
    public Set<K> keySet() {
        return nodes.keySet();
    }

    /**
     * Returns a {@link Collection} of all {@link TreeEntry} instances in the current instance.
     *
     * @return The collection of entries.
     */
    @Override
    public Collection<TreeEntry<K, V>> values() {
        return new ArrayList<>(nodes.values());
    }

    /**
     * Returns a {@link Set} of map entries (key and {@link TreeEntry}). Note: {@link Map.Entry#setValue(Object)} is not
     * supported on the returned set.
     *
     * @return The set of map entries.
     */
    @Override
    public Set<Map.Entry<K, TreeEntry<K, V>>> entrySet() {
        return nodes.entrySet().stream().map(this::wrap).collect(Collectors.toSet());
    }

    /**
     * Wraps a {@link TreeEntryNode} as an {@link EntryNodeWrapper}.
     * 
     * @param nodeEntry The map entry to wrap.
     * @return The wrapped entry.
     */
    private Map.Entry<K, TreeEntry<K, V>> wrap(final Map.Entry<K, TreeEntryNode<K, V>> nodeEntry) {
        return new EntryNodeWrapper<>(nodeEntry.getValue());
    }

    /**
     * Adds or updates a node.
     * <ul>
     * <li>If a node with the key does not exist, a new one is created.</li>
     * <li>If a node with the key exists, its value is replaced with the specified value.</li>
     * </ul>
     *
     * @param key   The key of the node.
     * @param value The value of the node.
     * @return The previous node with its old value if it existed, otherwise null.
     */
    @Override
    public TreeEntryNode<K, V> putNode(final K key, final V value) {
        TreeEntryNode<K, V> target = nodes.get(key);
        if (ObjectKit.isNotNull(target)) {
            final V oldVal = target.getValue();
            target.setValue(value);
            return target.copy(oldVal);
        }
        target = new TreeEntryNode<>(null, key, value);
        nodes.put(key, target);
        return null;
    }

    /**
     * Adds and links a parent and child node simultaneously. If nodes for {@code parentKey} or {@code childKey} do not
     * exist, they are created. If they exist, their values are updated.
     *
     * @param parentKey   The key of the parent node.
     * @param parentValue The value of the parent node.
     * @param childKey    The key of the child node.
     * @param childValue  The value of the child node.
     */
    @Override
    public void putLinkedNodes(final K parentKey, final V parentValue, final K childKey, final V childValue) {
        linkNodes(parentKey, childKey, (parent, child) -> {
            parent.setValue(parentValue);
            child.setValue(childValue);
        });
    }

    /**
     * Adds a child node and links it to a parent.
     *
     * @param parentKey  The key of the parent node.
     * @param childKey   The key of the child node.
     * @param childValue The value of the child node.
     */
    @Override
    public void putLinkedNodes(final K parentKey, final K childKey, final V childValue) {
        linkNodes(parentKey, childKey, (parent, child) -> child.setValue(childValue));
    }

    /**
     * Establishes a parent-child relationship between two nodes. If nodes for {@code parentKey} or {@code childKey} do
     * not exist, they are created with null values.
     *
     * @param parentKey The key of the parent node.
     * @param childKey  The key of the child node.
     * @param consumer  An optional consumer to perform actions on the parent and child nodes.
     */
    @Override
    public void linkNodes(final K parentKey, final K childKey, BiConsumer<TreeEntry<K, V>, TreeEntry<K, V>> consumer) {
        consumer = ObjectKit.defaultIfNull(consumer, (parent, child) -> {
        });
        final TreeEntryNode<K, V> parentNode = nodes.computeIfAbsent(parentKey, t -> new TreeEntryNode<>(null, t));
        TreeEntryNode<K, V> childNode = nodes.get(childKey);

        if (ObjectKit.isNull(childNode)) {
            childNode = new TreeEntryNode<>(parentNode, childKey);
            consumer.accept(parentNode, childNode);
            nodes.put(childKey, childNode);
            return;
        }

        if (ObjectKit.equals(parentNode, childNode.getDeclaredParent())) {
            consumer.accept(parentNode, childNode);
            return;
        }

        if (!childNode.hasParent()) {
            parentNode.addChild(childNode);
        } else if (allowOverrideParent) {
            childNode.getDeclaredParent().removeDeclaredChild(childNode.getKey());
            parentNode.addChild(childNode);
        } else {
            throw new IllegalArgumentException(StringKit.format(
                    "Node [{}] is already a child of [{}]. Overriding is not allowed.",
                    childNode.getKey(),
                    childNode.getDeclaredParent().getKey()));
        }
        consumer.accept(parentNode, childNode);
    }

    /**
     * Removes the direct link between a parent and child node, but does not remove the nodes from the collection.
     *
     * @param parentKey The key of the parent node.
     * @param childKey  The key of the child node.
     */
    @Override
    public void unlinkNode(final K parentKey, final K childKey) {
        final TreeEntryNode<K, V> childNode = nodes.get(childKey);
        if (ObjectKit.isNull(childNode) || !childNode.hasParent()) {
            return;
        }
        // Only unlink if the specified parent is the actual parent.
        if (Objects.equals(childNode.getDeclaredParent().getKey(), parentKey)) {
            childNode.getDeclaredParent().removeDeclaredChild(childNode.getKey());
        }
    }

    /**
     * Represents a node in the tree structure.
     *
     * @param <K> The type of the key.
     * @param <V> The type of the value.
     */
    public static class TreeEntryNode<K, V> implements TreeEntry<K, V> {

        /**
         * A map of child nodes, keyed by their unique key.
         */
        private final Map<K, TreeEntryNode<K, V>> children;
        /**
         * The unique key of this node.
         */
        private final K key;
        /**
         * The root of the tree this node belongs to.
         */
        private TreeEntryNode<K, V> root;
        /**
         * The direct parent of this node.
         */
        private TreeEntryNode<K, V> parent;
        /**
         * The distance from the root node (depth).
         */
        private int weight;
        /**
         * The value associated with this node.
         */
        private V value;

        /**
         * Creates a new node with a null value.
         *
         * @param parent The parent of this node.
         * @param key    The key for this node.
         */
        public TreeEntryNode(final TreeEntryNode<K, V> parent, final K key) {
            this(parent, key, null);
        }

        /**
         * Creates a new node.
         *
         * @param parent The parent of this node.
         * @param key    The key for this node.
         * @param value  The value for this node.
         */
        public TreeEntryNode(final TreeEntryNode<K, V> parent, final K key, final V value) {
            this.parent = parent;
            this.key = key;
            this.value = value;
            this.children = new LinkedHashMap<>();
            if (ObjectKit.isNull(parent)) {
                this.root = this;
                this.weight = 0;
            } else {
                parent.addChild(this);
                this.weight = parent.weight + 1;
                this.root = parent.root;
            }
        }

        /**
         * Gets the key of this entry.
         *
         * @return the key
         */
        @Override
        public K getKey() {
            return key;
        }

        /**
         * Gets the weight of this node.
         *
         * @return the weight
         */
        @Override
        public int getWeight() {
            return weight;
        }

        /**
         * Gets the value of this entry.
         *
         * @return the value
         */
        @Override
        public V getValue() {
            return value;
        }

        /**
         * Sets the value of this entry.
         *
         * @param value the new value
         * @return the old value
         */
        @Override
        public V setValue(final V value) {
            final V oldVal = this.value;
            this.value = value;
            return oldVal;
        }

        /**
         * Traverses upwards from the current node to its ancestors.
         *
         * @param includeCurrent If true, the current node is included in the traversal.
         * @param consumer       The action to perform on each ancestor.
         * @param breakTraverse  A predicate to stop the traversal.
         * @return The last node visited before the traversal was stopped.
         */
        TreeEntryNode<K, V> traverseParentNodes(
                final boolean includeCurrent,
                final Consumer<TreeEntryNode<K, V>> consumer,
                Predicate<TreeEntryNode<K, V>> breakTraverse) {
            breakTraverse = ObjectKit.defaultIfNull(breakTraverse, n -> false);
            TreeEntryNode<K, V> curr = includeCurrent ? this : this.parent;
            while (ObjectKit.isNotNull(curr)) {
                consumer.accept(curr);
                if (breakTraverse.test(curr)) {
                    break;
                }
                curr = curr.parent;
            }
            return curr;
        }

        /**
         * @return {@code true} if this node is a root node (has no parent).
         */
        public boolean isRoot() {
            return getRoot() == this;
        }

        /**
         * Getroot method.
         *
         * @return the TreeEntryNode&lt;K, V&gt; value
         */
        @Override
        public TreeEntryNode<K, V> getRoot() {
            if (ObjectKit.isNotNull(this.root)) {
                return this.root;
            }
            return this.root = traverseParentNodes(true, p -> {
            }, p -> !p.hasParent());
        }

        /**
         * Getdeclaredparent method.
         *
         * @return the TreeEntryNode&lt;K, V&gt; value
         */
        @Override
        public TreeEntryNode<K, V> getDeclaredParent() {
            return parent;
        }

        /**
         * Getparent method.
         *
         * @return the TreeEntryNode&lt;K, V&gt; value
         */
        @Override
        public TreeEntryNode<K, V> getParent(final K key) {
            return traverseParentNodes(false, p -> {
            }, p -> p.equalsKey(key));
        }

        /**
         * Foreachchild method.
         */
        @Override
        public void forEachChild(final boolean includeSelf, final Consumer<TreeEntry<K, V>> nodeConsumer) {
            traverseChildNodes(includeSelf, (index, child) -> nodeConsumer.accept(child), null);
        }

        /**
         * Checks if the given key is equal to this node's key.
         *
         * @param key The key to compare.
         * @return {@code true} if the keys are equal.
         */
        public boolean equalsKey(final K key) {
            return ObjectKit.equals(getKey(), key);
        }

        /**
         * Traverses all descendant nodes in breadth-first order.
         *
         * @param includeCurrent If true, the current node is included in the traversal.
         * @param consumer       The action to perform on each descendant.
         * @param breakTraverse  A predicate to stop the traversal.
         * @return The last node visited before the traversal was stopped.
         */
        TreeEntryNode<K, V> traverseChildNodes(
                final boolean includeCurrent,
                final BiConsumer<Integer, TreeEntryNode<K, V>> consumer,
                BiPredicate<Integer, TreeEntryNode<K, V>> breakTraverse) {
            breakTraverse = ObjectKit.defaultIfNull(breakTraverse, (i, n) -> false);
            final Deque<List<TreeEntryNode<K, V>>> keyNodeDeque = new LinkedList<>(List.of(List.of(this)));
            boolean needProcess = includeCurrent;
            int index = includeCurrent ? 0 : 1;
            TreeEntryNode<K, V> lastNode = null;
            while (!keyNodeDeque.isEmpty()) {
                final List<TreeEntryNode<K, V>> curr = keyNodeDeque.removeFirst();
                final List<TreeEntryNode<K, V>> next = new ArrayList<>();
                for (final TreeEntryNode<K, V> node : curr) {
                    if (needProcess) {
                        consumer.accept(index, node);
                        if (breakTraverse.test(index, node)) {
                            return node;
                        }
                    } else {
                        needProcess = true;
                    }
                    next.addAll(node.children.values());
                }
                if (!next.isEmpty()) {
                    keyNodeDeque.addLast(next);
                }
                lastNode = CollKit.getLast(next);
                index++;
            }
            return lastNode;
        }

        /**
         * Adds a child node.
         *
         * @param child The child node to add.
         * @throws IllegalArgumentException if adding the child would create a circular reference.
         */
        void addChild(final TreeEntryNode<K, V> child) {
            if (containsChild(child.key)) {
                return;
            }

            // Check for circular references.
            traverseParentNodes(
                    true,
                    s -> Assert
                            .notEquals(s.key, child.key, "Circular reference between [{}] and [{}]!", s.key, this.key),
                    null);

            // Update child's properties.
            child.parent = this;
            child.traverseChildNodes(true, (i, c) -> {
                c.root = getRoot();
                c.weight = i + getWeight() + 1;
            }, null);

            // Add to this node's children.
            children.put(child.key, child);
        }

        /**
         * Removes a direct child node.
         *
         * @param key The key of the child to remove.
         */
        void removeDeclaredChild(final K key) {
            final TreeEntryNode<K, V> child = children.get(key);
            if (ObjectKit.isNull(child)) {
                return;
            }
            this.children.remove(key);

            // Reset child's properties.
            child.parent = null;
            child.traverseChildNodes(true, (i, c) -> {
                c.root = child;
                c.weight = i;
            }, null);
        }

        /**
         * Getchild method.
         *
         * @return the TreeEntryNode&lt;K, V&gt; value
         */
        @Override
        public TreeEntryNode<K, V> getChild(final K key) {
            return traverseChildNodes(false, (i, c) -> {
            }, (i, c) -> c.equalsKey(key));
        }

        /**
         * Getdeclaredchildren method.
         */
        @Override
        public Map<K, TreeEntry<K, V>> getDeclaredChildren() {
            return new LinkedHashMap<>(this.children);
        }

        /**
         * Getchildren method.
         */
        @Override
        public Map<K, TreeEntry<K, V>> getChildren() {
            final Map<K, TreeEntry<K, V>> childrenMap = new LinkedHashMap<>();
            traverseChildNodes(false, (i, c) -> childrenMap.put(c.getKey(), c), null);
            return childrenMap;
        }

        /**
         * Clears all references (parent, children, root) from this node.
         */
        void clear() {
            this.root = null;
            this.children.clear();
            this.parent = null;
        }

        /**
         * Checks if this object equals another object.
         *
         * @param o the object to compare with
         * @return true if the objects are equal, false otherwise
         */
        @Override
        public boolean equals(final Object o) {
            if (this == o)
                return true;
            if (!(o instanceof TreeEntry<?, ?> treeEntry))
                return false;
            return ObjectKit.equals(this.getKey(), treeEntry.getKey());
        }

        /**
         * Returns the hash code value for this object.
         *
         * @return the hash code value
         */
        @Override
        public int hashCode() {
            return Objects.hash(getKey());
        }

        /**
         * Creates a copy of the current node.
         *
         * @param value The value for the new copied node.
         * @return The new node.
         */
        TreeEntryNode<K, V> copy(final V value) {
            final TreeEntryNode<K, V> copiedNode = new TreeEntryNode<>(this.parent, this.key,
                    ObjectKit.defaultIfNull(value, this.value));
            copiedNode.children.putAll(children);
            return copiedNode;
        }

    }

    /**
     * A wrapper for a {@link TreeEntryNode} to present it as a {@link Map.Entry}.
     *
     * @param <K> The key type.
     * @param <V> The value type.
     * @param <N> The type of the wrapped {@link TreeEntry}.
     */
    public static class EntryNodeWrapper<K, V, N extends TreeEntry<K, V>>
            implements Map.Entry<K, TreeEntry<K, V>>, Wrapper<N> {

        private final N entryNode;

        EntryNodeWrapper(final N entryNode) {
            this.entryNode = entryNode;
        }

        /**
         * Gets the key of this entry.
         *
         * @return the key
         */
        @Override
        public K getKey() {
            return entryNode.getKey();
        }

        /**
         * Gets the value of this entry.
         *
         * @return the value
         */
        @Override
        public TreeEntry<K, V> getValue() {
            return entryNode;
        }

        /**
         * Sets the value of this entry.
         *
         * @param value the new value
         * @return the old value
         */
        @Override
        public TreeEntry<K, V> setValue(final TreeEntry<K, V> value) {
            throw new UnsupportedOperationException();
        }

        /**
         * Gets the raw wrapped object.
         *
         * @return the raw object
         */
        @Override
        public N getRaw() {
            return entryNode;
        }
    }

}
