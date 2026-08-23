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
 * {@link org.miaixz.bus.auth.runtime.RuntimeServices} combines caller-owned infrastructure with one immutable
 * {@link org.miaixz.bus.auth.worker.WorkerSet} and framework-owned pure parsers. It does not implement
 * {@link org.miaixz.bus.auth.source.SourceServices}; runtime compilation creates one capability-limited scoped view
 * from each prepared Source Blueprint entry, Worker slots, and framework dependencies.
 * {@link org.miaixz.bus.auth.runtime.RuntimeBuilder} accepts explicit Source modules, freezes their indexes, and
 * returns a {@link org.miaixz.bus.auth.runtime.RuntimeManager};
 * {@link org.miaixz.bus.auth.runtime.RuntimeReloadService} explicitly loads and atomically publishes later Roster
 * revisions. {@link org.miaixz.bus.auth.worker.SourceWorker} is the public compiled-capability contract returned by
 * Source drivers and retained only inside a published runtime container.
 * </p>
 * <p>
 * Applications select protocol and Vendor drivers explicitly, build once, and request later reloads as project data
 * changes. Normal build invokes the explicitly supplied BlueprintLoader once and exposes the runtime only after the
 * initial snapshot commits; {@code buildEmpty()} is the named exception. Runtime assembly consumes public driver
 * contracts and the fixed Roster snapshot contract but contains no OAuth, OpenID Connect, SAML, SCIM, LDAP, RADIUS, or
 * Vendor business operation. It performs no classpath discovery, implicit protocol-data loading, global singleton
 * installation, or creation of transport and persistence resources.
 * </p>
 * <p>
 * Runtime closing stops new authentication and reload work while the Roster retains the last immutable snapshot for
 * read-only inspection. Closing does not close the caller-owned executor, cache backend, stores, loaders, keys, or
 * network resources, and it does not alter the application-wide {@link org.miaixz.bus.extra.json.JsonKit} provider
 * selection. The static {@link org.miaixz.bus.auth.FabricX} transport boundary owns its shared Fabric context
 * independently of runtime lifecycle. Reload is all-or-nothing, observes one Timeout, and must not expose candidate
 * options, compiled workers, credentials, protocol messages, exceptions, or stack traces when it fails.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.runtime;
