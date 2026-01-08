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
package org.miaixz.bus.auth.metric.jwt;

import java.io.Serial;
import java.util.Map;

/**
 * JWT Header information. This class extends {@link Claims} to specifically handle the header part of a JSON Web Token,
 * defining standard header parameters like algorithm, type, content type, and key ID.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class JWTHeader extends Claims {

    @Serial
    private static final long serialVersionUID = 2852289258085L;

    /**
     * The cryptographic algorithm used for signing or encrypting the JWT. Commonly used values include "HS256" (HMAC
     * SHA256).
     */
    public static String ALGORITHM = "alg";
    /**
     * The type of the token, typically "JWT".
     */
    public static String TYPE = "typ";
    /**
     * The content type of the JWT.
     */
    public static String CONTENT_TYPE = "cty";
    /**
     * The Key ID (kid) parameter, used to hint which key was used to secure the JWS.
     */
    public static String KEY_ID = "kid";

    /**
     * Adds the "alg" (algorithm) header parameter.
     *
     * @param algorithm the algorithm ID, e.g., "HS256"
     * @return this {@link JWTHeader} instance
     */
    public JWTHeader setAlgorithm(final String algorithm) {
        setClaim(ALGORITHM, algorithm);
        return this;
    }

    /**
     * Adds the "typ" (type) header parameter.
     *
     * @param type the type, e.g., "JWT"
     * @return this {@link JWTHeader} instance
     */
    public JWTHeader setType(final String type) {
        setClaim(TYPE, type);
        return this;
    }

    /**
     * Adds the "cty" (content type) header parameter.
     *
     * @param contentType the content type
     * @return this {@link JWTHeader} instance
     */
    public JWTHeader setContentType(final String contentType) {
        setClaim(CONTENT_TYPE, contentType);
        return this;
    }

    /**
     * Adds the "kid" (key ID) header parameter.
     *
     * @param keyId the Key ID
     * @return this {@link JWTHeader} instance
     */
    public JWTHeader setKeyId(final String keyId) {
        setClaim(KEY_ID, keyId);
        return this;
    }

    /**
     * Adds multiple custom JWT authentication headers.
     *
     * @param headerClaims a map containing multiple header claims to add
     * @return this {@link JWTHeader} instance
     */
    public JWTHeader addHeaders(final Map<String, ?> headerClaims) {
        putAll(headerClaims);
        return this;
    }

}
