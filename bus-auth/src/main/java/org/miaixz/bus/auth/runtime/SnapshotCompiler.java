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

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.Scheme.Options;
import org.miaixz.bus.auth.registry.SnapshotRoster;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.source.SourceLookup;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.NotFoundException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Compiles a validated Blueprint snapshot into one complete executable runtime container.
 * <p>
 * The compiler builds all indexes in local state and returns a container only after every enabled entry compiles. A
 * failure therefore exposes no partial container and leaves atomic publication to the reload service. Complete Library
 * resources are indexed first, followed by protocol-neutral Provider resources and executable Source resources. Each
 * Source receives its resolved owning Provider and Library. The complete snapshot revision is also bound to every
 * Source-scoped protocol-state cache as its generation. This class does not access committed Roster state or import
 * protocol or Vendor implementations.
 * </p>
 *
 * @author Kimi Liu
 */
final class SnapshotCompiler {

    /**
     * Frozen Source driver index keyed by stable Source scheme identifier.
     */
    private final SourceLookup sourceLookup;
    /**
     * Externally supplied runtime services used to create each capability-limited Source service view.
     */
    private final RuntimeServices runtimeServices;

    /**
     * Creates a pure snapshot compiler.
     *
     * @param sourceLookup    frozen Source lookup
     * @param runtimeServices externally owned runtime services
     * @throws IllegalArgumentException if a dependency is {@code null}
     */
    SnapshotCompiler(final SourceLookup sourceLookup, final RuntimeServices runtimeServices) {
        this.sourceLookup = Assert.notNull(sourceLookup, "Source lookup must not be null");
        this.runtimeServices = Assert.notNull(runtimeServices, "Runtime services must not be null");
    }

    /**
     * Retains an explicitly safe validation description while hiding operational exception details.
     *
     * @param cause Source compilation failure
     * @return non-sensitive structured fault description
     */
    private static String compilationDescription(final RuntimeException cause) {
        if (cause instanceof ValidateException && StringKit.isNotBlank(cause.getMessage())) {
            return "Enabled Source worker could not be compiled: " + cause.getMessage();
        }
        return "Enabled Source worker could not be compiled";
    }

    /**
     * Indexes enabled complete Library resources in stable snapshot order.
     *
     * @param snapshot  validated complete snapshot
     * @param libraries mutable local Library index
     */
    private static void compileLibraries(final Roster.Snapshot snapshot, final Map<String, Library> libraries) {
        for (Blueprint.Entry entry : snapshot.entries()) {
            if (entry.kind() == Blueprint.Kind.LIBRARY && entry.enabled()) {
                final Library library = (Library) entry.resource();
                libraries.put(library.getId(), library);
            }
        }
    }

    /**
     * Indexes enabled protocol-neutral Provider resources after resolving their required Library.
     *
     * @param snapshot        validated complete snapshot
     * @param libraries       resolved enabled Library index
     * @param providerEntries mutable enabled Provider Blueprint index
     */
    private static void indexProviders(
            final Roster.Snapshot snapshot,
            final Map<String, Library> libraries,
            final Map<String, Blueprint.ProviderEntry> providerEntries) {
        for (Blueprint.Entry candidate : snapshot.entries()) {
            if (candidate.kind() != Blueprint.Kind.PROVIDER || !candidate.enabled()) {
                continue;
            }
            final Blueprint.ProviderEntry entry = (Blueprint.ProviderEntry) candidate;
            final Provider provider = entry.resource();
            final Library library = libraries.get(provider.getLibrary_id());
            if (library == null) {
                throw new CompilationFailure(Blueprint.Kind.PROVIDER, provider.getId(), "library_id",
                        "Enabled Provider could not resolve its Library");
            }
            providerEntries.put(provider.getId(), entry);
        }
    }

    /**
     * Best-effort closes workers already created before compilation failed.
     *
     * @param workers partially compiled worker index
     */
    private static void close(final Map<Roster.Reference, SourceWorker> workers) {
        for (SourceWorker worker : workers.values()) {
            try {
                worker.close();
            } catch (RuntimeException ignored) {
                // Compilation failure remains primary while every already-created worker is given a close attempt.
            }
        }
    }

    /**
     * Compiles every enabled entry in a previously validated snapshot.
     *
     * @param snapshot complete snapshot that has passed {@code SnapshotValidator}
     * @return runtime container containing the Roster snapshot and compiled Source workers
     * @throws IllegalArgumentException if the snapshot, a required type, or a driver result is invalid
     * @throws RuntimeException         if a required relationship or selected driver rejects an entry
     */
    RuntimeContainer compile(final Roster.Snapshot snapshot) {
        Assert.notNull(snapshot, "Roster snapshot must not be null");
        final Map<String, Library> libraries = new LinkedHashMap<>();
        final Map<String, Blueprint.ProviderEntry> providerEntries = new LinkedHashMap<>();
        final Map<Roster.Reference, SourceWorker> workers = new LinkedHashMap<>();
        try {
            compileLibraries(snapshot, libraries);
            indexProviders(snapshot, libraries, providerEntries);
            compileSources(snapshot, libraries, providerEntries, workers);
            return new RuntimeContainer(new SnapshotRoster(snapshot.revision(), snapshot), workers);
        } catch (RuntimeException failure) {
            close(workers);
            throw failure;
        }
    }

    /**
     * Compiles enabled Source resources after resolving their required Provider and Library association.
     *
     * @param snapshot        validated complete snapshot
     * @param libraries       resolved enabled Library index
     * @param providerEntries resolved enabled Provider Blueprint index
     * @param workers         mutable local Source-worker index
     */
    private void compileSources(
            final Roster.Snapshot snapshot,
            final Map<String, Library> libraries,
            final Map<String, Blueprint.ProviderEntry> providerEntries,
            final Map<Roster.Reference, SourceWorker> workers) {
        for (Blueprint.Entry candidate : snapshot.entries()) {
            if (candidate.kind() != Blueprint.Kind.SOURCE || !candidate.enabled()) {
                continue;
            }
            final Blueprint.SourceEntry entry = (Blueprint.SourceEntry) candidate;
            final Source source = entry.resource();
            try {
                final SourceDriver<?> driver = sourceLookup.requireDriver(source.getType());
                final Blueprint.ProviderEntry providerEntry = providerEntries.get(source.getProvider_id());
                if (providerEntry == null) {
                    throw new NotFoundException("Owning Provider for enabled Source was not indexed");
                }
                final Provider provider = providerEntry.resource();
                final Library library = libraries.get(provider.getLibrary_id());
                if (library == null) {
                    throw new NotFoundException("Library for enabled Source was not indexed");
                }
                final Roster.Reference reference = Roster.Reference.source(source.getId());
                final SourceWorker worker = compile(driver, entry, provider, library, snapshot.revision().value());
                workers.put(reference, worker);
            } catch (CompilationFailure failure) {
                throw failure;
            } catch (RuntimeException cause) {
                throw new CompilationFailure(Blueprint.Kind.SOURCE, source.getId(), "worker",
                        compilationDescription(cause), cause);
            }
        }
    }

    /**
     * Captures a wildcard driver and compiles from one exact preparation and its matching scoped services.
     *
     * @param <O>        concrete Source options type
     * @param driver     exact typed Source driver
     * @param entry      Source Blueprint entry
     * @param provider   resolved Provider
     * @param library    resolved Library
     * @param generation complete snapshot revision used as the Source security-state generation
     * @return compiled Source worker
     */
    private <O extends Options<?>> SourceWorker compile(
            final SourceDriver<O> driver,
            final Blueprint.SourceEntry entry,
            final Provider provider,
            final Library library,
            final long generation) {
        final SourceDriver.Prepared<O> prepared = driver.prepare(entry, provider, library);
        return Assert.notNull(
                driver.compile(
                        prepared,
                        runtimeServices.scope(prepared.entry(), prepared.slots(), prepared.dependencies(), generation)),
                "Source driver result must not be null");
    }

    /**
     * Carries safe entry coordinates from local compilation to the reload report boundary.
     *
     * @author Kimi Liu
     */
    static final class CompilationFailure extends RuntimeException {

        /**
         * Blueprint kind that failed compilation.
         */
        private final Blueprint.Kind kind;
        /**
         * Safe Blueprint entry identifier.
         */
        private final String id;
        /**
         * Safe failing field name.
         */
        private final String field;
        /**
         * Safe failure description exposed in reports.
         */
        private final String safeDescription;

        /**
         * Creates a compilation failure without an underlying cause.
         *
         * @param kind            Blueprint kind
         * @param id              Blueprint entry identifier
         * @param field           failing field
         * @param safeDescription safe description
         */
        CompilationFailure(final Blueprint.Kind kind, final String id, final String field,
                final String safeDescription) {
            this(kind, id, field, safeDescription, null);
        }

        /**
         * Creates a compilation failure retaining an internal cause.
         *
         * @param kind            Blueprint kind
         * @param id              Blueprint entry identifier
         * @param field           failing field
         * @param safeDescription safe description
         * @param cause           internal cause
         */
        CompilationFailure(final Blueprint.Kind kind, final String id, final String field, final String safeDescription,
                final Throwable cause) {
            super(safeDescription, cause);
            this.kind = Assert.notNull(kind, "Compilation failure kind must not be null");
            this.id = Assert.notBlank(id, "Compilation failure resource id must not be blank");
            this.field = Assert.notBlank(field, "Compilation failure field must not be blank");
            this.safeDescription = Assert
                    .notBlank(safeDescription, "Compilation failure safe description must not be blank");
        }

        /**
         * {@return the safe structured snapshot fault}
         */
        Roster.Fault fault() {
            return Roster.Fault
                    .entry(kind, id, Roster.Fault.Stage.COMPILE, Optional.of(field), ErrorCode._500, safeDescription);
        }

    }

}
