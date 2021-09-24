/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.lang.tree;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 通过转换器将你的实体转化为TreeNodeMap节点实体 属性都存在此处,属性有序，可支持排序
 *
 * @param <T> ID类型
 * @author Kimi Liu
 * @version 6.2.9
 * @since JDK 1.8+
 */
public class Tree<T> extends LinkedHashMap<String, Object> implements Node<T> {

    private static final long serialVersionUID = 1L;

    private final NodeConfig nodeConfig;
    private Tree<T> parent;

    public Tree() {
        this(null);
    }

    /**
     * 构造
     *
     * @param nodeConfig TreeNode配置
     */
    public Tree(NodeConfig nodeConfig) {
        this.nodeConfig = ObjectKit.defaultIfNull(
                nodeConfig, NodeConfig.DEFAULT_CONFIG);
    }

    /**
     * 打印
     *
     * @param tree   树
     * @param writer Writer
     * @param intent 缩进量
     */
    private static void printTree(Tree<?> tree, PrintWriter writer, int intent) {
        writer.println(StringKit.format("{}{}[{}]", StringKit.repeat(Symbol.SPACE, intent), tree.getName(), tree.getId()));
        writer.flush();

        final List<? extends Tree<?>> children = tree.getChildren();
        if (CollKit.isNotEmpty(children)) {
            for (Tree<?> child : children) {
                printTree(child, writer, intent + 2);
            }
        }
    }

    /**
     * 获取节点配置
     *
     * @return 节点配置
     */
    public NodeConfig getConfig() {
        return this.nodeConfig;
    }

    /**
     * 获取父节点
     *
     * @return 父节点
     */
    public Tree<T> getParent() {
        return parent;
    }

    /**
     * 设置父节点
     *
     * @param parent 父节点
     * @return this
     */
    public Tree<T> setParent(Tree<T> parent) {
        this.parent = parent;
        if (null != parent) {
            this.setParentId(parent.getId());
        }
        return this;
    }

    /**
     * 获取ID对应的节点，如果有多个ID相同的节点，只返回第一个
     * 此方法只查找此节点及子节点，采用广度优先遍历。
     *
     * @param id ID
     * @return 节点
     */
    public Tree<T> getNode(T id) {
        return TreeKit.getNode(this, id);
    }

    /**
     * 获取所有父节点名称列表
     * 比如有个人在研发1部，他上面有研发部，接着上面有技术中心
     * 返回结果就是：[研发一部, 研发中心, 技术中心]
     *
     * @param id                 节点ID
     * @param includeCurrentNode 是否包含当前节点的名称
     * @return 所有父节点名称列表
     */
    public List<CharSequence> getParentsName(T id, boolean includeCurrentNode) {
        return TreeKit.getParentsName(getNode(id), includeCurrentNode);
    }

    /**
     * 获取所有父节点名称列表
     * 比如有个人在研发1部，他上面有研发部，接着上面有技术中心
     * 返回结果就是：[研发一部, 研发中心, 技术中心]
     *
     * @param includeCurrentNode 是否包含当前节点的名称
     * @return 所有父节点名称列表
     */
    public List<CharSequence> getParentsName(boolean includeCurrentNode) {
        return TreeKit.getParentsName(this, includeCurrentNode);
    }

    @Override
    public T getId() {
        return (T) this.get(nodeConfig.getIdKey());
    }

    @Override
    public Tree<T> setId(T id) {
        this.put(nodeConfig.getIdKey(), id);
        return this;
    }

    @Override
    public T getParentId() {
        return (T) this.get(nodeConfig.getParentIdKey());
    }

    @Override
    public Tree<T> setParentId(T parentId) {
        this.put(nodeConfig.getParentIdKey(), parentId);
        return this;
    }

    @Override
    public CharSequence getName() {
        return (CharSequence) this.get(nodeConfig.getNameKey());
    }

    @Override
    public Tree<T> setName(CharSequence name) {
        this.put(nodeConfig.getNameKey(), name);
        return this;
    }

    @Override
    public Comparable<?> getWeight() {
        return (Comparable<?>) this.get(nodeConfig.getWeightKey());
    }

    @Override
    public Tree<T> setWeight(Comparable<?> weight) {
        this.put(nodeConfig.getWeightKey(), weight);
        return this;
    }

    /**
     * 获取所有子节点
     *
     * @return 所有子节点
     */
    public List<Tree<T>> getChildren() {
        return (List<Tree<T>>) this.get(nodeConfig.getChildrenKey());
    }

    /**
     * 设置子节点，设置后会覆盖所有原有子节点
     *
     * @param children 子节点列表
     * @return this
     */
    public Tree<T> setChildren(List<Tree<T>> children) {
        this.put(nodeConfig.getChildrenKey(), children);
        return this;
    }

    /**
     * 增加子节点，同时关联子节点的父节点为当前节点
     *
     * @param children 子节点列表
     * @return this
     */
    @SafeVarargs
    public final Tree<T> addChildren(Tree<T>... children) {
        if (ArrayKit.isNotEmpty(children)) {
            List<Tree<T>> childrenList = this.getChildren();
            if (null == childrenList) {
                childrenList = new ArrayList<>();
                setChildren(childrenList);
            }
            for (Tree<T> child : children) {
                child.setParent(this);
                childrenList.add(child);
            }
        }
        return this;
    }

    /**
     * 扩展属性
     *
     * @param key   键
     * @param value 扩展值
     */
    public void putExtra(String key, Object value) {
        Assert.notEmpty(key, "Key must be not empty !");
        this.put(key, value);
    }

    @Override
    public String toString() {
        final StringWriter stringWriter = new StringWriter();
        printTree(this, new PrintWriter(stringWriter), 0);
        return stringWriter.toString();
    }

}
