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
 * Provides the fixed JWT/JWS signer allowlist: HS256, RS256, PS256, ES256, and EdDSA with Ed25519.
 * <p>
 * Signers bind an algorithm and key at construction time, accept exact pre-encoded compact segments, emit canonical
 * unpadded Base64url, and separate private signing use from public verification use. HMAC and asymmetric execution
 * reuse bus-crypto, JCA names come from bus-core {@code Algorithm}, ES256 owns strict DER conversion, and the retained
 * {@code none} compatibility type always fails closed. This package never resolves keys or trusts a token algorithm.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.metric.jwt.signature;
