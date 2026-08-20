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
 * Implements the RFC 5849 signature base string and request-signing rules.
 * <p>
 * {@link org.miaixz.bus.auth.protocol.oauth1.security.SignatureBaseString} normalizes the uppercase HTTP method,
 * base-string URI, and encoded parameter multiset exactly once.
 * {@link org.miaixz.bus.auth.protocol.oauth1.security.OAuth1Signer} generates protocol nonce and timestamp values,
 * resolves the registered consumer or private key, and produces the selected HMAC-SHA1 or RSA-SHA1 signature
 * parameters.
 * </p>
 * <p>
 * OAuth 1.0 clients invoke this package after constructing typed request parameters and before encoding the final
 * Authorization header. Cryptographic execution is delegated to bus-crypto and key or secret resolvers; security code
 * does not call JCA directly, perform transport, parse callbacks, store credentials, or implement Vendor signing
 * schemes.
 * </p>
 * <p>
 * Normalization includes query, form, and OAuth parameters exactly as RFC 5849 requires, excludes realm and
 * {@code oauth_signature}, preserves duplicate parameter ordering rules, and percent-encodes before sorting. Secret,
 * signing-key, base-string, and signature bytes are operation-scoped and erased; none enter logs or failure details.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.oauth1.security;
