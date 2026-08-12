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
package org.miaixz.bus.auth.metric.ssf;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.*;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.JWT.VerificationPolicy;
import org.miaixz.bus.auth.metric.SSF.EventHandler;
import org.miaixz.bus.auth.metric.SSF.ReceiverPort;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Verifies, atomically de-duplicates through JWT replay admission, dispatches, and acknowledges SET events.
 */
public final class Receiver implements ReceiverPort {

    /**
     * Trusted JWT policy.
     */
    private final VerificationPolicy policy;

    /**
     * Runtime.
     */
    private final Runtime runtime;

    /**
     * Product handler.
     */
    private final EventHandler handler;

    /**
     * Creates one receiver.
     *
     * @param policy  trusted policy
     * @param runtime runtime
     * @param handler handler
     */
    public Receiver(final VerificationPolicy policy, final Runtime runtime, final EventHandler handler) {
        this.policy = Assert.notNull(policy, () -> new ValidateException("SSF JWT policy must not be null"));
        Assert.isTrue(
                policy.requireReplay(),
                () -> new ValidateException("SSF receiver requires JWT replay protection"));
        this.runtime = Assert.notNull(runtime, () -> new ValidateException("SSF runtime must not be null"));
        this.handler = Assert.notNull(handler, () -> new ValidateException("SSF event handler must not be null"));
    }

    /**
     * Maps token rejection separately from product handler failure.
     *
     * @param failure failure
     * @return safe outcome
     */
    static Outcome<Void> failure(final Throwable failure) {
        final Throwable cause = failure instanceof CompletionException && failure.getCause() != null
                ? failure.getCause()
                : failure;
        if (cause instanceof ValidateException
                || cause instanceof org.miaixz.bus.core.lang.exception.ProtocolException) {
            return new Rejected<>(new Failure(FailureKind.AUTHENTICATION, ErrorCode._100533, false));
        }
        return new Failed<>(new Failure(FailureKind.INTERNAL, ErrorCode._FAILURE, true), cause);
    }

    /**
     * Receives one SET.
     *
     * @param invocation operation context
     * @param token      compact SET
     * @return outcome stage
     */
    @Override
    public CompletionStage<Outcome<Void>> receive(final Invocation invocation, final String token) {
        return SecurityEventToken.verify(token, policy, invocation, runtime)
                .thenCompose(event -> handler.handle(invocation, event))
                .<Outcome<Void>>thenApply(ignored -> new Success<>(null)).exceptionally(Receiver::failure);
    }

}
