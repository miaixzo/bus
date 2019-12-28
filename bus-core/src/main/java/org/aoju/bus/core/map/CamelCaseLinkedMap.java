package org.aoju.bus.core.map;

import java.util.HashMap;
import java.util.Map;

/**
 * 驼峰Key风格的LinkedHashMap
 * 对KEY转换为驼峰,get("int_value")和get("intValue")获得的值相同,put进入的值也会被覆盖
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @version 5.3.9
 * @since JDK 1.8+
 */
public class CamelCaseLinkedMap<K, V> extends CamelCaseMap<K, V> {

    /**
     * 构造
     */
    public CamelCaseLinkedMap() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     */
    public CamelCaseLinkedMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 构造
     *
     * @param m Map
     */
    public CamelCaseLinkedMap(Map<? extends K, ? extends V> m) {
        this(DEFAULT_LOAD_FACTOR, m);
    }

    /**
     * 构造
     *
     * @param loadFactor 加载因子
     * @param m          Map
     */
    public CamelCaseLinkedMap(float loadFactor, Map<? extends K, ? extends V> m) {
        this(m.size(), loadFactor);
        this.putAll(m);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     * @param loadFactor      加载因子
     */
    public CamelCaseLinkedMap(int initialCapacity, float loadFactor) {
        super(new HashMap<>(initialCapacity, loadFactor));
    }

}
