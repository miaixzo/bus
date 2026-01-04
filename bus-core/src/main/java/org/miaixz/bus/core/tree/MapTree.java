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
package org.miaixz.bus.core.tree;

import java.io.PrintWriter;
import java.io.Serial;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.*;

/**
 * A tree node implementation that uses a {@link LinkedHashMap} to store its properties. This allows for ordered
 * properties and supports sorting.
 *
 * @param <T> The type of the node's identifier.
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapTree<T> extends LinkedHashMap<String, Object> implements Node<T> {

    @Serial
    private static final long serialVersionUID = 2852250137281L;

    /**
     * Configuration for the tree node.
     */
    private final NodeConfig nodeConfig;
    /**
     * The parent node of this node.
     */
    private MapTree<T> parent;

    /**
     * Default constructor. Initializes with default {@link NodeConfig}.
     */
    public MapTree() {
        this(null);
    }

    /**
     * Constructor with specified node configuration.
     *
     * @param nodeConfig The configuration for the tree node.
     */
    public MapTree(final NodeConfig nodeConfig) {
        this.nodeConfig = ObjectKit.defaultIfNull(nodeConfig, NodeConfig.DEFAULT_CONFIG);
    }

    /**
     * Recursively prints the tree structure to a {@link PrintWriter}.
     *
     * @param tree   The tree node to print.
     * @param writer The writer to print to.
     * @param intent The indentation level.
     */
    private static void printTree(final MapTree<?> tree, final PrintWriter writer, final int intent) {
        writer.println(
                StringKit.format("{}{}[{}]", StringKit.repeat(Symbol.C_SPACE, intent), tree.getName(), tree.getId()));
        writer.flush();

        final List<? extends MapTree<?>> children = tree.getChildren();
        if (CollKit.isNotEmpty(children)) {
            for (final MapTree<?> child : children) {
                printTree(child, writer, intent + 2);
            }
        }
    }

    /**
     * Gets the node configuration.
     *
     * @return The node configuration.
     */
    public NodeConfig getConfig() {
        return this.nodeConfig;
    }

    /**
     * Gets the parent node.
     *
     * @return The parent node.
     */
    public MapTree<T> getParent() {
        return parent;
    }

    /**
     * Sets the parent node. Also sets the parent ID.
     *
     * @param parent The parent node.
     * @return this
     */
    public MapTree<T> setParent(final MapTree<T> parent) {
        this.parent = parent;
        if (null != parent) {
            this.setParentId(parent.getId());
        }
        return this;
    }

    /**
     * Gets the node with the specified ID. This method searches this node and its children using a breadth-first
     * search. If multiple nodes have the same ID, the first one found is returned.
     *
     * @param id The ID of the node to find.
     * @return The found node, or {@code null} if not found.
     */
    public MapTree<T> getNode(final T id) {
        return TreeKit.getNode(this, id);
    }

    /**
     * Gets the names of all parent nodes for a given node ID. For example, if an employee is in "Dept 1", which is
     * under "R &amp; D", which is under "Tech Center", the result would be: ["Dept 1", "R &amp; D", "Tech Center"].
     *
     * @param id                 The ID of the starting node.
     * @param includeCurrentNode Whether to include the name of the current node in the list.
     * @return A list of parent node names.
     */
    public List<CharSequence> getParentsName(final T id, final boolean includeCurrentNode) {
        return TreeKit.getParentsName(getNode(id), includeCurrentNode);
    }

    /**
     * Gets the names of all parent nodes for the current node. For example, if an employee is in "Dept 1", which is
     * under "R &amp; D", which is under "Tech Center", the result would be: ["Dept 1", "R &amp; D", "Tech Center"].
     *
     * @param includeCurrentNode Whether to include the name of the current node in the list.
     * @return A list of parent node names.
     */
    public List<CharSequence> getParentsName(final boolean includeCurrentNode) {
        return TreeKit.getParentsName(this, includeCurrentNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getId() {
        return (T) this.get(nodeConfig.getIdKey());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapTree<T> setId(final T id) {
        this.put(nodeConfig.getIdKey(), id);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getParentId() {
        return (T) this.get(nodeConfig.getParentIdKey());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapTree<T> setParentId(final T parentId) {
        this.put(nodeConfig.getParentIdKey(), parentId);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CharSequence getName() {
        return (CharSequence) this.get(nodeConfig.getNameKey());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapTree<T> setName(final CharSequence name) {
        this.put(nodeConfig.getNameKey(), name);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparable<?> getWeight() {
        return (Comparable<?>) this.get(nodeConfig.getWeightKey());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapTree<T> setWeight(final Comparable<?> weight) {
        this.put(nodeConfig.getWeightKey(), weight);
        return this;
    }

    /**
     * Gets the list of all child nodes.
     *
     * @return The list of child nodes.
     */
    public List<MapTree<T>> getChildren() {
        return (List<MapTree<T>>) this.get(nodeConfig.getChildrenKey());
    }

    /**
     * Sets the child nodes, replacing any existing children.
     *
     * @param children The list of child nodes. If {@code null}, existing children are removed.
     * @return this
     */
    public MapTree<T> setChildren(final List<MapTree<T>> children) {
        if (null == children) {
            this.remove(nodeConfig.getChildrenKey());
        }
        this.put(nodeConfig.getChildrenKey(), children);
        return this;
    }

    /**
     * Checks if this node has any children. A node with no children is a leaf node.
     *
     * @return {@code true} if the node has children, {@code false} otherwise.
     */
    public boolean hasChild() {
        return CollKit.isNotEmpty(getChildren());
    }

    /**
     * Recursively traverses the tree and processes each node using a depth-first strategy.
     *
     * @param consumer The consumer to process each node.
     */
    public void walk(final Consumer<MapTree<T>> consumer) {
        walk(consumer, false);
    }

    /**
     * Recursively traverses the tree and processes each node.
     *
     * @param consumer   The consumer to process each node.
     * @param broadFirst If {@code true}, uses breadth-first traversal; otherwise, uses depth-first traversal.
     */
    public void walk(final Consumer<MapTree<T>> consumer, final boolean broadFirst) {
        if (broadFirst) { // Breadth-first traversal
            final Queue<MapTree<T>> queue = new LinkedList<>();
            queue.offer(this);
            while (!queue.isEmpty()) {
                final MapTree<T> node = queue.poll();
                consumer.accept(node);
                final List<MapTree<T>> children = node.getChildren();
                if (CollKit.isNotEmpty(children)) {
                    children.forEach(queue::offer);
                }
            }
        } else { // Depth-first traversal
            final Stack<MapTree<T>> stack = new Stack<>();
            stack.add(this);
            while (!stack.isEmpty()) {
                final MapTree<T> node = stack.pop();
                consumer.accept(node);
                final List<MapTree<T>> children = node.getChildren();
                if (CollKit.isNotEmpty(children)) {
                    for (int i = children.size() - 1; i >= 0; i--) {
                        stack.push(children.get(i));
                    }
                }
            }
        }
    }

    /**
     * Recursively filters the tree and creates a new tree. If a node or any of its children satisfy the predicate, the
     * node is kept. Otherwise, the node and its children are discarded.
     *
     * @param predicate The filtering logic. A node is kept if {@link Predicate#test(Object)} returns {@code true}.
     * @return The filtered new tree, or {@code null} if the root node does not match.
     * @see #filter(Predicate)
     */
    public MapTree<T> filterNew(final Predicate<MapTree<T>> predicate) {
        return cloneTree().filter(predicate);
    }

    /**
     * Recursively filters this tree in-place. If a node or any of its children satisfy the predicate, the node is kept.
     * Otherwise, the node and its children are discarded. This method modifies the current tree.
     *
     * @param predicate The filtering logic. A node is kept if {@link Predicate#test(Object)} returns {@code true}.
     * @return The filtered node, or {@code null} if the node does not match.
     * @see #filterNew(Predicate)
     */
    public MapTree<T> filter(final Predicate<MapTree<T>> predicate) {
        if (null == predicate || predicate.test(this)) {
            // If this node matches, all its children are kept.
            return this;
        }

        final List<MapTree<T>> children = getChildren();
        if (CollKit.isNotEmpty(children)) {
            // Recursively filter children
            final List<MapTree<T>> filteredChildren = new ArrayList<>(children.size());
            MapTree<T> filteredChild;
            for (final MapTree<T> child : children) {
                filteredChild = child.filter(predicate);
                if (null != filteredChild) {
                    filteredChildren.add(filteredChild);
                }
            }
            if (CollKit.isNotEmpty(filteredChildren)) {
                // If any child matches, this node is kept.
                return this.setChildren(filteredChildren);
            } else {
                this.setChildren(null);
            }
        }

        // If this node and all its children do not match, discard it.
        return null;
    }

    /**
     * Adds child nodes and sets this node as their parent.
     *
     * @param children The child nodes to add.
     * @return this
     */
    @SafeVarargs
    public final MapTree<T> addChildren(final MapTree<T>... children) {
        if (ArrayKit.isNotEmpty(children)) {
            List<MapTree<T>> childrenList = this.getChildren();
            if (null == childrenList) {
                childrenList = new ArrayList<>();
                setChildren(childrenList);
            }
            for (final MapTree<T> child : children) {
                child.setParent(this);
                childrenList.add(child);
            }
        }
        return this;
    }

    /**
     * Adds an extra property to the node.
     *
     * @param key   The property key.
     * @param value The property value.
     */
    public void putExtra(final String key, final Object value) {
        Assert.notEmpty(key, "Key must be not empty !");
        this.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringWriter stringWriter = new StringWriter();
        printTree(this, new PrintWriter(stringWriter), 0);
        return stringWriter.toString();
    }

    /**
     * Recursively clones this node and its entire subtree. Note that this is a shallow clone of the node's properties;
     * reference-type properties are not cloned.
     *
     * @return A new node representing the cloned tree.
     */
    public MapTree<T> cloneTree() {
        final MapTree<T> result = ObjectKit.clone(this);
        result.setChildren(cloneChildren(result));
        return result;
    }

    /**
     * Recursively clones the child nodes.
     *
     * @param parent The new parent for the cloned children.
     * @return A new list of cloned child nodes.
     */
    private List<MapTree<T>> cloneChildren(final MapTree<T> parent) {
        final List<MapTree<T>> children = getChildren();
        if (null == children) {
            return null;
        }
        final List<MapTree<T>> newChildren = new ArrayList<>(children.size());
        children.forEach((t) -> newChildren.add(t.cloneTree().setParent(parent)));
        return newChildren;
    }

}
