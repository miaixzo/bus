package org.aoju.bus.cache.support;

import org.aoju.bus.cache.CacheConfig;
import com.google.common.base.Strings;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class KeyValueUtils {

    public static Map<String, Object> mapToKeyValue(Map proceedEntryValueMap,
                                                    Set<String> missKeys,
                                                    Map<Object, String> multiEntry2Key,
                                                    CacheConfig.Switch prevent) {
        Map<String, Object> keyValueMap = new HashMap<>(proceedEntryValueMap.size());
        proceedEntryValueMap.forEach((multiArgEntry, value) -> {
            String key = multiEntry2Key.get(multiArgEntry);
            if (Strings.isNullOrEmpty(key)) {
                return;
            }

            missKeys.remove(key);
            keyValueMap.put(key, value);
        });

        // 触发防击穿逻辑
        if (prevent == CacheConfig.Switch.ON && !missKeys.isEmpty()) {
            missKeys.forEach(key -> keyValueMap.put(key, PreventObjects.getPreventObject()));
        }

        return keyValueMap;
    }

    public static Map<String, Object> collectionToKeyValue(Collection proceedCollection, String idSpel, Set<String> missKeys, Map<Object, String> id2Key, CacheConfig.Switch prevent) {
        Map<String, Object> keyValueMap = new HashMap<>(proceedCollection.size());

        for (Object value : proceedCollection) {
            Object id = SpelCalculator.calcSpelWithNoContext(idSpel, value);
            String key = id2Key.get(id);

            if (!Strings.isNullOrEmpty(key)) {
                missKeys.remove(key);
                keyValueMap.put(key, value);
            }
        }

        if (prevent == CacheConfig.Switch.ON && !missKeys.isEmpty()) {
            missKeys.forEach(key -> keyValueMap.put(key, PreventObjects.getPreventObject()));
        }

        return keyValueMap;
    }
}
