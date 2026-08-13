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
 * Defines the internal encoding boundary for authentication wire values.
 *
 * <p>
 * Codecs may depend on bus-core encoding contracts, bus-crypto primitives, and explicitly injected bus-extra providers.
 * They must remain independent of transport selection, protocol response construction, runtime assembly, and vendor
 * behavior. Implementations must validate complete input, enforce explicit bounds, copy sensitive mutable data where
 * ownership crosses a boundary, and avoid including secrets in diagnostics.
 * </p>
 *
 * <p>
 * This package is internal to {@code bus-auth} and is intentionally not exported by JPMS.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.codec;
