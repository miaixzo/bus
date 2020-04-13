/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.utils;

import org.aoju.bus.core.lang.tree.TreeEntity;
import org.aoju.bus.core.lang.tree.TreeMap;
import org.aoju.bus.core.lang.tree.TreeNode;
import org.aoju.bus.core.lang.tree.parser.DefaultNodeParser;
import org.aoju.bus.core.lang.tree.parser.NodeParser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 树工具类
 * 提供通用树生成，特点：
 * 1、每个字段可自定义
 * 2、支持排序 树深度配置,自定义转换器等
 * 3、支持额外属性扩展
 * 4、贴心 许多属性,特性都有默认值处理
 * 5、使用简单 可一行代码生成树
 * 6、代码简洁轻量无额外依赖
 *
 * @author Kimi Liu
 * @version 5.8.3
 * @since JDK 1.8+
 */
public class TreeUtils {

    /**
     * 树构建
     *
     * @param list 源数据集合
     * @return List
     */
    public static List<TreeMap<Integer>> build(List<TreeNode<Integer>> list) {
        return build(list, 0);
    }

    /**
     * 树构建
     *
     * @param <E>      ID类型
     * @param list     源数据集合
     * @param parentId 最顶层父id值 一般为 0 之类
     * @return List
     */
    public static <E> List<TreeMap<E>> build(List<TreeNode<E>> list, E parentId) {
        return build(list, parentId, TreeEntity.DEFAULT, new DefaultNodeParser<>());
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
    public static <T, E> List<TreeMap<E>> build(List<T> list, E parentId, NodeParser<T, E> nodeParser) {
        return build(list, parentId, TreeEntity.DEFAULT, nodeParser);
    }

    /**
     * 树构建
     *
     * @param <T>        转换的实体 为数据源里的对象类型
     * @param <E>        ID类型
     * @param list       源数据集合
     * @param parentId   最顶层父id值 一般为 0 之类
     * @param treeEntity 配置
     * @param nodeParser 转换器
     * @return List
     */
    public static <T, E> List<TreeMap<E>> build(List<T> list, E parentId, TreeEntity treeEntity, NodeParser<T, E> nodeParser) {
        List<TreeMap<E>> treeMapNodes = CollUtils.newArrayList();
        TreeMap<E> treeMap;
        for (T obj : list) {
            treeMap = new TreeMap<>(treeEntity);
            nodeParser.parse(obj, treeMap);
            treeMapNodes.add(treeMap);
        }

        List<TreeMap<E>> finalTreeMapNodes = CollUtils.newArrayList();
        for (TreeMap<E> node : treeMapNodes) {
            if (parentId.equals(node.getParentId())) {
                finalTreeMapNodes.add(node);
                innerBuild(treeMapNodes, node, 0, treeEntity.getDeep());
            }
        }
        // 内存每层已经排过了 这是最外层排序
        finalTreeMapNodes = finalTreeMapNodes.stream().sorted().collect(Collectors.toList());
        return finalTreeMapNodes;
    }

    /**
     * 递归处理
     *
     * @param treeMapNodes 数据集合
     * @param parentNode   当前节点
     * @param deep         已递归深度
     * @param maxDeep      最大递归深度 可能为null即不限制
     */
    private static <T> void innerBuild(List<TreeMap<T>> treeMapNodes, TreeMap<T> parentNode, int deep, Integer maxDeep) {

        if (CollUtils.isEmpty(treeMapNodes)) {
            return;
        }
        //maxDeep 可能为空
        if (maxDeep != null && deep >= maxDeep) {
            return;
        }

        // 每层排序 TreeNodeMap 实现了Comparable接口
        treeMapNodes = treeMapNodes.stream().sorted().collect(Collectors.toList());
        for (TreeMap<T> childNode : treeMapNodes) {
            if (parentNode.getId().equals(childNode.getParentId())) {
                List<TreeMap<T>> children = parentNode.getChildren();
                if (children == null) {
                    children = CollUtils.newArrayList();
                    parentNode.setChildren(children);
                }
                children.add(childNode);
                childNode.setParent(parentNode);
                innerBuild(treeMapNodes, childNode, deep + 1, maxDeep);
            }
        }
    }

    /**
     * 获取ID对应的节点，如果有多个ID相同的节点，只返回第一个
     * 此方法只查找此节点及子节点，采用广度优先遍历。
     *
     * @param <T>  对象
     * @param id   节点ID
     * @param node 节点信息
     * @return 节点
     */
    public static <T> TreeMap<T> getNode(TreeMap<T> node, T id) {
        if (ObjectUtils.equal(id, node.getId())) {
            return node;
        }

        // 查找子节点
        TreeMap<T> childNode;
        for (TreeMap<T> child : node.getChildren()) {
            childNode = child.getNode(id);
            if (null != childNode) {
                return childNode;
            }
        }

        // 未找到节点
        return null;
    }

    /**
     * 获取所有父节点名称列表
     * 比如员工在研发一部，部门上级有研发部，接着上级有技术中心
     * 返回结果就是：[研发一部, 研发中心, 技术中心]
     *
     * @param <T>                节点ID类型
     * @param node               节点
     * @param includeCurrentNode 是否包含当前节点的名称
     * @return 所有父节点名称列表，node为null返回空List
     */
    public static <T> List<CharSequence> getParentsName(TreeMap<T> node, boolean includeCurrentNode) {
        final List<CharSequence> result = new ArrayList<>();
        if (null == node) {
            return result;
        }

        if (includeCurrentNode) {
            result.add(node.getName());
        }

        TreeMap<T> parent = node.getParent();
        while (null != parent) {
            result.add(parent.getName());
            parent = parent.getParent();
        }
        return result;
    }

}
