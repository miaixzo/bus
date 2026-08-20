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
package org.miaixz.bus.auth.cache;

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.cache.CacheX;

/**
 * Narrows a Bus cache to the atomic primitives required by authentication state.
 * <p>
 * Unlike the optional default methods on {@link CacheX}, every operation below is mandatory and must be implemented at
 * the backend linearization point. Implementations must receive keys that are already isolated by namespace and
 * authentication purpose and must never emulate take, compare-and-replace, or create-if-absent with multiple cache
 * calls.
 * </p>
 *
 * @param <K> isolated authentication state key type
 * @param <V> immutable authentication state value type
 * @author Kimi Liu
 */
public interface AtomicStore<K, V> extends CacheX<K, V> {

    /**
     * Atomically creates a value only when no unexpired value exists for the isolated key.
     *
     * @param key       isolated authentication state key
     * @param value     immutable value to create
     * @param ttlMillis positive backend time to live in milliseconds
     * @return stage containing whether this call created the value
     */
    @Override
    CompletionStage<Boolean> create(K key, V value, long ttlMillis);

    /**
     * Reads the value visible at one backend linearization point.
     *
     * @param key isolated authentication state key
     * @return stage containing the unexpired value or {@code null}
     */
    @Override
    CompletionStage<V> get(K key);

    /**
     * Atomically returns and removes one unexpired value.
     *
     * @param key isolated one-time authentication state key
     * @return stage containing the consumed value or {@code null}
     */
    @Override
    CompletionStage<V> take(K key);

    /**
     * Atomically replaces an unexpired value only when it equals the expected value.
     *
     * @param key       isolated authentication state key
     * @param expected  exact current value required for replacement
     * @param update    immutable replacement value
     * @param ttlMillis positive replacement time to live in milliseconds
     * @return stage containing whether the compare-and-replace succeeded
     */
    @Override
    CompletionStage<Boolean> replace(K key, V expected, V update, long ttlMillis);

    /**
     * Atomically removes one unexpired value.
     *
     * @param key isolated authentication state key
     * @return stage containing whether a value was removed
     */
    @Override
    CompletionStage<Boolean> delete(K key);

}
