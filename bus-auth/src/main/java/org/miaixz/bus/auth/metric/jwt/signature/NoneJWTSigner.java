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
