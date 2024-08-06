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
package org.miaixz.bus.core.tree;

import java.io.Serializable;

import org.miaixz.bus.core.xyz.CompareKit;

/**
 * 节点接口，提供节点相关的的方法定义
 *
 * @param <T> ID类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Node<T> extends Comparable<Node<T>>, Serializable {

    /**
     * 获取ID
     *
     * @return ID
     */
    T getId();

    /**
     * 设置ID
     *
     * @param id ID
     * @return this
     */
    Node<T> setId(T id);

    /**
     * 获取父节点ID
     *
     * @return 父节点ID
     */
    T getParentId();

    /**
     * 设置父节点ID
     *
     * @param parentId 父节点ID
     * @return this
     */
    Node<T> setParentId(T parentId);

    /**
     * 获取节点标签名称
     *
     * @return 节点标签名称
     */
    CharSequence getName();

    /**
     * 设置节点标签名称
     *
     * @param name 节点标签名称
     * @return this
     */
    Node<T> setName(CharSequence name);

    /**
     * 获取权重
     *
     * @return 权重
     */
    Comparable<?> getWeight();

    /**
     * 设置权重
     *
     * @param weight 权重
     * @return this
     */
    Node<T> setWeight(Comparable<?> weight);

    @Override
    default int compareTo(final Node node) {
        if (null == node) {
            return 1;
        }
        final Comparable weight = this.getWeight();
        final Comparable weightOther = node.getWeight();
        return CompareKit.compare(weight, weightOther);
    }

}
