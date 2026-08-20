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
 * {@link org.miaixz.bus.auth.runtime.ExecutionServices} combines caller-owned infrastructure with one immutable
 * {@link org.miaixz.bus.auth.worker.WorkerSet} and framework-owned pure parsers.
 * {@link org.miaixz.bus.auth.runtime.RuntimeBuilder} accepts explicit Source drivers, freezes their indexes, and
 * returns an {@link org.miaixz.bus.auth.runtime.AuthRuntime}; {@link org.miaixz.bus.auth.runtime.RuntimeReloadService}
 * explicitly loads and atomically publishes later Registry revisions. {@link org.miaixz.bus.auth.worker.SourceWorker}
 * is the public compiled-capability contract returned by Source drivers and retained only inside a published runtime
 * generation.
 * </p>
 * <p>
 * Applications select protocol and Vendor drivers explicitly, build once, then request the initial and subsequent
 * reloads. Runtime assembly consumes public driver contracts and the immutable Registry view contract but contains no
 * OAuth, OpenID Connect, SAML, SCIM, LDAP, RADIUS, or Vendor business operation. It performs no classpath discovery,
 * project data loading during build, global singleton installation, or creation of transport and persistence resources.
 * </p>
 * <p>
 * Runtime and Registry closing stop framework work but do not close caller-owned Fabric, executor, JSON provider,
 * stores, loaders, keys, or network resources. Reload is all-or-nothing, observes one Budget, and must not expose
 * candidate options, compiled workers, credentials, protocol messages, exceptions, or stack traces when it fails.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.runtime;
