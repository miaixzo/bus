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
 * Implements the non-exported Teambition OAuth completion and identity flow.
 * <p>
 * TeambitionSourceAdapter exposes Source authentication and standard AuthorizationRequest only. The standard
 * authorization encoder builds the redirect, and RedirectManager atomically owns state and exact callback correlation.
 * Completion obtains an operation-scoped Client Secret lease and uses the shared form codec to send the registered
 * {@code client_id}, {@code client_secret}, code, and {@code grant_type=code} fields.
 * </p>
 * <p>
 * The private token parser accepts only the frozen access-token and refresh-token shape and never invents
 * {@code token_type}, TokenResponse, or O2T. The refresh token is validated but not retained beyond the operation. The
 * adapter then invokes the profile endpoint with the historical {@code Authorization: OAuth2} scheme and creates
 * ExternalIdentity only from a non-blank {@code _id}; it does not expose UserInfo or a refresh method.
 * </p>
 * <p>
 * Private handling may depend on the authorization codec, vendor flow, shared form codec, Fabric, JSON, secret
 * resolution, and Bus validation, but protocol servers, Registry loaders, and external projects must not depend on it.
 * State, codes, Client Secret material, access and refresh tokens, authorization headers, profile bodies, and upstream
 * diagnostics must not escape through Context, tracing, logs, or public failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.teambition.internal;
