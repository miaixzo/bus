package org.aoju.bus.core.map.multi;

import java.util.*;

/**
 * 值作为集合List的Map实现,通过调用putValue可以在相同key时加入多个值,多个值用集合表示
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @version 5.2.2
 * @since JDK 1.8+
 */
public class ListValueMap<K, V> extends CollectionValueMap<K, V> {

    /**
     * 构造
     */
    public ListValueMap() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     */
    public ListValueMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 构造
     *
     * @param map Map
     */
    public ListValueMap(Map<? extends K, ? extends Collection<V>> map) {
        this(DEFAULT_LOAD_FACTOR, map);
    }

    /**
     * 构造
     *
     * @param loadFactor 加载因子
     * @param map        Map
     */
    public ListValueMap(float loadFactor, Map<? extends K, ? extends Collection<V>> map) {
        this(map.size(), loadFactor);
        this.putAll(map);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     * @param loadFactor      加载因子
     */
    public ListValueMap(int initialCapacity, float loadFactor) {
        super(new HashMap<K, Collection<V>>(initialCapacity, loadFactor));
    }

    @Override
    public List<V> get(Object key) {
        return (List<V>) super.get(key);
    }

    @Override
    protected Collection<V> createCollction() {
        return new ArrayList<>(DEFAULT_COLLCTION_INITIAL_CAPACITY);
    }

}
