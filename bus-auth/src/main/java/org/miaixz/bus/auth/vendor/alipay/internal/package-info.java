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
 * Implements the non-exported Alipay RSA2 authorization and signed gateway identity flow.
 * <p>
 * AlipaySourceAdapter binds browser state, emits the fixed GET authorization query, accepts the unique
 * {@code auth_code} callback, and invokes {@code alipay.system.oauth.token} followed by {@code alipay.user.info.share}.
 * Each gateway call adds the exact application, method, JSON, UTF-8 charset, RSA2, GMT+8 timestamp, version, and
 * business fields, canonicalizes non-empty fields by Unicode lexical key order, signs the UTF-8 bytes with the
 * configured private key, and sends one form to the fixed gateway.
 * </p>
 * <p>
 * The adapter composes RedirectManager, form and query codecs, KeyResolver, bus-crypto Sign, bus-core Base64,
 * JsonProvider raw-value extraction, SecurityBaseline, and Fabric. It exposes neither gateway methods nor private token
 * and profile objects. It does not publish OAuth token, refresh, revoke, or UserInfo operations, accept endpoint
 * overrides, cache key material, or implement an alternate unsigned compatibility path.
 * </p>
 * <p>
 * The response must be bounded JSON and carry a valid RSA2 signature over the exact un-re-serialized method envelope
 * before any business field is read. Gateway errors and non-{@code 10000} codes are rejected; malformed content,
 * invalid signatures, media failures, and unavailable transport fail closed. Token and profile {@code user_id} values
 * must be non-blank and byte-identical, and only that value becomes the ExternalIdentity subject. Private and public
 * keys, auth code, tokens, canonical form, signature, raw body, and user data never enter diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.alipay.internal;
