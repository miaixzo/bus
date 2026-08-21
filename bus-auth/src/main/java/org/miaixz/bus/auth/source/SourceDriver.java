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
package org.miaixz.bus.auth.source;

import java.util.Set;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.auth.worker.WorkerSlots;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Binds one protocol or Vendor scheme to the only factory capable of compiling matching Source registrations.
 * <p>
 * A driver is an immutable startup input. It does not register itself, call runtime assembly, load external data, or
 * access published Registry state.
 * </p>
 *
 * @param <O> exact immutable Source options type
 * @author Kimi Liu
 */
public interface SourceDriver<O extends Options<?>> {

    /**
     * Returns the management and capability scheme owned by this driver.
     *
     * @return exact Source scheme
     */
    Scheme<O> scheme();

    /**
     * Returns the Bus protocol owned by this driver.
     *
     * @return exact protocol or Vendor classification for the Source registration
     */
    default Protocol protocol() {
        return scheme().protocol();
    }

    /**
     * Reports whether this driver supports the actual protocol declared by a Source.
     *
     * @param protocol persisted protocol identifier
     * @return {@code true} when this driver accepts the protocol
     */
    default boolean supports(final String protocol) {
        if (protocol == null) {
            return false;
        }
        for (Protocol accepted : scheme().protocols()) {
            if (accepted.name().equalsIgnoreCase(protocol)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Requires and narrows the exact immutable options value accepted by this driver.
     *
     * @param options candidate options value
     * @return options narrowed to this driver's exact type
     * @throws org.miaixz.bus.core.lang.exception.ValidateException if the options type does not match this driver
     */
    O require(Options<?> options);

    /**
     * Validates Source-specific options and routing and returns the exact value accepted by this driver.
     * <p>
     * Registry validation may ignore the returned value, while {@link #prepare} retains it. Combining validation and
     * narrowing prevents one preparation from independently narrowing the same mutable boundary twice.
     * </p>
     *
     * @param source complete Source registration
     * @return exact validated options value
     */
    default O validate(final Source source) {
        return require(Assert.notNull(source, "Source registration must not be null").getOptions());
    }

    /**
     * Returns the project integration slots required by one already narrowed Source options value.
     * <p>
     * Runtime calls this method only from {@link #prepare(Registration.SourceEntry, Provider, Library)} and retains the
     * result with the exact options instance. Compilation therefore cannot independently select another requirement set
     * for the same registration.
     * </p>
     *
     * @param source  validated Source entity
     * @param options exact narrowed immutable options retained in the preparation
     * @return immutable Worker slots
     */
    default WorkerSlots slots(final Source source, final O options) {
        return WorkerSlots.none();
    }

    /**
     * Returns the framework infrastructure and protocol-state services required by the prepared Source.
     *
     * @param source  validated Source entity
     * @param options exact narrowed immutable options retained in the preparation
     * @return immutable framework dependency set
     */
    default Dependencies dependencies(final Source source, final O options) {
        return Dependencies.none();
    }

    /**
     * Validates and freezes all common Source compilation inputs and project-port requirements exactly once.
     *
     * @param registration complete Source registration
     * @param provider     resolved owning Provider
     * @param library      Library resolved through the owning Provider
     * @return immutable preparation consumed by compilation
     */
    default Prepared<O> prepare(
            final Registration.SourceEntry registration,
            final Provider provider,
            final Library library) {
        final Registration.SourceEntry record = Assert.notNull(registration, "Source registration must not be null");
        final Provider owner = Assert.notNull(provider, "Source Provider must not be null");
        final Library application = Assert.notNull(library, "Source Library must not be null");
        final Source source = Assert.notNull(record.resource(), "Registered Source must not be null");
        if (!scheme().id().equals(source.getType()) || !supports(source.getProtocol())
                || !owner.getId().equals(source.getProvider_id())
                || !application.getId().equals(owner.getLibrary_id())) {
            throw new ValidateException("Source registration does not match its driver, Provider, or Library");
        }
        final O options = Assert.notNull(validate(source), "Validated Source options must not be null");
        return new Prepared<>(record, owner, application, options,
                Assert.notNull(slots(source, options), "Source Worker slots must not be null"),
                Assert.notNull(dependencies(source, options), "Source framework dependencies must not be null"));
    }

    /**
     * Compiles one prepared complete Source registration with its exact retained requirements.
     *
     * @param prepared immutable preparation produced by this driver
     * @param services scoped externally supplied execution services
     * @return immutable executable Registry entry
     * @throws IllegalArgumentException if an argument does not match this driver
     */
    SourceWorker compile(Prepared<O> prepared, DriverServices services);

    /**
     * Freezes one driver's exact registration graph, narrowed options, and project integration requirements.
     *
     * @param registration complete registered Source
     * @param provider     resolved owning Provider
     * @param library      resolved owning Library
     * @param options      exact immutable narrowed options
     * @param slots        exact project integration requirements
     * @param dependencies exact framework infrastructure and state requirements
     * @param <O>          options type
     */
    record Prepared<O extends Options<?>>(Registration.SourceEntry registration, Provider provider, Library library,
            O options, WorkerSlots slots, Dependencies dependencies) {

        /**
         * Creates a complete immutable driver preparation.
         */
        public Prepared {
            Assert.notNull(registration, "Prepared Source registration must not be null");
            Assert.notNull(provider, "Prepared Source Provider must not be null");
            Assert.notNull(library, "Prepared Source Library must not be null");
            Assert.notNull(options, "Prepared Source options must not be null");
            Assert.notNull(slots, "Prepared Source Worker slots must not be null");
            Assert.notNull(dependencies, "Prepared Source framework dependencies must not be null");
        }

    }

    /**
     * Declares the exact framework-owned services visible to one compiled Source.
     *
     * @param values required framework services
     */
    record Dependencies(Set<Service> values) {

        /**
         * Creates an immutable dependency set.
         */
        public Dependencies {
            Assert.notNull(values, "Source framework dependency set must not be null");
            values = Set.copyOf(values);
        }

        /**
         * Returns an empty dependency set.
         *
         * @return empty dependency set
         */
        public static Dependencies none() {
            return new Dependencies(Set.of());
        }

        /**
         * Creates an immutable dependency set from exact services.
         *
         * @param services exact framework services
         * @return immutable dependency set
         */
        public static Dependencies of(final Service... services) {
            Assert.notNull(services, "Source framework services must not be null");
            return new Dependencies(Set.of(services));
        }

        /**
         * Returns whether one framework service was declared.
         *
         * @param service framework service
         * @return whether declared
         */
        public boolean contains(final Service service) {
            return values.contains(Assert.notNull(service, "Source framework service must not be null"));
        }

        /**
         * Identifies one framework-owned infrastructure or protocol-state service.
         */
        public enum Service {
            /** Shared Fabric execution context. */
            FABRIC_CONTEXT,
            /** Provider-neutral JSON implementation. */
            JSON_PROVIDER,
            /** Asynchronous Source executor. */
            EXECUTOR,
            /** Callback-state cache. */
            STATE_CACHE,
            /** One-time nonce cache. */
            NONCE_CACHE,
            /** Authorization-code cache. */
            AUTHORIZATION_CODE_CACHE,
            /** Device-code cache. */
            DEVICE_CODE_CACHE,
            /** Authorization lifecycle cache. */
            AUTHORIZATION_CACHE,
            /** Access-token cache. */
            ACCESS_TOKEN_CACHE,
            /** Refresh-token cache. */
            REFRESH_TOKEN_CACHE,
            /** Authentication Session cache. */
            SESSION_CACHE,
            /** Replay-prevention cache. */
            REPLAY_CACHE,
            /** Runtime security baseline. */
            SECURITY_BASELINE

        }

    }

}
