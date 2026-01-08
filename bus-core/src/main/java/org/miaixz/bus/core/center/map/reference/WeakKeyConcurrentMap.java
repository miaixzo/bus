/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.map.reference;

import java.io.Serial;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.miaixz.bus.core.lang.ref.Ref;
import org.miaixz.bus.core.lang.ref.StrongObject;
import org.miaixz.bus.core.lang.ref.WeakObject;

/**
 * A thread-safe {@link ReferenceConcurrentMap} implementation where keys are held by {@link WeakObject} references and
 * values are held by strong references. This means that entries in the map are subject to garbage collection when the
 * only remaining references to the keys are weak references. This is useful for implementing caches where entries
 * should be removed as soon as their keys are no longer strongly referenced elsewhere in the application, but the
 * values themselves should remain accessible as long as the key is present.
 *
 * @param <K> The type of keys in the map.
 * @param <V> The type of values in the map.
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeakKeyConcurrentMap<K, V> extends ReferenceConcurrentMap<K, V> {

    @Serial
    private static final long serialVersionUID = 2852278616531L;

    /**
     * Constructs a new {@code WeakKeyConcurrentMap} with a default {@link ConcurrentHashMap} as its underlying storage.
     */
    public WeakKeyConcurrentMap() {
        this(new ConcurrentHashMap<>());
    }

    /**
     * Constructs a new {@code WeakKeyConcurrentMap} that wraps the given {@link ConcurrentMap}.
     *
     * @param raw The underlying {@link ConcurrentMap} to be wrapped. Must not be {@code null}.
     */
    public WeakKeyConcurrentMap(final ConcurrentMap<Ref<K>, Ref<V>> raw) {
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
     * Wraps the given value in a {@link StrongObject} reference. For {@code WeakKeyConcurrentMap}, values are held by
     * strong references, so the queue is not used.
     *
     * @param value The value to wrap.
     * @param queue The {@link ReferenceQueue} (ignored for strong references).
     * @return A {@link StrongObject} containing the value.
     */
    @Override
    Ref<V> wrapValue(final V value, final ReferenceQueue<? super V> queue) {
        return new StrongObject<>(value);
    }

}
