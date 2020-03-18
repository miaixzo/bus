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
package org.aoju.bus.core.lang.tree;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.TreeUtils;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 通过转换器将你的实体转化为TreeNodeMap节点实体 属性都存在此处,属性有序，可支持排序
 *
 * @param <T> ID类型
 * @author Kimi Liu
 * @version 5.6.9
 * @since JDK 1.8+
 */
public class TreeMap<T> extends LinkedHashMap<String, Object> implements Node<T> {

    private static final long serialVersionUID = 1L;

    private TreeEntity TreeEntity;
    private TreeMap<T> parent;

    public TreeMap() {
        this(null);
    }

    /**
     * 构造
     *
     * @param TreeEntity TreeNode配置
     */
    public TreeMap(TreeEntity TreeEntity) {
        super();
        this.TreeEntity = ObjectUtils.defaultIfNull(
                TreeEntity, TreeEntity.DEFAULT);
    }

    /**
     * 获取父节点
     *
     * @return 父节点
     * @since 5.2.4
     */
    public TreeMap<T> getParent() {
        return parent;
    }

    /**
     * 获取ID对应的节点，如果有多个ID相同的节点，只返回第一个。
     * 此方法只查找此节点及子节点，采用广度优先遍历。
     *
     * @param id ID
     * @return 节点
     */
    public TreeMap<T> getNode(T id) {
        return TreeUtils.getNode(this, id);
    }

    /**
     * 获取所有父节点名称列表
     * 比如有员工在研发一部，上级部门为研发部，接着上面有技术中心
     * 返回结果就是：[研发一部, 研发中心, 技术中心]
     *
     * @param id                 节点ID
     * @param includeCurrentNode 是否包含当前节点的名称
     * @return 所有父节点名称列表
     */
    public List<CharSequence> getParentsName(T id, boolean includeCurrentNode) {
        return TreeUtils.getParentsName(getNode(id), includeCurrentNode);
    }

    /**
     * 获取所有父节点名称列表
     * 比如有员工在研发一部，上级部门为研发部，接着上面有技术中心
     * 返回结果就是：[研发一部, 研发中心, 技术中心]
     *
     * @param includeCurrentNode 是否包含当前节点的名称
     * @return 所有父节点名称列表
     */
    public List<CharSequence> getParentsName(boolean includeCurrentNode) {
        return TreeUtils.getParentsName(this, includeCurrentNode);
    }

    /**
     * 设置父节点
     *
     * @param parent 父节点
     */
    public TreeMap<T> setParent(TreeMap<T> parent) {
        this.parent = parent;
        if (null != parent) {
            this.setParentId(parent.getId());
        }
        return this;
    }

    @Override
    public T getId() {
        return (T) this.get(TreeEntity.getIdKey());
    }

    @Override
    public TreeMap<T> setId(T id) {
        this.put(TreeEntity.getIdKey(), id);
        return this;
    }

    @Override
    public T getParentId() {
        return (T) this.get(TreeEntity.getParentIdKey());
    }

    @Override
    public TreeMap<T> setParentId(T parentId) {
        this.put(TreeEntity.getParentIdKey(), parentId);
        return this;
    }

    @Override
    public CharSequence getName() {
        return (CharSequence) this.get(TreeEntity.getNameKey());
    }

    @Override
    public TreeMap<T> setName(CharSequence name) {
        this.put(TreeEntity.getNameKey(), name);
        return this;
    }

    @Override
    public Comparable<?> getWeight() {
        return (Comparable<?>) this.get(TreeEntity.getWeightKey());
    }

    @Override
    public TreeMap<T> setWeight(Comparable<?> weight) {
        this.put(TreeEntity.getWeightKey(), weight);
        return this;
    }

    public List<TreeMap<T>> getChildren() {
        return (List<TreeMap<T>>) this.get(TreeEntity.getChildrenKey());
    }

    public void setChildren(List<TreeMap<T>> children) {
        this.put(TreeEntity.getChildrenKey(), children);
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

}