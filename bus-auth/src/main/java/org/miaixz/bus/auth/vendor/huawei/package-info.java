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
 * Declares the Huawei Account Kit OpenID Connect Source variant.
 * <p>
 * HuaweiDefinition fixes {@code huawei/default}, issuer {@code https://accounts.huawei.com}, Discovery, authorization,
 * token, refresh, JWKS, proprietary profile, and revocation endpoints, CLIENT_SECRET form authentication, mandatory
 * S256 PKCE, RS256 ID Tokens, and exact {@code openid profile email} scopes. It publishes Source authentication plus
 * standard authentication, revocation, Discovery, and JWKS operations, but no public token or UserInfo capability
 * because Huawei's numeric errors and profile wire are platform-specific.
 * </p>
 * <p>
 * HuaweiSourceSettings contains only routing, client, secret-reference, exact HTTPS callback, and the frozen scopes. It
 * cannot configure endpoints, issuer, algorithms, PKCE, tokeninfo, SDK-only codes, client credentials, implicit or
 * hybrid flow, private token/profile records, or separate refresh operations. Remote metadata can confirm but never
 * rewrite the compiled definition.
 * </p>
 * <p>
 * Identity is the locally verified ID Token {@code sub}, representing Huawei UnionID, and must equal the proprietary
 * profile {@code unionID}; any bound OpenID must also match. Email, nickname, display name, image, token, and platform
 * status cannot replace the subject. Form-post callback, numeric errors, profile form, and unauthenticated revocation
 * remain explicit deviations.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.huawei;
