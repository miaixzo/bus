package org.aoju.bus.core.map.multi;

import java.util.*;

/**
 * 值作为集合Set（LinkedHashSet）的Map实现,通过调用putValue可以在相同key时加入多个值,多个值用集合表示
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @version 5.3.8
 * @since JDK 1.8+
 */
public class SetValueMap<K, V> extends CollectionValueMap<K, V> {

    /**
     * 构造
     */
    public SetValueMap() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     */
    public SetValueMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 构造
     *
     * @param m Map
     */
    public SetValueMap(Map<? extends K, ? extends Collection<V>> m) {
        this(DEFAULT_LOAD_FACTOR, m);
    }

    /**
     * 构造
     *
     * @param loadFactor 加载因子
     * @param m          Map
     */
    public SetValueMap(float loadFactor, Map<? extends K, ? extends Collection<V>> m) {
        this(m.size(), loadFactor);
        this.putAll(m);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     * @param loadFactor      加载因子
     */
    public SetValueMap(int initialCapacity, float loadFactor) {
        super(new HashMap<>(initialCapacity, loadFactor));
    }

    @Override
    public Set<V> get(Object key) {
        return (Set<V>) super.get(key);
    }

    @Override
    protected Collection<V> createCollction() {
        return new LinkedHashSet<>(DEFAULT_COLLCTION_INITIAL_CAPACITY);
    }

}
