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
 * Declares the Feishu confidential-client OAuth Source and optional enterprise Contact v3 variants.
 * <p>
 * FeishuManifest exposes {@code feishu/default} with fixed authorization, v3 token, refresh, and user-information
 * endpoints. It requires CLIENT_SECRET and S256 PKCE, has no default scope, accepts only optional
 * {@code offline_access}, and publishes Source authentication plus the standard authorization operation. JSON client
 * authentication, token envelopes, refresh rotation, and profile envelopes are registered platform deviations.
 * </p>
 * <p>
 * FeishuOptions contains only routing, client, secret-reference, exact callback, and scope data. Applications cannot
 * choose legacy v1 or v2 endpoints, disable PKCE, configure token JSON fields, publish private refresh behavior,
 * reinterpret the profile endpoint as OIDC UserInfo, or expose platform response records. The callback is an exact
 * registered HTTPS URI, and scopes are unique and bounded.
 * </p>
 * <p>
 * Source authentication accepts only a verified non-blank {@code union_id} as its subject. The required
 * {@code open_id}, optional user ID, tenant, employee, avatar, name, email, and mobile values remain attributes and
 * never replace that identity key. Tokens, verifier, secret, private envelopes, and personal attributes remain within
 * the operation's security boundary.
 * </p>
 * <p>
 * The separate {@code feishu/enterprise} HTTPS Variant uses an application Client ID and CLIENT_SECRET reference to
 * obtain a tenant token. It exposes only describe, snapshot, and retrieve for visible users, departments, groups, and
 * their membership or management relations through fixed Contact v3 targets. Coverage is PARTIAL because the result is
 * bounded by the application's granted contact scope; event subscriptions are external notifications and are not an
 * Enterprise changes implementation.
 * </p>
 * <p>
 * Enterprise invocation still enters the shared Dispatcher execution path. The package does not schedule sync jobs,
 * persist directory state, promise fields outside the allow-listed projection, or convert Contact events into durable
 * change cursors. Tenant tokens, application secrets, upstream documents, and personal values remain Source-private.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.feishu;
