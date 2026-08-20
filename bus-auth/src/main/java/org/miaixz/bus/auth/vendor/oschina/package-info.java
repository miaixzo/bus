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
 * Declares the public OSChina OAuth Vendor definition and externally loaded settings.
 * <p>
 * OsChinaDefinition fixes the sole {@code oschina/default} variant and its authorization, token, and profile endpoints.
 * It exposes redirect Source authentication plus standard OAuth authorization and token capabilities. The historical
 * token GET carrying {@code client_secret} and {@code dataType=json}, token {@code uid} extension, and profile GET
 * carrying {@code access_token} and {@code dataType=json} are explicit vendor deviations confined to the private
 * adapter; public requests and successful token results retain standard OAuth models and Bearer semantics.
 * </p>
 * <p>
 * OsChinaSourceSettings contains only routing, Client ID, Client Secret reference, one exact registered HTTP or HTTPS
 * callback, and unique syntactically valid OAuth scopes. The fixed endpoint set, query authentication, response
 * members, profile fields, PKCE behavior, and transport rules cannot be overridden or supplied as external settings.
 * </p>
 * <p>
 * This exported package provides registration and management metadata, not an executable client. Runtime use must enter
 * a Provider obtained from Registry and delegate to the non-exported adapter. Source completion accepts only the stable
 * profile {@code id} as ExternalIdentity subject; token {@code uid} remains a token extension and profile name, avatar,
 * URL, location, gender, and email remain attributes. Client secrets, state, codes, access or refresh tokens, complete
 * profile bodies, and upstream error descriptions must not enter diagnostics, Context, logs, or failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.oschina;
