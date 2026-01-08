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
package org.miaixz.bus.core.cache.provider;

import java.io.Serial;

import org.miaixz.bus.core.cache.CacheListener;
import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.mutable.Mutable;
import org.miaixz.bus.core.lang.ref.Ref;

/**
 * A cache implementation that uses weak references for its keys.
 * <p>
 * The presence of a mapping for a given key will not prevent the key from being reclaimed by the garbage collector.
 * When a key is garbage-collected, its entry is effectively removed from the cache.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeakCache<K, V> extends TimedCache<K, V> {

    @Serial
    private static final long serialVersionUID = 2852232505330L;

    /**
     * Constructs a weak-referenced cache with a specified timeout.
     *
     * @param timeout The timeout in milliseconds. A value of -1 or 0 means no timeout.
     */
    public WeakCache(final long timeout) {
        super(timeout, new WeakConcurrentMap<>());
    }

    /**
     * Sets a listener for cache events and registers a purge listener on the underlying {@link WeakConcurrentMap}.
     * <p>
     * The purge listener is triggered when an entry is removed by the garbage collector. It safely extracts the key and
     * value from the purged entry and passes them to the {@link CacheListener#onRemove(Object, Object)} method.
     *
     * @param listener The listener for cache events.
     * @return This {@link WeakCache} instance.
     */
    @Override
    public WeakCache<K, V> setListener(final CacheListener<K, V> listener) {
        super.setListener(listener);

        final WeakConcurrentMap<Mutable<K>, CacheObject<K, V>> map = (WeakConcurrentMap<Mutable<K>, CacheObject<K, V>>) this.cacheMap;
        // When a weak key is collected, the key in the listener will be null, so we handle it safely.
        map.setPurgeListener(
                (key, value) -> listener.onRemove(
                        Optional.ofNullable(key).map(Ref::get).map(Mutable::get).getOrNull(),
                        Optional.ofNullable(value).map(Ref::get).map(CacheObject::getValue).getOrNull()));

        return this;
    }

}
