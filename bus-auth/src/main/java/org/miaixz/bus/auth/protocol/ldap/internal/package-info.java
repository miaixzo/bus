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
 * Compiles and assembles non-exported LDAP version 3 runtime components.
 * <p>
 * LdapProviderDriver and LdapSourceDriver bind LDAP Provider and Source profiles to exact registration validation and
 * server operations to DirectoryStore or client identity resolution to one configured connection, bind, search, and
 * mapping pipeline.
 * </p>
 * <p>
 * RuntimeBuilder receives these drivers through the public Ldap facade. This package may depend on LDAP models, codecs,
 * profiles, services, resolvers, Fabric transport, SecurityBaseline, runtime contributions, and externally supplied
 * directory or credential ports. It does not expose public protocol operations, load project data, use reflection or
 * ServiceLoader, retain mutable global state, implement JNDI persistence, call Vendor adapters, or implement project
 * permissions.
 * </p>
 * <p>
 * Compilation fails closed for mismatched direction, protocol, namespace, endpoint, TLS mode, bind policy, search base,
 * schema, control, extension, limit, mapping, store, conformance, settings, or manifest. Only a complete immutable
 * candidate is published. Runtime connection state is isolated by trusted connection ID, StartTLS transitions are
 * atomic, credentials remain in SecretLease scope, and messages or directory data never escape into diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.ldap.internal;
