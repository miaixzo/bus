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
 * Implements the non-exported Taobao OAuth wire adaptation and token-derived identity flow.
 * <p>
 * TaobaoSourceAdapter routes only Source authentication and the declared standard authorization and token operations.
 * RedirectManager atomically owns state and exact callback correlation. Authorization accepts AuthorizationRequest and
 * adds only the registered {@code view=web}; token execution accepts AuthorizationCodeGrant in TokenRequest, obtains an
 * operation-scoped Client Secret lease, and sends the historical query-bearing empty-form POST.
 * </p>
 * <p>
 * Strict token parsing maps standard access-token members to TokenResponse and preserves Taobao identity members only
 * as extensions. Source completion validates and prefers {@code taobao_user_id}, falls back to {@code taobao_open_uid}
 * only when absent, and uses the Bus URL decoder for the optional nickname. The returned {@code id_token} is retained
 * as data but never verified or used as identity evidence, and no UserInfo or refresh operation is inferred.
 * </p>
 * <p>
 * Private handling may depend on standard OAuth models and codecs, vendor flow, Fabric, JSON, URL decoding, secret
 * resolution, and Bus validation, but protocol servers, Registry loaders, and external projects must not depend on it.
 * State, codes, secret material, tokens, identity extensions, nickname text, and response bodies must not escape
 * through Context, tracing, logs, or public failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.taobao.internal;
