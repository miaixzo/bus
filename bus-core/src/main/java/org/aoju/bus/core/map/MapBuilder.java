package org.aoju.bus.core.map;

import org.aoju.bus.core.utils.MapUtils;

import java.util.Map;

/**
 * Map创建类
 *
 * @param <K> Key类型
 * @param <V> Value类型
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class MapBuilder<K, V> {

    private Map<K, V> map;

    /**
     * 链式Map创建类
     *
     * @param map 要使用的Map实现类
     */
    public MapBuilder(Map<K, V> map) {
        this.map = map;
    }

    /**
     * 创建Builder
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @param map Map实体类
     * @return MapBuilder
     * @since 3.2.3
     */
    public static <K, V> MapBuilder<K, V> create(Map<K, V> map) {
        return new MapBuilder<>(map);
    }

    /**
     * 链式Map创建
     *
     * @param k Key类型
     * @param v Value类型
     * @return 当前类
     */
    public MapBuilder<K, V> put(K k, V v) {
        map.put(k, v);
        return this;
    }

    /**
     * 链式Map创建
     *
     * @param map 合并map
     * @return 当前类
     */
    public MapBuilder<K, V> putAll(Map<K, V> map) {
        this.map.putAll(map);
        return this;
    }

    /**
     * 创建后的map
     *
     * @return 创建后的map
     */
    public Map<K, V> map() {
        return map;
    }

    /**
     * 创建后的map
     *
     * @return 创建后的map
     * @since 3.3.0
     */
    public Map<K, V> build() {
        return map();
    }

    /**
     * 将map转成字符串
     *
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @return 连接字符串
     */
    public String join(String separator, final String keyValueSeparator) {
        return MapUtils.join(this.map, separator, keyValueSeparator);
    }

    /**
     * 将map转成字符串
     *
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @return 连接后的字符串
     */
    public String joinIgnoreNull(String separator, final String keyValueSeparator) {
        return MapUtils.joinIgnoreNull(this.map, separator, keyValueSeparator);
    }

    /**
     * 将map转成字符串
     *
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @param isIgnoreNull      是否忽略null的键和值
     * @return 连接后的字符串
     */
    public String join(String separator, final String keyValueSeparator, boolean isIgnoreNull) {
        return MapUtils.join(this.map, separator, keyValueSeparator, isIgnoreNull);
    }

}