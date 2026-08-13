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
package org.miaixz.bus.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Timeout;

/**
 * Immutable protocol-neutral facts, routing data, and authenticated identity for one operation.
 *
 * @author Kimi Liu
 */
public final class Context {

    /**
     * Security domain and tenant boundary for the operation.
     */
    private final Realm realm;

    /**
     * Stable correlation identifier safe for diagnostics.
     */
    private final String correlationId;

    /**
     * Creation instant supplied by the injected Fabric clock.
     */
    private final Instant createdAt;

    /**
     * Absolute operation deadline.
     */
    private final Instant deadline;

    /**
     * Provider and capability selected for the operation.
     */
    private final Route route;

    /**
     * Optional local protocol endpoint.
     */
    private final Endpoint localEndpoint;

    /**
     * Optional remote protocol endpoint.
     */
    private final Endpoint remoteEndpoint;

    /**
     * Optional authenticated principal snapshot.
     */
    private final Principal principal;

    /**
     * Immutable typed protocol and extension options.
     */
    private final Options options;

    /**
     * Creates a validated immutable operation context.
     *
     * @param realm          security domain
     * @param correlationId  stable correlation identifier
     * @param createdAt      creation instant
     * @param deadline       absolute deadline after creation
     * @param route          selected provider route
     * @param localEndpoint  optional local endpoint
     * @param remoteEndpoint optional remote endpoint
     * @param principal      optional authenticated principal
     * @param options        immutable typed options
     * @throws ValidateException if a required value is absent or the deadline is invalid
     */
    private Context(final Realm realm, final String correlationId, final Instant createdAt, final Instant deadline,
            final Route route, final Endpoint localEndpoint, final Endpoint remoteEndpoint, final Principal principal,
            final Options options) {
        this.realm = required(realm, "Realm");
        this.correlationId = text(correlationId, "Correlation identifier");
        this.createdAt = required(createdAt, "Creation time");
        this.deadline = required(deadline, "Deadline");
        if (!deadline.isAfter(createdAt)) {
            throw new ValidateException("Context deadline must be after creation");
        }
        this.route = required(route, "Route");
        this.localEndpoint = localEndpoint;
        this.remoteEndpoint = remoteEndpoint;
        this.principal = principal;
        this.options = required(options, "Options");
    }

    /**
     * Starts a context builder using an explicit Fabric clock.
     *
     * @param clock operation time source
     * @return staged context builder
     * @throws ValidateException if {@code clock} is null
     */
    public static Initial builder(final Clock clock) {
        return new Initial(required(clock, "Clock"));
    }

    /**
     * Validates and trims required text.
     *
     * @param value text to validate
     * @param label field label
     * @return trimmed text
     * @throws ValidateException if null or blank
     */
    private static String text(final String value, final String label) {
        if (value == null || value.isBlank()) {
            throw new ValidateException(label + " must not be blank");
        }
        return value.trim();
    }

    /**
     * Validates a required reference.
     *
     * @param value reference to validate
     * @param label field label
     * @param <T>   reference type
     * @return validated reference
     * @throws ValidateException if null
     */
    private static <T> T required(final T value, final String label) {
        if (value == null) {
            throw new ValidateException(label + " must not be null");
        }
        return value;
    }

    /**
     * Returns the immutable security domain.
     *
     * @return non-null security domain
     */
    public Realm realm() {
        return realm;
    }

    /**
     * Returns the tenant identifier derived exclusively from the realm.
     *
     * @return stable non-blank tenant identifier
     */
    public String tenantId() {
        return realm.id();
    }

    /**
     * Returns the stable diagnostic correlation identifier.
     *
     * @return non-blank correlation identifier
     */
    public String correlationId() {
        return correlationId;
    }

    /**
     * Returns the instant sampled once when this context was built.
     *
     * @return creation instant
     */
    public Instant createdAt() {
        return createdAt;
    }

    /**
     * Returns the absolute deadline after the creation instant.
     *
     * @return operation deadline
     */
    public Instant deadline() {
        return deadline;
    }

    /**
     * Returns the original operation duration between creation and deadline.
     *
     * @return positive timeout duration
     */
    public Duration timeout() {
        return Duration.between(createdAt, deadline);
    }

    /**
     * Calculates time remaining against an explicit Fabric clock.
     *
     * @param clock current time source
     * @return non-negative remaining duration
     * @throws ValidateException if {@code clock} is null
     */
    public Duration remaining(final Clock clock) {
        final Duration remaining = Duration.between(required(clock, "Clock").now(), deadline);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    /**
     * Tests the deadline against an explicit Fabric clock.
     *
     * @param clock current time source
     * @return true at or after the deadline
     * @throws ValidateException if {@code clock} is null
     */
    public boolean expired(final Clock clock) {
        return !required(clock, "Clock").now().isBefore(deadline);
    }

    /**
     * Returns the selected provider and capability route.
     *
     * @return immutable route
     */
    public Route route() {
        return route;
    }

    /**
     * Returns the optional local protocol endpoint.
     *
     * @return optional local endpoint
     */
    public Optional<Endpoint> localEndpoint() {
        return Optional.ofNullable(localEndpoint);
    }

    /**
     * Returns the optional remote protocol endpoint.
     *
     * @return optional remote endpoint
     */
    public Optional<Endpoint> remoteEndpoint() {
        return Optional.ofNullable(remoteEndpoint);
    }

    /**
     * Returns the optional authenticated principal snapshot.
     *
     * @return optional principal
     */
    public Optional<Principal> principal() {
        return Optional.ofNullable(principal);
    }

    /**
     * Returns the immutable typed option snapshot.
     *
     * @return non-null options
     */
    public Options options() {
        return options;
    }

    /**
     * Looks up a typed option.
     *
     * @param key typed option key
     * @param <T> option value type
     * @return optional option value
     */
    public <T> Optional<T> option(final Options.Key<T> key) {
        return Optional.ofNullable(options.get(key));
    }

    /**
     * Returns a context routed to a provider capability.
     *
     * @param providerId stable provider identifier
     * @param capability selected capability
     * @return immutable routed context
     * @throws ValidateException if the provider identifier or capability is invalid
     */
    public Context route(final String providerId, final Capability capability) {
        return copy(new Route(providerId, capability), localEndpoint, remoteEndpoint, principal, options);
    }

    /**
     * Returns a context retaining the provider and selecting another capability.
     *
     * @param capability selected capability
     * @return immutable derived context
     * @throws ValidateException if {@code capability} is null
     */
    public Context derive(final Capability capability) {
        return route(route.providerId(), capability);
    }

    /**
     * Returns a context with replacement local and remote endpoints.
     *
     * @param local  optional local endpoint
     * @param remote optional remote endpoint
     * @return immutable context view
     */
    public Context endpoints(final Endpoint local, final Endpoint remote) {
        return copy(route, local, remote, principal, options);
    }

    /**
     * Returns a context with a replacement remote endpoint.
     *
     * @param remote optional remote endpoint
     * @return immutable context view
     */
    public Context remoteEndpoint(final Endpoint remote) {
        return copy(route, localEndpoint, remote, principal, options);
    }

    /**
     * Returns a context with an authenticated principal.
     *
     * @param value principal snapshot
     * @return immutable authenticated context
     * @throws ValidateException if {@code value} is null
     */
    public Context principal(final Principal value) {
        return copy(route, localEndpoint, remoteEndpoint, required(value, "Principal"), options);
    }

    /**
     * Returns a copy without an authenticated principal.
     *
     * @return immutable anonymous context
     */
    public Context withoutPrincipal() {
        return copy(route, localEndpoint, remoteEndpoint, null, options);
    }

    /**
     * Returns a context containing a typed option.
     *
     * @param key   typed option key
     * @param value option value
     * @param <T>   option value type
     * @return immutable updated context
     */
    public <T> Context with(final Options.Key<T> key, final T value) {
        return copy(route, localEndpoint, remoteEndpoint, principal, options.with(key, value));
    }

    /**
     * Returns a context without a typed option.
     *
     * @param key typed option key
     * @return immutable updated context
     */
    public Context without(final Options.Key<?> key) {
        return copy(route, localEndpoint, remoteEndpoint, principal, options.without(key));
    }

    /**
     * Copies this context while replacing mutable operation views.
     *
     * @param selectedRoute     replacement route
     * @param local             replacement local endpoint
     * @param remote            replacement remote endpoint
     * @param selectedPrincipal replacement principal
     * @param selectedOptions   replacement options
     * @return validated immutable copy
     */
    private Context copy(
            final Route selectedRoute,
            final Endpoint local,
            final Endpoint remote,
            final Principal selectedPrincipal,
            final Options selectedOptions) {
        return new Context(realm, correlationId, createdAt, deadline, selectedRoute, local, remote, selectedPrincipal,
                selectedOptions);
    }

    /**
     * Returns a redacted representation that omits identity and routing data.
     *
     * @return fixed redacted text
     */
    @Override
    public String toString() {
        return "Context[REDACTED]";
    }

    /**
     * Immutable provider routing selection.
     *
     * @param providerId stable provider identifier
     * @param capability selected authentication capability
     * @author Kimi Liu
     */
    public record Route(String providerId, Capability capability) {

        /**
         * Validates and creates a provider route.
         *
         * @throws ValidateException if a component is absent or blank
         */
        public Route {
            providerId = text(providerId, "Provider identifier");
            capability = required(capability, "Capability");
        }
    }

    /**
     * Mutable, single-thread-confined builder for an immutable operation context.
     *
     * @author Kimi Liu
     */
    public static final class Initial {

        /**
         * Explicit time source used exactly once during build.
         */
        private final Clock clock;

        /**
         * Required security domain.
         */
        private Realm realm;

        /**
         * Required stable correlation identifier.
         */
        private String correlationId;

        /**
         * Maximum duration of the operation.
         */
        private Duration timeout = Timeout.defaults().read();

        /**
         * Required initial provider route.
         */
        private Route route;

        /**
         * Optional local endpoint.
         */
        private Endpoint local;

        /**
         * Optional remote endpoint.
         */
        private Endpoint remote;

        /**
         * Optional authenticated principal.
         */
        private Principal principal;

        /**
         * Immutable option snapshot.
         */
        private Options options = Options.empty();

        /**
         * Creates a builder with an explicit operation clock.
         *
         * @param clock non-null operation clock
         */
        private Initial(final Clock clock) {
            this.clock = clock;
        }

        /**
         * Sets the required security domain.
         *
         * @param value security domain
         * @return this builder
         */
        public Initial realm(final Realm value) {
            realm = value;
            return this;
        }

        /**
         * Sets the required stable correlation identifier.
         *
         * @param value correlation identifier
         * @return this builder
         */
        public Initial correlation(final String value) {
            correlationId = value;
            return this;
        }

        /**
         * Sets the positive operation timeout.
         *
         * @param value timeout duration
         * @return this builder
         */
        public Initial timeout(final Duration value) {
            timeout = value;
            return this;
        }

        /**
         * Sets the required provider route.
         *
         * @param providerId provider identifier
         * @param capability selected capability
         * @return this builder
         * @throws ValidateException if either argument is invalid
         */
        public Initial route(final String providerId, final Capability capability) {
            route = new Route(providerId, capability);
            return this;
        }

        /**
         * Sets optional local and remote endpoints.
         *
         * @param localEndpoint  optional local endpoint
         * @param remoteEndpoint optional remote endpoint
         * @return this builder
         */
        public Initial endpoints(final Endpoint localEndpoint, final Endpoint remoteEndpoint) {
            local = localEndpoint;
            remote = remoteEndpoint;
            return this;
        }

        /**
         * Sets the optional authenticated principal snapshot.
         *
         * @param value optional principal
         * @return this builder
         */
        public Initial principal(final Principal value) {
            principal = value;
            return this;
        }

        /**
         * Sets the required immutable option snapshot.
         *
         * @param value non-null options
         * @return this builder
         */
        public Initial options(final Options value) {
            options = value;
            return this;
        }

        /**
         * Builds a context and samples the clock exactly once.
         *
         * @return immutable operation context
         * @throws ValidateException if required fields or timeout are invalid
         */
        public Context build() {
            if (timeout == null || timeout.isZero() || timeout.isNegative()) {
                throw new ValidateException("Context timeout must be positive");
            }
            final Instant created = clock.now();
            return new Context(realm, correlationId, created, created.plus(timeout), route, local, remote, principal,
                    options);
        }
    }

}
