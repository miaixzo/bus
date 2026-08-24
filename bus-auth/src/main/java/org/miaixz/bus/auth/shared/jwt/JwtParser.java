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
package org.miaixz.bus.auth.shared.jwt;

import java.util.Set;

import org.miaixz.bus.auth.guard.AlgorithmGuard;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.auth.shared.jose.JwsService;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Parses signed compact JWTs without assigning trust to their Header or Claims Set.
 * <p>
 * This component reuses the JOSE parser so compact segments, protected Header syntax, Base64URL, and critical
 * extensions follow exactly the same rules as cryptographic verification. The resulting {@link UnverifiedJWT} exists
 * solely to support flows that must select a tenant or key before signature verification.
 * </p>
 *
 * @author Kimi Liu
 */
public final class JwtParser {

    /**
     * JOSE parser shared with verification semantics.
     */
    private final JwsService jwsService;

    /**
     * Creates the application-facing compact parser.
     * <p>
     * The HS256 allow-list is not used to trust or execute the unverified Header; it supplies the required signed-JWT
     * JOSE profile and later verification still binds its own explicit algorithm.
     * </p>
     */
    public JwtParser() {
        this.jwsService = new JwsService(new AlgorithmGuard(), Set.of(JwaAlgorithm.HS256.name()));
    }

    /**
     * Parses one three-segment compact JWS without validating its signature.
     *
     * @param compact exact compact representation
     * @return immutable explicitly unverified JWT
     */
    public UnverifiedJWT parse(final String compact) {
        Assert.notBlank(compact, "JWT compact value must not be blank");
        final JwsService.Jws jws = jwsService.parseCompact(compact, Set.of());
        final JwsService.Signature signature = jws.signatures().get(0);
        return new UnverifiedJWT(compact, signature.header(), claims(jws.payload()));
    }

    /**
     * Parses exact UTF-8 JSON payload bytes as an object-backed JWT Claims Set.
     *
     * @param payload parsed or cryptographically verified payload bytes
     * @return validated claims
     */
    private static JwtClaims claims(final byte[] payload) {
        final JsonValue value = JsonKit.readValue(payload);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("JWT Claims Set must be a JSON object");
        }
        return new JwtClaims(object);
    }

}
