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
 * Implements the non-exported WeChat and WeCom Source-authentication flows.
 * <p>
 * WeChatAdapterSupport routes the six frozen variants and exposes standard AuthorizationRequest only for Open Platform
 * and Official Account. Their browser flows atomically consume state, bind exact callbacks, encode official
 * {@code appid}, scope, and {@code wechat_redirect}, and privately parse token GET responses without inventing
 * {@code token_type}. Profile OpenID must equal token OpenID; base-scope completion uses the already verified OpenID.
 * </p>
 * <p>
 * Mini Program registers the one-time {@code js_code} through shared replay protection before resolving its secret,
 * invokes {@code jscode2session}, requires and discards {@code session_key}, and exposes only OpenID with optional
 * UnionID attribute. The three WeCom browser flows independently implement corporate QR, service-provider QR, and web
 * URLs, atomically consume state, retain client or provider tokens only in private records, preserve documented UserId
 * casing, and bind service-provider {@code user_info.userid} while retaining {@code corp_info.corpid} only as an
 * attribute.
 * </p>
 * <p>
 * Private handling may depend on standard authorization codecs, vendor flow, replay guards, Fabric, JSON, secret
 * resolution, and Bus validation, but protocol servers, Registry loaders, and external projects must not depend on it
 * or receive platform token/UserInfo records. Secrets, state, codes, tokens, session keys, agent identifiers, JSON
 * bodies, and upstream diagnostics must not escape through Context, tracing, logs, identities, or public failures;
 * owned secret bodies are cleared and responses are closed.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.wechat.internal;
