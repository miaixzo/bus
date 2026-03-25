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

import java.util.Map;

import org.miaixz.bus.auth.metric.JWT;
import org.miaixz.bus.auth.metric.jwt.signature.JWTSigner;
import org.miaixz.bus.core.xyz.MapKit;

/**
 * Utility class for creating JSON Web Tokens (JWT). This class provides convenient methods to construct JWTs with
 * various header and payload configurations, supporting different signing algorithms.
 * 
 * @author Kimi Liu
 * @since Java 21+
 */
public class JWTCreator {

    /**
     * Creates an HS256 (HmacSHA256) JWT Token.
     *
     * @param payload the payload claims of the JWT
     * @param key     the HS256 (HmacSHA256) secret key
     * @return the generated JWT Token string
     */
    public static String create(final Map<String, ?> payload, final byte[] key) {
        return create(MapKit.of(JWTHeader.TYPE, "JWT"), payload, key);
    }

    /**
     * Creates an HS256 (HmacSHA256) JWT Token with custom headers.
     *
     * @param headers the header claims of the JWT
     * @param payload the payload claims of the JWT
     * @param key     the HS256 (HmacSHA256) secret key
     * @return the generated JWT Token string
     */
    public static String create(final Map<String, ?> headers, final Map<String, ?> payload, final byte[] key) {
        return JWT.of().addHeaders(headers).addPayloads(payload).setKey(key).sign();
    }

    /**
     * Creates a JWT Token using a custom signer.
     *
     * @param payload the payload claims of the JWT
     * @param signer  the {@link JWTSigner} to use for signing
     * @return the generated JWT Token string
     */
    public static String create(final Map<String, Object> payload, final JWTSigner signer) {
        return create(null, payload, signer);
    }

    /**
     * Creates a JWT Token with custom headers and a custom signer.
     *
     * @param headers the header claims of the JWT
     * @param payload the payload claims of the JWT
     * @param signer  the {@link JWTSigner} to use for signing
     * @return the generated JWT Token string
     */
    public static String create(
            final Map<String, Object> headers,
            final Map<String, Object> payload,
            final JWTSigner signer) {
        return JWT.of().addHeaders(headers).addPayloads(payload).setSigner(signer).sign();
    }

}
