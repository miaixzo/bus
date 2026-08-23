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
 * Declares the public Weibo OAuth Vendor manifest and externally loaded options.
 * <p>
 * WeiboManifest fixes {@code weibo/default}, authorization, token, profile, and revocation endpoints, and exposes
 * redirect Source authentication, standard OAuth authorization, and standard revocation. It does not publish token
 * capability because the historical success response omits mandatory {@code token_type}. Comma scope, query-bearing
 * empty-form token POST, profile query plus historical OAuth2 header, and query revocation with {@code result=true} are
 * private deviations. The former fabricated local {@code API-RemoteIP} value is intentionally absent.
 * </p>
 * <p>
 * WeiboOptions contains routing, App Key, external Client Secret reference, exact registered HTTP or HTTPS callback,
 * and a unique ordered subset of the official scope vocabulary; an empty list normalizes to {@code all}. Fixed
 * endpoints, query/header behavior, fabricated network facts, response parsing, and identity binding cannot be
 * externally supplied.
 * </p>
 * <p>
 * This exported package exposes Source configuration metadata; execution enters a configured Source through Dispatcher.
 * Profile {@code id} and optional {@code idstr} must bind to the token {@code uid}, and only that verified UID becomes
 * subject. Secrets, state, codes, access tokens, UID, profile bodies, and errors must not enter diagnostics, Context,
 * tracing, logs, or public failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source.vendor.weibo;
