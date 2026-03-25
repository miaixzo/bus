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
 * JWT Signer interface encapsulation. Implementations of this interface provide signing functionalities for different
 * algorithms.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface JWTSigner {

    /**
     * Signs the JWT parts.
     *
     * @param headerBase64  Base64 representation of the JWT header JSON string
     * @param payloadBase64 Base64 representation of the JWT payload JSON string
     * @return the Base64 encoded signature result, which is the third part of the JWT
     */
    String sign(String headerBase64, String payloadBase64);

    /**
     * Verifies the JWT signature.
     *
     * @param headerBase64  Base64 representation of the JWT header JSON string
     * @param payloadBase64 Base64 representation of the JWT payload JSON string
     * @param signBase64    the Base64 representation of the signature to be verified
     * @return true if the signature is consistent and valid, false otherwise
     */
    boolean verify(String headerBase64, String payloadBase64, String signBase64);

    /**
     * Retrieves the algorithm used for signing.
     *
     * @return the algorithm name
     */
    String getAlgorithm();

    /**
     * Retrieves the algorithm ID, which is the shorthand form of the algorithm, e.g., HS256.
     *
     * @return the algorithm ID
     */
    default String getAlgorithmId() {
        return JWTSignerBuilder.getId(getAlgorithm());
    }

}
