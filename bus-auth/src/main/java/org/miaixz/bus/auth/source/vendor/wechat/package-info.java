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
 * Declares the WeChat and WeCom login family plus the optional WeCom enterprise directory Variant.
 * <p>
 * WeChatManifest separates {@code open}, {@code mp}, {@code mini}, {@code ee}, {@code ee-qrcode}, and {@code ee-web}.
 * Open and Official Account are OAuth clients exposing redirect Source authentication and standard authorization only;
 * their token responses omit mandatory {@code token_type}, so token, refresh, and profile calls remain private. Mini
 * Program and the three WeCom flows use their real HTTPS transport with Source authentication only. Every endpoint,
 * HTTP method, official field, envelope, and subject rule is variant-owned.
 * </p>
 * <p>
 * WeChatOptions contains the common routing, App ID or Corp ID, Client Secret reference, variant callback and scope,
 * plus the closed selectors {@code loginType}, {@code agentId}, {@code language}, and {@code userType}. Browser
 * variants require an exact callback; Mini Program prohibits callbacks and scopes. Open, Official Account, and ee-web
 * accept only their official identity scopes, while each WeCom selector is legal only for its owning variant.
 * </p>
 * <p>
 * This exported package exposes Source configuration metadata; execution enters a configured Source through Dispatcher.
 * Open and Official Account identities bind profile OpenID to token OpenID, Mini Program uses OpenID after discarding
 * session key, and WeCom uses the variant's verified user identifier. Third-party QR always uses
 * {@code user_info.userid}; {@code corp_info.corpid} is an attribute. Secrets, codes, tokens, session keys, agent IDs,
 * bodies, and diagnostics must not enter Context, tracing, logs, or public failures.
 * </p>
 * <p>
 * {@code wechat/ee-enterprise} remains a seventh Variant under the same Vendor because it connects to the same WeCom
 * product and credential authority. It uses a Corp ID and CLIENT_SECRET reference to expose describe, snapshot, and
 * retrieve for application-visible users, departments, parent/member/manager relations, and tags normalized as GROUP.
 * Tags are access groupings rather than organizational units, coverage is PARTIAL, and no changes operation exists.
 * </p>
 * <p>
 * Official department, department-user, tag, and tag-member endpoints are unpaged. The adapter rereads each complete
 * response without a local byte or record limit, then applies stable ordering, projection fingerprints, and bounded
 * output cursors. External projects call Dispatcher and own scheduling, reconciliation, and persistence; this package
 * neither creates another Vendor nor adds a synchronization subsystem.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source.vendor.wechat;
