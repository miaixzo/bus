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
package org.miaixz.bus.fabric.protocol;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Central entry for direct protocol execution and supported protocol-carrier transitions.
 * <p>
 * This class validates routing and cancellation only. Concrete request preparation, transport work, callbacks and
 * resource ownership remain in the protocol runners and call lifecycle.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Mediator {

    /**
     * Restricts construction because protocol routing is exposed through static operations.
     */
    private Mediator() {
        // Static routing entry.
    }

    /**
     * Executes a supported direct protocol type.
     *
     * @param type         non-null direct protocol type, excluding internal HTTP carrier types
     * @param cancellation non-null caller-owned scope checked before invocation
     * @param invocation   non-null operation receiving the same cancellation scope
     * @param <T>          invocation result type
     * @return result returned by the supplied invocation
     */
    public static <T> T execute(
            final Type type,
            final Cancellation cancellation,
            final Invocation<Cancellation, T> invocation) {
        final Type currentType = require(type, "Protocol type");
        final Cancellation currentCancellation = require(cancellation, "Cancellation");
        final Invocation<Cancellation, T> currentInvocation = require(invocation, "Protocol invocation");
        if (!direct(currentType)) {
            throw new ProtocolException("Unsupported direct protocol type: " + currentType);
        }
        currentCancellation.throwIfCancelled();
        return currentInvocation.invoke(currentCancellation);
    }

    /**
     * Executes a supported protocol-carrier transition.
     *
     * @param source       non-null logical protocol being carried
     * @param target       non-null supported carrier protocol type
     * @param cancellation non-null caller-owned scope checked before invocation
     * @param invocation   non-null target operation receiving the same cancellation scope
     * @param <T>          invocation result type
     * @return result returned by the supplied invocation
     */
    public static <T> T convert(
            final Type source,
            final Type target,
            final Cancellation cancellation,
            final Invocation<Cancellation, T> invocation) {
        final Type currentSource = require(source, "Source protocol type");
        final Type currentTarget = require(target, "Target protocol type");
        final Cancellation currentCancellation = require(cancellation, "Cancellation");
        final Invocation<Cancellation, T> currentInvocation = require(invocation, "Protocol invocation");
        if (!transition(currentSource, currentTarget)) {
            throw new ProtocolException("Unsupported protocol transition: " + currentSource + " -> " + currentTarget);
        }
        currentCancellation.throwIfCancelled();
        return currentInvocation.invoke(currentCancellation);
    }

    /**
     * Returns whether an operation is a supported direct client operation.
     *
     * @param type non-null protocol type to classify
     * @return {@code true} for a supported direct operation
     */
    private static boolean direct(final Type type) {
        return switch (type) {
            case HTTP, SOCKET, SSE, STOMP, WEBSOCKET -> true;
            case HTTP_STREAM, HTTP_UPGRADE -> false;
        };
    }

    /**
     * Returns whether a source-to-target carrier transition is supported.
     *
     * @param source non-null logical source protocol
     * @param target non-null proposed carrier protocol
     * @return {@code true} for a supported transition
     */
    private static boolean transition(final Type source, final Type target) {
        return source == Type.SSE && target == Type.HTTP_STREAM
                || source == Type.WEBSOCKET && target == Type.HTTP_UPGRADE
                || source == Type.STOMP && target == Type.WEBSOCKET;
    }

    /**
     * Validates a required routing value.
     *
     * @param value routing reference to validate
     * @param name  field label included in the validation error
     * @param <T>   routing reference type
     * @return validated non-null reference
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Represents a typed invocation with one input and one result.
     *
     * @param <I> invocation input type
     * @param <O> invocation result type
     */
    @FunctionalInterface
    public interface Invocation<I, O> {

        /**
         * Invokes the operation with the supplied input.
         *
         * @param input input supplied by the mediator
         * @return operation-specific result
         */
        O invoke(I input);

    }

    /**
     * Defines direct protocol types and internal carrier types.
     */
    public enum Type {

        /**
         * HTTP request execution.
         */
        HTTP,

        /**
         * HTTP response stream acquisition for a carrier protocol.
         */
        HTTP_STREAM,

        /**
         * HTTP connection upgrade for a carrier protocol.
         */
        HTTP_UPGRADE,

        /**
         * Socket client connection opening.
         */
        SOCKET,

        /**
         * Server-sent event session opening.
         */
        SSE,

        /**
         * STOMP session opening.
         */
        STOMP,

        /**
         * WebSocket session opening.
         */
        WEBSOCKET

    }

}
