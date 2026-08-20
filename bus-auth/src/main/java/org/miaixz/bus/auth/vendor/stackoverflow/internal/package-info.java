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
 * Implements the non-exported Stack Overflow OAuth completion and identity flow.
 * <p>
 * StackOverflowSourceAdapter exposes Source authentication and standard AuthorizationRequest only. RedirectManager
 * atomically owns state and exact callback correlation, while comma-delimited scope is applied only by the private URL
 * encoder. Code redemption obtains an operation-scoped Client Secret lease and sends the registered query-bearing form
 * POST, then strictly parses {@code access_token} and the optional {@code expires}/{@code no_expiry} semantics without
 * inventing {@code token_type}, TokenResponse, or a public token capability.
 * </p>
 * <p>
 * Completion calls fixed Stack Exchange {@code /me} with the access token, Stack Apps key, and site. It closes the
 * items envelope and creates ExternalIdentity only when exactly one registered user has a positive integral
 * {@code user_id}; profile fields remain attributes and never replace the subject.
 * </p>
 * <p>
 * Private handling may depend on vendor flow, Fabric, JSON, secret resolution, and Bus validation, but protocol
 * servers, Registry loaders, and external projects must not depend on it or receive a private token model. State,
 * codes, Client Secret material, access token, key, site, response bodies, and upstream diagnostics must not escape
 * through Context, evidence attributes, tracing, logs, or public failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.stackoverflow.internal;
