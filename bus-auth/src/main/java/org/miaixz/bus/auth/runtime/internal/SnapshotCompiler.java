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
package org.miaixz.bus.auth.runtime.internal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.registry.internal.ImmutableRegistryView;
import org.miaixz.bus.auth.registry.spi.RegistryView;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.shared.internal.RuntimeProvider;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.AlreadyExistsException;
import org.miaixz.bus.core.lang.exception.NotFoundException;

/**
 * Compiles a validated registration snapshot into one immutable Registry view.
 * <p>
 * The compiler builds all indexes in local state and returns a view only after every enabled record compiles. A failure
 * therefore exposes no partial view and leaves atomic publication to the reload service. Complete Library resources are
 * indexed first, followed by protocol-neutral Provider resources and executable Source resources. Each Source receives
 * its resolved owning Provider and Library. This class does not access Registry state or import protocol or Vendor
 * implementations.
 * </p>
 *
 * @author Kimi Liu
 */
public final class SnapshotCompiler {

    /**
     * Frozen Source driver index keyed by stable Source profile identifier.
     */
    private final Map<String, SourceDriver<?>> sources;

    /**
     * Externally supplied execution services passed unchanged to selected drivers.
     */
    private final ExecutionServices services;

    /**
     * Creates a pure snapshot compiler.
     *
     * @param sources  complete Source driver list
     * @param services externally owned execution services
     * @throws IllegalArgumentException if a dependency is {@code null}
     */
    public SnapshotCompiler(final List<SourceDriver<?>> sources, final ExecutionServices services) {
        this.sources = sourceIndex(sources);
        this.services = Assert.notNull(services, "Execution services must not be null");
    }

    /**
     * Builds the immutable Source driver index while rejecting duplicate registration types.
     *
     * @param drivers complete Source driver list
     * @return immutable Source driver index
     */
    private static Map<String, SourceDriver<?>> sourceIndex(final List<SourceDriver<?>> drivers) {
        Assert.notNull(drivers, "Source drivers must not be null");
        final Map<String, SourceDriver<?>> index = new LinkedHashMap<>();
        for (SourceDriver<?> driver : drivers) {
            final SourceDriver<?> checked = Assert.notNull(driver, "Source driver must not be null");
            final String type = Assert.notBlank(checked.profile().id(), "Source driver profile id must not be blank");
            if (index.putIfAbsent(type, checked) != null) {
                throw new AlreadyExistsException("Duplicate Source driver type: " + type);
            }
        }
        return Map.copyOf(index);
    }

    /**
     * Indexes enabled complete Library resources in stable snapshot order.
     *
     * @param snapshot  validated complete snapshot
     * @param libraries mutable local Library index
     */
    private static void compileLibraries(final Registry.Snapshot snapshot, final Map<String, Library> libraries) {
        for (Registration.Record<?> record : snapshot.records()) {
            if (record.kind() == Registration.Kind.LIBRARY && record.enabled()) {
                final Library library = (Library) record.resource();
                libraries.put(library.getId(), library);
            }
        }
    }

    /**
     * Indexes enabled protocol-neutral Provider resources after resolving their required Library.
     *
     * @param snapshot        validated complete snapshot
     * @param libraries       resolved enabled Library index
     * @param providerRecords mutable enabled Provider registration index
     */
    @SuppressWarnings("unchecked")
    private static void indexProviders(
            final Registry.Snapshot snapshot,
            final Map<String, Library> libraries,
            final Map<String, Registration.Record<Provider>> providerRecords) {
        for (Registration.Record<?> candidate : snapshot.records()) {
            if (candidate.kind() != Registration.Kind.PROVIDER || !candidate.enabled()) {
                continue;
            }
            final Registration.Record<Provider> record = (Registration.Record<Provider>) candidate;
            final Provider provider = record.resource();
            final Library library = libraries.get(provider.getLibrary_id());
            if (library == null) {
                throw new NotFoundException("Enabled Provider Library not found: " + provider.getLibrary_id());
            }
            providerRecords.put(provider.getId(), record);
        }
    }

    /**
     * Compiles every enabled record in a previously validated snapshot.
     *
     * @param snapshot complete snapshot that has passed {@code RegistrationValidator}
     * @return structurally immutable Registry view with the same revision and full registration snapshot
     * @throws IllegalArgumentException if the snapshot, a required type, or a driver result is invalid
     * @throws RuntimeException         if Library decoding or a selected driver rejects a record
     */
    public RegistryView compile(final Registry.Snapshot snapshot) {
        Assert.notNull(snapshot, "Registry snapshot must not be null");
        final Map<String, Library> libraries = new LinkedHashMap<>();
        final Map<String, Registration.Record<Provider>> providerRecords = new LinkedHashMap<>();
        final Map<Registry.Reference, RuntimeProvider> runtimes = new LinkedHashMap<>();
        compileLibraries(snapshot, libraries);
        indexProviders(snapshot, libraries, providerRecords);
        compileSources(snapshot, libraries, providerRecords, runtimes);
        return new ImmutableRegistryView(snapshot.revision(), snapshot, libraries, runtimes);
    }

    /**
     * Compiles enabled Source resources after resolving their required Provider and Library association.
     *
     * @param snapshot        validated complete snapshot
     * @param libraries       resolved enabled Library index
     * @param providerRecords resolved enabled Provider registration index
     * @param runtimes        mutable local runtime index
     */
    @SuppressWarnings("unchecked")
    private void compileSources(
            final Registry.Snapshot snapshot,
            final Map<String, Library> libraries,
            final Map<String, Registration.Record<Provider>> providerRecords,
            final Map<Registry.Reference, RuntimeProvider> runtimes) {
        for (Registration.Record<?> candidate : snapshot.records()) {
            if (candidate.kind() != Registration.Kind.SOURCE || !candidate.enabled()) {
                continue;
            }
            final Registration.Record<Source> record = (Registration.Record<Source>) candidate;
            final Source source = record.resource();
            final SourceDriver<?> driver = sources.get(source.getType());
            if (driver == null) {
                throw new NotFoundException("Source driver not found: " + source.getType());
            }
            final Registration.Record<Provider> providerRecord = providerRecords.get(source.getProvider_id());
            if (providerRecord == null) {
                throw new NotFoundException("Enabled Source Provider not found: " + source.getProvider_id());
            }
            final Provider provider = providerRecord.resource();
            final Library library = libraries.get(provider.getLibrary_id());
            if (library == null) {
                throw new NotFoundException("Enabled Source Library not found: " + provider.getLibrary_id());
            }
            final RuntimeProvider runtime = Assert.notNull(
                    driver.compile(record, provider, library, services),
                    "Source driver result must not be null");
            runtimes.put(Registry.Reference.source(source.getId()), runtime);
        }
    }

}
