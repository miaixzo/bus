/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.auth.metric.jwt.signature;

/**
 * JWT Signer interface encapsulation. Implementations of this interface provide signing functionalities for different
 * algorithms.
 *
 * @author Kimi Liu
 * @since Java 17+
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
