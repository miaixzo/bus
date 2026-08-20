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
 * Implements the non-exported RedNote marketing authorization and token wire operations.
 * <p>
 * RedNoteSourceAdapter routes exactly the Vendor-defined marketing authorization and token capabilities. Authorization
 * validates the registered callback and scopes before emitting the official {@code appId}, {@code scope},
 * {@code redirectUri}, and {@code state} query. Token execution accepts exactly one initial code or refresh token,
 * obtains an operation-scoped application-secret lease, and uses the shared form codec with the corresponding fixed
 * endpoint and official {@code app_id}, {@code secret}, and branch field names.
 * </p>
 * <p>
 * Response parsing closes the proprietary JSON success/error union, selects the branch-specific lifetime member, and
 * returns only MarketingTokenResponse. The adapter does not create RedirectManager, SourceAuthentication,
 * ExternalIdentity, OAuth AuthorizationRequest, TokenRequest, TokenResponse, or a separate public refresh method.
 * </p>
 * <p>
 * Private form and response handling may depend on Fabric, JSON, shared codecs, secret resolution, and Bus validation,
 * but protocol servers, Registry loaders, and external projects must not depend on it. Application secrets, state,
 * codes, access and refresh tokens, form bytes, response bodies, sub-error data, and upstream descriptions must not
 * escape through Context, tracing, logs, or public failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.rednote.internal;
