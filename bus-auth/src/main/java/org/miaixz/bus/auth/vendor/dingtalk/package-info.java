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
 * Declares DingTalk delegated OAuth, signed account-login, and enterprise directory Source variants.
 * <p>
 * DingTalkManifest exposes {@code dingtalk/oauth2} as OAUTH2 with fixed delegated authorization, JSON token, and
 * current-user endpoints, CLIENT_SECRET, prohibited PKCE, {@code openid} scope, Source authentication, and a public
 * authorization capability. It separately exposes {@code dingtalk/account} as a proprietary HTTPS Variant with fixed
 * scan-login and signed user-info endpoints, SHARED_SECRET, HMAC-SHA256, {@code snsapi_login}, and Source
 * authentication only.
 * </p>
 * <p>
 * DingTalkOptions adds optional {@code orgType}, {@code corpId}, exclusive-login flag, and exclusive corporation ID for
 * the delegated variant. Those fields are prohibited for account login. Users cannot select the historical {@code oidc}
 * alias, obsolete endpoints, platform client-authentication identifiers as standard methods, token/profile DTOs,
 * signing fields, or refresh and revoke capabilities. Each variant retains its own actual protocol and credential type.
 * </p>
 * <p>
 * Delegated options require CLIENT_SECRET, exact callback, official organization and exclusive-login combinations;
 * account options require SHARED_SECRET and prohibit delegated selectors. Delegated identity is keyed only by
 * {@code unionId}; signed account identity is keyed only by {@code unionid}. Neither {@code openId}, nickname, mobile,
 * temporary code, access token, corporation field, nor historical OIDC terminology may replace those subjects.
 * </p>
 * <p>
 * The distinct {@code dingtalk/enterprise} HTTPS Variant uses CLIENT_SECRET application credentials for describe,
 * snapshot, and retrieve over fixed management targets. Its recoverable snapshot traverses visible departments before
 * department members and then role and role-member pages, normalizing USER, ORGANIZATION, ROLE, parent, membership,
 * manager, and role-member values. Coverage remains PARTIAL and no changes capability is declared.
 * </p>
 * <p>
 * Delegated login organization selectors affect only the OAuth login request and never select enterprise directory
 * roots. External projects invoke the enterprise Variant through Dispatcher and own synchronization and persistence;
 * application tokens, upstream records, and cursors remain inside the Source operation boundary.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.dingtalk;
