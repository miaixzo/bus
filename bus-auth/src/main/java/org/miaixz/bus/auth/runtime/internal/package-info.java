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
 * Implements non-exported deterministic assembly of complete Registry snapshots.
 * <p>
 * {@link org.miaixz.bus.auth.runtime.internal.SnapshotCompiler} freezes the explicit Source driver index, resolves
 * Library and protocol-neutral Provider parents, delegates typed Source creation to the selected driver, and returns a
 * complete immutable Registry view only after every enabled record succeeds.
 * </p>
 * <p>
 * This package does not load external data, publish Registry state, discover implementations, import concrete protocol
 * or Vendor packages, execute authentication capabilities, or own caller resources. Failed assembly exposes no partial
 * view and leaves publication to the runtime reload service.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.runtime.internal;
