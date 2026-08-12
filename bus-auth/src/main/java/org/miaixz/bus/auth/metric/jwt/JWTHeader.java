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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.metric.JWT.TrustedAlgorithm;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.exception.ProtocolException;

/**
 * JWT Header information. This class extends {@link Claims} to specifically handle the header part of a JSON Web Token,
 * defining standard header parameters like algorithm, type, content type, and key ID.
 *
 * @author Kimi Liu
 */
public class JWTHeader extends Claims {

    /**
     * Serialization version identifier.
     */
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
     * The critical protected-header parameter.
     */
    public static String CRITICAL = "crit";

    /**
     * Constructs a new JWTHeader instance.
     */
    public JWTHeader() {
        // No initialization required.
    }

    /**
     * Returns whether one name is a registered header parameter that cannot be declared critical.
     *
     * @param name header parameter name
     * @return whether the name is registered by this implementation
     */
    private static boolean registered(final String name) {
        return ALGORITHM.equals(name) || TYPE.equals(name) || CONTENT_TYPE.equals(name) || KEY_ID.equals(name)
                || CRITICAL.equals(name);
    }

    /**
     * Rejects an unsafe protected header with the stable token-format error.
     */
    private static void reject() {
        throw new ProtocolException(ErrorCode._100533);
    }

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

    /**
     * Validates the protected header against a product-selected algorithm and no extension parameters.
     *
     * @param algorithm trusted product-selected algorithm
     * @return this validated header
     */
    public JWTHeader validate(final TrustedAlgorithm algorithm) {
        return validate(algorithm, Set.of());
    }

    /**
     * Validates algorithm, type, key identifier, and critical extension semantics. The header algorithm must exactly
     * match the product-selected algorithm. Every critical entry must be a unique non-registered name, must be present
     * in this header, and must appear in the caller's explicit understood set.
     *
     * @param algorithm          trusted product-selected algorithm
     * @param understoodCritical explicitly understood extension parameter names
     * @return this validated header
     */
    public JWTHeader validate(final TrustedAlgorithm algorithm, final Set<String> understoodCritical) {
        if (algorithm == null || !algorithm.identifier().equals(getClaim(ALGORITHM))) {
            reject();
        }
        final Object type = getClaim(TYPE);
        if (type != null && (!(type instanceof String value) || !"JWT".equalsIgnoreCase(value))) {
            reject();
        }
        final Object keyId = getClaim(KEY_ID);
        if (keyId != null && (!(keyId instanceof String value) || value.isBlank())) {
            reject();
        }
        critical(understoodCritical);
        return this;
    }

    /**
     * Returns the validated immutable critical extension set.
     *
     * @param understoodCritical explicitly understood extension names
     * @return immutable critical extension names
     */
    public Set<String> critical(final Set<String> understoodCritical) {
        if (understoodCritical == null) {
            reject();
        }
        final Object claim = getClaim(CRITICAL);
        if (claim == null) {
            return Set.of();
        }
        if (!(claim instanceof List<?>)) {
            reject();
        }
        final List<?> values = (List<?>) claim;
        if (values.isEmpty()) {
            reject();
        }
        final LinkedHashSet<String> result = new LinkedHashSet<>();
        for (final Object value : values) {
            if (!(value instanceof String name) || name.isBlank() || registered(name) || getClaim(name) == null
                    || !understoodCritical.contains(name) || !result.add(name)) {
                reject();
            }
        }
        return Set.copyOf(result);
    }

}
