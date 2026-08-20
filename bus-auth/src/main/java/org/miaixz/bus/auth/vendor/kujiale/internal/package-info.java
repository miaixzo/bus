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
 * Implements the non-exported Kujiale OAuth authentication flow.
 * <p>
 * KujialeSourceAdapter delegates the standard authorization operation while preserving comma-delimited scopes.
 * RedirectManager owns state without PKCE. Source completion resolves one secret lease, sends an empty form POST with
 * ordered token query parameters, performs the private access-token OpenID lookup, and then retrieves the profile bound
 * to that OpenID. Private refresh follows the same query and envelope rules and is not exported.
 * </p>
 * <p>
 * Every response is a strict {@code c/m/d/f} branch. Token data requires camel-case access and refresh tokens plus a
 * positive lifetime; lookup data must be one non-blank OpenID; profile data requires matching {@code openId} and a
 * non-blank user name. Decimal-string business errors are classified without exposing their messages or bodies, and no
 * private result is converted into TokenResponse, IntrospectionResponse, or UserInfoResponse.
 * </p>
 * <p>
 * State, code, secret, access and refresh tokens, complete queries, forms, bodies, user name, and avatar remain within
 * the asynchronous operation. Query credentials are passed only through Fabric's sensitive boundary and never reach
 * Context, Outcome details, tracing, metrics, exception text, or logs.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.kujiale.internal;
