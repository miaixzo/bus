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
 * Defines the public loading, validation, observation, and resource-access boundary of the Registry.
 * <p>
 * External projects implement {@link org.miaixz.bus.auth.registry.RegistrationLoader} and the generic
 * {@link org.miaixz.bus.auth.registry.ResourceService} to supply complete registration snapshots and management data.
 * Snapshot record lists are structurally frozen, while their Library, Provider, and Source entities remain mutable
 * persistence models owned by the external project. {@link org.miaixz.bus.auth.registry.RegistrationValidator} applies
 * cross-entity Library, Provider, Source, namespace, and Library-to-Provider-to-Source ownership rules before
 * compilation. {@link org.miaixz.bus.auth.registry.RegistryListener} observes publication lifecycle, while
 * {@link org.miaixz.bus.auth.registry.RegistryIssue} reports non-secret reload failures.
 * </p>
 * <p>
 * Runtime assembly calls this package through the root Registry and registration contracts. The public registry layer
 * remains neutral: runtime assembly supplies already compiled immutable views, and no concrete protocol service,
 * platform adapter, Driver, or persistence implementation is imported here. Registry users invoke only a
 * {@link org.miaixz.bus.auth.Registry.Reference}, never a compiled Source instance.
 * </p>
 * <p>
 * A reload validates and compiles the complete candidate before one atomic publication; partial views and fallback to
 * an invalid candidate are forbidden. Issues and listener notifications must omit settings bodies, credentials, tokens,
 * protocol messages, exceptions, and stack traces, and management resource access must preserve namespace isolation
 * established by the external project.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.registry;
