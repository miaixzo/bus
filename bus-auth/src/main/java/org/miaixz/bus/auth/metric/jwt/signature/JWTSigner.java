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
package org.miaixz.bus.auth.metric.jwt.signature;

/**
 * Compatibility contract for signing and verifying JWS compact input.
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
     * Signs two already encoded JWS segments using the configured trusted algorithm and signing key.
     *
     * @param headerBase64  unpadded Base64url representation of the JWT header
     * @param payloadBase64 unpadded Base64url representation of the JWT payload
     * @return unpadded Base64url signature segment
     */
    String sign(String headerBase64, String payloadBase64);

    /**
     * Verifies a signature over two already encoded JWS segments using the configured trusted algorithm and
     * verification key.
     *
     * @param headerBase64  unpadded Base64url representation of the JWT header
     * @param payloadBase64 unpadded Base64url representation of the JWT payload
     * @param signBase64    unpadded Base64url signature segment
     * @return {@code true} only for an exact valid signature; otherwise {@code false}
     */
    boolean verify(String headerBase64, String payloadBase64, String signBase64);

    /**
     * Returns the immutable JCA algorithm bound to this signer.
     *
     * @return configured JCA algorithm name
     */
    String getAlgorithm();

    /**
     * Returns the exact JOSE algorithm identifier mapped from the configured JCA algorithm.
     *
     * @return configured JOSE algorithm identifier
     */
    default String getAlgorithmId() {
        return JWTSignerBuilder.getId(getAlgorithm());
    }

}
