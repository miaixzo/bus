package org.miaixz.bus.core.map.concurrent;

/**
 * A class that can determine the selector of an entry. The total selector threshold
 * is used to determine when an eviction is required.
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @since Java 17+
 */
public interface EntryWeigher<K, V> {

    /**
     * Measures an entry's selector to determine how many units of capacity that
     * the key and value consumes. An entry must consume a minimum of one unit.
     *
     * @param key   the key to weigh
     * @param value the value to weigh
     * @return the entry's selector
     */
    int weightOf(K key, V value);
}
