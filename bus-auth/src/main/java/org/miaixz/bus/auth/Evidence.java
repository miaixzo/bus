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
package org.miaixz.bus.auth;

import java.time.Instant;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents one verified piece of authentication evidence and its assurance strength.
 * <p>
 * Evidence is created only after the responsible protocol, Vendor adapter, loader/parser chain, or identity worker
 * validates the source value. It records a safe derived claim and verification provenance, never a raw password, token,
 * assertion, private platform response, or other credential material.
 * </p>
 *
 * @param type     verified evidence category
 * @param strength achieved authentication strength
 * @param claim    verified derived claim and provenance
 * @author Kimi Liu
 */
public record Evidence(Type type, Strength strength, Claim claim) {

    /**
     * Creates verified authentication evidence.
     *
     * @param type     verified evidence category
     * @param strength achieved authentication strength
     * @param claim    verified derived claim and provenance
     * @throws IllegalArgumentException if any component is {@code null}
     */
    public Evidence {
        Assert.notNull(type, "Evidence type must not be null");
        Assert.notNull(strength, "Evidence strength must not be null");
        Assert.notNull(claim, "Evidence claim must not be null");
    }

    /**
     * Enumerates verified authentication evidence categories.
     *
     * @author Kimi Liu
     */
    public enum Type {

        /**
         * Evidence derived from successful password verification.
         */
        PASSWORD,

        /**
         * Evidence derived from a verified external federation response.
         */
        FEDERATED,

        /**
         * Evidence derived from successful certificate authentication.
         */
        CERTIFICATE,

        /**
         * Evidence derived from a validated security token.
         */
        TOKEN,

        /**
         * Evidence that combines multiple independently verified authentication factors.
         */
        MULTI_FACTOR

    }

    /**
     * Enumerates the assurance strength established by verified authentication evidence.
     *
     * @author Kimi Liu
     */
    public enum Strength {

        /**
         * One authentication factor was verified.
         */
        SINGLE_FACTOR,

        /**
         * Multiple independent authentication factors were verified.
         */
        MULTI_FACTOR,

        /**
         * Verified authentication uses a phishing-resistant mechanism.
         */
        PHISHING_RESISTANT

    }

    /**
     * Carries one safe verified claim together with its issuer and verification time.
     *
     * @param name       stable claim name
     * @param value      immutable provider-neutral verified value
     * @param issuer     trusted issuer or verifier identifier
     * @param verifiedAt instant at which the value was verified
     * @author Kimi Liu
     */
    public record Claim(String name, JsonValue value, String issuer, Instant verifiedAt) {

        /**
         * Creates a verified claim with explicit provenance.
         *
         * @param name       non-blank stable claim name
         * @param value      provider-neutral verified value
         * @param issuer     non-blank trusted issuer or verifier identifier
         * @param verifiedAt verification instant
         * @throws IllegalArgumentException if text is blank or another component is {@code null}
         */
        public Claim {
            Assert.notBlank(name, "Evidence claim name must not be blank");
            Assert.notNull(value, "Evidence claim value must not be null");
            Assert.notBlank(issuer, "Evidence claim issuer must not be blank");
            Assert.notNull(verifiedAt, "Evidence claim verification time must not be null");
        }

    }

}
