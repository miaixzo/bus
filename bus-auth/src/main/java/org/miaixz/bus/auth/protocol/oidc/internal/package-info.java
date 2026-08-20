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
 * Compiles and assembles non-exported OpenID Connect runtime components.
 * <p>
 * OpenIdProviderDriver and OpenIdSourceDriver bind the OpenID profiles to their runtime creation logic. The Provider
 * driver composes enabled OIDC services with the required OAuth 2.x server capabilities; OpenIdSourceDriver composes
 * the relying-party clients with the required OAuth 2.x client.
 * {@link org.miaixz.bus.auth.protocol.oidc.internal.IdTokenIssuer} is the narrow server-side port for
 * standards-compliant ID Token issuance, while its key and claim implementations remain externally supplied.
 * </p>
 * <p>
 * Runtime assembly receives these drivers through the public OpenIdConnect facade. This package may depend on OIDC and
 * OAuth models, profiles, codecs, JOSE/JWT, resolvers, stores, and SecurityBaseline, but never on runtime or Registry
 * implementations. It does not expose public protocol operations, load project data, discover implementations by
 * reflection or ServiceLoader, retain mutable global state, call Vendor adapters, or implement persistence, consent,
 * identity linking, or user authentication.
 * </p>
 * <p>
 * Compilation fails closed when direction, protocol, namespace, settings, issuer, endpoint, algorithm, key source,
 * OAuth composition, conformance, or manifest is inconsistent. A candidate is published only after every declared
 * capability is executable. ID Token issuance receives verified client, subject, consent, session, nonce, and one
 * Budget; IdTokenIssuer composes OpenIdTokenResponse without mutating OAuth TokenResponse, and signing keys and claims
 * remain operation-scoped and never enter failure details or diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.oidc.internal;
