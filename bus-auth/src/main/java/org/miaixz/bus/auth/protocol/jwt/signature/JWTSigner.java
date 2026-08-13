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
package org.miaixz.bus.auth.protocol.jwt.signature;

import org.miaixz.bus.auth.protocol.jwt.JWT.TrustedAlgorithm;

/**
 * Signs and verifies a complete JWS signing input with an explicitly bound algorithm.
 * <p>
 * Implementations bind one trusted JOSE algorithm at construction time. A token header must never select or replace
 * that algorithm. Signing requires signing-capable key material, while verification requires verification-capable key
 * material. Implementations reject unsupported algorithms and unusable keys during construction or signing, and return
 * {@code false} for a signature mismatch or malformed signature input.
 * </p>
 *
 * @author Kimi Liu
 */
public interface JWTSigner {

    /**
     * Signs the complete ASCII JWS signing input using the configured trusted algorithm and signing key.
     *
     * @param signingInput complete {@code BASE64URL(header).BASE64URL(payload)} bytes; never retained
     * @return newly allocated raw signature bytes
     */
    byte[] sign(byte[] signingInput);

    /**
     * Verifies raw signature bytes over a complete JWS signing input.
     *
     * @param signingInput complete {@code BASE64URL(header).BASE64URL(payload)} bytes; never retained
     * @param signature    raw untrusted signature bytes; never retained
     * @return {@code true} only for an exact valid signature; otherwise {@code false}
     */
    boolean verify(byte[] signingInput, byte[] signature);

    /**
     * Returns the immutable trusted JOSE algorithm bound to this signer.
     *
     * @return configured trusted algorithm
     */
    TrustedAlgorithm algorithm();

}
