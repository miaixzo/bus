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
 * Declares the Google web-server OpenID Connect Source variant.
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
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.google;
