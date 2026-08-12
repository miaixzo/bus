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
package org.miaixz.bus.auth.metric;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.Invocation;
import org.miaixz.bus.auth.metric.AuthMetric.Outcome;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.jwt.signature.JWTSigner;
import org.miaixz.bus.auth.metric.ssf.Receiver;
import org.miaixz.bus.auth.metric.ssf.StreamConfiguration;
import org.miaixz.bus.auth.metric.ssf.Transmitter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Defines the sole Shared Signals Framework receiver and transmitter facades and the closed event allow-list.
 */
public final class SSF {

    /**
     * CAEP session-revoked event.
     */
    public static final String CAEP_SESSION_REVOKED = "https://schemas.openid.net/secevent/caep/event-type/session-revoked";

    /**
     * CAEP credential-change event.
     */
    public static final String CAEP_CREDENTIAL_CHANGE = "https://schemas.openid.net/secevent/caep/event-type/credential-change";

    /**
     * CAEP assurance-level-change event.
     */
    public static final String CAEP_ASSURANCE_LEVEL_CHANGE = "https://schemas.openid.net/secevent/caep/event-type/assurance-level-change";

    /**
     * RISC account-disabled event.
     */
    public static final String RISC_ACCOUNT_DISABLED = "https://schemas.openid.net/secevent/risc/event-type/account-disabled";

    /**
     * RISC account-enabled event.
     */
    public static final String RISC_ACCOUNT_ENABLED = "https://schemas.openid.net/secevent/risc/event-type/account-enabled";

    /**
     * RISC identifier-changed event.
     */
    public static final String RISC_IDENTIFIER_CHANGED = "https://schemas.openid.net/secevent/risc/event-type/identifier-changed";

    /**
     * RISC sessions-revoked event.
     */
    public static final String RISC_SESSIONS_REVOKED = "https://schemas.openid.net/secevent/risc/event-type/sessions-revoked";

    /**
     * Closed allowed event types.
     */
    public static final Set<String> ALLOWED_EVENTS = Set.of(
            CAEP_SESSION_REVOKED,
            CAEP_CREDENTIAL_CHANGE,
            CAEP_ASSURANCE_LEVEL_CHANGE,
            RISC_ACCOUNT_DISABLED,
            RISC_ACCOUNT_ENABLED,
            RISC_IDENTIFIER_CHANGED,
            RISC_SESSIONS_REVOKED);

    /**
     * Prevents construction.
     */
    private SSF() {
        // No initialization required.
    }

    /**
     * Creates one receiver.
     *
     * @param policy  trusted JWT policy
     * @param runtime runtime
     * @param handler product event handler
     * @return receiver
     */
    public static ReceiverPort receiver(
            final JWT.VerificationPolicy policy,
            final Runtime runtime,
            final EventHandler handler) {
        return new Receiver(policy, runtime, handler);
    }

    /**
     * Creates one transmitter.
     *
     * @param configuration stream configuration
     * @param policy        trusted JWT policy
     * @param signer        trusted signer
     * @param runtime       runtime
     * @return transmitter
     */
    public static TransmitterPort transmitter(
            final StreamConfiguration configuration,
            final JWT.VerificationPolicy policy,
            final JWTSigner signer,
            final Runtime runtime) {
        return new Transmitter(configuration, policy, signer, runtime);
    }

    /**
     * Receiver contract.
     */
    @FunctionalInterface
    public interface ReceiverPort {

        /**
         * Verifies and dispatches one compact SET.
         *
         * @param invocation operation context
         * @param token      compact SET
         * @return processing outcome
         */
        CompletionStage<Outcome<Void>> receive(Invocation invocation, String token);
    }

    /**
     * Transmitter contract.
     */
    public interface TransmitterPort {

        /**
         * Pushes one event.
         *
         * @param invocation operation context
         * @param event      security event
         * @return delivery outcome
         */
        CompletionStage<Outcome<Void>> push(Invocation invocation, Event event);

        /**
         * Creates one compact SET for polling storage.
         *
         * @param event security event
         * @return compact SET
         */
        String poll(Event event);
    }

    /**
     * Product event dispatch port.
     */
    @FunctionalInterface
    public interface EventHandler {

        /**
         * Handles one verified event.
         *
         * @param invocation operation context
         * @param event      event
         * @return completion stage
         */
        CompletionStage<Void> handle(Invocation invocation, Event event);
    }

    /**
     * Immutable allowed security event.
     *
     * @param type    allowed event URI
     * @param subject stable subject identifier
     * @param claims  immutable event claims
     */
    public record Event(String type, String subject, Map<String, Object> claims) {

        /**
         * Validates one event.
         *
         * @param type    event type
         * @param subject subject
         * @param claims  claims
         */
        public Event {
            Assert.isTrue(ALLOWED_EVENTS.contains(type), () -> new ValidateException("SSF event type is not allowed"));
            subject = Assert.notBlank(subject, () -> new ValidateException("SSF subject must not be blank"));
            claims = Map
                    .copyOf(Assert.notNull(claims, () -> new ValidateException("SSF event claims must not be null")));
        }
    }

}
