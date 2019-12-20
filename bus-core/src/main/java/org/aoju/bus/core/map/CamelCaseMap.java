package org.aoju.bus.core.map;

import org.aoju.bus.core.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 驼峰Key风格的Map
 * 对KEY转换为驼峰,get("int_value")和get("intValue")获得的值相同,put进入的值也会被覆盖
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public class CamelCaseMap<K, V> extends CustomKeyMap<K, V> {

    /**
     * 构造
     */
    public CamelCaseMap() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     */
    public CamelCaseMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 构造
     *
     * @param map Map
     */
    public CamelCaseMap(Map<? extends K, ? extends V> map) {
        this(DEFAULT_LOAD_FACTOR, map);
    }

    /**
     * 构造
     *
     * @param loadFactor 加载因子
     * @param map        Map
     */
    public CamelCaseMap(float loadFactor, Map<? extends K, ? extends V> map) {
        this(map.size(), loadFactor);
        this.putAll(map);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     * @param loadFactor      加载因子
     */
    public CamelCaseMap(int initialCapacity, float loadFactor) {
        super(new HashMap<K, V>(initialCapacity, loadFactor));
    }

    /**
     * 将Key转为驼峰风格,如果key为字符串的话
     *
     * @param key KEY
     * @return 驼峰Key
     */
    @Override
    protected Object customKey(Object key) {
        if (null != key && key instanceof CharSequence) {
            key = StringUtils.toCamelCase(key.toString());
        }
        return key;
    }

}
