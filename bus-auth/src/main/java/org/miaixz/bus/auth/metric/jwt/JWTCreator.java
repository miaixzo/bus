/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.util.Map;

import org.miaixz.bus.auth.metric.JWT;
import org.miaixz.bus.auth.metric.jwt.signature.JWTSigner;
import org.miaixz.bus.core.xyz.MapKit;

/**
 * Utility class for creating JSON Web Tokens (JWT). This class provides convenient methods to construct JWTs with
 * various header and payload configurations, supporting different signing algorithms.
 * 
 * @author Kimi Liu
 * @since Java 17+
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
