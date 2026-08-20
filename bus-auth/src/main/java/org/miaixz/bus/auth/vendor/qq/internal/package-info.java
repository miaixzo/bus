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
 * Implements the non-exported QQ Open Platform and Mini Program authentication flows.
 * <p>
 * For {@code qq/open}, QqSourceAdapter exposes standard AuthorizationRequest encoding and Source authentication only.
 * RedirectManager atomically owns state and exact callback correlation. Code redemption remains private because the
 * historical query-authenticated empty-form response lacks standard {@code token_type}; the adapter therefore parses
 * its text parameters without constructing TokenResponse, then validates the JSONP OpenID response, its Client ID, and
 * the profile response before choosing the bound OpenID or requested available UnionID as subject.
 * </p>
 * <p>
 * For {@code qq/mini-program}, the adapter accepts only a Source OneTimeCode, registers its digest through the shared
 * VENDOR_AUTH replay guard, obtains a short-lived App Secret lease, and invokes the fixed {@code jscode2session}
 * endpoint. The returned {@code session_key} is required but discarded; only OpenID becomes subject and optional
 * UnionID remains an attribute.
 * </p>
 * <p>
 * Private wire handling may depend on vendor flow, shared replay protection, Fabric, JSON, and Bus validation, but
 * protocol servers, Registry loaders, and external projects must not depend on it or receive private token models.
 * State, login codes, callback codes, secrets, access and refresh tokens, session keys, JSONP text, profile bodies, and
 * upstream diagnostics must not escape through Context, identity attributes, tracing, logs, or public failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.qq.internal;
