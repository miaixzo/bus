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
package org.miaixz.bus.core.center.map;

import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ObjectKit;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 允许拥有一个父节点与多个子节点的{@link Map.Entry}实现， 表示一个以key作为唯一标识，并且可以挂载一个对应值的树节点， 提供一些基于该节点对其所在树结构进行访问的方法
 *
 * @param <V> 节点的key类型
 * @param <K> 节点的value类型
 * @author Kimi Liu
 * @see ForestMap
 * @since Java 17+
 */
public interface TreeEntry<K, V> extends Map.Entry<K, V> {

    /**
     * 获取以当前节点作为叶子节点的树结构，然后获取当前节点与根节点的距离
     *
     * @return 当前节点与根节点的距离
     */
    int getWeight();

    /**
     * 获取以当前节点作为叶子节点的树结构，然后获取该树结构的根节点
     *
     * @return 根节点
     */
    TreeEntry<K, V> getRoot();

    /**
     * 当前节点是否存在直接关联的父节点
     *
     * @return 是否
     */
    default boolean hasParent() {
        return ObjectKit.isNotNull(getDeclaredParent());
    }

    /**
     * 获取当前节点直接关联的父节点
     *
     * @return 父节点，当节点不存在对应父节点时返回null
     */
    TreeEntry<K, V> getDeclaredParent();

    /**
     * 获取以当前节点作为叶子节点的树结构，然后获取该树结构中当前节点的指定父节点
     *
     * @param key 指定父节点的key
     * @return 指定父节点，当不存在时返回null
     */
    TreeEntry<K, V> getParent(K key);

    /**
     * 获取以当前节点作为叶子节点的树结构，然后确认该树结构中当前节点是否存在指定父节点
     *
     * @param key 指定父节点的key
     * @return 是否
     */
    default boolean containsParent(final K key) {
        return ObjectKit.isNotNull(getParent(key));
    }

    /**
     * 获取以当前节点作为根节点的树结构，然后遍历所有节点
     *
     * @param includeSelf  是否处理当前节点
     * @param nodeConsumer 对节点的处理
     */
    void forEachChild(boolean includeSelf, Consumer<TreeEntry<K, V>> nodeConsumer);

    /**
     * 获取当前节点直接关联的子节点
     *
     * @return 节点
     */
    Map<K, TreeEntry<K, V>> getDeclaredChildren();

    /**
     * 获取以当前节点作为根节点的树结构，然后获取该树结构中的当前节点的全部子节点
     *
     * @return 节点
     */
    Map<K, TreeEntry<K, V>> getChildren();

    /**
     * 当前节点是否有子节点
     *
     * @return 是否
     */
    default boolean hasChildren() {
        return CollKit.isNotEmpty(getDeclaredChildren());
    }

    /**
     * 获取以当前节点作为根节点的树结构，然后获取该树结构中的当前节点的指定子节点
     *
     * @param key 指定子节点的key
     * @return 节点
     */
    TreeEntry<K, V> getChild(K key);

    /**
     * 获取以当前节点作为根节点的树结构，然后确认该树结构中当前节点是否存在指定子节点
     *
     * @param key 指定子节点的key
     * @return 是否
     */
    default boolean containsChild(final K key) {
        return ObjectKit.isNotNull(getChild(key));
    }

}
