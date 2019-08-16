package org.aoju.bus.core.map;

import java.util.HashMap;
import java.util.Map;

/**
 * 忽略大小写的Map
 * 对KEY忽略大小写，get("Value")和get("value")获得的值相同，put进入的值也会被覆盖
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @version 3.0.6
 * @since JDK 1.8
 */
public class CaseInsensitiveMap<K, V> extends CustomKeyMap<K, V> {

    /**
     * 构造
     */
    public CaseInsensitiveMap() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     */
    public CaseInsensitiveMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 构造
     *
     * @param m Map
     */
    public CaseInsensitiveMap(Map<? extends K, ? extends V> m) {
        this(DEFAULT_LOAD_FACTOR, m);
    }

    /**
     * 构造
     *
     * @param loadFactor 加载因子
     * @param m          Map
     * @since 3.1.2
     */
    public CaseInsensitiveMap(float loadFactor, Map<? extends K, ? extends V> m) {
        this(m.size(), loadFactor);
        this.putAll(m);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     * @param loadFactor      加载因子
     */
    public CaseInsensitiveMap(int initialCapacity, float loadFactor) {
        super(new HashMap<K, V>(initialCapacity, loadFactor));
    }

    /**
     * 将Key转为小写
     *
     * @param key KEY
     * @return 小写KEY
     */
    @Override
    protected Object customKey(Object key) {
        if (null != key && key instanceof CharSequence) {
            key = key.toString().toLowerCase();
        }
        return key;
    }

}
