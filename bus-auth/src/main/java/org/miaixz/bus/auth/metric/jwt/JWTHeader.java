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
package org.miaixz.bus.auth.metric.jwt;

import java.io.Serial;
import java.util.Map;

/**
 * JWT Header information. This class extends {@link Claims} to specifically handle the header part of a JSON Web Token,
 * defining standard header parameters like algorithm, type, content type, and key ID.
 * 
 * @author Kimi Liu
 * @since Java 21+
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
