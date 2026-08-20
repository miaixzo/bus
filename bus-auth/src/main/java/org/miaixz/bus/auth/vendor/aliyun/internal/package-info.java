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
 * Implements the non-exported standards-conforming Aliyun OpenID Connect adapter.
 * <p>
 * AliyunSourceAdapter composes RedirectManager with standard OIDC and OAuth codecs. It generates state, nonce, and S256
 * PKCE, emits the standard Authentication Request, validates the exact callback, executes authorization-code or
 * refresh-token grants with client_secret_post, supports standard revocation, validates Discovery, obtains the JWK Set,
 * verifies the ID Token locally, calls UserInfo with Bearer authentication, and binds both subjects.
 * </p>
 * <p>
 * The adapter returns standard TokenResponse, Revocation, OpenIdProviderMetadata, JwkSet, and UserInfo operation
 * results for their declared capabilities and only ExternalIdentity for the composed Source authentication flow. It may
 * use shared OAuth/OIDC codecs, JOSE/JWT, PKCE stores, SecretResolver, JsonProvider, SecurityBaseline, and Fabric. It
 * exposes no Aliyun token/profile DTO and does not implement query-secret or query-token fallbacks, implicit flow,
 * remote token validation, endpoint discovery beyond the fixed profile, or an independent refresh method.
 * </p>
 * <p>
 * The chain binds exact issuer, client, redirect URI, scopes, state, nonce, PKCE verifier, and Budget. Metadata must
 * reproduce the fixed issuer and endpoints and advertise code, public subject, required scopes, RS256, and S256. JWK
 * selection requires one RSA signing key matching protected {@code kid} and RS256. ID Token issuer, audience, azp,
 * time, nonce, and applicable hashes are verified before UserInfo, whose {@code sub} must match exactly. Only that
 * verified subject enters ExternalIdentity; secrets, codes, verifiers, tokens, JWK material, bodies, and claims never
 * enter logs.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.aliyun.internal;
