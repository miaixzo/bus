package org.aoju.bus.core.map;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义键的Map，默认HashMap实现
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class CustomKeyMap<K, V> extends HashMap<K, V> {

    private static final long serialVersionUID = 4043263744224569870L;

    /**
     * 构造
     */
    public CustomKeyMap() {
        super();
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     * @param loadFactor      加载因子
     */
    public CustomKeyMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     */
    public CustomKeyMap(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }

    /**
     * 构造
     *
     * @param m Map
     */
    public CustomKeyMap(Map<? extends K, ? extends V> m) {
        super((int) (m.size() / 0.75));
        putAll(m);
    }

    /**
     * 构造
     *
     * @param loadFactor 加载因子
     * @param m          Map
     * @since 3.1.2
     */
    public CustomKeyMap(float loadFactor, Map<? extends K, ? extends V> m) {
        super(m.size(), loadFactor);
        putAll(m);
    }

    @Override
    public V get(Object key) {
        return super.get(customKey(key));
    }

    @Override
    public V put(K key, V value) {
        return super.put((K) customKey(key), value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(customKey(key));
    }

    /**
     * 自定义键
     *
     * @param key KEY
     * @return 自定义KEY
     */
    protected abstract Object customKey(Object key);

}
