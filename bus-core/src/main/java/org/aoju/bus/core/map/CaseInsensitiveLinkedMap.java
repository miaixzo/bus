package org.aoju.bus.core.map;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 忽略大小写的LinkedHashMap
 * 对KEY忽略大小写，get("Value")和get("value")获得的值相同，put进入的值也会被覆盖
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @version 3.6.5
 * @since JDK 1.8
 */
public class CaseInsensitiveLinkedMap<K, V> extends CaseInsensitiveMap<K, V> {

    /**
     * 构造
     */
    public CaseInsensitiveLinkedMap() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     */
    public CaseInsensitiveLinkedMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 构造
     *
     * @param map Map
     */
    public CaseInsensitiveLinkedMap(Map<? extends K, ? extends V> map) {
        this(DEFAULT_LOAD_FACTOR, map);
    }

    /**
     * 构造
     *
     * @param loadFactor 加载因子
     * @param map        Map
     * @since 3.1.9
     */
    public CaseInsensitiveLinkedMap(float loadFactor, Map<? extends K, ? extends V> map) {
        this(map.size(), loadFactor);
        this.putAll(map);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     * @param loadFactor      加载因子
     */
    public CaseInsensitiveLinkedMap(int initialCapacity, float loadFactor) {
        super(new LinkedHashMap<K, V>(initialCapacity, loadFactor));
    }

}
