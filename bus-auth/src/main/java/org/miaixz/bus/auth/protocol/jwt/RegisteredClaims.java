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
package org.miaixz.bus.auth.protocol.jwt;

/**
 * Registered JWT claim names defined by RFC 7519.
 *
 * @author Kimi Liu
 */
public final class RegisteredClaims {

    /**
     * Issuer claim name.
     */
    public static final String ISSUER = "iss";

    /**
     * Subject claim name.
     */
    public static final String SUBJECT = "sub";

    /**
     * Audience claim name.
     */
    public static final String AUDIENCE = "aud";

    /**
     * Expiration time claim name.
     */
    public static final String EXPIRES_AT = "exp";

    /**
     * Not-before claim name.
     */
    public static final String NOT_BEFORE = "nbf";

    /**
     * Issued-at claim name.
     */
    public static final String ISSUED_AT = "iat";

    /**
     * JWT identifier claim name.
     */
    public static final String JWT_ID = "jti";

    /**
     * Prevents construction of the constants catalog.
     */
    private RegisteredClaims() {
        // No initialization required.
    }

}
