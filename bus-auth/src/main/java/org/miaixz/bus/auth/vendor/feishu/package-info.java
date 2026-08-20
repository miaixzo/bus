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
 * Declares the Feishu confidential-client OAuth Source variant.
 * <p>
 * FeishuDefinition exposes {@code feishu/default} with fixed authorization, v3 token, refresh, and user-information
 * endpoints. It requires CLIENT_SECRET and S256 PKCE, has no default scope, accepts only optional
 * {@code offline_access}, and publishes Source authentication plus the standard authorization operation. JSON client
 * authentication, token envelopes, refresh rotation, and profile envelopes are registered platform deviations.
 * </p>
 * <p>
 * FeishuSourceSettings contains only routing, client, secret-reference, exact callback, and scope data. Applications
 * cannot choose legacy v1 or v2 endpoints, disable PKCE, configure token JSON fields, publish private refresh behavior,
 * reinterpret the profile endpoint as OIDC UserInfo, or expose platform response records. The callback is an exact
 * registered HTTPS URI, and scopes are unique and bounded.
 * </p>
 * <p>
 * Source authentication accepts only a verified non-blank {@code union_id} as its subject. The required
 * {@code open_id}, optional user ID, tenant, employee, avatar, name, email, and mobile values remain attributes and
 * never replace that identity key. Tokens, verifier, secret, private envelopes, and personal attributes remain within
 * the operation's security boundary.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.feishu;
