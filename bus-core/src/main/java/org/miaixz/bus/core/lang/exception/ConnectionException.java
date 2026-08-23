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
package org.miaixz.bus.core.lang.exception;

import java.io.Serial;
import java.util.Objects;

/**
 * Describes a structured failure while establishing or using a network connection.
 * <p>
 * The exception deliberately contains no proxy-specific types. Network clients can therefore use the same failure model
 * for direct routes, proxies, relays, secure handshakes, and session setup. Retry policy remains the caller's
 * responsibility; {@link #canSwitchRoute()} only states whether the recorded failure is safe for an ordered route
 * candidate switch.
 *
 * @author Kimi Liu
 */
public class ConnectionException extends SocketException {

    /**
     * The serialization version identifier.
     */
    @Serial
    private static final long serialVersionUID = 2852299832471L;

    /**
     * Connection lifecycle phase in which the failure occurred.
     */
    private final Phase phase;

    /**
     * Component whose behavior or state caused the failure.
     */
    private final Scope scope;

    /**
     * Best known state of application-data delivery when the failure occurred.
     */
    private final Delivery delivery;

    /**
     * Redacted identifier of the physical or logical route used by the failed attempt.
     */
    private final String routeId;

    /**
     * Creates a structured connection failure.
     *
     * @param phase    connection lifecycle phase in which the failure occurred
     * @param scope    component responsible for the failure
     * @param delivery application-data delivery state at the failure boundary
     * @param routeId  non-null, redacted route identifier suitable for logs and metrics
     * @param message  human-readable failure description
     * @param cause    underlying failure, or {@code null} when no lower-level cause exists
     */
    public ConnectionException(final Phase phase, final Scope scope, final Delivery delivery, final String routeId,
            final String message, final Throwable cause) {
        super(message, cause);
        this.phase = Objects.requireNonNull(phase, "Connection failure phase must not be null");
        this.scope = Objects.requireNonNull(scope, "Connection failure scope must not be null");
        this.delivery = Objects.requireNonNull(delivery, "Connection delivery state must not be null");
        this.routeId = Objects.requireNonNull(routeId, "Connection route identifier must not be null");
    }

    /**
     * Returns the connection lifecycle phase in which the failure occurred.
     *
     * @return non-null failure phase
     */
    public Phase phase() {
        return phase;
    }

    /**
     * Returns the component responsible for the failure.
     *
     * @return non-null failure scope
     */
    public Scope scope() {
        return scope;
    }

    /**
     * Returns the best known application-data delivery state.
     *
     * @return non-null delivery state
     */
    public Delivery delivery() {
        return delivery;
    }

    /**
     * Returns the redacted identifier of the failed route.
     *
     * @return non-null route identifier suitable for diagnostics
     */
    public String routeId() {
        return routeId;
    }

    /**
     * Returns whether an ordered route selector can safely try its next candidate.
     * <p>
     * A route switch is safe only when the failure is isolated to the current route and application data was definitely
     * not delivered. The method does not authorize repeating an application request after any bytes may have been sent.
     *
     * @return {@code true} when switching to the next route cannot duplicate application data
     */
    public boolean canSwitchRoute() {
        return scope == Scope.ROUTE && delivery == Delivery.NOT_STARTED;
    }

    /**
     * Connection lifecycle phases used to locate a failure without depending on a concrete transport implementation.
     */
    public enum Phase {

        /**
         * Resolving a host or service name into a network destination.
         */
        RESOLVE,

        /**
         * Establishing the underlying TCP, UDP, or equivalent transport.
         */
        TRANSPORT_CONNECT,

        /**
         * Negotiating an intermediary such as an HTTP proxy, SOCKS proxy, or relay.
         */
        ROUTE_NEGOTIATION,

        /**
         * Establishing transport security, including TLS negotiation and peer verification.
         */
        SECURITY_HANDSHAKE,

        /**
         * Creating the protocol session after transport and security setup complete.
         */
        SESSION_SETUP,

        /**
         * Reading application or protocol data from an established connection.
         */
        READ,

        /**
         * Writing application or protocol data to an established connection.
         */
        WRITE
    }

    /**
     * Identifies which part of the connection path caused a failure.
     */
    public enum Scope {

        /**
         * The selected physical route, proxy, relay, or intermediary failed.
         */
        ROUTE,

        /**
         * The requested target service or its identity failed independently of the selected route.
         */
        TARGET,

        /**
         * Local configuration is invalid or unsupported and another route must not hide the problem.
         */
        CONFIGURATION
    }

    /**
     * Describes whether application data may already have crossed the connection boundary.
     */
    public enum Delivery {

        /**
         * Application data was definitely not sent.
         */
        NOT_STARTED,

        /**
         * Delivery cannot be determined reliably, so automatic route switching is unsafe.
         */
        UNKNOWN,

        /**
         * At least part of the application data was sent.
         */
        STARTED
    }

}
