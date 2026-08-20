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
 * Declares six public WeChat and WeCom identity Source variants and their externally loaded settings.
 * <p>
 * WeChatDefinition separates {@code open}, {@code mp}, {@code mini}, {@code ee}, {@code ee-qrcode}, and {@code ee-web}.
 * Open and Official Account are OAuth clients exposing redirect Source authentication and standard authorization only;
 * their token responses omit mandatory {@code token_type}, so token, refresh, and profile calls remain private. Mini
 * Program and the three WeCom flows are VENDOR_AUTH with Source authentication only. Every endpoint, HTTP method,
 * official field, envelope, and subject rule is variant-owned.
 * </p>
 * <p>
 * WeChatSourceSettings contains the common routing, App ID or Corp ID, Client Secret reference, variant callback and
 * scope, plus the closed selectors {@code loginType}, {@code agentId}, {@code language}, and {@code userType}. Browser
 * variants require an exact callback; Mini Program prohibits callbacks and scopes. Open, Official Account, and ee-web
 * accept only their official identity scopes, while each WeCom selector is legal only for its owning variant.
 * </p>
 * <p>
 * This exported package is registration metadata; execution enters a Registry-obtained Provider. Open and Official
 * Account identities bind profile OpenID to token OpenID, Mini Program uses OpenID after discarding session key, and
 * WeCom uses the variant's verified user identifier. Third-party QR always uses {@code user_info.userid};
 * {@code corp_info.corpid} is an attribute. Secrets, codes, tokens, session keys, agent IDs, bodies, and diagnostics
 * must not enter Context, tracing, logs, or public failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.wechat;
