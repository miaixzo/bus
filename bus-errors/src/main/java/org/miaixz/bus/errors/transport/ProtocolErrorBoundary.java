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
package org.miaixz.bus.errors.transport;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.miaixz.bus.errors.ErrorBoundary;
import org.miaixz.bus.errors.ErrorContext;
import org.miaixz.bus.errors.ErrorDecision;
import org.miaixz.bus.errors.FailureFacts;

/**
 * Claims one registered protocol terminal action per operation.
 *
 * @author Kimi Liu
 */
public final class ProtocolErrorBoundary {

    /**
     * Prevents construction of the stateless protocol boundary utility.
     */
    private ProtocolErrorBoundary() {
    }

    /**
     * Runs a broker callback whose success result acknowledges delivery. On failure the original exception is returned
     * to the broker by throwing it, so its existing redelivery and acknowledgement rules stay in force.
     *
     * @param boundary    classifier and observer for the broker
     * @param protocol    broker identifier
     * @param delivery    callback returning the acknowledgement only after successful delivery
     * @param diagnostics existing diagnostic hook
     * @param <T>         broker acknowledgement result
     * @return acknowledgement supplied by the successful callback
     */
    public static <T> T deliver(
            ErrorBoundary boundary,
            String protocol,
            Supplier<T> delivery,
            Consumer<RuntimeException> diagnostics) {
        try {
            return delivery.get();
        } catch (RuntimeException failure) {
            var operation = boundary.open();
            var context = new ErrorContext("consumer", protocol, ErrorContext.Phase.COMMITTED);
            var decision = operation.decide(context, FailureFacts.UNKNOWN, failure);
            ErrorBoundary.runCallback(context, "protocol-diagnostics", null, () -> diagnostics.accept(failure));
            operation.observe(context, decision, true, failure, null);
            throw failure;
        }
    }

    /**
     * Runs a broker callback with the standard diagnostic channel and preserves its acknowledgement contract.
     *
     * @param boundary classifier and observer for the broker
     * @param protocol broker identifier
     * @param delivery callback that acknowledges only successful delivery
     * @param <T>      acknowledgement result
     * @return acknowledgement supplied by the successful callback
     */
    public static <T> T deliver(ErrorBoundary boundary, String protocol, Supplier<T> delivery) {
        return deliver(boundary, protocol, delivery, ignored -> {
        });
    }

    /**
     * Claims an asynchronous termination delegated to the transport runtime.
     *
     * @param operation state for the active operation
     * @param context   current protocol phase
     * @param decision  original failure decision
     * @return whether this caller owns the terminal signal
     */
    public static boolean claim(ErrorBoundary.Operation operation, ErrorContext context, ErrorDecision decision) {
        return claim(operation, context, decision, null, null);
    }

    /**
     * Claims asynchronous termination and includes safe correlation and cause metadata in the terminal event.
     *
     * @param operation state for the active operation
     * @param context   current protocol phase
     * @param decision  original failure decision
     * @param failure   diagnostic exception, or null
     * @param requestId server-generated request identifier, or null
     * @return whether this caller owns the terminal signal
     */
    public static boolean claim(
            ErrorBoundary.Operation operation,
            ErrorContext context,
            ErrorDecision decision,
            Throwable failure,
            String requestId) {
        if (decision.action() != ErrorDecision.Action.TERMINATE || !operation.claimTermination()) {
            return false;
        }
        operation.observe(context, decision, true, failure, requestId);
        return true;
    }

    /**
     * Runs the transport owner's terminal action and emits one terminal event.
     *
     * @param operation      state for the active operation
     * @param context        current protocol phase
     * @param decision       original failure decision
     * @param terminalAction protocol-specific close, trailer or disposition action
     * @return whether the action was claimed and executed
     */
    public static boolean terminate(
            ErrorBoundary.Operation operation,
            ErrorContext context,
            ErrorDecision decision,
            Runnable terminalAction) {
        if (decision.action() != ErrorDecision.Action.TERMINATE || !operation.claimTermination()) {
            return false;
        }
        operation.complete(context, decision, null, null, () -> {
            terminalAction.run();
            return null;
        });
        return true;
    }

}
