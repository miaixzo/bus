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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * JWT signer for unsigned JWTs.
 * <p>
 * Implements the {@link JWTSigner} interface for scenarios where JWTs do not require signature verification (algorithm
 * identifier is "none"). This signer returns an empty signature and verifies if the provided signature is empty.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 * @see JWTSigner
 */
public class NoneJWTSigner implements JWTSigner {

    /**
     * Algorithm identifier for no signature, with a value of "none".
     */
    public static final String ID_NONE = Normal.NONE;

    /**
     * Singleton instance of the signer for unsigned JWTs.
     */
    public static NoneJWTSigner NONE = new NoneJWTSigner();

    /**
     * Generates a JWT signature.
     * <p>
     * For the "none" algorithm, an empty string is returned as the signature.
     * </p>
     *
     * @param headerBase64  Base64 encoded JWT header
     * @param payloadBase64 Base64 encoded JWT payload
     * @return an empty string as the signature
     */
    @Override
    public String sign(final String headerBase64, final String payloadBase64) {
        return Normal.EMPTY;
    }

    /**
     * Verifies the JWT signature.
     * <p>
     * Checks if the provided signature is an empty string, indicating successful verification for unsigned JWTs.
     * </p>
     *
     * @param headerBase64  Base64 encoded JWT header
     * @param payloadBase64 Base64 encoded JWT payload
     * @param signBase64    Base64 encoded signature to be verified
     * @return true if the signature is empty, false otherwise
     */
    @Override
    public boolean verify(final String headerBase64, final String payloadBase64, final String signBase64) {
        return StringKit.isEmpty(signBase64);
    }

    /**
     * Retrieves the signing algorithm identifier.
     *
     * @return the algorithm identifier "none"
     */
    @Override
    public String getAlgorithm() {
        return ID_NONE;
    }

}
