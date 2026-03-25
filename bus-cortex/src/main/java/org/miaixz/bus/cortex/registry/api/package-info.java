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
 * HTTP API service registration, instance tracking and query support for distributed service discovery.
 * <p>
 * When a service node starts, it calls {@code ApiRegistry.register(definition, instance)} to persist both the routing
 * definition and the live instance snapshot under separate CacheX keys. A uniqueness lock key
 * ({@code reg:{ns}:unique:{method}:{version}}) prevents two nodes with different fingerprints from registering as the
 * same logical endpoint. Instance metadata is stored under {@code reg:{ns}:instance:{method}:{version}:{fingerprint}}
 * with a TTL derived from the definition's {@code ttl} field (default 3 600 000 ms). On shutdown, the node calls
 * {@code deregisterInstance(namespace, method, version, fingerprint)}, which removes both the instance entry and the
 * lock key, leaving the shared routing definition in place.
 * <p>
 * {@code ApiDefinition} is the persisted routing definition that extends the base asset class; its no-arg constructor
 * pre-sets {@code species} to API. All gateway-facing fields — path, HTTP method, auth policy, timeout, throttle rate,
 * load-balance strategy and signature flag — are declared in the parent and inherited unchanged. {@code ApiRegistry} is
 * the concrete CacheX-backed registry that provides {@code register}, {@code deregisterInstance} and
 * {@code queryInstances(namespace, method, version)}, which scans all matching instance keys and deserialises them into
 * a list; passing {@code null} for method or version broadens the scan to all values within the namespace.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.cortex.registry.api;
