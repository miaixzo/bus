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
 * Implements the non-exported Meituan browser authentication flow.
 * <p>
 * MeituanSourceAdapter delegates standard OAuth authorization while preserving the required empty scope.
 * RedirectManager owns state without PKCE. Source completion resolves one secret lease, posts the exact
 * {@code app_id,secret,code,grant_type} token form, then posts {@code app_id,secret,access_token} to the private
 * profile endpoint. Private refresh uses its registered form and is not published.
 * </p>
 * <p>
 * Token, profile, and error envelopes are separate private records. Success requires non-blank token pairs with a
 * positive lifetime or a non-blank OpenID and nickname; decimal-string platform errors are mutually exclusive with
 * success and classified without copying {@code error_msg}. Missing token type and scope are never fabricated.
 * </p>
 * <p>
 * State, code, secret, access and refresh tokens, forms, bodies, nickname, and avatar remain inside the asynchronous
 * operation and never enter Context, Outcome details, attributes other than verified profile output, tracing, exception
 * text, or logs. The secret lease closes only after the token and profile stages finish.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.meituan.internal;
