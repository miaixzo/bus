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
package org.miaixz.bus.auth.worker.loader;

import java.security.Key;
import java.time.Instant;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.shared.jose.JwkSet;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Loads cryptographic key records from project-owned key storage.
 *
 * @author Kimi Liu
 */
public interface KeyLoader extends Loader<KeyLoader.Request, KeyLoader.Record> {

    /**
     * Loads the public key set exposed by one issuer and use.
     *
     * @param criteria validated Source and public-key listing criteria
     * @param context  immutable non-secret invocation context
     * @param timeout  shared end-to-end operation timeout
     * @return asynchronous project loading outcome
     */
    CompletionStage<Outcome<Listing>> list(Criteria criteria, Context context, Timeout timeout);

    /**
     * Identifies one exact cryptographic key required by a protocol operation.
     *
     * @param registration exact Source registration requesting the key
     * @param issuer       expected key issuer
     * @param keyId        optional exact key identifier
     * @param use          expected protocol use
     * @param algorithm    expected registered algorithm
     * @param at           required validity instant
     * @author Kimi Liu
     */
    record Request(Blueprint.SourceEntry registration, String issuer, Optional<String> keyId, String use,
            String algorithm, Instant at) {

        /**
         * Validates one exact cryptographic key lookup request.
         */
        public Request {
            Assert.notNull(registration, "Key registration must not be null");
            Assert.notBlank(issuer, "Key request issuer must not be blank");
            Assert.notNull(keyId, "Key request identifier container must not be null");
            final String identifier = keyId.getOrNull();
            if (identifier != null && identifier.isBlank()) {
                throw new ValidateException("Key request identifier must not be blank when present");
            }
            keyId = Optional.ofNullable(identifier);
            Assert.notBlank(use, "Key request use must not be blank");
            Assert.notBlank(algorithm, "Key request algorithm must not be blank");
            Assert.notNull(at, "Key request validity instant must not be null");
        }

    }

    /**
     * Identifies the public key set required by a protocol operation.
     *
     * @param registration exact Source registration requesting the keys
     * @param issuer       expected key issuer
     * @param use          expected protocol use
     * @param at           required validity instant
     * @author Kimi Liu
     */
    record Criteria(Blueprint.SourceEntry registration, String issuer, String use, Instant at) {

        /**
         * Validates one public-key-set listing criteria.
         */
        public Criteria {
            Assert.notNull(registration, "Public key registration must not be null");
            Assert.notBlank(issuer, "Public key criteria issuer must not be blank");
            Assert.notBlank(use, "Public key criteria use must not be blank");
            Assert.notNull(at, "Public key criteria validity instant must not be null");
        }

    }

    /**
     * Project-adapted key material awaiting framework parsing.
     *
     * @param sourceId  exact Source identifier that owns the returned data
     * @param issuer    returned issuer
     * @param keyId     returned key identifier
     * @param use       returned protocol use
     * @param algorithm returned registered algorithm
     * @param key       returned key material
     * @param notBefore inclusive validity start
     * @param notAfter  exclusive validity end
     * @author Kimi Liu
     */
    record Record(String sourceId, String issuer, String keyId, String use, String algorithm, Key key,
            Instant notBefore, Instant notAfter) {

    }

    /**
     * Project-adapted public key listing awaiting framework parsing.
     *
     * @param sourceId exact Source identifier that owns the returned data
     * @param issuer   returned issuer
     * @param use      returned protocol use
     * @param keys     public-only JSON Web Key set
     * @author Kimi Liu
     */
    record Listing(String sourceId, String issuer, String use, JwkSet keys) {

    }

}
