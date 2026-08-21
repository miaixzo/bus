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
 * Defines the loading, validation, publication, and read-only state boundary of the Registry.
 * <p>
 * External projects implement {@link org.miaixz.bus.auth.worker.RegistrationLoader} to supply complete registration
 * snapshots. CRUD and management data access remain project responsibilities. Registration entries detach the
 * framework-owned Library, Provider, and Source fields on entry and return detached copies, while snapshot record lists
 * are structurally frozen. The loader converts persisted Source configuration into typed Options before this boundary.
 * {@link org.miaixz.bus.auth.registry.RegistrationValidator} applies cross-entity Library, Provider, Source, namespace,
 * and Library-to-Provider-to-Source ownership rules before compilation.
 * {@link org.miaixz.bus.auth.worker.RegistryListener} observes publication lifecycle, while
 * {@link org.miaixz.bus.auth.registry.RegistryIssue} reports non-secret reload failures.
 * </p>
 * <p>
 * Runtime assembly supplies already compiled immutable views. No concrete protocol service, platform adapter, Driver,
 * persistence implementation, security decision, audit operation, or capability execution belongs to this package.
 * {@link org.miaixz.bus.auth.Authenticator} consumes a Registry reference without adding execution duties to Registry.
 * </p>
 * <p>
 * A reload validates and compiles the complete candidate before one atomic publication; partial views and fallback to
 * an invalid candidate are forbidden. Issues and listener notifications must omit options bodies, credentials, tokens,
 * protocol messages, exceptions, and stack traces, and management resource access must preserve namespace isolation
 * established by the external project.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.registry;
