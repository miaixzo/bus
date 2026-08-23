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

import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.source.protocol.ProtocolConnector;
import org.miaixz.bus.auth.source.vendor.VendorConnector;

/**
 * Defines the single SPI boundary for every build-time Source registration.
 * <p>
 * The sealed root admits only the protocol and Vendor registration families, while their non-sealed child contracts
 * remain open to external implementations. Discovery uses this root as its only Source SPI service type.
 * {@link #accept(Visitor)} performs type-safe family dispatch, and the inherited typed
 * {@link Registry.Connector#connect(Registry)} callback declares the registration contents inside the selected
 * registry.
 * </p>
 *
 * @param <K> stable registration key type
 * @param <R> exact build-scoped registry accepted by this connector
 * @author Kimi Liu
 */
public sealed interface SourceConnector<K, R extends Registry<K, ?>> extends Registry.Connector<K, R>
        permits ProtocolConnector, VendorConnector {

    /**
     * Dispatches this connector to its exact Source registration family.
     *
     * @param visitor unified Source connector visitor
     */
    void accept(Visitor visitor);

    /**
     * Receives the two supported Source connector families without runtime type inspection.
     *
     * @author Kimi Liu
     */
    interface Visitor {

        /**
         * Visits one protocol registration.
         *
         * @param connector protocol connector
         */
        void visit(ProtocolConnector connector);

        /**
         * Visits one Vendor platform registration.
         *
         * @param connector Vendor connector
         */
        void visit(VendorConnector connector);

    }

}
