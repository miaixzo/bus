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
package org.miaixz.bus.auth.identity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.shared.claim.ClaimSet;
import org.miaixz.bus.core.lang.Assert;

/**
 * Builds provider-neutral verified claims from a stable Subject and authentication evidence.
 * <p>
 * The produced ClaimSet is internal identity data. OAuth, OpenID Connect, SAML, SCIM, LDAP, and other protocol services
 * remain responsible for mapping it to every formal standard field and wire representation.
 * </p>
 *
 * @author Kimi Liu
 */
public interface ClaimService {

    /**
     * Builds the provider-neutral ClaimSet for one authenticated Subject.
     *
     * @param request stable Subject and verified evidence
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing verified claims, expected rejection, or operational failure
     */
    CompletionStage<Outcome<ClaimSet>> claims(Request request, Context context, Timeout.Budget timeout);

    /**
     * Carries immutable ClaimSet construction input.
     *
     * @param subject  stable resolved Subject
     * @param evidence verified authentication evidence
     * @author Kimi Liu
     */
    record Request(Subject subject, List<Evidence> evidence) {

        /**
         * Creates detached immutable claim construction input.
         *
         * @param subject  stable Subject
         * @param evidence verified evidence
         * @throws IllegalArgumentException if a component or evidence entry is {@code null}
         */
        public Request {
            Assert.notNull(subject, "Claim request Subject must not be null");
            Assert.notNull(evidence, "Claim request evidence list must not be null");
            final List<Evidence> copy = new ArrayList<>(evidence.size());
            for (Evidence item : evidence) {
                copy.add(Assert.notNull(item, "Claim request evidence entry must not be null"));
            }
            evidence = List.copyOf(copy);
        }

    }

}
