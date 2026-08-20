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
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.crypto.builtin.CertificateChain;
import org.miaixz.bus.crypto.builtin.TrustRootIndex;

/**
 * Resolves externally managed certificate chains together with their applicable trust-root index.
 * <p>
 * The resolver selects material by issuer, standard use, and validity instant. Verification code combines the returned
 * values with the Bus {@code CertificateChainCleaner}; this port neither treats the certificate builder as a verifier
 * nor downloads or implicitly trusts missing roots.
 * </p>
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface CertificateResolver {

    /**
     * Resolves one certificate chain and its trust roots.
     *
     * @param request immutable certificate query
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing successful certificate material, expected rejection, or operational failure
     */
    CompletionStage<Outcome<ResolvedCertificate>> resolve(Query request, Context context, Timeout.Budget timeout);

    /**
     * Carries exact certificate lookup coordinates.
     *
     * @param issuer trusted issuer or certificate authority lexical value
     * @param use    exact standard certificate-use lexical value
     * @param at     validity instant
     * @author Kimi Liu
     */
    record Query(String issuer, String use, Instant at) {

        /**
         * Creates an immutable certificate query.
         *
         * @param issuer trusted issuer or authority lexical value
         * @param use    exact standard certificate-use lexical value
         * @param at     validity instant
         * @throws IllegalArgumentException if text is blank or the instant is {@code null}
         */
        public Query {
            Assert.notBlank(issuer, "Certificate query issuer must not be blank");
            Assert.notBlank(use, "Certificate query use must not be blank");
            Assert.notNull(at, "Certificate query validity instant must not be null");
        }

    }

    /**
     * Carries an immutable raw certificate chain and the trust roots applicable to its validation.
     *
     * @param chain      non-empty raw certificate chain with leaf first
     * @param trustRoots trust-root index used by Bus certificate chain cleaning
     * @author Kimi Liu
     */
    record ResolvedCertificate(CertificateChain chain, TrustRootIndex trustRoots) {

        /**
         * Creates resolved certificate material with an explicit trust boundary.
         *
         * @param chain      non-empty raw certificate chain
         * @param trustRoots applicable trust-root index
         * @throws IllegalArgumentException if a component is {@code null}
         * @throws ValidateException        if the chain has no leaf certificate
         */
        public ResolvedCertificate {
            Assert.notNull(chain, "Resolved certificate chain must not be null");
            Assert.notNull(trustRoots, "Resolved certificate trust roots must not be null");
            if (chain.empty()) {
                throw new ValidateException("Resolved certificate chain must contain a leaf certificate");
            }
        }

    }

}
