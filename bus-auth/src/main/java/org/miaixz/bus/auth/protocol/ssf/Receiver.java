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
package org.miaixz.bus.auth.protocol.ssf;

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Outcome.*;
import org.miaixz.bus.auth.cache.StateStore;
import org.miaixz.bus.auth.protocol.jwt.JWT.VerificationPolicy;
import org.miaixz.bus.auth.protocol.jwt.KeyResolver;
import org.miaixz.bus.auth.protocol.ssf.SSF.EventHandler;
import org.miaixz.bus.auth.protocol.ssf.SSF.ReceiverPort;
import org.miaixz.bus.auth.runtime.Limits;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Clock;

/**
 * Verifies, atomically de-duplicates through JWT replay admission, dispatches, and acknowledges SET events.
 *
 * @author Kimi Liu
 */
public final class Receiver implements ReceiverPort {

    /**
     * Trusted JWT verification and replay policy.
     */
    private final VerificationPolicy policy;

    /**
     * JSON provider used by the hardened JWT verifier.
     */
    private final JsonProvider json;

    /**
     * Trusted security clock.
     */
    private final Clock clock;

    /**
     * Trusted JWT key resolver.
     */
    private final KeyResolver keys;

    /**
     * Atomic replay store.
     */
    private final StateStore states;

    /**
     * Immutable verification limits.
     */
    private final Limits limits;

    /**
     * Product callback invoked only after complete SET verification.
     */
    private final EventHandler handler;

    /**
     * Creates one receiver.
     *
     * @param policy  trusted policy
     * @param json    JSON provider
     * @param clock   trusted clock
     * @param keys    trusted key resolver
     * @param states  atomic replay store
     * @param limits  immutable protocol limits
     * @param handler handler
     * @throws ValidateException if a dependency is null or replay protection is disabled
     */
    public Receiver(final VerificationPolicy policy, final JsonProvider json, final Clock clock, final KeyResolver keys,
            final StateStore states, final Limits limits, final EventHandler handler) {
        this.policy = Assert.notNull(policy, () -> new ValidateException("SSF JWT policy must not be null"));
        Assert.isTrue(
                policy.requireReplay(),
                () -> new ValidateException("SSF receiver requires JWT replay protection"));
        this.json = Assert.notNull(json, () -> new ValidateException("SSF JSON provider must not be null"));
        this.clock = Assert.notNull(clock, () -> new ValidateException("SSF clock must not be null"));
        this.keys = Assert.notNull(keys, () -> new ValidateException("SSF key resolver must not be null"));
        this.states = Assert.notNull(states, () -> new ValidateException("SSF state store must not be null"));
        this.limits = Assert.notNull(limits, () -> new ValidateException("SSF limits must not be null"));
        this.handler = Assert.notNull(handler, () -> new ValidateException("SSF event handler must not be null"));
    }

    /**
     * Maps token rejection separately from product handler failure.
     *
     * @param failure failure
     * @return safe outcome
     */
    static Outcome<Void> failure(final Throwable failure) {
        final Throwable cause = ExceptionKit.unwrap(failure);
        if (cause instanceof ValidateException
                || cause instanceof org.miaixz.bus.core.lang.exception.ProtocolException) {
            return new Rejected<>(new Failure(Kind.AUTHENTICATION, ErrorCode._100533, false));
        }
        return new Failed<>(new Failure(Kind.INTERNAL, ErrorCode._FAILURE, true), cause);
    }

    /**
     * Receives one SET.
     *
     * @param invocation operation context
     * @param token      compact SET
     * @return outcome stage
     * @throws ValidateException if a synchronous verifier precondition is invalid
     */
    @Override
    public CompletionStage<Outcome<Void>> receive(final Context invocation, final String token) {
        return SecurityEventToken.verify(token, policy, invocation, json, clock, keys, states, limits)
                .thenCompose(event -> handler.handle(invocation, event))
                .<Outcome<Void>>thenApply(ignored -> new Success<>(null)).exceptionally(Receiver::failure);
    }

}
