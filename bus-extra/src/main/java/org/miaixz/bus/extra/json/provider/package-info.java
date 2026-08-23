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
 * Adapts supported JSON engines to the exported provider-neutral JSON contracts.
 * <p>
 * {@link org.miaixz.bus.extra.json.provider.JacksonProvider}, {@link org.miaixz.bus.extra.json.provider.GsonProvider},
 * and {@link org.miaixz.bus.extra.json.provider.FastJsonProvider} translate engine-specific parsing and mapping
 * behavior to {@link org.miaixz.bus.extra.json.JsonProvider}. Their shared base centralizes conversion to the immutable
 * {@link org.miaixz.bus.extra.json.JsonValue} model without making engine nodes part of that model.
 * </p>
 * <p>
 * Dependencies flow from these adapters to the JSON root contracts and to their respective engines. The root package,
 * authentication frameworks, and protocol codecs must not depend back on a concrete adapter, inspect the classpath, or
 * select a provider through mutable global state. Applications choose and configure one adapter at composition time,
 * then inject it through the consuming runtime boundary.
 * </p>
 * <p>
 * Each adapter must preserve configured input-size and nesting limits, duplicate-member policy, exact numeric values,
 * and deterministic UTF-8 output. Engine convenience features must not enable polymorphic type loading or weaken an
 * untrusted caller's explicit read options, and adapter failures must not disclose complete secret-bearing payloads.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.extra.json.provider;
