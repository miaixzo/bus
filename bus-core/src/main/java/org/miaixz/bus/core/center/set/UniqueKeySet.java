/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.set;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

import org.miaixz.bus.core.center.map.MapBuilder;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * 唯一键的Set 通过自定义唯一键，通过{@link #uniqueGenerator}生成节点对象对应的键作为Map的key，确定唯一
 * 此Set与HashSet不同的是，HashSet依赖于{@link Object#equals(Object)}确定唯一 但是很多时候我们无法对对象进行修改，此时在外部定义一个唯一规则，即可完成去重。
 * 
 * <pre>
 * {@code
 * Set<UniqueTestBean> set = new UniqueKeySet<>(UniqueTestBean::getId);
 * }
 * </pre>
 *
 * @param <K> 唯一键类型
 * @param <V> 值对象
 * @author Kimi Liu
 * @since Java 17+
 */
public class UniqueKeySet<K, V> extends AbstractSet<V> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852280266036L;

    private final Function<V, K> uniqueGenerator;
    private Map<K, V> map;

    /**
     * 构造
     *
     * @param uniqueGenerator 唯一键生成规则函数，用于生成对象对应的唯一键
     */
    public UniqueKeySet(final Function<V, K> uniqueGenerator) {
        this(false, uniqueGenerator);
    }

    /**
     * 构造
     *
     * @param uniqueGenerator 唯一键生成规则函数，用于生成对象对应的唯一键
     * @param c               初始化加入的集合
     */
    public UniqueKeySet(final Function<V, K> uniqueGenerator, final Collection<? extends V> c) {
        this(false, uniqueGenerator, c);
    }

    /**
     * 构造
     *
     * @param isLinked        是否保持加入顺序
     * @param uniqueGenerator 唯一键生成规则函数，用于生成对象对应的唯一键
     */
    public UniqueKeySet(final boolean isLinked, final Function<V, K> uniqueGenerator) {
        this(MapBuilder.of(isLinked), uniqueGenerator);
    }

    /**
     * 构造
     *
     * @param isLinked        是否保持加入顺序
     * @param uniqueGenerator 唯一键生成规则函数，用于生成对象对应的唯一键
     * @param c               初始化加入的集合
     */
    public UniqueKeySet(final boolean isLinked, final Function<V, K> uniqueGenerator, final Collection<? extends V> c) {
        this(isLinked, uniqueGenerator);
        addAll(c);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始容量
     * @param loadFactor      增长因子
     * @param uniqueGenerator 唯一键生成规则函数，用于生成对象对应的唯一键
     */
    public UniqueKeySet(final int initialCapacity, final float loadFactor, final Function<V, K> uniqueGenerator) {
        this(MapBuilder.of(new HashMap<>(initialCapacity, loadFactor)), uniqueGenerator);
    }

    /**
     * 构造
     *
     * @param builder         初始Map，定义了Map类型
     * @param uniqueGenerator 唯一键生成规则函数，用于生成对象对应的唯一键
     */
    public UniqueKeySet(final MapBuilder<K, V> builder, final Function<V, K> uniqueGenerator) {
        this.map = builder.build();
        this.uniqueGenerator = uniqueGenerator;
    }

    @Override
    public Iterator<V> iterator() {
        return map.values().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return map.containsKey(this.uniqueGenerator.apply((V) o));
    }

    @Override
    public boolean add(final V v) {
        return null == map.put(this.uniqueGenerator.apply(v), v);
    }

    /**
     * 加入值，如果值已经存在，则忽略之
     *
     * @param v 值
     * @return 是否成功加入
     */
    public boolean addIfAbsent(final V v) {
        return null == map.putIfAbsent(this.uniqueGenerator.apply(v), v);
    }

    /**
     * 加入集合中所有的值，如果值已经存在，则忽略之
     *
     * @param c 集合
     * @return 是否有一个或多个被加入成功
     */
    public boolean addAllIfAbsent(final Collection<? extends V> c) {
        boolean modified = false;
        for (final V v : c)
            if (addIfAbsent(v)) {
                modified = true;
            }
        return modified;
    }

    @Override
    public boolean remove(final Object o) {
        return null != map.remove(this.uniqueGenerator.apply((V) o));
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public UniqueKeySet<K, V> clone() {
        try {
            final UniqueKeySet<K, V> newSet = (UniqueKeySet<K, V>) super.clone();
            newSet.map = ObjectKit.clone(this.map);
            return newSet;
        } catch (final CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

}
