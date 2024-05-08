package org.miaixz.bus.core.beans.copier.provider;

import org.miaixz.bus.core.beans.copier.ValueProvider;
import org.miaixz.bus.core.convert.Convert;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Map值提供者
 */
public class MapValueProvider implements ValueProvider<String> {

    private final Map map;

    /**
     * 构造
     *
     * @param map map
     */
    public MapValueProvider(final Map map) {
        this.map = map;
    }

    @Override
    public Object value(final String key, final Type valueType) {
        return Convert.convert(valueType, map.get(key));
    }

    @Override
    public boolean containsKey(final String key) {
        return map.containsKey(key);
    }

}
