/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
 * 树工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TreeKit {

    /**
     * 构建单root节点树
     *
     * @param list 源数据集合
     * @return {@link MapTree}
     */
    public static MapTree<Integer> buildSingle(final List<TreeNode<Integer>> list) {
        return buildSingle(list, 0);
    }

    /**
     * 树构建
     *
     * @param list 源数据集合
     * @return List
     */
    public static List<MapTree<Integer>> build(final List<TreeNode<Integer>> list) {
        return build(list, 0);
    }

    /**
     * 构建单root节点树 它会生成一个以指定ID为ID的空的节点，然后逐级增加子节点。
     *
     * @param <E>      ID类型
     * @param list     源数据集合
     * @param parentId 最顶层父id值 一般为 0 之类
     * @return {@link MapTree}
     */
    public static <E> MapTree<E> buildSingle(final List<TreeNode<E>> list, final E parentId) {
        return buildSingle(list, parentId, NodeConfig.DEFAULT_CONFIG, new DefaultNodeParser<>());
    }

    /**
     * 树构建
     *
     * @param <E>      ID类型
     * @param list     源数据集合
     * @param parentId 最顶层父id值 一般为 0 之类
     * @return List
     */
    public static <E> List<MapTree<E>> build(final List<TreeNode<E>> list, final E parentId) {
        return build(list, parentId, NodeConfig.DEFAULT_CONFIG, new DefaultNodeParser<>());
    }

    /**
     * 构建单root节点树 它会生成一个以指定ID为ID的空的节点，然后逐级增加子节点。
     *
     * @param <T>        转换的实体 为数据源里的对象类型
     * @param <E>        ID类型
     * @param list       源数据集合
     * @param parentId   最顶层父id值 一般为 0 之类
     * @param nodeParser 转换器
     * @return {@link MapTree}
     */
    public static <T, E> MapTree<E> buildSingle(final List<T> list, final E parentId,
            final NodeParser<T, E> nodeParser) {
        return buildSingle(list, parentId, NodeConfig.DEFAULT_CONFIG, nodeParser);
    }

    /**
     * 树构建
     *
     * @param <T>        转换的实体 为数据源里的对象类型
     * @param <E>        ID类型
     * @param list       源数据集合
     * @param parentId   最顶层父id值 一般为 0 之类
     * @param nodeParser 转换器
     * @return List
     */
    public static <T, E> List<MapTree<E>> build(final List<T> list, final E parentId,
            final NodeParser<T, E> nodeParser) {
        return build(list, parentId, NodeConfig.DEFAULT_CONFIG, nodeParser);
    }

    /**
     * 树构建
     *
     * @param <T>        转换的实体 为数据源里的对象类型
     * @param <E>        ID类型
     * @param list       源数据集合
     * @param rootId     最顶层父id值 一般为 0 之类
     * @param nodeConfig 配置
     * @param nodeParser 转换器
     * @return List
     */
    public static <T, E> List<MapTree<E>> build(final List<T> list, final E rootId, final NodeConfig nodeConfig,
            final NodeParser<T, E> nodeParser) {
        return buildSingle(list, rootId, nodeConfig, nodeParser).getChildren();
    }

    /**
     * 构建单root节点树 它会生成一个以指定ID为ID的空的节点，然后逐级增加子节点。
     *
     * @param <T>        转换的实体 为数据源里的对象类型
     * @param <E>        ID类型
     * @param list       源数据集合
     * @param rootId     最顶层父id值 一般为 0 之类
     * @param nodeConfig 配置
     * @param nodeParser 转换器
     * @return {@link MapTree}
     */
    public static <T, E> MapTree<E> buildSingle(final List<T> list, final E rootId, final NodeConfig nodeConfig,
            final NodeParser<T, E> nodeParser) {
        return TreeBuilder.of(rootId, nodeConfig).append(list, nodeParser).build();
    }

    /**
     * 树构建，按照权重排序
     *
     * @param <E>    ID类型
     * @param map    源数据Map
     * @param rootId 最顶层父id值 一般为 0 之类
     * @return List
     */
    public static <E> List<MapTree<E>> build(final Map<E, MapTree<E>> map, final E rootId) {
        return buildSingle(map, rootId).getChildren();
    }

    /**
     * 单点树构建，按照权重排序 它会生成一个以指定ID为ID的空的节点，然后逐级增加子节点。
     *
     * @param <E>    ID类型
     * @param map    源数据Map
     * @param rootId 根节点id值 一般为 0 之类
     * @return {@link MapTree}
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
     * 获取ID对应的节点，如果有多个ID相同的节点，只返回第一个。 此方法只查找此节点及子节点，采用递归深度优先遍历。
     *
     * @param <T>  ID类型
     * @param node 节点
     * @param id   ID
     * @return 节点
     */
    public static <T> MapTree<T> getNode(final MapTree<T> node, final T id) {
        if (ObjectKit.equals(id, node.getId())) {
            return node;
        }

        final List<MapTree<T>> children = node.getChildren();
        if (null == children) {
            return null;
        }

        // 查找子节点
        MapTree<T> childNode;
        for (final MapTree<T> child : children) {
            childNode = child.getNode(id);
            if (null != childNode) {
                return childNode;
            }
        }

        // 未找到节点
        return null;
    }

    /**
     * 获取所有父节点名称列表 比如有个人在研发1部，他上面有研发部，接着上面有技术中心 返回结果就是：[研发一部, 研发中心, 技术中心]
     *
     * @param <T>                节点ID类型
     * @param node               节点
     * @param includeCurrentNode 是否包含当前节点的名称
     * @return 所有父节点名称列表，node为null返回空List
     */
    public static <T> List<CharSequence> getParentsName(final MapTree<T> node, final boolean includeCurrentNode) {
        return getParents(node, includeCurrentNode, MapTree::getName);
    }

    /**
     * 获取所有父节点ID列表 比如有个人在研发1部，他上面有研发部，接着上面有技术中心 返回结果就是：[研发部, 技术中心]
     *
     * @param <T>                节点ID类型
     * @param node               节点
     * @param includeCurrentNode 是否包含当前节点的名称
     * @return 所有父节点ID列表，node为null返回空List
     */
    public static <T> List<T> getParentsId(final MapTree<T> node, final boolean includeCurrentNode) {
        return getParents(node, includeCurrentNode, MapTree::getId);
    }

    /**
     * 获取所有父节点指定函数结果列表
     *
     * @param <T>                节点ID类型
     * @param <E>                字段值类型
     * @param node               节点
     * @param includeCurrentNode 是否包含当前节点的名称
     * @param fieldFunc          获取父节点名称的函数
     * @return 所有父节点字段值列表，node为null返回空List
     */
    public static <T, E> List<E> getParents(final MapTree<T> node, final boolean includeCurrentNode,
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
                // 根节点的null不加入
                result.add(fieldValue);
            }
        }
        return result;
    }

    /**
     * 获取所有父节点ID列表 创建空Tree的节点
     *
     * @param id  节点ID
     * @param <E> 节点ID类型
     * @return {@link MapTree}
     */
    public static <E> MapTree<E> createEmptyNode(final E id) {
        return new MapTree<E>().setId(id);
    }

    /**
     * 深度优先,遍历树,将树换为数组
     *
     * @param root       树的根节点
     * @param broadFirst 是否广度优先遍历
     * @param <E>        节点ID类型
     * @return 树所有节点列表
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
