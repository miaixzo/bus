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
 * Defines typed bus-cache wrappers for one-time and lifecycle-bound authentication state.
 * <p>
 * Purpose-specific caches separate authorization state, nonce, authorization code, device code, access token, refresh
 * token, session, and replay records. Each cache adds only its fixed key prefix and exact immutable value type before
 * delegating create, get, take, replace, and delete to {@link org.miaixz.bus.cache.CacheX}. Immutable
 * {@link org.miaixz.bus.auth.cache.ExpiringValue} values retain protocol-visible expiry without defining a backend.
 * </p>
 * <p>
 * Protocol, Vendor, identity, and guard code call these wrappers. The runtime supplies one bus-cache backend; this
 * package contains no in-memory, database, distributed-cache, serialization, connection, or persistence implementation
 * and does not call Roster or protocol services.
 * </p>
 * <p>
 * The selected bus-cache backend must support the atomic operations used by authentication. bus-auth never emulates an
 * unsupported atomic operation with multiple cache calls and never selects or manages a concrete cache backend. Its
 * serializer must preserve the versioned AuthCache envelope and immutable entry runtime types, and must
 * deterministically encode an equal expected value used by distributed compare-and-replace.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.cache;
