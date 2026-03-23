/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.cortex.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.cortex.Listener;
import org.miaixz.bus.cortex.Vector;

/**
 * Manages watch subscriptions and notifies listeners on changes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WatchManager {

    /**
     * Watch entries keyed by generated watch identifier.
     */
    private final Map<String, WatchSubscription<?>> entries = new ConcurrentHashMap<>();
    /**
     * Maximum number of watch subscriptions allowed per namespace.
     */
    private final int maxWatchesPerNamespace;
    /**
     * Maximum idle time in milliseconds before a watch subscription expires.
     */
    private final long watchExpireMs;

    /**
     * Creates a WatchManager with default limits (1000 watches per namespace, 24 h expiry).
     */
    public WatchManager() {
        this(1000, 86400000L);
    }

    /**
     * Creates a WatchManager with explicit limits.
     *
     * @param maxWatchesPerNamespace maximum concurrent watches per namespace
     * @param watchExpireMs          milliseconds of inactivity after which a watch is removed
     */
    public WatchManager(int maxWatchesPerNamespace, long watchExpireMs) {
        this.maxWatchesPerNamespace = maxWatchesPerNamespace;
        this.watchExpireMs = watchExpireMs;
    }

    /**
     * Adds a watch subscription and returns its unique ID.
     *
     * @param vector   vector defining what to watch
     * @param listener listener to notify on change
     * @param <T>      watched type
     * @return watch identifier
     */
    public <T> String add(Vector vector, Listener<T> listener) {
        String ns = vector.getNamespace() != null ? vector.getNamespace() : "";
        long nsCount = entries.values().stream()
                .filter(e -> ns.equals(e.getVector().getNamespace() != null ? e.getVector().getNamespace() : ""))
                .count();
        if (nsCount >= maxWatchesPerNamespace) {
            throw new IllegalStateException("Max watches per namespace exceeded: " + ns);
        }
        String watchId = ID.fastSimpleUUID();
        WatchSubscription<T> entry = new WatchSubscription<>(vector, listener, new ArrayList<>(),
                System.currentTimeMillis());
        entries.put(watchId, entry);
        return watchId;
    }

    /**
     * Removes a watch subscription by ID.
     *
     * @param watchId watch identifier to remove
     */
    public void remove(String watchId) {
        entries.remove(watchId);
    }

    /**
     * Notifies matching watch listeners about a value change.
     *
     * @param key      changed key
     * @param newValue new value
     * @param <T>      value type
     */
    public <T> void notify(String key, T newValue) {
        for (WatchSubscription<?> entry : entries.values()) {
            entry.setLastAccess(System.currentTimeMillis());
            WatchSubscription<T> typed = (WatchSubscription<T>) entry;
            List<T> added = newValue != null ? List.of(newValue) : List.of();
            typed.getListener().accept(added, List.of(), List.of());
        }
    }

    /**
     * Notifies config-specific watch listeners.
     *
     * @param key     config key that changed
     * @param content new config content
     */
    public void notifyConfig(String key, String content) {
        notify(key, content);
    }

    /**
     * Removes stale watch entries that have not been accessed recently.
     */
    public void cleanExpired() {
        long now = System.currentTimeMillis();
        entries.entrySet().removeIf(e -> (now - e.getValue().getLastAccess()) > watchExpireMs);
    }

    /**
     * Returns an unmodifiable view of all watch entries.
     *
     * @return unmodifiable entry map
     */
    public Map<String, WatchSubscription<?>> entries() {
        return Collections.unmodifiableMap(entries);
    }

}
