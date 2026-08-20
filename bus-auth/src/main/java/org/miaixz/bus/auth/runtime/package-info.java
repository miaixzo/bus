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
 * Defines deterministic framework assembly, externally owned services, reload, and lifecycle contracts.
 * <p>
 * {@link org.miaixz.bus.auth.shared.ExecutionServices} is the sole aggregate through which an external project supplies
 * Fabric, JSON, executor, resolver, store, audit, consent, and security-baseline dependencies.
 * {@link org.miaixz.bus.auth.runtime.RuntimeBuilder} accepts explicit Source drivers together with the external
 * registration loader, freezes the resulting driver indexes, and returns an
 * {@link org.miaixz.bus.auth.runtime.AuthRuntime}; {@link org.miaixz.bus.auth.runtime.RuntimeReloadService} explicitly
 * loads and atomically publishes later Registry revisions.
 * </p>
 * <p>
 * Applications select protocol and Vendor drivers explicitly, build once, then request the initial and subsequent
 * reloads. Runtime assembly consumes public driver contracts and the internal Registry SPI but contains no OAuth,
 * OpenID Connect, SAML, SCIM, LDAP, RADIUS, or Vendor business operation. It performs no classpath discovery, project
 * data loading during build, global singleton installation, or creation of transport and persistence resources.
 * </p>
 * <p>
 * Runtime and Registry closing stop framework work but do not close caller-owned Fabric, executor, JSON provider,
 * stores, resolvers, keys, or network resources. Reload is all-or-nothing, observes one Budget, and must not expose
 * candidate settings, compiled providers, credentials, protocol messages, exceptions, or stack traces when it fails.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.runtime;
