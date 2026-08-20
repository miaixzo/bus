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
 * Implements the non-exported VK OAuth wire adaptation and Source identity flow.
 * <p>
 * VkSourceAdapter routes Source authentication and standard authorization, token, and revocation capabilities.
 * Authorization delegates to the shared client after binding Client ID, callback, scope, state, and S256. The shared
 * browser flow atomically stores and consumes state and PKCE verifier. Token execution accepts only
 * AuthorizationCodeGrant or RefreshTokenGrant, uses the standard encoder, and appends only the registered state,
 * {@code device_id}, and refresh Client ID fields; the historical fabricated IP value is not sent.
 * </p>
 * <p>
 * Strict token decoding returns TokenResponse and keeps {@code user_id} only as an extension. Source completion sends
 * the fixed private-user form, requires a non-blank {@code user.user_id}, and binds it to token {@code user_id} when
 * present; an unverified ID token is ignored. Revocation maps RevocationRequest to the registered access-token and
 * Client-ID form and accepts only the exact string or numeric one marker.
 * </p>
 * <p>
 * Private handling may depend on standard OAuth clients, vendor flow, Fabric, JSON, shared codecs, and Bus validation,
 * but protocol servers, Registry loaders, and external projects must not depend on it. State, PKCE verifier, device ID,
 * codes, tokens, response bodies, and upstream diagnostics must not escape through Context, tracing, logs, or public
 * failures; owned bodies are cleared and responses are closed.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.vk.internal;
