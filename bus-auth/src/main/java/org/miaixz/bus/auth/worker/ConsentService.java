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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.guard.ScopeValidator;
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
    CompletionStage<Outcome<Optional<Snapshot>>> find(Request request, Context context, Timeout.Budget timeout);

    /**
     * Obtains an explicit approval or denial from the external consent implementation.
     *
     * @param request validated authorization context and minimum display data
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing a bounded decision, expected rejection, or operational failure
     */
    CompletionStage<Outcome<Decision>> decide(Request request, Context context, Timeout.Budget timeout);

    /**
     * Records an approved consent snapshot through the external persistence implementation.
     *
     * @param request approved decision and validity timestamps
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing the stored snapshot, expected rejection, or operational failure
     */
    CompletionStage<Outcome<Snapshot>> record(Save request, Context context, Timeout.Budget timeout);

    /**
     * Carries the minimum validated, non-secret authorization context displayed by an external consent implementation.
     *
     * @param sourceId    exact registered Source owning the authorization server
     * @param providerId  exact Provider owning the Source
     * @param subject     stable subject reference considering the grant
     * @param clientId    validated registered client identifier
     * @param clientName  non-sensitive display name
     * @param redirectUri validated redirect URI lexical value
     * @param scopes      non-empty requested OAuth scope-token set
     * @param resources   ordered requested resource indicators
     */
    record Request(String sourceId, String providerId, Subject.Reference subject, String clientId, String clientName,
            String redirectUri, Set<String> scopes, List<String> resources) {

        /**
         * Validates and freezes the consent interaction context shown to the subject.
         */
        public Request {
            Assert.notBlank(sourceId, "Consent request Source id must not be blank");
            Assert.notBlank(providerId, "Consent request Provider id must not be blank");
            Assert.notNull(subject, "Consent request subject must not be null");
            Assert.notBlank(clientId, "Consent request client identifier must not be blank");
            Assert.notBlank(clientName, "Consent request client display name must not be blank");
            Assert.notBlank(redirectUri, "Consent request redirect URI must not be blank");
            Assert.notNull(scopes, "Consent request scope set must not be null");
            scopes = Set.copyOf(scopes);
            if (scopes.isEmpty()) {
                throw new ValidateException("Consent request scope set must not be empty");
            }
            new ScopeValidator().validateRequested(List.copyOf(scopes), scopes);
            resources = Snapshot.immutableResources(resources);
        }

    }

    /**
     * Captures an external consent decision and the exact subset of requested OAuth scopes approved by the subject.
     *
     * @param request       immutable authorization context presented for decision
     * @param status        explicit approval or denial
     * @param grantedScopes exact approved scope subset, empty for denial
     */
    record Decision(Request request, Status status, Set<String> grantedScopes) {

        /**
         * Validates that the decision grants only a subset of requested scopes and that denial grants none.
         */
        public Decision {
            Assert.notNull(request, "Consent decision request must not be null");
            Assert.notNull(status, "Consent decision status must not be null");
            Assert.notNull(grantedScopes, "Consent decision granted scopes must not be null");
            grantedScopes = Set.copyOf(grantedScopes);
            if (status == Status.APPROVED) {
                if (grantedScopes.isEmpty()) {
                    throw new ValidateException("Approved consent must grant at least one requested scope");
                }
                new ScopeValidator().validateGranted(List.copyOf(grantedScopes), List.copyOf(request.scopes()));
            } else if (!grantedScopes.isEmpty()) {
                throw new ValidateException("Denied consent must not grant scopes");
            }
        }

        /**
         * Creates an approved decision for an exact non-empty subset of requested scopes.
         *
         * @param request       consent interaction being approved
         * @param grantedScopes approved subset of requested scopes
         * @return validated approved decision
         */
        public static Decision approve(final Request request, final Set<String> grantedScopes) {
            return new Decision(request, Status.APPROVED, grantedScopes);
        }

        /**
         * Creates a denied decision with no granted scopes.
         *
         * @param request consent interaction being denied
         * @return validated denied decision
         */
        public static Decision deny(final Request request) {
            return new Decision(request, Status.DENIED, Set.of());
        }

    }

    /**
     * Represents an externally persisted OAuth/OIDC authorization consent snapshot.
     *
     * @param sourceId   exact registered Source owning the authorization server
     * @param providerId exact Provider owning the Source
     * @param subject    stable subject reference that granted authorization
     * @param clientId   exact registered client identifier
     * @param scopes     non-empty granted OAuth scope-token set
     * @param resources  ordered authorized resource indicators
     * @param grantedAt  instant at which authorization was granted
     * @param expiresAt  optional exclusive expiration instant
     */
    record Snapshot(String sourceId, String providerId, Subject.Reference subject, String clientId, Set<String> scopes,
            List<String> resources, Instant grantedAt, Optional<Instant> expiresAt) {

        /**
         * Validates and freezes one durable consent snapshot and its validity interval.
         */
        public Snapshot {
            Assert.notBlank(sourceId, "Consent Source id must not be blank");
            Assert.notBlank(providerId, "Consent Provider id must not be blank");
            Assert.notNull(subject, "Consent subject must not be null");
            Assert.notBlank(clientId, "Consent client identifier must not be blank");
            Assert.notNull(scopes, "Consent scope set must not be null");
            scopes = Set.copyOf(scopes);
            if (scopes.isEmpty()) {
                throw new ValidateException("Consent granted scope set must not be empty");
            }
            new ScopeValidator().validateGranted(List.copyOf(scopes), List.copyOf(scopes));
            resources = immutableResources(resources);
            Assert.notNull(grantedAt, "Consent granted-at instant must not be null");
            Assert.notNull(expiresAt, "Consent expiration container must not be null");
            expiresAt = Optional.ofNullable(expiresAt.getOrNull());
            final Instant expiration = expiresAt.getOrNull();
            if (expiration != null && !expiration.isAfter(grantedAt)) {
                throw new ValidateException("Consent expiration must be later than its grant instant");
            }
        }

        /**
         * Validates and freezes ordered unique resource indicators.
         *
         * @param values resource indicators to copy
         * @return immutable ordered resource-indicator list
         */
        private static List<String> immutableResources(final List<String> values) {
            Assert.notNull(values, "Consent resource list must not be null");
            final Set<String> unique = new HashSet<>(values.size());
            for (String value : values) {
                Assert.notBlank(value, "Consent resource indicator must not be blank");
                if (!unique.add(value)) {
                    throw new ValidateException("Consent resource indicators must not contain duplicates");
                }
            }
            return List.copyOf(values);
        }

        /**
         * Tests whether this consent snapshot is active at an exact instant.
         *
         * @param instant evaluation instant
         * @return whether the instant lies inside the grant interval
         */
        public boolean activeAt(final Instant instant) {
            Assert.notNull(instant, "Consent evaluation instant must not be null");
            final Instant expiration = expiresAt.getOrNull();
            return !instant.isBefore(grantedAt) && (expiration == null || instant.isBefore(expiration));
        }

    }

    /**
     * Supplies the approved decision and validity interval used to construct a persisted consent snapshot.
     *
     * @param decision  approved bounded consent decision
     * @param grantedAt instant at which the grant was made
     * @param expiresAt optional exclusive expiration instant
     * @author Kimi Liu
     */
    record Save(Decision decision, Instant grantedAt, Optional<Instant> expiresAt) {

        /**
         * Validates that only an approved decision with a coherent validity interval can be recorded.
         *
         * @throws IllegalArgumentException if a component is {@code null}
         * @throws ValidateException        if the decision is denied or expiration is not later than grant time
         */
        public Save {
            Assert.notNull(decision, "Recorded consent decision must not be null");
            Assert.notNull(grantedAt, "Recorded consent granted-at instant must not be null");
            Assert.notNull(expiresAt, "Recorded consent expiration container must not be null");
            expiresAt = Optional.ofNullable(expiresAt.getOrNull());
            if (decision.status() != Status.APPROVED) {
                throw new ValidateException("Only an approved consent decision can be recorded");
            }
            final Instant expiration = expiresAt.getOrNull();
            if (expiration != null && !expiration.isAfter(grantedAt)) {
                throw new ValidateException("Recorded consent expiration must be later than its grant instant");
            }
        }

    }

    /**
     * Enumerates the only application-level consent outcomes.
     */
    enum Status {

        /** The subject approved a non-empty subset of requested scopes. */
        APPROVED,

        /** The subject denied the consent interaction. */
        DENIED

    }

}
