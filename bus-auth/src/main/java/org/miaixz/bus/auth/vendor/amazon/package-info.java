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
 * Declares the Login with Amazon OAuth 2.0 Vendor definition and client settings.
 * <p>
 * AmazonDefinition exposes the single {@code amazon/default} OAUTH2 variant with fixed authorization, token,
 * token-info, and customer-profile endpoints. It declares client_secret_post, CLIENT_SECRET, optional S256 PKCE,
 * {@code profile}, {@code profile:user_id}, and {@code postal_code} defaults, and Source authentication plus standard
 * OAuth authorization and token capabilities. Amazon token-info is recorded as a platform identity step, not RFC 7662.
 * </p>
 * <p>
 * AmazonSourceSettings contains the common registration values and one explicit PKCE switch. Users cannot override the
 * platform endpoints, client authentication, token-info transport, Bearer profile transport, or default protocol. This
 * package exports no Amazon token-info or profile DTO, custom protocol model, introspection capability, UserInfo model,
 * independent refresh operation, or private response fields.
 * </p>
 * <p>
 * Credentials must reference CLIENT_SECRET, callback ownership is exact and HTTPS in production, requested scopes are
 * unique registered Amazon values, and enabled PKCE always uses S256. The public token operation preserves standard
 * authorization-code and refresh-token TokenRequest and TokenResponse semantics. Source authentication is successful
 * only after token-info audience equals the configured client ID and the profile contains a non-blank stable
 * {@code user_id}, which becomes the sole external subject.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.amazon;
