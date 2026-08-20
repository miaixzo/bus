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
 * Implements the non-exported Okta OpenID Connect Source-authentication composition.
 * <p>
 * OktaSourceAdapter delegates every declared OpenID Connect operation unchanged to StandardAdapter. Its local Source
 * flow uses RedirectManager to atomically create and consume state and nonce without PKCE, binds the callback to the
 * exact registered URI, decodes one standard success or error branch, and requires the authorization-response issuer to
 * equal the resolved Okta issuer.
 * </p>
 * <p>
 * Completion redeems a standard AuthorizationCodeGrant and requires a Bearer TokenResponse with an ID Token. The
 * adapter obtains the issuer-bound JWK Set, uniquely selects a public RSA signing key by protected {@code kid},
 * {@code alg}, {@code use}, and {@code kty}, and reuses shared JOSE, JWT, issuer, time, and OpenID Connect verification
 * services for RS256 signature, issuer, audience, authorized party, nonce, time, and applicable artifact-hash checks.
 * It then invokes standard UserInfo and requires its {@code sub} to equal the verified ID Token subject.
 * </p>
 * <p>
 * Only the final subject and provider-neutral attributes leave this package as ExternalIdentity and Evidence. Private
 * verification composition may depend on standard protocol client models, shared JOSE/JWT services, Bus crypto, guard,
 * Fabric, and vendor flow primitives; protocol servers, Registry loaders, and external projects must not depend on this
 * package. Secret material, state, nonce, codes, compact tokens, JWK integers, claims, UserInfo bodies, and upstream
 * diagnostics must not escape through Context, tracing, logs, or public failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.okta.internal;
