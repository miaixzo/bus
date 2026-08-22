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
 * Declares the public RedNote marketing authorization profile and externally loaded options.
 * <p>
 * RedNoteManifest fixes the proprietary {@code rednote/marketing} HTTPS Variant and its authorization, initial-token,
 * and refresh-token endpoints. It publishes only the application capabilities
 * {@code vendor.rednote.marketing_authorize} and {@code vendor.rednote.marketing_token}, with dedicated nested request
 * and response records that preserve official field names and the initial-versus-refresh token union. It does not
 * publish Source authentication, ExternalIdentity, OAuth AuthorizationRequest, TokenResponse, or O2A/O2T capabilities.
 * </p>
 * <p>
 * RedNoteOptions contains routing, application ID, external application-secret reference, exact registered HTTP or
 * HTTPS callback, and a unique ordered subset of the frozen marketing scopes. Fixed endpoints, camel-case query names,
 * form field aliases, response codes, lifetime branch, and token parsing rules remain profile or adapter owned and
 * cannot be configured by external projects.
 * </p>
 * <p>
 * This exported package provides management contracts for an authorization-only integration. Runtime invocation must
 * enter a Provider obtained from Registry; no identity or session may be inferred from its token response. Application
 * secrets, state, codes, access and refresh tokens, response bodies, and platform diagnostics must not enter options
 * diagnostics, Context, tracing, logs, or public failure details.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.rednote;
