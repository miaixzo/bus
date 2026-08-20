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
 * Implements the non-exported DingTalk delegated and signed account-login adapters.
 * <p>
 * DingTalkSourceAdapter selects behavior only from the compiled variant. The delegated path generates a state-bound
 * authorization request with official organization and exclusive-login extensions, accepts callback {@code authCode},
 * sends the camel-case JSON authorization-code request with client secret, and calls current-user with the sole
 * {@code x-acs-dingtalk-access-token} header. Its public authorization capability retains standard input and URL output
 * while the callback, token object, and identity response remain private.
 * </p>
 * <p>
 * The account path emits the fixed {@code snsapi_login} redirect, treats callback code only as {@code tmp_auth_code},
 * derives epoch-millisecond timestamp and HMAC-SHA256 over its UTF-8 text with a SHARED_SECRET lease, Base64- and
 * query-encodes the signature, and posts the one-field JSON body. No OAuth token, refresh, revoke, embedded QR branch,
 * hex or Base64URL signature, direct JCA, shared DTO, or cross-variant fallback is exposed.
 * </p>
 * <p>
 * Both paths bind exact callback target, unique state, variant, endpoint, and Budget. Delegated token success requires
 * non-blank camel-case access and refresh tokens and positive {@code expireIn}; user identity requires non-blank
 * {@code unionId}. Account response requires integral {@code errcode=0}, non-blank {@code unionid} and {@code openid},
 * with only unionid used as subject. Secrets, temporary code, authCode, timestamp, signature, access tokens, mobile,
 * response bodies, and platform messages never enter failure details or logs.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.dingtalk.internal;
