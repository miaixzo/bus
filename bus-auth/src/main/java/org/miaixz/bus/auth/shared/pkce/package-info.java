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
 * Implements the RFC 7636 Proof Key for Code Exchange value and validation primitives.
 * <p>
 * {@link org.miaixz.bus.auth.shared.pkce.CodeVerifier} and {@link org.miaixz.bus.auth.shared.pkce.CodeChallenge} retain
 * their distinct wire values; {@link org.miaixz.bus.auth.shared.pkce.PkceMethod} represents the registered
 * transformation; {@link org.miaixz.bus.auth.shared.pkce.PkceGenerator} creates high-entropy verifier material and its
 * challenge; and {@link org.miaixz.bus.auth.shared.pkce.PkceValidator} validates a token request against the stored
 * authorization challenge.
 * </p>
 * <p>
 * OAuth and OpenID Connect clients, servers, and eligible Vendor adapters compose this package. It depends only on
 * bus-core encoding, secure randomness, and bus-crypto digest primitives and does not perform authorization routing,
 * code storage, callback handling, token issuance, or protocol profile selection.
 * </p>
 * <p>
 * Verifiers enforce the RFC length and unreserved-character grammar; S256 uses SHA-256 and unpadded Base64url and is
 * the required secure method unless an exact profile explicitly permits another registered method. Verifiers are
 * one-time credentials: callers store them under isolated expiring keys, compare derived values safely, consume them
 * atomically, erase temporary bytes, and never log verifier or challenge material.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.shared.pkce;
