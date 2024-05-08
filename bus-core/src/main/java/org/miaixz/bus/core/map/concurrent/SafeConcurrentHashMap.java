package org.miaixz.bus.core.map.concurrent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 安全的ConcurrentHashMap实现
 * 此类用于解决在JDK8中调用{@link ConcurrentHashMap#computeIfAbsent(Object, Function)}可能造成的死循环问题
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class SafeConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {

    private static final long serialVersionUID = 1L;

    /**
     * 构造，默认初始大小（16）
     */
    public SafeConcurrentHashMap() {
        super();
    }

    /**
     * 构造
     *
     * @param initialCapacity 预估初始大小
     */
    public SafeConcurrentHashMap(final int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * 构造
     *
     * @param m 初始键值对
     */
    public SafeConcurrentHashMap(final Map<? extends K, ? extends V> m) {
        super(m);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始容量
     * @param loadFactor      增长系数
     */
    public SafeConcurrentHashMap(final int initialCapacity, final float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * 构造
     *
     * @param initialCapacity  初始容量
     * @param loadFactor       增长系数
     * @param concurrencyLevel 并发级别，即Segment的个数
     */
    public SafeConcurrentHashMap(final int initialCapacity,
                                 final float loadFactor, final int concurrencyLevel) {
        super(initialCapacity, loadFactor, concurrencyLevel);
    }

    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        return super.computeIfAbsent(key, mappingFunction);
    }

}
