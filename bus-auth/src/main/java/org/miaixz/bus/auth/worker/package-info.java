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
 * Defines the complete project-facing authentication data boundary and compiled framework workers.
 * <p>
 * Loader interfaces are the only entry points for project-owned databases, files, directories, key stores, remote
 * services, and configuration systems. Output ports deliver consent, dynamic credentials, and Registry notifications.
 * {@link org.miaixz.bus.auth.worker.WorkerSet} freezes only protocol execution ports; RegistrationLoader and
 * RegistryListener are assembled directly by RuntimeBuilder, while identity completion remains optional under
 * {@code worker.identity}.
 * </p>
 * <p>
 * {@link org.miaixz.bus.auth.worker.WorkerSlots} declares the exact integration slots needed by one Source driver,
 * while {@link org.miaixz.bus.auth.worker.SourceWorker} remains the compiled capability worker produced by that driver,
 * and {@link org.miaixz.bus.auth.worker.SessionCoordinator} coordinates framework session cache transitions with the
 * project SessionWorker. Loaders, stores, consent services, and SessionWorker point from framework code to project
 * implementations; SourceWorker points in the opposite direction and is invoked by the framework after compilation.
 * Sharing this package does not make project ports responsible for parsing, registration, cache management, or protocol
 * policy. Loaders do not parse authentication-domain records, enforce protocol policy, mutate Registry state, manage
 * caches, or execute unrelated security responsibilities.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.worker;
