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
 * Implements the non-exported Toutiao OAuth completion and identity flow.
 * <p>
 * ToutiaoSourceAdapter exposes Source authentication and standard AuthorizationRequest only. RedirectManager atomically
 * owns state and exact callback correlation. Its private authorization encoder maps Client ID to official
 * {@code client_key} and adds only registered {@code auth_only} and {@code display} values. Completion obtains an
 * operation-scoped Client Secret lease and sends the historical query-bearing empty-form token POST.
 * </p>
 * <p>
 * Token parsing strictly validates {@code access_token}, {@code expires_in}, and {@code open_id} without inventing
 * {@code token_type}, TokenResponse, or O2T. The adapter immediately invokes the fixed profile endpoint with
 * {@code client_key} and {@code access_token}, closes the data envelope, and creates ExternalIdentity only from a
 * non-blank {@code uid}; {@code uid_type=14} affects display only. String and numeric forms of registered remote
 * {@code error_code} map directly to shared Bus Outcome and ErrorCode, with no custom error type.
 * </p>
 * <p>
 * Private handling may depend on vendor flow, Fabric, JSON, secret resolution, and Bus validation, but protocol
 * servers, Registry loaders, and external projects must not depend on it or receive private token records. State,
 * codes, Client Secret material, access token, OpenID, profile bodies, and platform diagnostics must not escape through
 * Context, tracing, logs, or public failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.toutiao.internal;
