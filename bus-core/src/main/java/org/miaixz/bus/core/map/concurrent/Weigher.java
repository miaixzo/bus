package org.miaixz.bus.core.map.concurrent;

/**
 * A class that can determine the selector of a value. The total selector threshold
 * is used to determine when an eviction is required.
 *
 * @param <V> 值类型
 * @author ben.manes@gmail.com (Ben Manes)
 * @see <a href="http://code.google.com/p/concurrentlinkedhashmap/">
 * http://code.google.com/p/concurrentlinkedhashmap/</a>
 */
public interface Weigher<V> {

    /**
     * Measures an object's selector to determine how many units of capacity that
     * the value consumes. A value must consume a minimum of one unit.
     *
     * @param value the object to weigh
     * @return the object's selector
     */
    int weightOf(V value);

}
