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
 * Defines the Blueprint control plane that validates complete snapshots and materializes read-only Roster views.
 * <p>
 * The root {@link org.miaixz.bus.auth.Registry} accepts build-time {@link org.miaixz.bus.auth.Registry.Connector}
 * declarations; this package does not implement Source or Vendor connector registries. It begins after the
 * implementation set is frozen and processes externally loaded Library, Provider, and Source Blueprint snapshots into
 * immutable {@link org.miaixz.bus.auth.Roster} revisions.
 * </p>
 * <p>
 * External projects implement {@link org.miaixz.bus.auth.worker.loader.BlueprintLoader} to supply complete Blueprint
 * snapshots. CRUD and management data access remain project responsibilities. Blueprint entries detach the
 * framework-owned Library, Provider, and Source fields on entry and return detached copies, while snapshot entry lists
 * are structurally frozen. The loader converts persisted Source configuration into typed
 * {@link org.miaixz.bus.auth.Scheme.Options} before this boundary.
 * {@link org.miaixz.bus.auth.registry.SnapshotValidator} applies only framework-required identity,
 * Library-to-Provider-to-Source ownership, enabled-parent, and Source routing rules before compilation. Presentation,
 * launch, code, name, icon, ordering, CRUD, and project uniqueness policies remain outside bus-auth.
 * {@link org.miaixz.bus.auth.worker.RosterListener} observes publication lifecycle, while
 * {@link org.miaixz.bus.auth.Roster.Fault} reports non-secret reload failures.
 * </p>
 * <p>
 * {@link org.miaixz.bus.auth.registry.SnapshotRoster} indexes one validated fixed revision, while
 * {@link org.miaixz.bus.auth.registry.CurrentRoster} exposes the currently published revision without owning runtime
 * publication. Runtime assembly retains compiled workers, container leases, atomic publication, retirement, and worker
 * lifecycle in the runtime package. No concrete protocol service, platform adapter, Driver, persistence implementation,
 * security decision, audit operation, or capability execution belongs to this package.
 * </p>
 * <p>
 * A reload validates and compiles the complete candidate before one atomic publication; partial views and fallback to
 * an invalid candidate are forbidden. Faults and listener notifications must omit options bodies, credentials, tokens,
 * protocol messages, exceptions, and stack traces.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.registry;
