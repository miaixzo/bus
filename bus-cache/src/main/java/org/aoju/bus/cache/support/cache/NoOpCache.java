package org.aoju.bus.cache.support.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class NoOpCache implements Cache {

    @Override
    public Object read(String key) {
        return null;
    }

    @Override
    public void write(String key, Object value, long expire) {
        // no op
    }

    @Override
    public Map<String, Object> read(Collection<String> keys) {
        return Collections.emptyMap();
    }

    @Override
    public void write(Map<String, Object> keyValueMap, long expire) {
        // no op
    }

    @Override
    public void remove(String... keys) {
        // no op
    }
}
