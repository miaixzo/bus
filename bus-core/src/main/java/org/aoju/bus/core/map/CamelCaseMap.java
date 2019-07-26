package org.aoju.bus.core.map;

import org.aoju.bus.core.utils.StringUtils;

import java.util.Map;

/**
 * 驼峰Key风格的Map<br>
 * 对KEY转换为驼峰，get("int_value")和get("intValue")获得的值相同，put进入的值也会被覆盖
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class CamelCaseMap<K, V> extends CustomKeyMap<K, V> {

    private static final long serialVersionUID = 4043263744224569870L;

    /**
     * 构造
     */
    public CamelCaseMap() {
        super();
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     * @param loadFactor      加载因子
     */
    public CamelCaseMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     */
    public CamelCaseMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * 构造
     *
     * @param m Map
     */
    public CamelCaseMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    /**
     * 将Key转为驼峰风格，如果key为字符串的话
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
