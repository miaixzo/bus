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

import java.util.Collection;

import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.core.net.Protocol;

/**
 * Collects protocol-owned Source drivers while one {@link ProtocolConnector} is being applied.
 * <p>
 * Driver binding is valid only during the connect callback of a registered {@link ProtocolConnector}. Connector
 * registration and removal remain available only until this build-scoped registry is frozen; neither operation mutates
 * runtime Roster state.
 * </p>
 *
 * @author Kimi Liu
 */
public interface ProtocolRegistry extends Registry<Protocol, ProtocolConnector> {

    /**
     * Binds one Source driver to the currently active protocol registration.
     *
     * @param driver Source driver to bind
     * @return this registry
     */
    ProtocolRegistry bind(ProtocolDriver<?> driver);

    /**
     * Binds Source drivers to the currently active protocol registration in iteration order.
     *
     * @param drivers Source drivers to bind
     * @return this registry
     */
    ProtocolRegistry bindAll(Collection<? extends ProtocolDriver<?>> drivers);

    /**
     * Registers one complete protocol registration.
     *
     * @param connector protocol connector to register
     * @return this registry
     */
    @Override
    ProtocolRegistry register(ProtocolConnector connector);

    /**
     * Atomically registers all supplied protocol registrations.
     *
     * @param connectors protocol connectors to register
     * @return this registry
     */
    @Override
    ProtocolRegistry registerAll(Collection<? extends ProtocolConnector> connectors);

    /**
     * Removes the complete protocol registration owned by one key.
     *
     * @param key protocol registration key
     * @return this registry
     */
    @Override
    ProtocolRegistry unregister(Protocol key);

    /**
     * Atomically removes all protocol registrations owned by the supplied keys.
     *
     * @param keys protocol registration keys
     * @return this registry
     */
    @Override
    ProtocolRegistry unregisterAll(Collection<? extends Protocol> keys);

}
