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

import java.util.*;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.function.BiConsumerX;
import org.miaixz.bus.core.center.function.BiPredicateX;
import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.center.function.PredicateX;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Wrapper;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * An insertion-ordered {@link ForestMap} of keyed nodes with explicit parent-child relationships.
 *
 * <p>
 * This collection can be viewed as a {@link LinkedHashMap} where the key is {@link TreeEntryNode#getKey()} and the
 * value is a {@link TreeEntryNode} instance. Each key-value pair is treated as a {@link TreeEntryNode}, and using the
 * same key will always access the same node.
 *
 * <p>
 * Nodes form parent-child relationships through their keys, ultimately creating a multi-way tree structure. Multiple
 * parallel multi-way trees constitute a forest within this collection. Users can manipulate or access the forest
 * through the methods of {@link ForestMap} itself, or by obtaining a {@link TreeEntry} and using the node's own
 * methods. This class is not thread-safe. The key set is backed by the forest and preserves relationship cleanup on
 * removal; the values and entry sets are detached collection snapshots containing the existing node objects.
 *
 * @param <K> The type of the keys.
 * @param <V> The type of the values.
 * @author Kimi Liu
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
        detachNode(target);
        return target;
    }

    /**
     * Disconnects a removed node while preserving each direct child's subtree. Children of a removed root become roots;
     * children of an internal node are reattached to its former parent.
     *
     * @param target node already removed from the backing node collection
     */
    private void detachNode(final TreeEntryNode<K, V> target) {
        final TreeEntryNode<K, V> parent = target.getDeclaredParent();
        if (parent != null) {
            parent.removeDeclaredChild(target.getKey());
        }
        for (final TreeEntryNode<K, V> child : new ArrayList<>(target.children.values())) {
            target.removeDeclaredChild(child.getKey());
            if (parent != null) {
                parent.addChild(child);
            }
        }
        target.clear();
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
     * Returns a backed set of all node keys. Removing a key or using the iterator's removal operation also detaches the
     * removed node and reconnects its direct children as specified by {@link #remove(Object)}.
     *
     * @return The set of keys.
     */
    @Override
    public Set<K> keySet() {
        return new AbstractSet<>() {

            /**
             * Creates an insertion-ordered key iterator whose removal operation also updates node relationships.
             *
             * @return a backed iterator over the current node keys
             */
            @Override
            public Iterator<K> iterator() {
                final Iterator<TreeEntryNode<K, V>> iterator = nodes.values().iterator();
                return new Iterator<>() {

                    /**
                     * Last returned node whose relationships must be detached on removal.
                     */
                    private TreeEntryNode<K, V> last;

                    /**
                     * Tests whether the backing node iterator has another node.
                     *
                     * @return {@code true} if another node key is available
                     */
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    /**
                     * Advances to the next node and records it as the target of a subsequent removal.
                     *
                     * @return the next node's key
                     * @throws NoSuchElementException          if no node remains
                     * @throws ConcurrentModificationException if the backing forest was structurally modified outside
                     *                                         this iterator
                     */
                    @Override
                    public K next() {
                        last = iterator.next();
                        return last.getKey();
                    }

                    /**
                     * Removes the last returned node and detaches its relationships while preserving child subtrees.
                     * Direct children are reattached to the former parent, or become roots when the removed node was a
                     * root.
                     *
                     * @throws IllegalStateException           if no node was returned or it has already been removed
                     * @throws ConcurrentModificationException if the backing forest was structurally modified outside
                     *                                         this iterator
                     */
                    @Override
                    public void remove() {
                        if (last == null) {
                            throw new IllegalStateException();
                        }
                        iterator.remove();
                        detachNode(last);
                        last = null;
                    }
                };
            }

            /**
             * Returns the number of current node keys in the backing forest.
             *
             * @return the current node count
             */
            @Override
            public int size() {
                return nodes.size();
            }

            /**
             * Tests whether the backing forest currently contains a node with the supplied key.
             *
             * @param key node key to locate
             * @return {@code true} if the node exists
             */
            @Override
            public boolean contains(final Object key) {
                return nodes.containsKey(key);
            }

            /**
             * Removes the node with the supplied key through the forest's relationship-aware removal operation.
             *
             * @param key key of the node to remove
             * @return {@code true} if a node was removed
             */
            @Override
            public boolean remove(final Object key) {
                return LinkedForestMap.this.remove(key) != null;
            }

            /**
             * Removes all forest nodes and clears their parent, child, root, and depth relationships.
             */
            @Override
            public void clear() {
                LinkedForestMap.this.clear();
            }
        };
    }

    /**
     * Returns a detached collection snapshot containing the current node objects. Removing snapshot elements does not
     * remove forest nodes; modifying a retained node still modifies that node.
     *
     * @return The collection of entries.
     */
    @Override
    public Collection<TreeEntry<K, V>> values() {
        return new ArrayList<>(nodes.values());
    }

    /**
     * Returns a detached set snapshot of key-to-node wrappers. Set changes do not modify the forest; node objects are
     * still shared. Replacing a wrapped node through {@link Map.Entry#setValue(Object)} is not supported.
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
     * @throws IllegalArgumentException if changing an existing parent is disallowed or the link would create a cycle
     */
    @Override
    public void linkNodes(final K parentKey, final K childKey, BiConsumerX<TreeEntry<K, V>, TreeEntry<K, V>> consumer) {
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
     * @author Kimi Liu
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
         * Gets the depth of this node relative to its root.
         *
         * @return the depth, with zero representing a root node
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
         * @return the node matching the stop predicate, or {@code null} when the ancestor chain is exhausted
         */
        TreeEntryNode<K, V> traverseParentNodes(
                final boolean includeCurrent,
                final ConsumerX<TreeEntryNode<K, V>> consumer,
                PredicateX<TreeEntryNode<K, V>> breakTraverse) {
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
         * Tests whether this node is the root node of its tree.
         *
         * @return {@code true} if this node is a root node (has no parent).
         */
        public boolean isRoot() {
            return getRoot() == this;
        }

        /**
         * Returns the cached root, or discovers and caches the topmost ancestor if the cached root was cleared.
         *
         * @return the root node, including this node when it has no parent
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
         * Returns this node's direct parent.
         *
         * @return the direct parent, or {@code null} for a root node
         */
        @Override
        public TreeEntryNode<K, V> getDeclaredParent() {
            return parent;
        }

        /**
         * Searches ancestors, starting with the direct parent, for the specified key.
         *
         * @param key ancestor key to locate
         * @return the matching ancestor, or {@code null} if none matches
         */
        @Override
        public TreeEntryNode<K, V> getParent(final K key) {
            return traverseParentNodes(false, p -> {
            }, p -> p.equalsKey(key));
        }

        /**
         * Visits descendants in breadth-first order, optionally including this node.
         *
         * @param includeSelf  whether to visit this node before its descendants
         * @param nodeConsumer action applied to each visited node
         */
        @Override
        public void forEachChild(final boolean includeSelf, final ConsumerX<TreeEntry<K, V>> nodeConsumer) {
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
         * @param consumer       action applied with the traversal level and visited node
         * @param breakTraverse  optional predicate that stops traversal after the current node's action
         * @return the node matching the stop predicate, or {@code null} when traversal completes without a match
         */
        TreeEntryNode<K, V> traverseChildNodes(
                final boolean includeCurrent,
                final BiConsumerX<Integer, TreeEntryNode<K, V>> consumer,
                BiPredicateX<Integer, TreeEntryNode<K, V>> breakTraverse) {
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
         * Detaches a direct child without discarding its subtree. The child becomes its subtree's root, and descendant
         * root references and depths are recalculated.
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
         * Searches descendants in breadth-first order for the specified key.
         *
         * @param key descendant key to locate
         * @return the first matching descendant, or {@code null} if none matches
         */
        @Override
        public TreeEntryNode<K, V> getChild(final K key) {
            return traverseChildNodes(false, (i, c) -> {
            }, (i, c) -> c.equalsKey(key));
        }

        /**
         * Copies the direct-child mappings into a new insertion-ordered map. The child nodes themselves remain shared.
         *
         * @return a detached map containing the direct child nodes
         */
        @Override
        public Map<K, TreeEntry<K, V>> getDeclaredChildren() {
            return new LinkedHashMap<>(this.children);
        }

        /**
         * Collects all descendant mappings into a new map in breadth-first encounter order. Node objects remain shared.
         *
         * @return a detached map containing all descendant nodes
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
            this.weight = 0;
        }

        /**
         * Compares this node with another tree entry using the node key, without comparing values or relationships.
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
         * Creates a shallow node copy using the supplied non-null value, or this node's value when it is null. Parent
         * and child node objects remain shared rather than being recursively copied.
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
     * @author Kimi Liu
     */
    public static class EntryNodeWrapper<K, V, N extends TreeEntry<K, V>>
            implements Map.Entry<K, TreeEntry<K, V>>, Wrapper<N> {

        /**
         * The wrapped tree entry node exposed through the {@link Map.Entry} view.
         */
        private final N entryNode;

        /**
         * Creates a map entry view for the supplied tree entry node.
         *
         * @param entryNode the tree entry node to wrap
         */
        public EntryNodeWrapper(final N entryNode) {
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
         * Rejects replacement of the wrapped node. Node contents can be changed through the returned node itself.
         *
         * @param value the new value
         * @return never returns normally
         * @throws UnsupportedOperationException always
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
