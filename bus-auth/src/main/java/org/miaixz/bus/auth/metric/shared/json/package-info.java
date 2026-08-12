/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * Provides the single shared boundary for strict authentication JSON input. The package accepts complete raw UTF-8
 * documents, enforces the configured byte and nesting limits before object mapping, rejects malformed UTF-8, duplicate
 * object names, trailing content, invalid Unicode surrogate pairs, and non-JSON numeric values, and then invokes the
 * product-supplied JSON provider exactly once. It never builds a second JSON tree, writes JSON, performs protocol DTO
 * validation, or owns the injected provider. Protocol-specific member policies remain in their protocol packages.
 * <p>
 * <strong>Bus dependencies:</strong> {@code org.miaixz.bus.extra.json.JsonProvider} performs the sole object mapping
 * after the raw scanner succeeds; {@code org.miaixz.bus.auth.metric.AuthMetric.Limits} supplies the closed byte and
 * depth bounds; bus-core validation exceptions report programming-contract violations without introducing another JSON
 * abstraction.
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.metric.shared.json;
