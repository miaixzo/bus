package org.aoju.bus.pager.cache;

/**
 * Simple cache interface
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface Cache<K, V> {

    V get(K key);

    void put(K key, V value);
}
