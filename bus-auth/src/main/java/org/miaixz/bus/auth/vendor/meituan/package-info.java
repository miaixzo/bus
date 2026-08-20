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
 * Declares the Meituan OAuth Source variant.
 * <p>
 * MeituanDefinition fixes {@code meituan/default}, authorization, token, refresh, and profile endpoints,
 * {@code app_id}/{@code secret} form authentication, prohibited PKCE, and an explicitly empty scope. It publishes
 * Source authentication and standard OAuth authorization only; renamed client fields, missing token type and scope,
 * HTTP-200 platform errors, private refresh, and profile fields remain registered deviations.
 * </p>
 * <p>
 * MeituanSourceSettings contains routing, canonical decimal app ID, secret reference, exact HTTPS callback, and no
 * scopes. It cannot configure endpoints, PKCE, platform form fields, private token/profile records, refresh capability,
 * UserInfo, or revocation. The public authorization operation always emits the required empty {@code scope} parameter.
 * </p>
 * <p>
 * Identity accepts only the non-blank profile {@code openid} as its subject. Nickname and avatar remain attributes and
 * cannot replace the key. The current {@code error_msg} spelling is authoritative; the historical unsupported
 * {@code erroe_msg} spelling is not accepted as compatibility behavior.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.meituan;
