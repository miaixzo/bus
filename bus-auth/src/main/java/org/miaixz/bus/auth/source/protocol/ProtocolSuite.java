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
package org.miaixz.bus.auth.source.protocol;

import java.util.*;

import org.miaixz.bus.auth.source.SourceDiscovery;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.logger.Logger;

/**
 * Registers protocol connectors atomically and freezes their declared drivers for runtime assembly.
 * <p>
 * This build-scoped implementation is package-owned so protocol extensions depend only on {@link ProtocolConnector} and
 * {@link ProtocolRegistry}. Each registration invokes the connector's connect callback against detached staging state
 * and commits only after the complete callback succeeds. The suite retains declared drivers, not connector instances,
 * and performs no protocol connection, Source loading, compilation, Roster access, or runtime mutation.
 * </p>
 *
 * @author Kimi Liu
 */
public class ProtocolSuite implements ProtocolRegistry {

    /**
     * Protocol registrations retained in deterministic registration order.
     */
    private final Map<Protocol, List<ProtocolDriver<?>>> registrations;

    /**
     * Scheme identifiers retained for cross-registration collision detection.
     */
    private final Set<String> schemes;

    /**
     * Protocol currently allowed to bind drivers during one connector callback.
     */
    private Protocol active;

    /**
     * Drivers emitted by the currently active connector callback.
     */
    private List<ProtocolDriver<?>> emitted;

    /**
     * Whether this suite has frozen its driver registrations.
     */
    private boolean frozen;

    /**
     * Creates an empty mutable protocol registration suite.
     */
    public ProtocolSuite() {
        this(new LinkedHashMap<>(), new LinkedHashSet<>());
    }

    /**
     * Creates a mutable staging suite from detached connector state.
     *
     * @param registrations detached protocol registration map
     * @param schemes       detached scheme identifier set
     */
    private ProtocolSuite(final Map<Protocol, List<ProtocolDriver<?>>> registrations, final Set<String> schemes) {
        this.registrations = registrations;
        this.schemes = schemes;
    }

    /**
     * Loads every visible protocol connector from the unified Source SPI in stable protocol order.
     *
     * @return mutable suite containing all visible protocol registrations
     * @throws ValidateException if no connector is visible or registrations conflict
     */
    public static ProtocolSuite load() {
        return (ProtocolSuite) new ProtocolSuite().registerAll(SourceDiscovery.load().requireProtocols());
    }

    /**
     * Binds one Source driver to the active protocol registration.
     *
     * @param driver Source driver to bind
     * @return this suite
     */
    @Override
    public synchronized ProtocolRegistry bind(final ProtocolDriver<?> driver) {
        return bindAll(List.of(Assert.notNull(driver, "Source driver must not be null")));
    }

    /**
     * Binds Source drivers to the active protocol registration in iteration order.
     *
     * @param drivers Source drivers to bind
     * @return this suite
     */
    @Override
    public synchronized ProtocolRegistry bindAll(final Collection<? extends ProtocolDriver<?>> drivers) {
        mutable();
        if (active == null || emitted == null) {
            throw new ValidateException("Protocol drivers may be bound only during a protocol connector callback");
        }
        Assert.notNull(drivers, "Source driver collection must not be null");
        for (ProtocolDriver<?> candidate : drivers) {
            final ProtocolDriver<?> driver = Assert.notNull(candidate, "Protocol driver must not be null");
            if (driver.protocol() != active) {
                throw new ValidateException("Source driver protocol does not match its connector key");
            }
            final String scheme = Assert.notBlank(driver.scheme().id(), "Source driver scheme id must not be blank");
            if (!schemes.add(scheme)) {
                throw new ValidateException("Duplicate Source driver scheme id: " + scheme);
            }
            emitted.add(driver);
        }
        return this;
    }

    /**
     * Atomically registers one protocol registration.
     *
     * @param connector protocol connector to register
     * @return this suite
     */
    @Override
    public synchronized ProtocolRegistry register(final ProtocolConnector connector) {
        return registerAll(List.of(Assert.notNull(connector, "Protocol connector must not be null")));
    }

    /**
     * Atomically registers all protocol registrations in iteration order.
     *
     * @param connectors protocol connectors to register
     * @return this suite
     */
    @Override
    public synchronized ProtocolRegistry registerAll(final Collection<? extends ProtocolConnector> connectors) {
        mutable();
        Assert.notNull(connectors, "Protocol connector collection must not be null");
        final ProtocolSuite staged = copy();
        for (ProtocolConnector candidate : connectors) {
            staged.apply(Assert.notNull(candidate, "Protocol connector must not be null"));
        }
        replace(staged);
        return this;
    }

    /**
     * Atomically removes one protocol registration.
     *
     * @param key protocol registration key
     * @return this suite
     */
    @Override
    public synchronized ProtocolRegistry unregister(final Protocol key) {
        return unregisterAll(List.of(Assert.notNull(key, "Protocol connector key must not be null")));
    }

    /**
     * Atomically removes all protocol registrations owned by the supplied keys.
     *
     * @param keys protocol registration keys
     * @return this suite
     */
    @Override
    public synchronized ProtocolRegistry unregisterAll(final Collection<? extends Protocol> keys) {
        mutable();
        Assert.notNull(keys, "Protocol connector key collection must not be null");
        final ProtocolSuite staged = copy();
        final Set<Protocol> requested = new LinkedHashSet<>();
        for (Protocol candidate : keys) {
            final Protocol key = Assert.notNull(candidate, "Protocol connector key must not be null");
            if (!requested.add(key)) {
                throw new ValidateException("Duplicate protocol connector removal key: " + key.name());
            }
            final List<ProtocolDriver<?>> removed = staged.registrations.remove(key);
            if (removed == null) {
                throw new ValidateException("Protocol connector is not registered: " + key.name());
            }
            for (ProtocolDriver<?> driver : removed) {
                staged.schemes.remove(driver.scheme().id());
            }
        }
        replace(staged);
        return this;
    }

    /**
     * Reports whether a protocol registration is currently registered.
     *
     * @param key protocol registration key
     * @return {@code true} when registered
     */
    @Override
    public synchronized boolean contains(final Protocol key) {
        return registrations.containsKey(Assert.notNull(key, "Protocol connector key must not be null"));
    }

    /**
     * Freezes this suite and returns all configured Source drivers in deterministic registration order.
     *
     * @return immutable Source driver list
     */
    public synchronized ProtocolModule freeze() {
        mutable();
        final List<ProtocolDriver<?>> drivers = new ArrayList<>();
        for (List<ProtocolDriver<?>> registration : registrations.values()) {
            drivers.addAll(registration);
        }
        frozen = true;
        Logger.info(
                false,
                "Auth",
                "Protocol registry frozen: protocols={}, drivers={}",
                registrations.size(),
                drivers.size());
        return new ProtocolModule(drivers);
    }

    /**
     * Applies one connector to this staging suite.
     *
     * @param connector checked connector
     */
    private void apply(final ProtocolConnector connector) {
        final Protocol key = Assert.notNull(connector.key(), "Protocol connector key must not be null");
        if (registrations.containsKey(key)) {
            throw new ValidateException("Duplicate protocol connector: " + key.name());
        }
        active = key;
        emitted = new ArrayList<>();
        try {
            connector.connect(this);
            if (emitted.isEmpty()) {
                throw new ValidateException("Protocol connector must bind at least one driver: " + key.name());
            }
            registrations.put(key, List.copyOf(emitted));
        } catch (RuntimeException cause) {
            Logger.error(
                    false,
                    "Auth",
                    cause,
                    "Protocol connector registration failed: key={}, connector={}, exception={}",
                    key,
                    connector.getClass().getName(),
                    cause.getClass().getSimpleName());
            throw cause;
        } finally {
            active = null;
            emitted = null;
        }
    }

    /**
     * Creates detached mutable staging state for an atomic operation.
     *
     * @return detached suite copy
     */
    private ProtocolSuite copy() {
        return new ProtocolSuite(new LinkedHashMap<>(registrations), new LinkedHashSet<>(schemes));
    }

    /**
     * Commits detached staging state after a complete successful operation.
     *
     * @param staged successfully mutated staging suite
     */
    private void replace(final ProtocolSuite staged) {
        registrations.clear();
        registrations.putAll(staged.registrations);
        schemes.clear();
        schemes.addAll(staged.schemes);
    }

    /**
     * Rejects mutation after this suite has frozen its driver registrations.
     */
    private void mutable() {
        if (frozen) {
            throw new ValidateException("Protocol registry is already frozen");
        }
    }

}
