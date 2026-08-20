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
 * Implements RFC 9449 Demonstrating Proof of Possession proof issuance and verification.
 * <p>
 * {@link org.miaixz.bus.auth.shared.dpop.DpopProof} retains one typed proof,
 * {@link org.miaixz.bus.auth.shared.dpop.DpopIssuer} creates its asymmetric signed JWT,
 * {@link org.miaixz.bus.auth.shared.dpop.DpopVerifier} verifies JOSE integrity and key binding, and
 * {@link org.miaixz.bus.auth.shared.dpop.DpopValidator} checks method, target URI, issue time, identifier, nonce, and
 * access-token hash requirements.
 * </p>
 * <p>
 * OAuth token and protected-resource operations compose this package with shared JWT, JOSE, key resolvers, the Budget
 * clock, and replay storage. DPoP does not issue access tokens, route HTTP, choose a client, replace TLS, or silently
 * convert a Bearer request into a proof-bound request.
 * </p>
 * <p>
 * Validation requires an allowed asymmetric algorithm and public JWK, exact normalized HTTP method and target URI,
 * bounded clock skew, unique {@code jti}, atomic replay rejection, and the applicable nonce and {@code ath} binding.
 * Proofs, private keys, access tokens, nonces, complete target URIs, and signed JWTs must not enter logs or failure
 * details, and DPoP-bound and Bearer token semantics remain distinct.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.shared.dpop;
