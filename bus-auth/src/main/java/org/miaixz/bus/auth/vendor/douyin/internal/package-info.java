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
 * Implements the non-exported Douyin open-platform and mini-program authentication flows.
 * <p>
 * DouyinSourceAdapter selects exactly one compiled variant. Open uses RedirectManager state, emits {@code client_key},
 * code response type, comma-delimited scope, callback, and state, accepts the strict code callback, sends the ordered
 * client-key and client-secret token form, and posts access token plus open ID to the profile endpoint. Both token and
 * profile success and error envelopes are decoded as private bounded JSON.
 * </p>
 * <p>
 * Mini-program accepts only SourceAuthenticationRequest.OneTimeCode, atomically consumes the code for its namespace and
 * purpose, resolves one secret lease, posts JSON containing {@code appid}, {@code secret}, and {@code code} to the
 * fixed production endpoint, and returns a completed Source authentication result. It does not implement browser
 * callback, OAuth token, refresh, revoke, profile, anonymous code, sandbox, game, or third-party-service variants.
 * </p>
 * <p>
 * Open token success requires message success, integral zero error code, non-blank access/open/refresh values and
 * scope, and positive lifetimes; profile requires zero errors, matching open ID, and non-blank union ID, which is the
 * subject. Mini success requires integral {@code err_no=0}, non-blank log ID, session key and openid, and empty
 * anonymous_openid; only openid is the subject and optional unionid is an attribute. Every secret lease closes with its
 * HTTP stage, and code, secret, session key, tokens, forms, JSON, log IDs, and platform errors never enter diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.douyin.internal;
