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
 * Defines the non-exported immutable-view and invocation SPI used inside the bus-auth module.
 * <p>
 * {@link org.miaixz.bus.auth.shared.internal.RuntimeProvider} is the executable entry retained by an immutable
 * {@link org.miaixz.bus.auth.registry.spi.RegistryView}. Source drivers create these entries during runtime snapshot
 * assembly; Registry code only reads and invokes the completed view.
 * </p>
 * <p>
 * The package is deliberately absent from JPMS exports, so applications interact through public drivers, Registry, and
 * service ports rather than invoking RuntimeProvider directly. Implementations do not read a mutable Registry or load
 * project data.
 * </p>
 * <p>
 * Compilation must fail closed on type, protocol, namespace, settings, endpoint, capability, or dependency mismatch.
 * RuntimeProvider invocation remains protected by Registry lifecycle, budget, manifest, and request-type checks; an SPI
 * implementation must not return internal instances, downgrade failures, or serialize Outcome and exceptions as
 * protocol responses.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.registry.spi;
