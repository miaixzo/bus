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
package org.miaixz.bus.core.center.map.reference;

import java.io.Serial;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.miaixz.bus.core.lang.ref.Ref;
import org.miaixz.bus.core.lang.ref.WeakObject;

/**
 * A thread-safe {@link ReferenceConcurrentMap} implementation where both keys and values are held by {@link WeakObject}
 * references. This means that entries in the map are subject to garbage collection when the only remaining references
 * to the keys or values are weak references. This is useful for implementing caches where entries should be removed as
 * soon as they are no longer strongly referenced elsewhere in the application.
 *
 * @param <K> The type of keys in the map.
 * @param <V> The type of values in the map.
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeakConcurrentMap<K, V> extends ReferenceConcurrentMap<K, V> {

    @Serial
    private static final long serialVersionUID = 2852278213373L;

    /**
     * Constructs a new {@code WeakConcurrentMap} with a default {@link ConcurrentHashMap} as its underlying storage.
     */
    public WeakConcurrentMap() {
        this(new ConcurrentHashMap<>());
    }

    /**
     * Constructs a new {@code WeakConcurrentMap} that wraps the given {@link ConcurrentMap}.
     *
     * @param raw The underlying {@link ConcurrentMap} to be wrapped. Must not be {@code null}.
     */
    public WeakConcurrentMap(final ConcurrentMap<Ref<K>, Ref<V>> raw) {
        super(raw);
    }

    /**
     * Wraps the given key in a {@link WeakObject} reference.
     *
     * @param key   The key to wrap.
     * @param queue The {@link ReferenceQueue} to register the weak reference with.
     * @return A {@link WeakObject} containing the key.
     */
    @Override
    Ref<K> wrapKey(final K key, final ReferenceQueue<? super K> queue) {
        return new WeakObject<>(key, queue);
    }

    /**
     * Wraps the given value in a {@link WeakObject} reference.
     *
     * @param value The value to wrap.
     * @param queue The {@link ReferenceQueue} to register the weak reference with.
     * @return A {@link WeakObject} containing the value.
     */
    @Override
    Ref<V> wrapValue(final V value, final ReferenceQueue<? super V> queue) {
        return new WeakObject<>(value, queue);
    }

}
