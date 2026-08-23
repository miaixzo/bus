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
 * Defines the exported, provider-neutral JSON value model and processing contract.
 * <p>
 * Callers use {@link JsonValue} when a stable immutable JSON tree is required and inject a {@link JsonProvider} when
 * object mapping, bounded parsing, duplicate-member rejection, or serialization is required. Read and write options
 * describe operation-scoped limits and representation behavior; filters and annotations remain optional mapping
 * concerns rather than dependencies of the value model.
 * </p>
 * <p>
 * Engine adapters depend on this package and implement its contracts. This package may depend on Bus core language,
 * reflection, and codec primitives, but its public signatures never expose Jackson, Gson, Fastjson, or another engine
 * type. Frameworks that require deterministic provider selection receive a {@code JsonProvider} from their runtime
 * dependency boundary instead of consulting a global provider or {@link JsonKit}.
 * </p>
 * <p>
 * Untrusted input must be parsed with explicit byte, depth, and duplicate-name policy. A JSON tree is data, not an
 * authorization decision: callers remain responsible for field vocabularies, numeric ranges, secret erasure, and
 * preventing tokens, credentials, or personal data from entering logs and diagnostic failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.extra.json;
