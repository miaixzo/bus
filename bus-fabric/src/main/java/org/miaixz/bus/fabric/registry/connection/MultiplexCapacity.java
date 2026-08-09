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
package org.miaixz.bus.fabric.registry.connection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Destination;

/**
 * Shared state for multiplex candidates and protocol-published logical capacity.
 * <p>
 * The connection pool retains its existing single synchronization boundary; this state holder does not introduce a lock
 * or duplicate HTTP/2 stream accounting.
 *
 * @author Kimi Liu
 */
final class MultiplexCapacity {

    /**
     * Active multiplex candidates by destination.
     */
    final Map<Destination, ArrayDeque<Connection>> candidates = new LinkedHashMap<>();

    /**
     * Destinations proven to negotiate HTTP/1.
     */
    final Set<Destination> http1Destinations = ConcurrentHashMap.newKeySet();

    /**
     * Destinations proven to negotiate a multiplex protocol.
     */
    final Set<Destination> multiplexDestinations = ConcurrentHashMap.newKeySet();

    /**
     * Listener registrations keyed by physical connection identity.
     */
    final Map<Connection, Connection.Registration> registrations = new IdentityHashMap<>();

}
