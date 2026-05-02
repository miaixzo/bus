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
 * HTTP API service-definition and runtime-instance registry support.
 * <p>
 * {@code ApiAssets} defaults its type to API. Optional runtime hints such as heartbeat interval, lease duration and
 * route key are carried by the typed {@code ApiAssets.Meta} payload in metadata. {@code ApiRegistry} persists service
 * definitions through the shared {@code StoreBackedRegistry} path and separately projects runtime instances to
 * {@code reg:{ns}:instance:{method}:{version}:{fingerprint}} cache keys. When a durable {@code RegistryStore} is
 * present, instance queries read from that store first and warm the cache projection from the returned snapshots.
 * <p>
 * {@code register(service, instance)} upserts the logical service definition and one live instance snapshot.
 * {@code deregisterInstance(namespace, method, version, fingerprint)} removes one runtime instance while leaving the
 * shared route definition intact. {@code queryInstances(namespace, method, version)} always scopes reads to the
 * resolved namespace, defaulting blank values to {@code Normal.DEFAULT}; omitting method and version broadens the scan
 * only within that namespace.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.cortex.registry.api;
