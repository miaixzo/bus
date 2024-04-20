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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 通过转换器将你的实体转化为TreeNodeMap节点实体 属性都存在此处,属性有序，可支持排序
 *
 * @param <T> ID类型
 * @author Kimi Liu
 * @since Java 17+
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
     * 此方法只查找此节点及子节点，采用广度优先遍历
     *
     * @param id ID
     * @return 节点
     */
    public Tree<T> getNode(T id) {
        return TreeKit.getNode(this, id);
    }

    /**
     * 获取所有父节点名称列表
     *
     * <p>
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
     *
     * <p>
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
     * @param children 子节点列表，如果为{@code null}表示移除子节点
     * @return this
     */
    public Tree<T> setChildren(List<Tree<T>> children) {
        if (null == children) {
            this.remove(nodeConfig.getChildrenKey());
        }
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
     * 是否有子节点，无子节点则此为叶子节点
     *
     * @return 是否有子节点
     */
    public boolean hasChild() {
        return CollKit.isNotEmpty(getChildren());
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

    /**
     * 递归树并处理子树下的节点：
     *
     * @param consumer 节点处理器
     */
    public void walk(Consumer<Tree<T>> consumer) {
        consumer.accept(this);
        final List<Tree<T>> children = getChildren();
        if (CollKit.isNotEmpty(children)) {
            children.forEach((tree) -> tree.walk(consumer));
        }
    }

    /**
     * 递归过滤并生成新的树
     * 通过{@link Predicate}指定的过滤规则，本节点或子节点满足过滤条件，则保留当前节点，否则抛弃节点及其子节点
     *
     * @param filter 节点过滤规则函数，只需处理本级节点本身即可
     * @return 过滤后的节点，{@code null} 表示不满足过滤要求，丢弃之
     */
    public Tree<T> filterNew(Predicate<Tree<T>> filter) {
        return cloneTree().filter(filter);
    }

    /**
     * 递归过滤当前树，注意此方法会修改当前树
     * 通过{@link Predicate}指定的过滤规则，本节点或子节点满足过滤条件，则保留当前节点，否则抛弃节点及其子节点
     *
     * @param filter 节点过滤规则函数，只需处理本级节点本身即可
     * @return 过滤后的节点，{@code null} 表示不满足过滤要求，丢弃之
     */
    public Tree<T> filter(Predicate<Tree<T>> filter) {
        if (filter.test(this)) {
            // 本节点满足，则包括所有子节点都保留
            return this;
        }
        final List<Tree<T>> children = getChildren();
        if (CollKit.isNotEmpty(children)) {
            // 递归过滤子节点
            final List<Tree<T>> filteredChildren = new ArrayList<>(children.size());
            Tree<T> filteredChild;
            for (Tree<T> child : children) {
                filteredChild = child.filter(filter);
                if (null != filteredChild) {
                    filteredChildren.add(filteredChild);
                }
            }
            if (CollKit.isNotEmpty(filteredChildren)) {
                // 子节点有符合过滤条件的节点，则本节点保留
                return this.setChildren(filteredChildren);
            } else {
                this.setChildren(null);
            }
        }

        // 子节点都不符合过滤条件，检查本节点
        return null;
    }

    /**
     * 递归克隆当前节点（即克隆整个树，保留字段值）
     * 注意，此方法只会克隆节点，节点属性如果是引用类型，不会克隆
     *
     * @return 新的节点
     */
    public Tree<T> cloneTree() {
        final Tree<T> result = ObjectKit.clone(this);
        result.setChildren(cloneChildren());
        return result;
    }

    /**
     * 递归复制子节点
     *
     * @return 新的子节点列表
     */
    private List<Tree<T>> cloneChildren() {
        final List<Tree<T>> children = getChildren();
        if (null == children) {
            return null;
        }
        final List<Tree<T>> newChildren = new ArrayList<>(children.size());
        children.forEach((t) -> newChildren.add(t.cloneTree()));
        return newChildren;
    }

    @Override
    public String toString() {
        final StringWriter stringWriter = new StringWriter();
        printTree(this, new PrintWriter(stringWriter), 0);
        return stringWriter.toString();
    }

}
