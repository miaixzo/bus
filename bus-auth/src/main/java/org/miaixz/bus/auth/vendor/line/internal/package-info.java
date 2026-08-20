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
 * Implements the non-exported LINE OpenID Connect authentication flow.
 * <p>
 * LineSourceAdapter uses RedirectManager for independent state, nonce, and S256 verifier values, validates the exact
 * query callback, and retains standard TokenRequest and TokenResponse operations. Discovery confirms the frozen
 * endpoints and official UserInfo metadata without replacing the profile endpoint. JWKS is used only for ES256; HS256
 * verification uses the operation's channel-secret lease through bus-crypto.
 * </p>
 * <p>
 * Initial tokens require access, refresh, compact ID Token, Bearer type, positive lifetime, and scope; refresh tokens
 * do not require a new ID Token. Before profile retrieval, the ID Token is locally verified for algorithm, signature,
 * issuer, audience, time, nonce, and subject. The Bearer profile response must bind {@code userId} to that subject and
 * preserve optional profile fields only as attributes.
 * </p>
 * <p>
 * Standard revocation maps the request token to LINE's {@code access_token} form member and accepts only HTTP 200 with
 * an empty body. One secret lease spans Source token, HS256 verification, and profile stages; independent token and
 * revocation calls own one lease each. All correlation values, secrets, tokens, JWTs, headers, bodies, and personal
 * data remain outside Context, failure details, tracing, and logs.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.line.internal;
