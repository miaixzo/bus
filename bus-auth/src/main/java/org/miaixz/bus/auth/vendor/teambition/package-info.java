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
 * Declares the public Teambition OAuth Vendor manifest and externally loaded options.
 * <p>
 * TeambitionManifest fixes {@code teambition/default}, authorization, token, profile, and historical refresh endpoints,
 * and exposes redirect Source authentication plus standard OAuth authorization. It does not publish OAuth token
 * capability because the private token response omits mandatory {@code token_type}. The non-standard
 * {@code grant_type=code}, compact token response, {@code Authorization: OAuth2} profile scheme, and user-bound refresh
 * endpoint are registered deviations and remain internal.
 * </p>
 * <p>
 * TeambitionOptions contains only routing, Client ID, Client Secret reference, and one exact registered HTTP or HTTPS
 * callback. Scopes must be empty because the historical authorization request defines none. Fixed endpoints, grant
 * value, token parser, authorization scheme, refresh behavior, profile record, and identity key cannot be supplied by
 * external projects.
 * </p>
 * <p>
 * This exported package is registration metadata; execution enters a Registry-obtained Provider. Only a non-blank
 * profile {@code _id} becomes ExternalIdentity subject. Secrets, state, codes, access or refresh tokens, authorization
 * headers, profile bodies, and upstream errors must not enter diagnostics, Context, tracing, logs, or public failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.teambition;
