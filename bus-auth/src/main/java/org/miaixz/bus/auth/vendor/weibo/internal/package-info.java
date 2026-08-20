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
 * Implements the non-exported Weibo OAuth completion, profile, and revocation flow.
 * <p>
 * WeiboSourceAdapter routes Source authentication, standard AuthorizationRequest, and RevocationRequest. The private
 * authorization encoder applies comma scope, while RedirectManager atomically owns state and exact callback
 * correlation. Code redemption obtains an operation-scoped Client Secret lease and sends the historical query-bearing
 * empty-form POST, then parses access token and token-bound UID without inventing {@code token_type}, TokenResponse, or
 * O2T.
 * </p>
 * <p>
 * The fixed profile call carries query access token and UID plus the historical OAuth2 authorization header, never a
 * fabricated local IP. Strict parsing requires profile {@code id}, optional {@code idstr}, and token UID to agree
 * before creating ExternalIdentity. Revocation maps the standard request to the fixed query GET and accepts only exact
 * {@code result=true}.
 * </p>
 * <p>
 * Private handling may depend on standard authorization and revocation models, vendor flow, Fabric, JSON, secret
 * resolution, and Bus validation, but protocol servers, Registry loaders, and external projects must not depend on it.
 * State, codes, secret material, tokens, UIDs, headers, profile bodies, and upstream diagnostics must not escape
 * through Context, tracing, logs, or public failures; responses are bounded, duplicate-rejecting, and closed.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.weibo.internal;
