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
 * Declares the Kujiale OAuth Source variant.
 * <p>
 * KujialeManifest fixes {@code kujiale/default}, authorization, query-authenticated token and refresh, private OpenID
 * lookup, and profile endpoints, CLIENT_SECRET, prohibited PKCE, and registered comma-delimited scopes. It publishes
 * Source authentication and standard OAuth authorization only; empty-form POSTs, query secrets and tokens, camel-case
 * members, and {@code c/m/d/f} envelopes remain registered private deviations.
 * </p>
 * <p>
 * KujialeOptions contains routing, client, secret reference, exact HTTPS callback, and ordered unique supported scopes.
 * It cannot configure endpoints, authentication placement, PKCE, private lookup, envelope fields, token models, refresh
 * capability, UserInfo, introspection, or revocation.
 * </p>
 * <p>
 * Identity requires the private OpenID lookup and profile {@code openId} to match byte for byte. That value alone is
 * the subject; user name and avatar remain attributes. Query-carried secrets and tokens require Fabric redaction and
 * cannot appear in redirects, outcomes, tracing, metrics, exceptions, or logs.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source.vendor.kujiale;
