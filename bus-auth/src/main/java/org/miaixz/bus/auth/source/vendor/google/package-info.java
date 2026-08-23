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
 * Declares Google web-server OpenID Connect login and Workspace management Source variants.
 * <p>
 * GoogleManifest fixes {@code google/default}, issuer {@code https://accounts.google.com}, Discovery, authorization,
 * token, refresh, JWKS, UserInfo, and revocation endpoints, CLIENT_SECRET form authentication, required S256 PKCE,
 * RS256 ID Tokens, and the exact {@code openid profile email} scopes. Its manifest publishes Source authentication and
 * the standard OIDC authentication, token, revocation, Discovery, JWKS, and UserInfo operations.
 * </p>
 * <p>
 * GoogleOptions contains routing, client, secret-reference, exact HTTPS redirect URI, and those three scopes. It cannot
 * configure endpoints, issuer, algorithms, PKCE, tokeninfo, implicit or hybrid flow, device flow, JavaScript callbacks,
 * hosted-domain policy, incremental authorization, DPoP, introspection, or RP-Initiated Logout.
 * </p>
 * <p>
 * Remote metadata can only confirm the frozen manifest and cannot rewrite it. Source identity is the locally verified
 * ID Token {@code sub}, which must equal UserInfo {@code sub}; email, hosted domain, names, and profile data remain
 * standard claims and never replace the subject. The documented legacy token issuer is an explicit verification
 * deviation, not an alternative configured issuer.
 * </p>
 * <p>
 * {@code google/workspace} is a separate HTTPS Realm Variant. It resolves a referenced RSA private key, creates a
 * bounded RS256 domain-wide-delegation assertion for the configured service-account client and delegated administrator,
 * and requests only the fixed Admin SDK read scopes. It exposes describe, snapshot, and retrieve for supported users,
 * organizational units, groups, roles, and their selected relations; coverage is UNKNOWN and no changes exists.
 * </p>
 * <p>
 * OIDC client secrets, browser scopes, and login endpoints cannot authorize Workspace management. Conversely, the
 * Workspace key and delegated subject do not participate in login. Admin watch channels are external notifications that
 * a project may orchestrate; they are not durable Realm CHANGES. Dispatcher invocation remains separate from project
 * scheduling, checkpointing, synchronization, and persistence.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source.vendor.google;
