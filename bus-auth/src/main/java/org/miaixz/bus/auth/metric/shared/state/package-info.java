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
 * Adapts authentication state contracts to atomic Bus cache operations and protects stored state envelopes.
 *
 * <p>
 * Every logical state transition maps to exactly one atomic cache operation. Implementations preserve tenant isolation
 * in cache keys, convert expiration without truncation or overflow, copy mutable payloads at both boundaries, and
 * propagate asynchronous completion without blocking. A composite read followed by write or delete is not an atomic
 * substitute and is forbidden.
 * </p>
 *
 * <p>
 * This package depends inward on {@link org.miaixz.bus.auth.metric.AuthMetric} contracts and outward only on approved
 * bus-cache, bus-core, and bus-crypto APIs. Protocol packages consume the state port and never depend on cache
 * backends. Cache instances and their executors remain owned by the product runtime; adapters in this package never
 * initialize, select, close, or otherwise manage an injected cache.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.metric.shared.state;
