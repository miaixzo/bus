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
 * Implements the non-exported Facebook Login authentication flow.
 * <p>
 * FacebookSourceAdapter delegates only the conforming authorization operation. Source authentication uses
 * RedirectManager state, validates the code or documented denial callback, obtains the private Graph token through its
 * fixed GET query, and calls {@code /me} with a Bearer header and the fixed profile field set. It does not construct a
 * standard TokenResponse from the platform response that lacks {@code token_type}.
 * </p>
 * <p>
 * One client-secret lease covers both the token query and the profile proof. The proof is lowercase hexadecimal
 * HMAC-SHA-256 over the access token with the client secret and is sent only as {@code appsecret_proof}; the access
 * token itself never enters the query. Graph error codes are classified by their documented rejection, rate-limit,
 * temporary, and server categories without copying messages or trace identifiers to failure details.
 * </p>
 * <p>
 * Profile success requires a non-blank digit-only application-scoped ID. Token, secret, Authorization header, proof,
 * callback code, request URL, Graph trace, profile fields, and complete response bodies remain within the asynchronous
 * operation and are never logged or exposed through a protocol model.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.facebook.internal;
