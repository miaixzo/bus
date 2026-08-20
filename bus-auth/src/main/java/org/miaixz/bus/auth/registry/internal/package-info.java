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
 * Implements the non-exported immutable and atomic Registry runtime.
 * <p>
 * {@link org.miaixz.bus.auth.registry.internal.ImmutableRegistryView} stores read-only Library and Source runtime
 * indexes, and {@link org.miaixz.bus.auth.registry.internal.AtomicRegistryState} publishes a complete view atomically.
 * {@link org.miaixz.bus.auth.registry.internal.DefaultRegistry} applies lifecycle, reference, capability, request type,
 * and time-budget checks before delegating through the internal RuntimeProvider contract.
 * </p>
 * <p>
 * Runtime assembly supplies complete immutable views produced by its snapshot compiler. Protocol and Vendor business
 * behavior remains behind compiled RuntimeProvider instances; it must not be copied into Registry classes. The package
 * is absent from JPMS exports and applications never retain its state or views.
 * </p>
 * <p>
 * Reload either publishes one completely valid revision or leaves the previous revision unchanged. Closing rejects new
 * invocations without closing caller-owned resources. Internal maps, compiled instances, settings, credentials,
 * protocol messages, exceptions, and stack traces must not escape through public lookup, listener, or failure details.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.registry.internal;
