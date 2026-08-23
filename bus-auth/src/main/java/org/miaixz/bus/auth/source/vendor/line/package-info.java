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
 * Declares the LINE web OpenID Connect Source variant.
 * <p>
 * LineManifest fixes {@code line/default}, issuer {@code https://access.line.me}, Discovery, authorization, token,
 * refresh, JWKS, legacy-compatible profile, and revocation endpoints, CLIENT_SECRET form authentication, mandatory S256
 * PKCE, and HS256 or ES256 ID Tokens. It publishes Source authentication plus standard authentication, token,
 * revocation, Discovery, and JWKS operations; the retained {@code /v2/profile} resource is not OIDC UserInfo.
 * </p>
 * <p>
 * LineOptions contains routing, channel ID, secret reference, exact HTTPS callback, and unique scopes containing
 * {@code profile} and {@code openid}. It cannot configure endpoints, issuer, PKCE, algorithms, response mode, native or
 * LIFF behavior, prompts, private profile models, remote token verification, or deauthorization.
 * </p>
 * <p>
 * Identity is the locally verified ID Token {@code sub}, which must equal profile {@code userId}. Profile name,
 * picture, status, email, and token data cannot replace it. Discovery/UserInfo and algorithm differences, independent
 * nonce, and LINE's {@code access_token} revocation field remain explicit deviations; obsolete JSON revocation success
 * is excluded.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source.vendor.line;
