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

import java.io.Serial;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.Builder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.tree.parser.NodeParser;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * A builder for creating tree structures from a flat list of nodes.
 *
 * @param <E> The type of the node identifier.
 * @author Kimi Liu
 * @since Java 17+
 */
public class TreeBuilder<E> implements Builder<MapTree<E>> {

    @Serial
    private static final long serialVersionUID = 2852250515557L;

    private final Map<E, MapTree<E>> idTreeMap;
    private boolean isBuild;
    private MapTree<E> root;

    /**
     * Constructs a new TreeBuilder with a specified root node.
     *
     * @param root The root node of the tree.
     */
    public TreeBuilder(final MapTree<E> root) {
        this.root = root;
        this.idTreeMap = new LinkedHashMap<>();
    }

    /**
     * Constructs a new TreeBuilder with a specified root ID and configuration.
     *
     * @param rootId The ID of the root node.
     * @param config The configuration for the tree nodes.
     */
    public TreeBuilder(final E rootId, final NodeConfig config) {
        this(new MapTree<E>(config).setId(rootId));
    }

    /**
     * Creates a new {@code TreeBuilder} with a specified root ID.
     *
     * @param <T>    The type of the node identifier.
     * @param rootId The ID of the root node.
     * @return A new {@code TreeBuilder} instance.
     */
    public static <T> TreeBuilder<T> of(final T rootId) {
        return of(rootId, null);
    }

    /**
     * Creates a new {@code TreeBuilder} with a specified root ID and configuration.
     *
     * @param <T>    The type of the node identifier.
     * @param rootId The ID of the root node.
     * @param config The configuration for the tree nodes.
     * @return A new {@code TreeBuilder} instance.
     */
    public static <T> TreeBuilder<T> of(final T rootId, final NodeConfig config) {
        return new TreeBuilder<>(rootId, config);
    }

    /**
     * Sets the ID of the root node.
     *
     * @param id The ID to set.
     * @return this
     */
    public TreeBuilder<E> setId(final E id) {
        this.root.setId(id);
        return this;
    }

    /**
     * Sets the parent ID of the root node.
     *
     * @param parentId The parent ID to set.
     * @return this
     */
    public TreeBuilder<E> setParentId(final E parentId) {
        this.root.setParentId(parentId);
        return this;
    }

    /**
     * Sets the name of the root node.
     *
     * @param name The name to set.
     * @return this
     */
    public TreeBuilder<E> setName(final CharSequence name) {
        this.root.setName(name);
        return this;
    }

    /**
     * Sets the weight of the root node.
     *
     * @param weight The weight to set.
     * @return this
     */
    public TreeBuilder<E> setWeight(final Comparable<?> weight) {
        this.root.setWeight(weight);
        return this;
    }

    /**
     * Adds an extra property to the root node.
     *
     * @param key   The property key.
     * @param value The property value.
     * @return this
     */
    public TreeBuilder<E> putExtra(final String key, final Object value) {
        Assert.notEmpty(key, "Key must be not empty !");
        this.root.put(key, value);
        return this;
    }

    /**
     * Appends a map of nodes to be included in the tree.
     *
     * @param map A map where the key is the node ID and the value is the node itself.
     * @return this
     */
    public TreeBuilder<E> append(final Map<E, MapTree<E>> map) {
        checkBuilt();

        this.idTreeMap.putAll(map);
        return this;
    }

    /**
     * Appends an iterable collection of nodes to be included in the tree.
     *
     * @param trees The iterable collection of nodes.
     * @return this
     */
    public TreeBuilder<E> append(final Iterable<MapTree<E>> trees) {
        checkBuilt();
        if (null != trees) {
            append(trees.iterator());
        }
        return this;
    }

    /**
     * Appends nodes from an iterator to be included in the tree.
     *
     * @param iterator The iterator providing the nodes.
     * @return this
     */
    public TreeBuilder<E> append(final Iterator<MapTree<E>> iterator) {
        checkBuilt();

        MapTree<E> tree;
        while (iterator.hasNext()) {
            tree = iterator.next();
            if (null != tree) {
                this.idTreeMap.put(tree.getId(), tree);
            }
        }

        return this;
    }

    /**
     * Appends a list of beans, converting them to tree nodes using a parser.
     *
     * @param <T>        The type of the bean.
     * @param list       The list of beans.
     * @param nodeParser The parser to convert a bean to a {@link MapTree} node.
     * @return this
     */
    public <T> TreeBuilder<E> append(final Iterable<T> list, final NodeParser<T, E> nodeParser) {
        checkBuilt();

        final NodeConfig config = this.root.getConfig();
        final Iterator<T> iterator = list.iterator();
        return append(new Iterator<>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public MapTree<E> next() {
                final MapTree<E> node = new MapTree<>(config);
                nodeParser.parse(iterator.next(), node);

                if (ObjectKit.equals(node.getId(), root.getId())) {
                    // If the provided list contains the root node, reuse it.
                    TreeBuilder.this.root = node;
                    return null;
                }

                return node;
            }
        });
    }

    /**
     * Resets the builder to its initial state, allowing it to be reused.
     *
     * @return this
     */
    public TreeBuilder<E> reset() {
        this.idTreeMap.clear();
        this.root.setChildren(null);
        this.isBuild = false;
        return this;
    }

    /**
     * Builds the tree structure.
     *
     * @return The root node of the built tree.
     */
    @Override
    public MapTree<E> build() {
        checkBuilt();

        buildFromMap();
        cutTree();

        this.isBuild = true;
        this.idTreeMap.clear();

        return root;
    }

    /**
     * Builds a list of top-level trees (forest). This is useful when there is no single root node. For example:
     * 
     * <pre>
     * - User Management
     *   - User List
     *   - Add User
     * - Department Management
     *   - Department List
     *   - Add Department
     * </pre>
     *
     * @return A list of root nodes for the forest.
     */
    public List<MapTree<E>> buildList() {
        if (isBuild) {
            // Already built
            return this.root.getChildren();
        }
        return build().getChildren();
    }

    /**
     * Builds the tree from the internal map of nodes.
     */
    private void buildFromMap() {
        if (MapKit.isEmpty(this.idTreeMap)) {
            return;
        }

        final Map<E, MapTree<E>> eTreeMap = MapKit.sortByValue(this.idTreeMap, false);
        E parentId;
        for (final MapTree<E> node : eTreeMap.values()) {
            if (null == node) {
                continue;
            }
            parentId = node.getParentId();
            if (ObjectKit.equals(this.root.getId(), parentId)) {
                this.root.addChildren(node);
                continue;
            }

            final MapTree<E> parentNode = eTreeMap.get(parentId);
            if (null != parentNode) {
                parentNode.addChildren(node);
            }
        }
    }

    /**
     * Prunes the tree to a specified depth.
     */
    private void cutTree() {
        final NodeConfig config = this.root.getConfig();
        final Integer deep = config.getDeep();
        if (null == deep || deep < 0) {
            return;
        }
        cutTree(this.root, 0, deep);
    }

    /**
     * Recursively prunes the tree branches.
     *
     * @param tree        The current node.
     * @param currentDeep The current depth.
     * @param maxDeep     The maximum allowed depth.
     */
    private void cutTree(final MapTree<E> tree, final int currentDeep, final int maxDeep) {
        if (null == tree) {
            return;
        }
        if (currentDeep == maxDeep) {
            // Prune children
            tree.setChildren(null);
            return;
        }

        final List<MapTree<E>> children = tree.getChildren();
        if (CollKit.isNotEmpty(children)) {
            for (final MapTree<E> child : children) {
                cutTree(child, currentDeep + 1, maxDeep);
            }
        }
    }

    /**
     * Checks if the tree has already been built. Throws an {@link IllegalStateException} if it has.
     */
    private void checkBuilt() {
        Assert.isFalse(isBuild, "Current tree has been built.");
    }

}
