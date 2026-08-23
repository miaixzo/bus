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

import java.util.Collection;
import java.util.Set;

import org.miaixz.bus.auth.source.protocol.ProtocolConnector;
import org.miaixz.bus.auth.source.protocol.ProtocolSuite;
import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorConnector;
import org.miaixz.bus.auth.source.vendor.VendorSuite;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.logger.Logger;

/**
 * Assembles every discovered Source connector through one visitor and freezes both registration families together.
 * <p>
 * A load operates on a fresh unpublished suite. Any discovery or connector failure therefore discards the complete
 * candidate rather than exposing partial assembly state. Protocol and Vendor registries retain their exact atomic
 * binding rules and remain independent after visitor dispatch.
 * </p>
 *
 * @author Kimi Liu
 */
public final class SourceSuite implements SourceConnector.Visitor {

    /**
     * Build-scoped protocol registry.
     */
    private final ProtocolSuite protocolSuite;

    /**
     * Build-scoped Vendor registry.
     */
    private final VendorSuite vendorSuite;

    /**
     * Creates an empty unified Source assembly suite.
     */
    public SourceSuite() {
        this.protocolSuite = new ProtocolSuite();
        this.vendorSuite = new VendorSuite();
    }

    /**
     * Discovers the single Source SPI and registers every visible protocol and Vendor connector.
     *
     * @return mutable suite containing the complete visible Source implementation set
     */
    public static SourceSuite load() {
        return load((Set<Vendor.Id>) null);
    }

    /**
     * Discovers every protocol connector and only the selected Vendor connectors from the single Source SPI.
     *
     * @param selected selected Vendor platform keys, or {@code null} for every visible Vendor
     * @return mutable suite containing the selected visible Source implementation set
     */
    public static SourceSuite load(final Set<Vendor.Id> selected) {
        Logger.info(
                true,
                "Auth",
                "Source suite assembly started: vendorSelection={}",
                selected == null ? "all" : selected.size());
        final SourceDiscovery.Discovered discovered = SourceDiscovery.load();
        final SourceSuite suite = new SourceSuite();
        for (ProtocolConnector connector : discovered.requireProtocols()) {
            connector.accept(suite);
        }
        for (VendorConnector connector : discovered.vendors(selected)) {
            connector.accept(suite);
        }
        Logger.info(
                false,
                "Auth",
                "Source suite assembly completed: protocols={}, vendors={}",
                discovered.protocols().size(),
                selected == null ? discovered.vendors().size() : selected.size());
        return suite;
    }

    /**
     * Discovers every protocol connector and only the selected Vendor connectors from the single Source SPI.
     *
     * @param selected selected Vendor platform keys
     * @return mutable suite containing the selected visible Source implementation set
     */
    public static SourceSuite load(final Vendor.Id... selected) {
        Assert.notNull(selected, "Vendor selection must not be null");
        return load(Set.of(selected));
    }

    /**
     * Registers one explicit Source connector through the same visitor used for SPI discovery.
     *
     * @param connector protocol or Vendor connector
     * @return this suite
     */
    public SourceSuite register(final SourceConnector<?, ?> connector) {
        Assert.notNull(connector, "Source connector must not be null").accept(this);
        return this;
    }

    /**
     * Registers explicit Source connectors in iteration order through the unified visitor.
     *
     * @param connectors protocol or Vendor connectors
     * @return this suite
     */
    public SourceSuite registerAll(final Collection<? extends SourceConnector<?, ?>> connectors) {
        Assert.notNull(connectors, "Source connector collection must not be null");
        for (SourceConnector<?, ?> connector : connectors) {
            register(Assert.notNull(connector, "Source connector must not be null"));
        }
        return this;
    }

    /**
     * Registers one protocol connector in the protocol registry branch.
     *
     * @param connector protocol connector
     */
    @Override
    public void visit(final ProtocolConnector connector) {
        protocolSuite.register(Assert.notNull(connector, "Protocol connector must not be null"));
    }

    /**
     * Registers one Vendor connector in the Vendor registry branch.
     *
     * @param connector Vendor connector
     */
    @Override
    public void visit(final VendorConnector connector) {
        vendorSuite.register(Assert.notNull(connector, "Vendor connector must not be null"));
    }

    /**
     * Freezes the complete protocol and Vendor registration state into one immutable Source aggregate.
     *
     * @return immutable Source aggregate
     */
    public SourceAggregate freeze() {
        final var protocols = protocolSuite.freeze();
        final var vendors = vendorSuite.freeze();
        Logger.info(
                false,
                "Auth",
                "Source suite frozen: protocolDrivers={}, protocolDescriptors={}, vendorDrivers={}, vendorDescriptors={}",
                protocols.drivers().size(),
                protocols.descriptors().size(),
                vendors.drivers().size(),
                vendors.descriptors().size());
        return new SourceAggregate(protocols, vendors);
    }

}
