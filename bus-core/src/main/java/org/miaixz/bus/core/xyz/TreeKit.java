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
package org.miaixz.bus.core.xyz;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.miaixz.bus.core.tree.MapTree;
import org.miaixz.bus.core.tree.NodeConfig;
import org.miaixz.bus.core.tree.TreeBuilder;
import org.miaixz.bus.core.tree.TreeNode;
import org.miaixz.bus.core.tree.parser.DefaultNodeParser;
import org.miaixz.bus.core.tree.parser.NodeParser;

/**
 * Tree utility class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TreeKit {

    /**
     * Builds a tree with a single root node.
     *
     * @param list The source data collection.
     * @return A {@link MapTree}.
     */
    public static MapTree<Integer> buildSingle(final Iterable<TreeNode<Integer>> list) {
        return buildSingle(list, 0);
    }

    /**
     * Builds a tree structure.
     *
     * @param list The source data collection.
     * @return A list of root nodes.
     */
    public static List<MapTree<Integer>> build(final Iterable<TreeNode<Integer>> list) {
        return build(list, 0);
    }

    /**
     * Builds a tree with a single root node. It creates an empty node with the specified parent ID and then adds child
     * nodes recursively.
     *
     * @param <E>      The type of the ID.
     * @param list     The source data collection.
     * @param parentId The ID of the parent for the top-level nodes (e.g., 0).
     * @return A {@link MapTree}.
     */
    public static <E> MapTree<E> buildSingle(final Iterable<TreeNode<E>> list, final E parentId) {
        return buildSingle(list, parentId, NodeConfig.DEFAULT_CONFIG, new DefaultNodeParser<>());
    }

    /**
     * Builds a tree structure.
     *
     * @param <E>      The type of the ID.
     * @param list     The source data collection.
     * @param parentId The ID of the parent for the top-level nodes.
     * @return A list of root nodes.
     */
    public static <E> List<MapTree<E>> build(final Iterable<TreeNode<E>> list, final E parentId) {
        return build(list, parentId, NodeConfig.DEFAULT_CONFIG, new DefaultNodeParser<>());
    }

    /**
     * Builds a tree with a single root node.
     *
     * @param <T>        The type of the source data object.
     * @param <E>        The type of the ID.
     * @param list       The source data collection.
     * @param parentId   The ID of the parent for the top-level nodes.
     * @param nodeParser The parser to convert a data object to a tree node.
     * @return A {@link MapTree}.
     */
    public static <T, E> MapTree<E> buildSingle(
            final Iterable<T> list,
            final E parentId,
            final NodeParser<T, E> nodeParser) {
        return buildSingle(list, parentId, NodeConfig.DEFAULT_CONFIG, nodeParser);
    }

    /**
     * Builds a tree structure.
     *
     * @param <T>        The type of the source data object.
     * @param <E>        The type of the ID.
     * @param list       The source data collection.
     * @param parentId   The ID of the parent for the top-level nodes.
     * @param nodeParser The parser to convert a data object to a tree node.
     * @return A list of root nodes.
     */
    public static <T, E> List<MapTree<E>> build(
            final Iterable<T> list,
            final E parentId,
            final NodeParser<T, E> nodeParser) {
        return build(list, parentId, NodeConfig.DEFAULT_CONFIG, nodeParser);
    }

    /**
     * Builds a tree structure.
     *
     * @param <T>        The type of the source data object.
     * @param <E>        The type of the ID.
     * @param list       The source data collection.
     * @param rootId     The ID of the parent for the top-level nodes.
     * @param nodeConfig The configuration for the tree nodes.
     * @param nodeParser The parser to convert a data object to a tree node.
     * @return A list of root nodes.
     */
    public static <T, E> List<MapTree<E>> build(
            final Iterable<T> list,
            final E rootId,
            final NodeConfig nodeConfig,
            final NodeParser<T, E> nodeParser) {
        return buildSingle(list, rootId, nodeConfig, nodeParser).getChildren();
    }

    /**
     * Builds a tree with a single root node.
     *
     * @param <T>        The type of the source data object.
     * @param <E>        The type of the ID.
     * @param list       The source data collection.
     * @param rootId     The ID of the parent for the top-level nodes.
     * @param nodeConfig The configuration for the tree nodes.
     * @param nodeParser The parser to convert a data object to a tree node.
     * @return A {@link MapTree}.
     */
    public static <T, E> MapTree<E> buildSingle(
            final Iterable<T> list,
            final E rootId,
            final NodeConfig nodeConfig,
            final NodeParser<T, E> nodeParser) {
        return TreeBuilder.of(rootId, nodeConfig).append(list, nodeParser).build();
    }

    /**
     * Builds a tree structure from a map of pre-constructed tree nodes.
     *
     * @param <E>    The type of the ID.
     * @param map    The map of source data (ID -> MapTree).
     * @param rootId The ID of the parent for the top-level nodes.
     * @return A list of root nodes.
     */
    public static <E> List<MapTree<E>> build(final Map<E, MapTree<E>> map, final E rootId) {
        return buildSingle(map, rootId).getChildren();
    }

    /**
     * Builds a tree with a single root node from a map of pre-constructed tree nodes.
     *
     * @param <E>    The type of the ID.
     * @param map    The map of source data (ID -> MapTree).
     * @param rootId The ID of the root node.
     * @return A {@link MapTree}.
     */
    public static <E> MapTree<E> buildSingle(final Map<E, MapTree<E>> map, final E rootId) {
        final MapTree<E> tree = CollKit.getFirstNoneNull(map.values());
        if (null != tree) {
            final NodeConfig config = tree.getConfig();
            return TreeBuilder.of(rootId, config).append(map).build();
        }
        return createEmptyNode(rootId);
    }

    /**
     * Gets a node by its ID using a depth-first search.
     *
     * @param <T>  The type of the ID.
     * @param node The starting node.
     * @param id   The ID to find.
     * @return The found node, or `null`.
     */
    public static <T> MapTree<T> getNode(final MapTree<T> node, final T id) {
        if (null == node) {
            return null;
        }
        if (ObjectKit.equals(id, node.getId())) {
            return node;
        }

        final List<MapTree<T>> children = node.getChildren();
        if (null == children) {
            return null;
        }

        MapTree<T> childNode;
        for (final MapTree<T> child : children) {
            childNode = child.getNode(id);
            if (null != childNode) {
                return childNode;
            }
        }
        return null;
    }

    /**
     * Gets a list of names of all parent nodes.
     *
     * @param <T>                The type of the node ID.
     * @param node               The node.
     * @param includeCurrentNode If `true`, includes the name of the current node.
     * @return A list of all parent node names.
     */
    public static <T> List<CharSequence> getParentsName(final MapTree<T> node, final boolean includeCurrentNode) {
        return getParents(node, includeCurrentNode, MapTree::getName);
    }

    /**
     * Gets a list of IDs of all parent nodes.
     *
     * @param <T>                The type of the node ID.
     * @param node               The node.
     * @param includeCurrentNode If `true`, includes the ID of the current node.
     * @return A list of all parent node IDs.
     */
    public static <T> List<T> getParentsId(final MapTree<T> node, final boolean includeCurrentNode) {
        return getParents(node, includeCurrentNode, MapTree::getId);
    }

    /**
     * Gets a list of a specific field's value from all parent nodes.
     *
     * @param <T>                The type of the node ID.
     * @param <E>                The type of the field value.
     * @param node               The node.
     * @param includeCurrentNode If `true`, includes the value from the current node.
     * @param fieldFunc          The function to extract the field value.
     * @return A list of field values from all parent nodes.
     */
    public static <T, E> List<E> getParents(
            final MapTree<T> node,
            final boolean includeCurrentNode,
            final Function<MapTree<T>, E> fieldFunc) {
        final List<E> result = new ArrayList<>();
        if (null == node) {
            return result;
        }

        if (includeCurrentNode) {
            result.add(fieldFunc.apply(node));
        }

        MapTree<T> parent = node.getParent();
        E fieldValue;
        while (null != parent) {
            fieldValue = fieldFunc.apply(parent);
            parent = parent.getParent();
            if (null != fieldValue || null != parent) {
                result.add(fieldValue);
            }
        }
        return result;
    }

    /**
     * Creates an empty tree node with a given ID.
     *
     * @param id  The node ID.
     * @param <E> The type of the node ID.
     * @return A {@link MapTree}.
     */
    public static <E> MapTree<E> createEmptyNode(final E id) {
        return new MapTree<E>().setId(id);
    }

    /**
     * Flattens a tree into a list.
     *
     * @param root       The root node of the tree.
     * @param broadFirst If `true`, uses breadth-first traversal; otherwise, depth-first.
     * @param <E>        The type of the node ID.
     * @return A list of all nodes in the tree.
     */
    public static <E> List<MapTree<E>> toList(final MapTree<E> root, final boolean broadFirst) {
        if (Objects.isNull(root)) {
            return null;
        }
        final List<MapTree<E>> list = new ArrayList<>();
        root.walk(list::add, broadFirst);

        return list;
    }

}
