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
package org.miaixz.bus.auth.runtime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.protocol.Conformance;
import org.miaixz.bus.auth.source.DriverDirectory;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorDirectory;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.AlreadyExistsException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Describes exactly which authentication schemes and Vendor variants were assembled into one runtime.
 * <p>
 * The descriptor is startup metadata only. It does not read Registry registrations, decide which Source is enabled,
 * compile workers, load project data, or execute a capability.
 * </p>
 *
 * @author Kimi Liu
 */
public class RuntimeDescriptor {

    /**
     * Schemes in deterministic runtime assembly order.
     */
    private final List<SchemeDescriptor> schemes;
    /**
     * Exact scheme identifier index over {@link #schemes}.
     */
    private final Map<String, SchemeDescriptor> schemesById;
    /**
     * Vendor manifests in deterministic module order.
     */
    private final List<VendorDescriptor> vendors;
    /**
     * Exact Vendor identifier index over {@link #vendors}.
     */
    private final Map<Vendor.Id, VendorDescriptor> vendorsById;

    /**
     * Freezes one runtime's implementation inventory.
     *
     * @param drivers complete selected Source drivers
     * @param vendors selected Vendor directory, or empty when Vendor support is not assembled
     */
    public RuntimeDescriptor(final DriverDirectory drivers, final Optional<VendorDirectory> vendors) {
        Assert.notNull(drivers, "Runtime descriptor drivers must not be null");
        final List<SchemeDescriptor> ordered = new ArrayList<>(drivers.drivers().size());
        final Map<String, SchemeDescriptor> indexed = new LinkedHashMap<>(drivers.drivers().size());
        for (SourceDriver<?> driver : drivers.drivers()) {
            final Scheme<?> scheme = Assert.notNull(
                    Assert.notNull(driver, "Runtime descriptor driver must not be null").scheme(),
                    "Runtime descriptor scheme must not be null");
            final String id = Assert.notBlank(scheme.id(), "Runtime descriptor scheme id must not be blank");
            final SchemeDescriptor descriptor = new SchemeDescriptor(id, scheme.protocol(), scheme.protocols(),
                    scheme.manifest(), scheme.conformance(), scheme.form());
            if (indexed.putIfAbsent(id, descriptor) != null) {
                throw new AlreadyExistsException("Duplicate runtime descriptor scheme: " + id);
            }
            ordered.add(descriptor);
        }
        this.schemes = List.copyOf(ordered);
        this.schemesById = Map.copyOf(indexed);
        Assert.notNull(vendors, "Runtime descriptor Vendor container must not be null");
        final List<VendorDescriptor> vendorList = new ArrayList<>();
        final Map<Vendor.Id, VendorDescriptor> vendorIndex = new LinkedHashMap<>();
        final VendorDirectory directory = vendors.getOrNull();
        if (directory != null) {
            for (VariantManifest<?> manifest : directory.manifests()) {
                final VendorDescriptor descriptor = new VendorDescriptor(manifest.vendor(), manifest.metadata(),
                        manifest.form(), manifest.variants());
                vendorList.add(descriptor);
                vendorIndex.put(descriptor.id(), descriptor);
            }
        }
        this.vendors = List.copyOf(vendorList);
        this.vendorsById = Map.copyOf(vendorIndex);
    }

    /**
     * Returns schemes in deterministic runtime assembly order.
     *
     * @return immutable assembled scheme descriptors
     */
    public List<SchemeDescriptor> schemes() {
        return schemes;
    }

    /**
     * Finds one assembled scheme by its exact Source type identifier.
     *
     * @param id exact Source type identifier
     * @return optional assembled scheme descriptor
     */
    public Optional<SchemeDescriptor> scheme(final String id) {
        Assert.notBlank(id, "Runtime descriptor scheme id must not be blank");
        return Optional.ofNullable(schemesById.get(id));
    }

    /**
     * Returns every assembled Vendor manifest in deterministic module order.
     *
     * @return immutable assembled Vendor descriptors
     */
    public List<VendorDescriptor> vendors() {
        return vendors;
    }

    /**
     * Finds one assembled Vendor manifest.
     *
     * @param id Vendor platform identifier
     * @return optional assembled Vendor descriptor
     */
    public Optional<VendorDescriptor> vendor(final Vendor.Id id) {
        Assert.notNull(id, "Vendor id must not be null");
        return Optional.ofNullable(vendorsById.get(id));
    }

    /**
     * Finds one exact assembled Vendor variant without selecting a configured Source.
     *
     * @param id      Vendor platform identifier
     * @param variant platform-specific variant identifier
     * @return optional exact assembled variant
     */
    public Optional<VariantManifest.Variant> variant(final Vendor.Id id, final Vendor.Variant variant) {
        Assert.notNull(id, "Vendor id must not be null");
        Assert.notNull(variant, "Vendor variant must not be null");
        final VendorDescriptor descriptor = vendorsById.get(id);
        if (descriptor == null) {
            return Optional.empty();
        }
        for (VariantManifest.Variant candidate : descriptor.variants()) {
            if (candidate.variant().equals(variant)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    /**
     * Immutable runtime projection of one assembled scheme.
     *
     * @param id          exact Source type identifier
     * @param protocol    primary transport protocol
     * @param protocols   all transport protocols accepted by the scheme
     * @param manifest    capabilities exposed by the assembled scheme
     * @param conformance optional protocol conformance metadata
     * @param form        configuration form metadata
     *
     * @author Kimi Liu
     */
    public record SchemeDescriptor(String id, Protocol protocol, Set<Protocol> protocols, Capability.Manifest manifest,
            Optional<Conformance> conformance, Scheme.Form form) {

        /**
         * Validates and freezes one assembled scheme projection.
         */
        public SchemeDescriptor {
            Assert.notBlank(id, "Scheme descriptor id must not be blank");
            Assert.notNull(protocol, "Scheme descriptor protocol must not be null");
            protocols = Set.copyOf(protocols);
            Assert.notNull(manifest, "Scheme descriptor capabilities must not be null");
            Assert.notNull(conformance, "Scheme descriptor conformance container must not be null");
            Assert.notNull(form, "Scheme descriptor form must not be null");
        }

    }

    /**
     * Immutable runtime projection of one assembled Vendor manifest.
     *
     * @param id       Vendor platform identifier
     * @param metadata Vendor display and classification metadata
     * @param form     common Vendor configuration form
     * @param variants immutable supported platform variants
     *
     * @author Kimi Liu
     */
    public record VendorDescriptor(Vendor.Id id, Vendor.Metadata metadata, Scheme.Form form,
            List<VariantManifest.Variant> variants) {

        /**
         * Validates and freezes one assembled Vendor manifest projection.
         */
        public VendorDescriptor {
            Assert.notNull(id, "Vendor descriptor id must not be null");
            Assert.notNull(metadata, "Vendor descriptor metadata must not be null");
            Assert.notNull(form, "Vendor descriptor form must not be null");
            variants = List.copyOf(variants);
        }

    }

}
