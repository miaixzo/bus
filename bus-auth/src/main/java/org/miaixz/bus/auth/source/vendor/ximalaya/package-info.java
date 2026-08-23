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
 * Declares the public Ximalaya OAuth Vendor manifest and externally loaded options.
 * <p>
 * XimalayaManifest fixes {@code ximalaya/default}, authorization, token, and profile endpoints, CLIENT_SECRET_POST,
 * prohibited PKCE, empty scope, and redirect Source authentication plus standard OAuth authorization. It does not
 * publish token capability because the historical token response omits mandatory {@code token_type}. Authorization and
 * callback device binding, private token fields, signed profile query, closed error vocabularies, and profile identity
 * fields remain registered private deviations.
 * </p>
 * <p>
 * XimalayaOptions contains routing, application ID, external Client Secret reference, exact registered HTTP or HTTPS
 * callback, empty scopes, and bounded official {@code deviceId}, {@code clientOsType}, and {@code packageId} selectors.
 * The OS type must be one of 1, 2, or 3. Fixed endpoints, signature construction, response parsing, and identity
 * binding cannot be externally supplied.
 * </p>
 * <p>
 * This exported package exposes Source configuration metadata; execution enters a configured Source through Dispatcher.
 * Callback device ID must equal options, and profile {@code id} must equal token {@code uid}; only that bound ID
 * becomes subject. Secrets, state, codes, tokens, device/package identifiers, signature material, bodies, and
 * diagnostics must not enter Context, tracing, logs, or public failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source.vendor.ximalaya;
