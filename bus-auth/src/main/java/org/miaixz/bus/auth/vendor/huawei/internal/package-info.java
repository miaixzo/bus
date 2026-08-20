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
 * Implements the non-exported Huawei Account Kit authentication flow.
 * <p>
 * HuaweiSourceAdapter generates bounded platform-compatible state, nonce, and S256 material, emits a form-post
 * authentication request, and accepts the exact HTTPS form callback. A missing RFC 9207 issuer parameter is tolerated
 * only for the already bound Source and callback; a present issuer must match, and the later ID Token issuer is always
 * verified. The private token form includes {@code supportAlg=RS256} and strictly decodes numeric error envelopes.
 * </p>
 * <p>
 * Discovery must exactly confirm the frozen issuer, endpoints, form-post mode, pairwise subjects, scopes, client
 * authentication, S256, and RS256. JWKS accepts only the exact public RSA signing key. Before profile access, the
 * compact ID Token is locally verified for signature, issuer, audience, authorized party, time, nonce, access-token
 * hash, and subject. The proprietary profile form then binds UnionID and OpenID to those verified claims.
 * </p>
 * <p>
 * Standard revocation accepts RevocationRequest but sends only Huawei's {@code token} field and requires exact
 * empty-JSON success. One secret lease spans the Source token and profile chain; revocation uses none. State, nonce,
 * code, verifier, secret, tokens, JWT, headers, forms, profiles, numeric descriptions, and bodies never enter Context,
 * failure details, tracing, or logs.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.huawei.internal;
