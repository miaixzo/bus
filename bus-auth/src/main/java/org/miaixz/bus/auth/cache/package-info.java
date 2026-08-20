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
/**
 * Defines atomic storage ports for one-time and lifecycle-bound authentication state.
 * <p>
 * {@link org.miaixz.bus.auth.cache.AtomicStore} narrows {@link org.miaixz.bus.cache.CacheX} to mandatory linearizable
 * create, get, take, replace, and delete operations. Purpose-specific ports separate authorization state, nonce,
 * authorization code, device code, access token, refresh token, session, and replay records; immutable
 * {@link org.miaixz.bus.auth.cache.ExpiringValue} values retain framework-visible expiry without defining a backend.
 * </p>
 * <p>
 * Protocol, Vendor, identity, and guard code depend on these narrow ports. External projects adapt a bus-cache backend
 * through ExecutionServices; this package contains no in-memory, database, distributed-cache, or persistence
 * implementation and does not call Registry or protocol services.
 * </p>
 * <p>
 * Backends must perform create-if-absent, consume, and compare-and-replace at one linearization point and enforce the
 * supplied lifetime. Keys are isolated by namespace, registration, protocol, and purpose before storage. Implementors
 * must not emulate atomic operations with multiple calls, extend expired state, log keys or values, or retain token,
 * verifier, code, nonce, and session material beyond the requested lifecycle.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.cache;
