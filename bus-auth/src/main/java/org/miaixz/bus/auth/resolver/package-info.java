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
 * Defines external lookup ports for authentication resources, identities, and cryptographic material.
 * <p>
 * Client, subject, attribute, group, and resource resolvers obtain project-owned records for one typed operation.
 * Credential and certificate resolvers expose controlled metadata, while key and secret resolvers return only the
 * material authorized by a bounded query. {@link org.miaixz.bus.auth.shared.SecretLease} owns and erases resolved
 * character material; {@link org.miaixz.bus.auth.resolver.CredentialStore} persists protocol-generated dynamic
 * credentials that must survive across invocations.
 * </p>
 * <p>
 * {@link org.miaixz.bus.auth.shared.ExecutionServices} receives implementations from the external project, and
 * protocol, Vendor, identity, and guard code call the narrow port required by the current operation. This package
 * declares no implementation, data loader, persistence model, network client, Registry access, global locator, or
 * fallback resolver. A resolver does not invoke another Provider or Source to answer a query.
 * </p>
 * <p>
 * Every query is scoped by registered identifiers, purpose, algorithm, current Budget time, and applicable namespace.
 * Plaintext secret material exists only inside an operation-owned SecretLease and is erased on close; keys and
 * certificates must satisfy use, algorithm, identifier, and validity constraints. Implementations must not cache an
 * authorization decision beyond its lifetime or expose material, query contents, personal data, exceptions, and stack
 * traces in failures or logs.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.resolver;
