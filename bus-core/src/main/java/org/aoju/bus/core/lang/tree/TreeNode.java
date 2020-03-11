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

/**
 * 树节点 每个属性都可以在{@link TreeEntity}中被重命名
 * 在你的项目里它可以是部门实体、地区实体等任意类树节点实体
 * 类树节点实体: 包含key，父Key.不限于这些属性的可以构造成一颗树的实体对象
 *
 * @author Kimi Liu
 * @version 5.6.8
 * @since JDK 1.8+
 */
public class TreeNode<T> implements Comparable<TreeMap<T>> {

    /**
     * ID
     */
    private T id;

    /**
     * 父节点ID
     */
    private T parentId;

    /**
     * 名称
     */
    private CharSequence name;

    /**
     * 顺序 越小优先级越高 默认0
     */
    private Comparable<?> weight = 0;


    /**
     * 空构造
     */
    public TreeNode() {
    }

    /**
     * 构造
     *
     * @param id       ID
     * @param parentId 父节点ID
     * @param name     名称
     * @param weight   权重
     */
    public TreeNode(T id, T parentId, String name, Comparable<?> weight) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        if (weight != null) {
            this.weight = weight;
        }

    }

    /**
     * 获取ID
     *
     * @return ID
     */
    public T getId() {
        return id;
    }

    /**
     * 设置ID
     *
     * @param id ID
     */
    public void setId(T id) {
        this.id = id;
    }

    /**
     * 获取父节点ID
     *
     * @return 父节点ID
     */
    public T getParentId() {
        return this.parentId;
    }

    /**
     * 设置父节点ID
     *
     * @param parentId 父节点ID
     * @return 父节点ID
     */
    public TreeNode<T> setParentId(T parentId) {
        this.parentId = parentId;
        return this;
    }

    /**
     * 获取节点标签名称
     *
     * @return 节点标签名称
     */
    public CharSequence getName() {
        return name;
    }

    /**
     * 设置节点标签名称
     *
     * @param name 节点标签名称
     * @return this
     */
    public TreeNode<T> setName(CharSequence name) {
        this.name = name;
        return this;
    }

    /**
     * 获取权重
     *
     * @return 权重
     */
    public Comparable<?> getWeight() {
        return weight;
    }

    /**
     * 设置权重
     *
     * @param weight 权重
     * @return this
     */
    public TreeNode<T> setWeight(Comparable<?> weight) {
        this.weight = weight;
        return this;
    }

    @Override
    public int compareTo(TreeMap treeMap) {
        final Comparable weight = this.getWeight();
        if (null != weight) {
            final Comparable weightOther = treeMap.getWeight();
            return weight.compareTo(weightOther);
        }
        return 0;
    }

}
