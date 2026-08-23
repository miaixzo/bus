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

import java.util.*;

import org.miaixz.bus.auth.source.protocol.ProtocolConnector;
import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorConnector;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.logger.Logger;

/**
 * Discovers the single {@link SourceConnector} SPI and returns its strongly typed registration families.
 * <p>
 * This is the only class in bus-auth that loads Source connector services. The visitor performs family dispatch without
 * {@code instanceof}, and each family is sorted by its own stable key before any registry mutation occurs.
 * </p>
 *
 * @author Kimi Liu
 */
public final class SourceDiscovery {

    /**
     * Prevents construction of the unified discovery utility.
     */
    private SourceDiscovery() {

    }

    /**
     * Loads and classifies every visible Source connector from the unified SPI.
     *
     * @return immutable discovered connector families
     * @throws ValidateException if no Source connector is visible
     */
    public static Discovered load() {
        Logger.debug(true, "Auth", "Source connector discovery started");
        try {
            final Collector collector = new Collector();
            for (SourceConnector connector : services()) {
                Assert.notNull(connector, "Source connector must not be null").accept(collector);
            }
            final Discovered discovered = collector.freeze();
            Logger.info(
                    false,
                    "Auth",
                    "Source connector discovery completed: connectors={}, protocols={}, vendors={}",
                    discovered.protocols().size() + discovered.vendors().size(),
                    discovered.protocols().size(),
                    discovered.vendors().size());
            return discovered;
        } catch (RuntimeException cause) {
            Logger.error(
                    false,
                    "Auth",
                    cause,
                    "Source connector discovery failed: exception={}",
                    cause.getClass().getSimpleName());
            throw cause;
        }
    }

    /**
     * Loads the reifiable raw generic service type at the single SPI boundary.
     *
     * @return visible Source connector services
     */
    private static Iterable<SourceConnector> services() {
        return NormalSpiLoader.loadList(SourceConnector.class);
    }

    /**
     * Collects visitor callbacks before producing a deterministic immutable discovery snapshot.
     *
     * @author Kimi Liu
     */
    private static final class Collector implements SourceConnector.Visitor {

        /**
         * Discovered protocol connectors.
         */
        private final List<ProtocolConnector> protocols = new ArrayList<>();

        /**
         * Discovered Vendor connectors.
         */
        private final List<VendorConnector> vendors = new ArrayList<>();

        /**
         * Collects one protocol connector.
         *
         * @param connector protocol connector
         */
        @Override
        public void visit(final ProtocolConnector connector) {
            protocols.add(Assert.notNull(connector, "Protocol connector must not be null"));
        }

        /**
         * Collects one Vendor connector.
         *
         * @param connector Vendor connector
         */
        @Override
        public void visit(final VendorConnector connector) {
            vendors.add(Assert.notNull(connector, "Vendor connector must not be null"));
        }

        /**
         * Freezes both families in their stable key order.
         *
         * @return immutable discovery snapshot
         */
        private Discovered freeze() {
            if (protocols.isEmpty() && vendors.isEmpty()) {
                throw new ValidateException("No Source connectors were discovered");
            }
            protocols.sort(Comparator.comparing(connector -> connector.key().name()));
            vendors.sort(Comparator.comparing(connector -> connector.key().value()));
            return new Discovered(protocols, vendors);
        }

    }

    /**
     * Stores the immutable typed result of one unified Source SPI discovery.
     *
     * @param protocols protocol connectors in stable protocol order
     * @param vendors   Vendor connectors in stable platform order
     * @author Kimi Liu
     */
    public record Discovered(List<ProtocolConnector> protocols, List<VendorConnector> vendors) {

        /**
         * Validates and detaches both connector families.
         */
        public Discovered {
            Assert.notNull(protocols, "Discovered protocol connectors must not be null");
            Assert.notNull(vendors, "Discovered Vendor connectors must not be null");
            protocols = List.copyOf(protocols);
            vendors = List.copyOf(vendors);
        }

        /**
         * Requires at least one visible protocol connector.
         *
         * @return immutable protocol connector list
         */
        public List<ProtocolConnector> requireProtocols() {
            if (protocols.isEmpty()) {
                throw new ValidateException("No protocol connectors were discovered");
            }
            return protocols;
        }

        /**
         * Selects Vendor connectors by stable platform id while retaining discovery order.
         *
         * @param selected selected platform keys, or {@code null} for every visible Vendor
         * @return immutable selected Vendor connector list
         */
        public List<VendorConnector> vendors(final Set<Vendor.Id> selected) {
            if (vendors.isEmpty()) {
                throw new ValidateException("No Vendor connectors were discovered");
            }
            if (selected == null) {
                return vendors;
            }
            final Set<Vendor.Id> requested = new LinkedHashSet<>();
            for (Vendor.Id candidate : selected) {
                requested.add(Assert.notNull(candidate, "Selected Vendor id must not be null"));
            }
            final List<VendorConnector> filtered = new ArrayList<>();
            for (VendorConnector connector : vendors) {
                if (requested.remove(Assert.notNull(connector.key(), "Vendor connector key must not be null"))) {
                    filtered.add(connector);
                }
            }
            if (!requested.isEmpty()) {
                throw new ValidateException("Unknown Vendor connector selection");
            }
            return List.copyOf(filtered);
        }

    }

}
