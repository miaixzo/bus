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
 * Declares the Baidu OAuth 2.0 Vendor definition and client registration settings.
 * <p>
 * BaiduDefinition exposes the single {@code baidu/default} OAUTH2 variant with fixed authorization, token, and
 * account-profile endpoints. Only authorization is standards-conforming and public. The definition records the private
 * GET query client-secret token exchange, token response without {@code token_type}, and query access-token profile
 * call as deviations used solely by Source authentication. PKCE is prohibited and {@code basic} is the default scope.
 * </p>
 * <p>
 * BaiduSourceSettings contains only routing, client ID, CLIENT_SECRET reference, exact callback, and registered scopes.
 * Users cannot override endpoints, enable PKCE, publish the historical revoke path, or select the private query
 * authentication as a standard OAuth method. This package exports no private token/profile DTO, standard token or
 * revocation capability, OIDC UserInfo, scope enum, or platform error representation.
 * </p>
 * <p>
 * Callback ownership is exact and production callbacks are HTTPS; requested scopes are unique registered values. The
 * public authorization capability preserves RFC 6749 request semantics, while the private token and profile chain stays
 * inside the adapter because its success response cannot form a TokenResponse. A verified non-blank profile
 * {@code openid} is the only external subject; deprecated {@code userid}, optional {@code unionid}, tokens, and profile
 * display fields cannot replace it.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.baidu;
