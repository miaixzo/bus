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
 * Declares the public Toutiao OAuth Vendor manifest and externally loaded options.
 * <p>
 * ToutiaoManifest fixes {@code toutiao/default}, authorization, token, and profile endpoints, and exposes redirect
 * Source authentication plus standard OAuth authorization. It does not publish token capability because the historical
 * token response omits mandatory {@code token_type}. Authorization
 * {@code client_key}/{@code auth_only}/{@code display}, query-bearing empty-form token POST, private token fields, and
 * the {@code client_key}/{@code access_token} profile envelope remain registered deviations in the private completion
 * flow.
 * </p>
 * <p>
 * ToutiaoOptions contains routing, official {@code client_key}, Client Secret reference, and one exact registered HTTP
 * or HTTPS callback. Scopes must remain empty because the frozen request sends none. Fixed endpoints, extension query
 * fields, token parser, profile envelope, anonymous display rule, and error mapping cannot be externally supplied.
 * </p>
 * <p>
 * This exported package is registration metadata; execution enters a Registry-obtained Provider. Only non-blank profile
 * {@code uid} becomes ExternalIdentity subject, with the historical anonymous display applied only when
 * {@code uid_type=14}. Secrets, state, codes, tokens, OpenID, profile bodies, and platform errors must not enter
 * diagnostics, Context, tracing, logs, or public failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.toutiao;
