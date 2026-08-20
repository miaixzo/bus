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
 * Implements the non-exported Ximalaya OAuth completion and signed profile flow.
 * <p>
 * XimalayaSourceAdapter exposes Source authentication and standard AuthorizationRequest only. Its private encoder adds
 * official {@code client_os_type} and {@code device_id}, while RedirectManager atomically owns state and exact callback
 * correlation. A successful callback must return the registered device ID byte-for-byte. Completion resolves one Client
 * Secret lease for the private token and profile stages.
 * </p>
 * <p>
 * The token form strictly validates access token, refresh token, lifetime, UID, optional device and scope, and the
 * closed historical/current error vocabularies without inventing {@code token_type}, TokenResponse, or O2T. Profile
 * parameters are canonically sorted and signed with Bus Base64, HMAC-SHA1, and MD5 primitives. Profile ID must equal
 * the token UID before ExternalIdentity is created.
 * </p>
 * <p>
 * Private handling may depend on authorization codecs, vendor flow, Fabric, JSON, Bus codecs and crypto, secret
 * resolution, and validation, but protocol servers, Registry loaders, and external projects must not depend on it or
 * treat the profile as OpenID Connect UserInfo. State, codes, secret material, tokens, device/package values, canonical
 * text, signature bytes, bodies, and diagnostics must not escape through Context, tracing, logs, or public failures;
 * temporary byte arrays are cleared and responses are closed.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.ximalaya.internal;
