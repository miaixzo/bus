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
 * Declares the public Taobao OAuth Vendor definition and externally loaded settings.
 * <p>
 * TaobaoDefinition fixes {@code taobao/default}, authorization and token endpoints, and redirect Source authentication
 * plus standard OAuth authorization and token capabilities. The authorization {@code view=web}, query-bearing
 * empty-form token POST, and Taobao identity fields in the token response are registered deviations. Standard
 * access-token members map to TokenResponse; {@code taobao_user_id}, {@code taobao_open_uid}, {@code taobao_user_nick},
 * and {@code id_token} remain extensions, and the ID token is never accepted as identity evidence. No refresh endpoint
 * or UserInfo capability is declared.
 * </p>
 * <p>
 * TaobaoSourceSettings contains only routing, Client ID, Client Secret reference, and one exact registered HTTP or
 * HTTPS callback. Scopes must remain empty because the historical authorization request defines none. Fixed endpoints,
 * view, token query/form behavior, response extensions, decoding rules, and identity precedence cannot be externally
 * set.
 * </p>
 * <p>
 * This exported package provides registration metadata; execution enters a Registry-obtained Provider. Identity uses a
 * verified {@code taobao_user_id}, falling back to {@code taobao_open_uid} only when the former is absent; decoded
 * nickname is an attribute. Secrets, state, codes, tokens, extension values, and bodies must not enter diagnostics,
 * Context, tracing, logs, or public failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.taobao;
