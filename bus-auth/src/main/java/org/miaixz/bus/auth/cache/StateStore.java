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

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;

/**
 * Cache-backed specialization of the authentication runtime's atomic state port.
 *
 * @author Kimi Liu
 */
public interface StateStore {

    /**
     * Atomically stores state when no unexpired value exists for the tenant-scoped key.
     *
     * @param context non-null operation context supplying the tenant
     * @param key     non-blank logical state key
     * @param value   non-null caller-owned state bytes
     * @param ttl     positive state lifetime
     * @return non-null stage containing whether insertion occurred
     */
    CompletionStage<Boolean> putIfAbsent(Context context, String key, byte[] value, Duration ttl);

    /**
     * Reads state without consuming it.
     *
     * @param context non-null operation context supplying the tenant
     * @param key     non-blank logical state key
     * @return non-null stage containing independent state bytes or an empty optional
     */
    CompletionStage<Optional<byte[]>> get(Context context, String key);

    /**
     * Atomically reads and removes state once.
     *
     * @param context non-null operation context supplying the tenant
     * @param key     non-blank logical state key
     * @return non-null stage containing independent consumed bytes or an empty optional
     */
    CompletionStage<Optional<byte[]>> take(Context context, String key);

    /**
     * Atomically replaces state only when the current bytes equal the expected bytes.
     *
     * @param context  non-null operation context supplying the tenant
     * @param key      non-blank logical state key
     * @param expected non-null caller-owned expected bytes
     * @param update   non-null caller-owned replacement bytes
     * @param ttl      positive replacement lifetime
     * @return non-null stage containing whether replacement occurred
     */
    CompletionStage<Boolean> compareAndSet(Context context, String key, byte[] expected, byte[] update, Duration ttl);

    /**
     * Atomically removes state for a tenant-scoped key.
     *
     * @param context non-null operation context supplying the tenant
     * @param key     non-blank logical state key
     * @return non-null stage containing whether removal occurred
     */
    CompletionStage<Boolean> remove(Context context, String key);

}
