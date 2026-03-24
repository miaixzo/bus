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

import org.miaixz.bus.auth.metric.JWT;

/**
 * JWT parser.
 * <p>
 * Provides functionality to parse JWT tokens, converting string-formatted JWT tokens (in the format
 * header.payload.signature) into {@link JWT} objects.
 * </p>
 * 
 * @author Kimi Liu
 * @since Java 21+
 */
public class JWTParser {

    /**
     * Parses a JWT token.
     * <p>
     * Parses the input JWT token string into a {@link JWT} object, including the header, payload, and signature parts.
     * </p>
     *
     * @param token the JWT token string, in the format header.payload.signature
     * @return the parsed {@link JWT} object
     * @throws IllegalArgumentException if the token is blank or malformed
     */
    public static JWT parseToken(final String token) {
        // Calls the JWT.of method to parse the token string
        return JWT.of(token);
    }

}
