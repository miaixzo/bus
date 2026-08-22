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
 * Defines provider-neutral JWT values, issuance, signature verification, and registered-claim validation.
 * <p>
 * {@link org.miaixz.bus.auth.shared.jwt.Jwt} retains the encoded JOSE header, claims, and signature relationship;
 * {@link org.miaixz.bus.auth.shared.jwt.JwtClaims} preserves typed JSON claim values.
 * {@link org.miaixz.bus.auth.shared.jwt.JwtIssuer} signs tokens through JOSE,
 * {@link org.miaixz.bus.auth.shared.jwt.JwtVerifier} verifies their protected representation, and
 * {@link org.miaixz.bus.auth.shared.jwt.JwtValidator} applies issuer, audience, subject, time, and caller-selected
 * claim requirements.
 * </p>
 * <p>
 * Formal protocols and shared DPoP compose these operations under their own token purpose and wire contract. This
 * package depends on JOSE, provider-neutral JSON, the Timeout clock, and key loaders; it is not a Provider, Source,
 * protocol dispatcher, token endpoint, session service, or remote validation client.
 * </p>
 * <p>
 * Verification precedes trust in every claim and binds an explicit algorithm, key, issuer, audience, purpose, and time
 * policy. Validators reject duplicate or mistyped registered claims, unsigned or algorithm-confused input, expired or
 * premature tokens, and unbounded payloads. Compact JWTs, keys, sensitive claims, and complete validation failures must
 * not be logged or reused across token purposes.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.shared.jwt;
