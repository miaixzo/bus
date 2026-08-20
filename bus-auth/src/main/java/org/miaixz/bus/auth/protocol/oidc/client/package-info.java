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
 * Implements the OpenID Connect Relying Party and generic Source direction.
 * <p>
 * {@link org.miaixz.bus.auth.protocol.oidc.client.OpenIdClient} composes the OAuth 2.x client operations with
 * Discovery, Authentication Request URL encoding, typed OpenIdTokenResponse handling, UserInfo, and RP-Initiated
 * Logout. DiscoveryClient obtains composed Provider Metadata, AuthorizationResponseValidator binds the OAuth browser
 * response, IdTokenVerifier validates the signed identity assertion, and UserInfoClient binds returned claims to the
 * verified subject. OpenIdClientSettings and OpenIdSourceProfile expose only the endpoints, algorithms, claims, and
 * capabilities supported by a generic OIDC Source.
 * </p>
 * <p>
 * This package consumes OIDC and OAuth models and codecs, shared JOSE/JWT/PKCE stores, resolvers, SecurityBaseline, and
 * Fabric transport. It does not issue ID Tokens, host Provider endpoints, create local identities, persist project
 * registrations, choose a Vendor, infer undeclared algorithms or endpoints, or hide callback verification behind a
 * non-standard login or token-exchange operation.
 * </p>
 * <p>
 * A relying-party flow binds the exact issuer, client, redirect URI, response type, scope, state, nonce, PKCE verifier,
 * and one Budget. Discovery issuer and authorization-response issuer prevent mix-up; JWK selection is unambiguous and
 * algorithm/key compatible; ID Token signature, issuer, audience, azp, time, nonce, and applicable hashes are verified
 * before UserInfo; UserInfo {@code sub} must equal the verified ID Token subject. Cached JWK Sets obey response policy,
 * unknown keys trigger at most one bounded refresh, and tokens, verifiers, assertions, or claims never enter logs.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.oidc.client;
