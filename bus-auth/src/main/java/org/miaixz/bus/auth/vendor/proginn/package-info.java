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
 * Declares the public Proginn OAuth Vendor definition and externally loaded settings.
 * <p>
 * ProginnDefinition fixes {@code proginn/default}, the authorization, token, and basic-profile endpoints,
 * CLIENT_SECRET_POST authentication, prohibited PKCE, default {@code basic} scope, and redirect Source authentication
 * plus standard OAuth authorization and token capabilities. Authorization and token wire models remain standard; only
 * the private profile GET carrying {@code access_token} in the query is a registered Source-completion deviation.
 * </p>
 * <p>
 * ProginnSourceSettings contains only routing, Client ID, Client Secret reference, one exact registered HTTP or HTTPS
 * callback, and a unique ordered subset of {@code basic}, {@code email}, {@code realname}, and {@code cellphone}. Fixed
 * endpoints, token form members, profile query fields, response models, PKCE controls, and identity selectors cannot be
 * supplied by external projects.
 * </p>
 * <p>
 * This exported package provides registration and management metadata. Runtime invocation must enter a Provider
 * obtained from Registry and delegate to the non-exported adapter. Only the non-blank Proginn profile {@code uid}
 * becomes ExternalIdentity subject; nickname, avatar, and email remain attributes. Client secrets, state, callback
 * codes, access or refresh tokens, complete profile bodies, and upstream errors must not enter diagnostics, Context,
 * tracing, logs, or public failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.proginn;
