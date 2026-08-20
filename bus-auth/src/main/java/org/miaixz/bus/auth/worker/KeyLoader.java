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
package org.miaixz.bus.auth.worker;

import java.time.Instant;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.shared.jose.JwkSet;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/** Loads cryptographic key records from project-owned key storage. */
public interface KeyLoader {

    CompletionStage<Outcome<KeyRecord>> load(Request request, Context context, Timeout.Budget timeout);

    CompletionStage<Outcome<JwkSet>> loadPublic(PublicRequest request, Context context, Timeout.Budget timeout);

    record Request(String issuer, Optional<String> keyId, String use, String algorithm, Instant at) {

        public Request {
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

    record PublicRequest(String issuer, String use, Instant at) {

        public PublicRequest {
            Assert.notBlank(issuer, "Public key request issuer must not be blank");
            Assert.notBlank(use, "Public key request use must not be blank");
            Assert.notNull(at, "Public key request validity instant must not be null");
        }
    }
}
