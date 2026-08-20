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
package org.miaixz.bus.auth.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;

/**
 * Stores OAuth 2.0 Device Authorization Grant state for atomic polling and token issuance.
 * <p>
 * The backend key is an isolated irreversible digest of the device code; the user code is retained only for the
 * verification interaction. Polling updates use atomic replace so interval enforcement, approval, denial, and the
 * single transition to token consumption cannot race. An external implementation's {@code create} operation must
 * atomically enforce a project-local unique user-code index together with the primary device-code digest and must
 * remove that index on expiration or deletion. The external verification service uses that private index to locate and
 * compare-and-replace the primary entry; bus-auth does not define its Controller or operator authorization.
 * </p>
 *
 * @author Kimi Liu
 */
public interface DeviceCodeStore extends AtomicStore<String, ExpiringValue<DeviceCodeStore.Entry>> {

    /**
     * Enumerates the atomic lifecycle of an OAuth device authorization.
     *
     * @author Kimi Liu
     */
    enum Status {

        /**
         * User authorization has not completed and polling returns {@code authorization_pending}.
         */
        PENDING,

        /**
         * The user approved access and the entry may be converted to tokens once.
         */
        APPROVED,

        /**
         * The user denied access and polling returns {@code access_denied}.
         */
        DENIED,

        /**
         * Tokens were already issued and the device code cannot be used again.
         */
        CONSUMED

    }

    /**
     * Carries the immutable state of one RFC 8628 device authorization.
     *
     * @param providerId   OAuth Provider identifier
     * @param clientId     OAuth client identifier
     * @param userCode     short verification code displayed to the user
     * @param scope        requested and approved OAuth scope values
     * @param status       current atomic device authorization state
     * @param interval     minimum interval between token endpoint polls
     * @param lastPolledAt actual most recent poll time, absent before the first poll
     * @param subjectId    authorized subject for approved or consumed state
     * @author Kimi Liu
     */
    record Entry(String providerId, String clientId, String userCode, List<String> scope, Status status,
            Duration interval, Optional<Instant> lastPolledAt, Optional<String> subjectId) {

        /**
         * Creates an immutable device authorization state value.
         *
         * @param providerId   OAuth Provider identifier
         * @param clientId     OAuth client identifier
         * @param userCode     user-facing verification code
         * @param scope        OAuth scope values
         * @param status       current state
         * @param interval     positive minimum poll interval
         * @param lastPolledAt optional actual most recent poll time
         * @param subjectId    optional authorized subject
         * @throws IllegalArgumentException if a required value is absent, scope contains a blank entry, interval is not
         *                                  positive, or subject presence conflicts with status
         */
        public Entry {
            Assert.notBlank(providerId, "Device code Provider id must not be blank");
            Assert.notBlank(clientId, "Device code client id must not be blank");
            Assert.notBlank(userCode, "Device user code must not be blank");
            Assert.notNull(scope, "Device code scope must not be null");
            final List<String> scopeCopy = new ArrayList<>(scope.size());
            for (String value : scope) {
                scopeCopy.add(Assert.notBlank(value, "Device code scope must not contain blank values"));
            }
            scope = List.copyOf(scopeCopy);
            Assert.notNull(status, "Device code status must not be null");
            Assert.notNull(interval, "Device polling interval must not be null");
            Assert.isTrue(!interval.isZero() && !interval.isNegative(), "Device polling interval must be positive");
            Assert.notNull(lastPolledAt, "Device last-poll container must not be null");
            lastPolledAt = Optional.ofNullable(lastPolledAt.getOrNull());
            Assert.notNull(subjectId, "Device subject container must not be null");
            if (!subjectId.isEmpty()) {
                Assert.notBlank(subjectId.getOrNull(), "Device subject id must not be blank");
            }
            subjectId = Optional.ofNullable(subjectId.getOrNull());
            final boolean requiresSubject = status == Status.APPROVED || status == Status.CONSUMED;
            Assert.isTrue(
                    requiresSubject == !subjectId.isEmpty(),
                    "Approved or consumed device state must have exactly one subject");
        }

    }

}
