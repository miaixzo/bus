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
 * Defines the exported, product-neutral authentication protocol kernel.
 *
 * <p>
 * {@link org.miaixz.bus.auth.metric.AuthMetric} is the cross-protocol contract namespace. It owns immutable request,
 * response, identity, failure, policy, and runtime values together with ports supplied by a product. Protocol facades
 * and engines in child packages implement wire semantics without selecting users, tenants, persistence backends,
 * network runtimes, keys, secrets, clocks, random sources, or product policy.
 * </p>
 *
 * <p>
 * The module exports this package as its stable assembly boundary. Internal shared implementation packages remain
 * unexported; products obtain their supported adapters only through methods on exported facades. Protocol-specific
 * packages expose a public type only when the protocol contract requires product assembly or compatibility.
 * </p>
 *
 * <p>
 * The product owns every object injected through {@code AuthMetric.Runtime}, including cache and Fabric contexts,
 * executors, listeners, key material, and persistence implementations. Protocol code never creates threads, installs
 * shutdown hooks, or closes injected resources. Asynchronous stages complete before the product releases their owning
 * runtime resources.
 * </p>
 *
 * <p>
 * Dependency flow is fixed: product code depends on exported metric contracts; protocol engines depend on those
 * contracts and narrowly approved shared helpers; adapters depend on their owning Bus component APIs. The metric kernel
 * does not depend on a product, on another protocol's implementation, or on a duplicate utility, transport, cache,
 * cryptography, or serialization facade.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.metric;
