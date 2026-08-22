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
package org.miaixz.bus.auth.resolver;

import java.time.Instant;

import org.miaixz.bus.auth.Blueprint;
import org.miaixz.bus.auth.shared.jose.Jwk;
import org.miaixz.bus.auth.shared.jose.JwkSet;
import org.miaixz.bus.auth.worker.loader.KeyLoader;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Pure parser for project-loaded cryptographic keys.
 *
 * @author Kimi Liu
 */
public class KeyParser {

    /**
     * Creates a stateless cryptographic key parser.
     */
    public KeyParser() {
        // No initialization required.
    }

    /**
     * Validates Source ownership, key lookup coordinates, and validity interval.
     *
     * @param registration exact Source registration that requested the key
     * @param request      exact key lookup request
     * @param record       project-loaded key record
     * @return validated key material
     */
    public KeyMaterial parse(
            final Blueprint.SourceEntry registration,
            final KeyLoader.Request request,
            final KeyLoader.Record record) {
        final String sourceId = Assert.notNull(registration, "Key Source registration must not be null").resource()
                .getId();
        final KeyLoader.Request expected = Assert.notNull(request, "Key request must not be null");
        final KeyLoader.Record loaded = Assert.notNull(record, "Loaded key record must not be null");
        if (!sourceId.equals(loaded.sourceId())) {
            throw new ValidateException("Loaded key does not belong to the requested Source");
        }
        if (!expected.issuer().equals(Assert.notBlank(loaded.issuer(), "Loaded key issuer must not be blank"))) {
            throw new ValidateException("Loaded key issuer does not match the requested issuer");
        }
        if (!expected.use().equals(Assert.notBlank(loaded.use(), "Loaded key use must not be blank"))) {
            throw new ValidateException("Loaded key use does not match the requested use");
        }
        if (!expected.algorithm()
                .equals(Assert.notBlank(loaded.algorithm(), "Loaded key algorithm must not be blank"))) {
            throw new ValidateException("Loaded key algorithm does not match the requested algorithm");
        }
        if (expected.keyId().isPresent() && !expected.keyId().getOrThrow().equals(loaded.keyId())) {
            throw new ValidateException("Loaded key identifier does not match the requested identifier");
        }
        final Instant notBefore = Assert.notNull(loaded.notBefore(), "Loaded key not-before instant must not be null");
        final Instant notAfter = Assert.notNull(loaded.notAfter(), "Loaded key not-after instant must not be null");
        if (expected.at().isBefore(notBefore) || !expected.at().isBefore(notAfter)) {
            throw new ValidateException("Loaded key is not valid at the requested instant");
        }
        return new KeyMaterial(loaded.keyId(), loaded.algorithm(), loaded.key(), loaded.notBefore(), loaded.notAfter());
    }

    /**
     * Validates Source ownership and rejects private or symmetric material from a public key set.
     *
     * @param registration exact Source registration that requested the keys
     * @param criteria     exact public-key listing criteria
     * @param listing      project-loaded public-key listing
     * @return detached public-only key set
     */
    public JwkSet parsePublic(
            final Blueprint.SourceEntry registration,
            final KeyLoader.Criteria criteria,
            final KeyLoader.Listing listing) {
        final String sourceId = Assert.notNull(registration, "Public key Source registration must not be null")
                .resource().getId();
        final KeyLoader.Criteria expected = Assert.notNull(criteria, "Public key criteria must not be null");
        final KeyLoader.Listing loaded = Assert.notNull(listing, "Loaded public key listing must not be null");
        if (!sourceId.equals(loaded.sourceId()) || !expected.issuer().equals(loaded.issuer())
                || !expected.use().equals(loaded.use())) {
            throw new ValidateException("Loaded public keys do not match the requested Source, issuer, or use");
        }
        final JwkSet keys = Assert.notNull(loaded.keys(), "Loaded public JWK Set must not be null");
        for (Jwk key : keys.keys()) {
            final Jwk checked = Assert.notNull(key, "Loaded public JWK must not be null");
            if (checked.hasPrivateMaterial() || "oct".equals(checked.keyType())) {
                throw new ValidateException("Loaded public JWK Set contains non-public key material");
            }
        }
        return new JwkSet(keys.keys(), keys.extensions());
    }

}
