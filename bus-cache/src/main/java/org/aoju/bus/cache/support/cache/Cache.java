package org.aoju.bus.cache.support.cache;

import java.util.Collection;
import java.util.Map;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface Cache {

    Object read(String key);

    void write(String key, Object value, long expire);

    Map<String, Object> read(Collection<String> keys);

    void write(Map<String, Object> keyValueMap, long expire);

    void remove(String... keys);
}