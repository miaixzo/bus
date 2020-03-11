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

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 通过转换器将你的实体转化为TreeNodeMap节点实体 属性都存在此处,属性有序，可支持排序
 *
 * @param <T> ID类型
 * @author Kimi Liu
 * @version 5.6.8
 * @since JDK 1.8+
 */
public class TreeMap<T> extends LinkedHashMap<String, Object> implements Comparable<TreeMap<T>> {

    private static final long serialVersionUID = 1L;

    private TreeEntity treeEntity;

    public TreeMap() {
        this(null);
    }

    /**
     * 构造
     *
     * @param treeEntity TreeNode配置
     */
    public TreeMap(TreeEntity treeEntity) {
        super();
        this.treeEntity = ObjectUtils.defaultIfNull(
                treeEntity, TreeEntity.DEFAULT);
    }

    /**
     * 获取节点ID
     *
     * @return 节点ID
     */
    public T getId() {
        return (T) this.get(treeEntity.getIdKey());
    }

    /**
     * 设置节点ID
     *
     * @param id 节点ID
     * @return this
     */
    public TreeMap<T> setId(T id) {
        this.put(treeEntity.getIdKey(), id);
        return this;
    }

    /**
     * 获取父节点ID
     *
     * @return 父节点ID
     */
    public T getParentId() {
        return (T) this.get(treeEntity.getParentIdKey());
    }

    public TreeMap<T> setParentId(T parentId) {
        this.put(treeEntity.getParentIdKey(), parentId);
        return this;
    }

    public T getName() {
        return (T) this.get(treeEntity.getNameKey());
    }

    public TreeMap<T> setName(Object name) {
        this.put(treeEntity.getNameKey(), name);
        return this;
    }

    public Comparable<?> getWeight() {
        return (Comparable<?>) this.get(treeEntity.getWeightKey());
    }

    public TreeMap<T> setWeight(Comparable<?> weight) {
        this.put(treeEntity.getWeightKey(), weight);
        return this;
    }

    public List<TreeMap<T>> getChildren() {
        return (List<TreeMap<T>>) this.get(treeEntity.getChildrenKey());
    }

    public void setChildren(List<TreeMap<T>> children) {
        this.put(treeEntity.getChildrenKey(), children);
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
    public int compareTo(TreeMap<T> treeMap) {
        final Comparable weight = this.getWeight();
        if (null != weight) {
            final Comparable weightOther = treeMap.getWeight();
            return weight.compareTo(weightOther);
        }
        return 0;
    }

}