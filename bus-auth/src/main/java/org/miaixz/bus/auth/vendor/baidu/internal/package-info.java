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
 * Implements the non-exported Baidu OAuth authorization and private identity chain.
 * <p>
 * BaiduSourceAdapter delegates the declared authorization capability to standard OAuth request encoding and uses
 * RedirectManager for state-bound Source authentication. After a strict code or access-denied callback, it resolves one
 * client-secret lease, sends the registered authorization-code GET query, consumes Baidu's private token object, and
 * calls the account endpoint with query {@code access_token} and fixed {@code get_unionid=1}.
 * </p>
 * <p>
 * The adapter may use standard authorization models, shared query codecs, SecretResolver, JsonProvider,
 * SecurityBaseline, and Fabric. Token and profile records remain private. It does not convert the response lacking
 * {@code token_type} to TokenResponse, expose refresh or revoke, publish the profile as UserInfo, send PKCE, enable
 * popup extensions, accept endpoint overrides, or log the credential-bearing URL.
 * </p>
 * <p>
 * Callback target, method, multiplicity, state, and branch are exact. Token query order, bounded JSON members,
 * non-blank access and refresh tokens, integral non-negative expiry, and official error shape are strict. Profile JSON
 * must contain non-blank {@code openid}; optional current fields retain their types, and only {@code openid} becomes
 * ExternalIdentity.subject. Secret, code, query URL, access and refresh tokens, session material, profile body, and
 * platform error text are discarded before completion and never enter diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.baidu.internal;
