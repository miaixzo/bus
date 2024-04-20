/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.lang.tree;

import org.miaixz.bus.core.builder.Builder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.tree.parser.NodeParser;
import org.miaixz.bus.core.toolkit.CollKit;
import org.miaixz.bus.core.toolkit.MapKit;
import org.miaixz.bus.core.toolkit.ObjectKit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 树构建器
 *
 * @param <E> ID类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class TreeBuilder<E> implements Builder<Tree<E>> {

    private static final long serialVersionUID = 1L;

    private final Tree<E> root;
    private final Map<E, Tree<E>> idTreeMap;
    private boolean isBuild;

    /**
     * 构造
     *
     * @param rootId 根节点ID
     * @param config 配置
     */
    public TreeBuilder(final E rootId, final NodeConfig config) {
        root = new Tree<>(config);
        root.setId(rootId);
        this.idTreeMap = new LinkedHashMap<>();
    }

    /**
     * 创建Tree构建器
     *
     * @param rootId 根节点ID
     * @param <T>    ID类型
     * @return TreeBuilder
     */
    public static <T> TreeBuilder<T> of(T rootId) {
        return of(rootId, null);
    }

    /**
     * 创建Tree构建器
     *
     * @param rootId 根节点ID
     * @param config 配置
     * @param <T>    ID类型
     * @return TreeBuilder
     */
    public static <T> TreeBuilder<T> of(T rootId, NodeConfig config) {
        return new TreeBuilder<>(rootId, config);
    }

    /**
     * 设置ID
     *
     * @param id ID
     * @return this
     */
    public TreeBuilder<E> setId(E id) {
        this.root.setId(id);
        return this;
    }

    /**
     * 设置父节点ID
     *
     * @param parentId 父节点ID
     * @return this
     */
    public TreeBuilder<E> setParentId(E parentId) {
        this.root.setParentId(parentId);
        return this;
    }

    /**
     * 设置节点标签名称
     *
     * @param name 节点标签名称
     * @return this
     */
    public TreeBuilder<E> setName(CharSequence name) {
        this.root.setName(name);
        return this;
    }

    /**
     * 设置权重
     *
     * @param weight 权重
     * @return this
     */
    public TreeBuilder<E> setWeight(Comparable<?> weight) {
        this.root.setWeight(weight);
        return this;
    }

    /**
     * 扩展属性
     *
     * @param key   键
     * @param value 扩展值
     * @return this
     */
    public TreeBuilder<E> putExtra(String key, Object value) {
        Assert.notEmpty(key, "Key must be not empty !");
        this.root.put(key, value);
        return this;
    }

    /**
     * 增加节点列表，增加的节点是不带子节点的
     *
     * @param map 节点列表
     * @return this
     */
    public TreeBuilder<E> append(Map<E, Tree<E>> map) {
        checkBuilt();

        this.idTreeMap.putAll(map);
        return this;
    }

    /**
     * 增加节点列表，增加的节点是不带子节点的
     *
     * @param trees 节点列表
     * @return this
     */
    public TreeBuilder<E> append(Iterable<Tree<E>> trees) {
        checkBuilt();

        for (Tree<E> tree : trees) {
            this.idTreeMap.put(tree.getId(), tree);
        }
        return this;
    }

    /**
     * 增加节点列表，增加的节点是不带子节点的
     *
     * @param list       Bean列表
     * @param <T>        Bean类型
     * @param nodeParser 节点转换器，用于定义一个Bean如何转换为Tree节点
     * @return this
     */
    public <T> TreeBuilder<E> append(List<T> list, NodeParser<T, E> nodeParser) {
        return append(list, null, nodeParser);
    }

    /**
     * 增加节点列表，增加的节点是不带子节点的
     *
     * @param list       Bean列表
     * @param <T>        Bean类型
     * @param nodeParser 节点转换器，用于定义一个Bean如何转换为Tree节点
     * @return this
     */
    public <T> TreeBuilder<E> append(List<T> list, E rootId, NodeParser<T, E> nodeParser) {
        checkBuilt();

        final NodeConfig config = this.root.getConfig();
        final Map<E, Tree<E>> map = new LinkedHashMap<>(list.size(), 1);
        Tree<E> node;
        for (T t : list) {
            node = new Tree<>(config);
            nodeParser.parse(t, node);
            if (null != rootId && false == rootId.getClass().equals(node.getId().getClass())) {
                throw new IllegalArgumentException("rootId type is node.getId().getClass()!");
            }
            map.put(node.getId(), node);
        }
        return append(map);
    }

    /**
     * 重置Builder，实现复用
     *
     * @return this
     */
    public TreeBuilder<E> reset() {
        this.idTreeMap.clear();
        this.root.setChildren(null);
        this.isBuild = false;
        return this;
    }

    @Override
    public Tree<E> build() {
        checkBuilt();

        buildFromMap();
        cutTree();

        this.isBuild = true;
        this.idTreeMap.clear();

        return root;
    }

    /**
     * 构建树列表，没有顶层节点，例如：
     *
     * <pre>
     * -用户管理
     *  -用户管理
     *    +用户添加
     * - 部门管理
     *  -部门管理
     *    +部门添加
     * </pre>
     *
     * @return 树列表
     */
    public List<Tree<E>> buildList() {
        if (isBuild) {
            // 已经构建过了
            return this.root.getChildren();
        }
        return build().getChildren();
    }

    /**
     * 开始构建
     */
    private void buildFromMap() {
        if (MapKit.isEmpty(this.idTreeMap)) {
            return;
        }

        final Map<E, Tree<E>> eTreeMap = MapKit.sort(this.idTreeMap, false);
        List<Tree<E>> rootTreeList = CollKit.newArrayList();
        E parentId;
        for (Tree<E> node : eTreeMap.values()) {
            if (null == node) {
                continue;
            }
            parentId = node.getParentId();
            if (ObjectKit.equals(this.root.getId(), parentId)) {
                this.root.addChildren(node);
                rootTreeList.add(node);
                continue;
            }

            final Tree<E> parentNode = eTreeMap.get(parentId);
            if (null != parentNode) {
                parentNode.addChildren(node);
            }
        }
    }

    /**
     * 树剪枝
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
     * 树剪枝叶
     *
     * @param tree        节点
     * @param currentDepp 当前层级
     * @param maxDeep     最大层级
     */
    private void cutTree(Tree<E> tree, int currentDepp, int maxDeep) {
        if (null == tree) {
            return;
        }
        if (currentDepp == maxDeep) {
            // 剪枝
            tree.setChildren(null);
            return;
        }

        final List<Tree<E>> children = tree.getChildren();
        if (CollKit.isNotEmpty(children)) {
            for (Tree<E> child : children) {
                cutTree(child, currentDepp + 1, maxDeep);
            }
        }
    }

    /**
     * 检查是否已经构建
     */
    private void checkBuilt() {
        Assert.isFalse(isBuild, "Current tree has been built.");
    }

}
