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
 * Implements the non-exported Proginn OAuth Source identity flow.
 * <p>
 * ProginnSourceAdapter delegates validated standard AuthorizationRequest and TokenRequest operations to
 * StandardAdapter. RedirectManager atomically creates and consumes state, binds the callback to the exact registered
 * target, and rejects nonce, PKCE, extensions, and callback branches outside the frozen OAuth code flow.
 * Authorization-code completion therefore remains expressed as AuthorizationCodeGrant and TokenResponse.
 * </p>
 * <p>
 * After a successful Bearer token response, the adapter privately invokes the fixed basic-profile endpoint with the
 * registered {@code access_token} query deviation. Strict JSON parsing accepts only the frozen definition vocabulary or
 * a closed standard error object and requires a non-blank {@code uid}; that identifier alone becomes ExternalIdentity
 * subject and Evidence claim.
 * </p>
 * <p>
 * The adapter may depend on standard OAuth client models and codecs, vendor browser flow, Fabric transport, JSON
 * services, and Bus validation, but protocol servers, Registry loaders, and external projects must not depend on this
 * package or treat its profile as OpenID Connect UserInfo. State, callback codes, Client Secret leases, tokens, profile
 * bodies, nickname, avatar, email, and upstream error descriptions must not escape through Context, tracing, logs, or
 * public failure details.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.proginn.internal;
