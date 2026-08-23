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
 * Declares Aliyun OpenID Connect login and Alibaba Cloud RAM identity-read variants.
 * <p>
 * AliyunManifest exposes the single {@code aliyun/default} OIDC variant with fixed authorization, token, UserInfo,
 * revocation, Discovery, and JWK Set endpoints. It declares client_secret_post, CLIENT_SECRET, mandatory S256 PKCE,
 * RS256 ID Tokens, {@code openid profile} defaults, and only the Source authentication, OIDC authorization, token,
 * revocation, Discovery, JWK Set, and UserInfo capabilities implemented by its adapter. Its deviation list is empty.
 * </p>
 * <p>
 * AliyunOptions contains only routing, public client ID, secret reference, exact callback, and ordered official scopes.
 * Fixed platform endpoints, client authentication, algorithms, PKCE policy, Discovery defaults, and response fields are
 * manifest-owned and cannot be overridden by users. This package exports no private token or profile DTO, custom scope
 * enum, endpoint selector, issuer selector, or Vendor-specific alternative to standard OIDC models.
 * </p>
 * <p>
 * Credentials must reference CLIENT_SECRET, callback ownership is exact and HTTPS in production, and requested scopes
 * are unique official {@code openid}, {@code profile}, or {@code aliuid} values that always contain the first two.
 * Published conformance and capabilities correspond only to the corrected standard FORM, Bearer, PKCE, nonce,
 * Discovery, JWKS, ID Token, and UserInfo flow; historical query secrets, query access tokens, and empty scope behavior
 * are not retained as deviations.
 * </p>
 * <p>
 * {@code aliyun/ram} is a separate HTTPS Variant using an AccessKey ID and referenced SHARED_SECRET. It signs fixed RAM
 * RPC actions with ACS3-HMAC-SHA256 and exposes describe, snapshot, and retrieve for RAM USER, GROUP, ROLE, group
 * membership, and role-trust user relations. RAM identities are administrative cloud identities and are not
 * automatically classified as employees; coverage is PARTIAL and no changes operation exists.
 * </p>
 * <p>
 * OIDC login credentials and scopes do not authorize RAM reads. The RAM Variant remains under the same Aliyun Vendor,
 * uses only Bus HTTP, encoding, clock, random, digest, and HMAC components, and keeps AccessKey material inside one
 * Source invocation. External projects call Dispatcher and own sync scheduling, business classification, and storage.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source.vendor.aliyun;
