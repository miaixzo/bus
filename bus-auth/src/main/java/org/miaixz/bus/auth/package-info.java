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
 * Defines the exported protocol- and Vendor-neutral domain language of the authentication framework.
 * <p>
 * The root types identify managed {@link org.miaixz.bus.auth.Library Libraries}, project-owned
 * {@link org.miaixz.bus.auth.Provider Provider groups}, configured {@link org.miaixz.bus.auth.Source Sources},
 * immutable {@link org.miaixz.bus.auth.Blueprint Blueprints}, and invocation contracts such as
 * {@link org.miaixz.bus.auth.Capability}, {@link org.miaixz.bus.auth.Context}, {@link org.miaixz.bus.auth.Timeout},
 * {@link org.miaixz.bus.auth.Callback}, and {@link org.miaixz.bus.auth.Outcome}. Credentials, identities, subjects,
 * sessions, endpoints, and forms retain cross-cutting semantics shared by protocol, registry, runtime, identity, and
 * Vendor packages. {@link org.miaixz.bus.auth.Scheme} owns description subcontracts such as
 * {@link org.miaixz.bus.auth.Scheme.Options} and {@link org.miaixz.bus.auth.Scheme.Conformance};
 * {@link org.miaixz.bus.auth.Identity} owns verified {@link org.miaixz.bus.auth.Identity.Evidence} values.
 * </p>
 * <p>
 * {@link org.miaixz.bus.auth.Registry} and its nested {@link org.miaixz.bus.auth.Registry.Connector} define the shared
 * build-time extension vocabulary. Specialized Source and Vendor contracts retain their own typed binding operations,
 * while the root contracts standardize keyed single registration, atomic bulk registration, removal, lookup, and
 * freeze-before-runtime semantics without importing either specialization.
 * </p>
 * <p>
 * {@link org.miaixz.bus.auth.Realm} is the protocol-neutral immutable resource and Capability contract shared by any
 * compiled Source that can describe, snapshot, change-track, or retrieve managed identities and relationships. It is
 * not a persistence entity, service, manager, loader, or runtime access layer. Projects select an existing Source with
 * {@link org.miaixz.bus.auth.Roster.Reference} and invoke Realm capabilities only through
 * {@link org.miaixz.bus.auth.Dispatcher}.
 * </p>
 * <p>
 * Dependencies flow from specialized second-level packages toward these contracts. Root types use Bus core and
 * protocol-neutral transport or JSON value primitives only; they do not import protocol implementations, Vendor
 * adapters, registry implementation classes, runtime assembly, or project persistence. External projects provide data
 * and ports through the relevant specialized worker, parser, and service packages.
 * </p>
 * <p>
 * These objects are framework control-plane and invocation values, not protocol wire documents. A transport adapter
 * must encode only the applicable OAuth, OpenID Connect, SAML, SCIM, LDAP, or RADIUS standard model and must never
 * serialize Roster references, Context, Timeout, Callback, Outcome, Bus errors, credentials, exceptions, stack traces,
 * or secret material to an authentication user.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth;
