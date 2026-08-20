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
 * Declares the JD OAuth and Zeus profile Source variant.
 * <p>
 * JdDefinition fixes {@code jd/default}, authorization-code token and current refresh endpoints, the signed Zeus
 * profile gateway, CLIENT_SECRET, prohibited PKCE, and the registered JD login scopes. It publishes Source
 * authentication and standard OAuth authorization only; {@code app_key}, {@code app_secret}, missing token type,
 * platform error branches, private refresh, MD5 query signing, and response envelopes are explicit deviations.
 * </p>
 * <p>
 * JdSourceSettings contains routing, app key, secret reference, exact HTTPS callback, and unique supported scopes. It
 * cannot configure endpoints, the obsolete OIDC-named refresh path, signing algorithms, gateway method, token models,
 * profile envelopes, refresh capability, or revocation. Fixed addresses and secret material remain definition-owned.
 * </p>
 * <p>
 * Identity is the canonical non-blank token {@code open_id} or {@code xid}; when both are present they must match.
 * Nickname, image, gender, access token, and gateway data cannot replace that subject. Profile data is accepted only
 * after the signed response envelope is strictly validated.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.jd;
