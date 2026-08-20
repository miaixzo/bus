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
import org.miaixz.bus.auth.shared.consent.Consent;
import org.miaixz.bus.auth.shared.consent.ConsentDecision;
import org.miaixz.bus.auth.shared.consent.ConsentRequest;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Defines the external application port for consent lookup, user decision, and durable recording.
 * <p>
 * Implementations own UI interaction and persistence. This contract does not authorize operators, expose a Controller,
 * or encode OAuth/OIDC authorization responses.
 * </p>
 *
 * @author Kimi Liu
 */
public interface ConsentService {

    /**
     * Finds an existing consent applicable to the validated request context.
     *
     * @param request validated authorization context
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing an optional snapshot, expected rejection, or operational failure
     */
    CompletionStage<Outcome<Optional<Consent>>> find(ConsentRequest request, Context context, Timeout.Budget timeout);

    /**
     * Obtains an explicit approval or denial from the external consent implementation.
     *
     * @param request validated authorization context and minimum display data
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing a bounded decision, expected rejection, or operational failure
     */
    CompletionStage<Outcome<ConsentDecision>> decide(ConsentRequest request, Context context, Timeout.Budget timeout);

    /**
     * Records an approved consent snapshot through the external persistence implementation.
     *
     * @param request approved decision and validity timestamps
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing the stored snapshot, expected rejection, or operational failure
     */
    CompletionStage<Outcome<Consent>> record(Record request, Context context, Timeout.Budget timeout);

    /**
     * Supplies the approved decision and validity interval used to construct a persisted Consent snapshot.
     *
     * @param decision  approved bounded consent decision
     * @param grantedAt instant at which the grant was made
     * @param expiresAt optional exclusive expiration instant
     * @author Kimi Liu
     */
    record Record(ConsentDecision decision, Instant grantedAt, Optional<Instant> expiresAt) {

        /**
         * Validates that only an approved decision with a coherent validity interval can be recorded.
         *
         * @throws IllegalArgumentException if a component is {@code null}
         * @throws ValidateException        if the decision is denied or expiration is not later than grant time
         */
        public Record {
            Assert.notNull(decision, "Recorded consent decision must not be null");
            Assert.notNull(grantedAt, "Recorded consent granted-at instant must not be null");
            Assert.notNull(expiresAt, "Recorded consent expiration container must not be null");
            expiresAt = Optional.ofNullable(expiresAt.getOrNull());
            if (decision.status() != ConsentDecision.Status.APPROVED) {
                throw new ValidateException("Only an approved consent decision can be recorded");
            }
            final Instant expiration = expiresAt.getOrNull();
            if (expiration != null && !expiration.isAfter(grantedAt)) {
                throw new ValidateException("Recorded consent expiration must be later than its grant instant");
            }
        }

    }

}
