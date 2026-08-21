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

import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.auth.Library;
import org.miaixz.bus.auth.Options;
import org.miaixz.bus.auth.Provider;
import org.miaixz.bus.auth.Registration;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.Source;
import org.miaixz.bus.auth.registry.SnapshotFault;
import org.miaixz.bus.auth.registry.SnapshotRegistry;
import org.miaixz.bus.auth.source.DriverDirectory;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.NotFoundException;

/**
 * Compiles a validated registration snapshot into one complete executable runtime container.
 * <p>
 * The compiler builds all indexes in local state and returns a container only after every enabled record compiles. A
 * failure therefore exposes no partial container and leaves atomic publication to the reload service. Complete Library
 * resources are indexed first, followed by protocol-neutral Provider resources and executable Source resources. Each
 * Source receives its resolved owning Provider and Library. This class does not access committed Registry state or
 * import protocol or Vendor implementations.
 * </p>
 *
 * @author Kimi Liu
 */
final class SnapshotCompiler {

    /**
     * Frozen Source driver index keyed by stable Source scheme identifier.
     */
    private final DriverDirectory sources;

    /**
     * Externally supplied execution services passed unchanged to selected drivers.
     */
    private final RuntimeServices services;

    /**
     * Creates a pure snapshot compiler.
     *
     * @param sources  frozen Source driver directory
     * @param services externally owned execution services
     * @throws IllegalArgumentException if a dependency is {@code null}
     */
    SnapshotCompiler(final DriverDirectory sources, final RuntimeServices services) {
        this.sources = Assert.notNull(sources, "Source driver directory must not be null");
        this.services = Assert.notNull(services, "Execution services must not be null");
    }

    /**
     * Indexes enabled complete Library resources in stable snapshot order.
     *
     * @param snapshot  validated complete snapshot
     * @param libraries mutable local Library index
     */
    private static void compileLibraries(final Registry.Snapshot snapshot, final Map<String, Library> libraries) {
        for (Registration.Entry record : snapshot.records()) {
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
    private static void indexProviders(
            final Registry.Snapshot snapshot,
            final Map<String, Library> libraries,
            final Map<String, Registration.ProviderEntry> providerRecords) {
        for (Registration.Entry candidate : snapshot.records()) {
            if (candidate.kind() != Registration.Kind.PROVIDER || !candidate.enabled()) {
                continue;
            }
            final Registration.ProviderEntry record = (Registration.ProviderEntry) candidate;
            final Provider provider = record.resource();
            final Library library = libraries.get(provider.getLibrary_id());
            if (library == null) {
                throw new CompilationFailure(Registration.Kind.PROVIDER, provider.getId(), "library_id",
                        "Enabled Provider could not resolve its Library");
            }
            providerRecords.put(provider.getId(), record);
        }
    }

    /**
     * Compiles every enabled record in a previously validated snapshot.
     *
     * @param snapshot complete snapshot that has passed {@code SnapshotValidator}
     * @return runtime container containing the Snapshot Registry and compiled Source workers
     * @throws IllegalArgumentException if the snapshot, a required type, or a driver result is invalid
     * @throws RuntimeException         if a required relationship or selected driver rejects a record
     */
    RuntimeContainer compile(final Registry.Snapshot snapshot) {
        Assert.notNull(snapshot, "Registry snapshot must not be null");
        final Map<String, Library> libraries = new LinkedHashMap<>();
        final Map<String, Registration.ProviderEntry> providerRecords = new LinkedHashMap<>();
        final Map<Registry.Reference, SourceWorker> workers = new LinkedHashMap<>();
        try {
            compileLibraries(snapshot, libraries);
            indexProviders(snapshot, libraries, providerRecords);
            compileSources(snapshot, libraries, providerRecords, workers);
            return new RuntimeContainer(new SnapshotRegistry(snapshot.revision(), snapshot), workers);
        } catch (RuntimeException failure) {
            close(workers);
            throw failure;
        }
    }

    /**
     * Best-effort closes workers already created before compilation failed.
     *
     * @param workers partially compiled worker index
     */
    private static void close(final Map<Registry.Reference, SourceWorker> workers) {
        for (SourceWorker worker : workers.values()) {
            try {
                worker.close();
            } catch (RuntimeException ignored) {
                // Compilation failure remains primary while every already-created worker is given a close attempt.
            }
        }
    }

    /**
     * Compiles enabled Source resources after resolving their required Provider and Library association.
     *
     * @param snapshot        validated complete snapshot
     * @param libraries       resolved enabled Library index
     * @param providerRecords resolved enabled Provider registration index
     * @param workers         mutable local Source-worker index
     */
    private void compileSources(
            final Registry.Snapshot snapshot,
            final Map<String, Library> libraries,
            final Map<String, Registration.ProviderEntry> providerRecords,
            final Map<Registry.Reference, SourceWorker> workers) {
        for (Registration.Entry candidate : snapshot.records()) {
            if (candidate.kind() != Registration.Kind.SOURCE || !candidate.enabled()) {
                continue;
            }
            final Registration.SourceEntry record = (Registration.SourceEntry) candidate;
            final Source source = record.resource();
            try {
                final SourceDriver<?> driver = sources.require(source.getType());
                final Registration.ProviderEntry providerRecord = providerRecords.get(source.getProvider_id());
                if (providerRecord == null) {
                    throw new NotFoundException("Enabled Source Provider was not indexed");
                }
                final Provider provider = providerRecord.resource();
                final Library library = libraries.get(provider.getLibrary_id());
                if (library == null) {
                    throw new NotFoundException("Enabled Source Library was not indexed");
                }
                final SourceWorker worker = compile(driver, record, provider, library);
                workers.put(Registry.Reference.source(source.getId()), worker);
            } catch (CompilationFailure failure) {
                throw failure;
            } catch (RuntimeException cause) {
                throw new CompilationFailure(Registration.Kind.SOURCE, source.getId(), "worker",
                        "Enabled Source worker could not be compiled", cause);
            }
        }
    }

    /**
     * Captures a wildcard driver and compiles from one exact preparation and its matching scoped services.
     *
     * @param <O>      concrete Source options type
     * @param driver   exact typed Source driver
     * @param record   Source registration
     * @param provider resolved Provider
     * @param library  resolved Library
     * @return compiled Source worker
     */
    private <O extends Options<?>> SourceWorker compile(
            final SourceDriver<O> driver,
            final Registration.SourceEntry record,
            final Provider provider,
            final Library library) {
        final SourceDriver.Prepared<O> prepared = driver.prepare(record, provider, library);
        return Assert.notNull(
                driver.compile(
                        prepared,
                        services.scope(prepared.registration(), prepared.slots(), prepared.dependencies())),
                "Source driver result must not be null");
    }

    /**
     * Carries safe entry coordinates from local compilation to the reload report boundary.
     */
    static final class CompilationFailure extends RuntimeException {

        /** Registration kind that failed compilation. */
        private final Registration.Kind kind;
        /** Safe registration identifier. */
        private final String id;
        /** Safe failing field name. */
        private final String field;
        /** Safe failure description exposed in reports. */
        private final String safeDescription;

        /**
         * Creates a compilation failure without an underlying cause.
         *
         * @param kind            registration kind
         * @param id              registration identifier
         * @param field           failing field
         * @param safeDescription safe description
         */
        CompilationFailure(final Registration.Kind kind, final String id, final String field,
                final String safeDescription) {
            this(kind, id, field, safeDescription, null);
        }

        /**
         * Creates a compilation failure retaining an internal cause.
         *
         * @param kind            registration kind
         * @param id              registration identifier
         * @param field           failing field
         * @param safeDescription safe description
         * @param cause           internal cause
         */
        CompilationFailure(final Registration.Kind kind, final String id, final String field,
                final String safeDescription, final Throwable cause) {
            super(safeDescription, cause);
            this.kind = Assert.notNull(kind, "Compilation failure kind must not be null");
            this.id = Assert.notBlank(id, "Compilation failure resource id must not be blank");
            this.field = Assert.notBlank(field, "Compilation failure field must not be blank");
            this.safeDescription = Assert
                    .notBlank(safeDescription, "Compilation failure safe description must not be blank");
        }

        /** {@return the safe structured snapshot fault} */
        SnapshotFault fault() {
            return SnapshotFault
                    .entry(kind, id, SnapshotFault.Stage.COMPILE, Optional.of(field), ErrorCode._500, safeDescription);
        }

    }

}
