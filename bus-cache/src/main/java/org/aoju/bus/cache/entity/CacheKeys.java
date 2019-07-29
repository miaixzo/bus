package org.aoju.bus.cache.entity;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class CacheKeys {

    private Map<String, Object> hitKeyMap;

    private Set<String> missKeySet;

    public CacheKeys() {
    }

    public CacheKeys(Map<String, Object> hitKeyMap, Set<String> missKeySet) {
        this.hitKeyMap = hitKeyMap;
        this.missKeySet = missKeySet;
    }

    public Map<String, Object> getHitKeyMap() {
        return hitKeyMap == null ? Collections.emptyMap() : hitKeyMap;
    }

    public Set<String> getMissKeySet() {
        return missKeySet == null ? Collections.emptySet() : missKeySet;
    }
}
