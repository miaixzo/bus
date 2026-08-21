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
import org.miaixz.bus.auth.Registration;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.crypto.builtin.CertificateChain;
import org.miaixz.bus.crypto.builtin.TrustRootIndex;

/**
 * Loads certificate material from project-owned storage.
 */
@FunctionalInterface
public interface CertificateLoader {

    /**
     * Loads certificate material within the exact Source registration scope.
     *
     * @param registration exact Source registration requesting the data
     * @param request      validated certificate lookup coordinates
     * @param context      immutable non-secret invocation context
     * @param timeout      shared end-to-end operation budget
     * @return asynchronous project loading outcome
     */
    CompletionStage<Outcome<Record>> load(
            Registration.SourceEntry registration,
            Request request,
            Context context,
            Timeout.Budget timeout);

    /**
     * Identifies certificate material required by one protocol operation.
     *
     * @param issuer expected certificate issuer
     * @param use    expected protocol use
     * @param at     required validity instant
     */
    record Request(String issuer, String use, Instant at) {

        /**
         * Validates one complete certificate lookup request.
         */
        public Request {
            Assert.notBlank(issuer, "Certificate request issuer must not be blank");
            Assert.notBlank(use, "Certificate request use must not be blank");
            Assert.notNull(at, "Certificate request validity instant must not be null");
        }

    }

    /**
     * Loaded certificate chain and trust-root boundary awaiting parsing.
     *
     * @param sourceId   exact Source identifier that owns the returned data
     * @param issuer     returned issuer
     * @param use        returned protocol use
     * @param chain      returned certificate chain
     * @param trustRoots project trust boundary used to validate the chain
     * @param notBefore  inclusive validity start
     * @param notAfter   exclusive validity end
     */
    record Record(String sourceId, String issuer, String use, CertificateChain chain, TrustRootIndex trustRoots,
            Instant notBefore, Instant notAfter) {

    }

}
