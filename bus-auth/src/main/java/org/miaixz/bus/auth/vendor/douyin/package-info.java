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
 * Declares Douyin open-platform OAuth and ordinary mini-program Source variants.
 * <p>
 * DouyinManifest exposes {@code douyin/open} as OAUTH2 with fixed authorization, token, profile, and refresh endpoints,
 * CLIENT_SECRET, prohibited PKCE, required {@code user_info} scope, and Source authentication only. Its client-key
 * fields, comma scopes, forms, response envelopes, and missing token type remain registered deviations. It separately
 * exposes {@code douyin/mini-program} as VENDOR_AUTH with the fixed JSON {@code jscode2session} endpoint and
 * one-time-code Source authentication.
 * </p>
 * <p>
 * DouyinOptions applies the same routing and credential reference shape to two distinct variants. Open requires an
 * exact HTTPS callback without query or fragment and unique official scopes containing {@code user_info}; mini-program
 * prohibits callback and scope options. Users cannot configure endpoints, issuer, PKCE, optionalScope, refresh switch,
 * anonymous or game login, sandbox, private token/profile/session DTOs, or standard OAuth capabilities for proprietary
 * responses.
 * </p>
 * <p>
 * Both variants require CLIENT_SECRET but never expose its value. Open identity is keyed only by verified
 * {@code union_id}, never {@code open_id}; mini-program identity is keyed only by verified {@code openid}, with
 * optional {@code unionid} as an attribute. Mini-program {@code session_key} remains server-local and cannot become a
 * token, identity attribute, Session field, Context value, or user-visible result.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.douyin;
