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
package org.miaixz.bus.core.codec.hash;

import org.miaixz.bus.core.xyz.HashKit;

import java.io.Serializable;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 一致性Hash算法
 * 算法详解：<a href="http://blog.csdn.net/sparkliang/article/details/5279393">http://blog.csdn.net/sparkliang/article/details/5279393</a>
 * 算法实现：<a href="https://weblogs.java.net/blog/2007/11/27/consistent-hashing">https://weblogs.java.net/blog/2007/11/27/consistent-hashing</a>
 *
 * @param <T> 节点类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConsistentHash<T> implements Serializable {

    private static final long serialVersionUID = -1L;
    /**
     * 复制的节点个数
     */
    private final int numberOfReplicas;
    /**
     * 一致性Hash环
     */
    private final SortedMap<Integer, T> circle = new TreeMap<>();
    /**
     * Hash计算对象，用于自定义hash算法
     */
    Hash32<Object> hashFunc;

    /**
     * 构造，使用Java默认的Hash算法
     *
     * @param numberOfReplicas 复制的节点个数，增加每个节点的复制节点有利于负载均衡
     * @param nodes            节点对象
     */
    public ConsistentHash(final int numberOfReplicas, final Collection<T> nodes) {
        this.numberOfReplicas = numberOfReplicas;
        this.hashFunc = key -> {
            //默认使用FNV1hash算法
            return HashKit.fnvHash(key.toString());
        };
        //初始化节点
        for (final T node : nodes) {
            add(node);
        }
    }

    /**
     * 构造
     *
     * @param hashFunc         hash算法对象
     * @param numberOfReplicas 复制的节点个数，增加每个节点的复制节点有利于负载均衡
     * @param nodes            节点对象
     */
    public ConsistentHash(final Hash32<Object> hashFunc, final int numberOfReplicas, final Collection<T> nodes) {
        this.numberOfReplicas = numberOfReplicas;
        this.hashFunc = hashFunc;
        //初始化节点
        for (final T node : nodes) {
            add(node);
        }
    }

    /**
     * 增加节点
     * 每增加一个节点，就会在闭环上增加给定复制节点数
     * 例如复制节点数是2，则每调用此方法一次，增加两个虚拟节点，这两个节点指向同一Node
     * 由于hash算法会调用node的toString方法，故按照toString去重
     *
     * @param node 节点对象
     */
    public void add(final T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            circle.put(hashFunc.hash32(node.toString() + i), node);
        }
    }

    /**
     * 移除节点的同时移除相应的虚拟节点
     *
     * @param node 节点对象
     */
    public void remove(final T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            circle.remove(hashFunc.hash32(node.toString() + i));
        }
    }

    /**
     * 获得一个最近的顺时针节点
     *
     * @param key 为给定键取Hash，取得顺时针方向上最近的一个虚拟节点对应的实际节点
     * @return 节点对象
     */
    public T get(final Object key) {
        if (circle.isEmpty()) {
            return null;
        }
        int hash = hashFunc.hash32(key);
        if (!circle.containsKey(hash)) {
            final SortedMap<Integer, T> tailMap = circle.tailMap(hash);    //返回此映射的部分视图，其键大于等于 hash
            hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        }
        // 正好命中
        return circle.get(hash);
    }

}
