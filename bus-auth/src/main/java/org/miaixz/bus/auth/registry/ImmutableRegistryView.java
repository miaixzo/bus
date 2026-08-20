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
package org.miaixz.bus.auth.registry;

import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.auth.Library;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.xyz.SerializeKit;

/**
 * Stores one immutable revision-consistent set of Library and Source-worker indexes.
 * <p>
 * The mutable Bus Library persistence entity is deep-copied on entry and lookup through the shared Bus serialization
 * utility, preventing request-scoped launch data or external mutation from changing the committed view. Source workers
 * are public framework contracts held behind an unmodifiable map for generation consistency.
 * </p>
 *
 * @author Kimi Liu
 */
public final class ImmutableRegistryView implements RegistryView {

    /**
     * Revision shared by the snapshot and every index.
     */
    private final Registry.Revision revision;

    /**
     * Complete source snapshot with a structurally frozen record list.
     */
    private final Registry.Snapshot snapshot;

    /**
     * Deep-copied enabled Library entities indexed by resource identifier.
     */
    private final Map<String, Library> libraries;

    /**
     * Compiled enabled Source workers indexed by kind-safe reference.
     */
    private final Map<Registry.Reference, SourceWorker> workers;

    /**
     * Creates a detached immutable view after successful local compilation.
     *
     * @param revision  compiled snapshot revision
     * @param snapshot  complete source snapshot with the same revision
     * @param libraries enabled Library index
     * @param workers   enabled compiled Source-worker index
     * @throws IllegalArgumentException if a container, entry, key, or revision relationship is invalid
     */
    public ImmutableRegistryView(final Registry.Revision revision, final Registry.Snapshot snapshot,
            final Map<String, Library> libraries, final Map<Registry.Reference, SourceWorker> workers) {
        this.revision = Assert.notNull(revision, "Registry view revision must not be null");
        this.snapshot = Assert.notNull(snapshot, "Registry view snapshot must not be null");
        Assert.equals(revision, snapshot.revision(), "Registry view and snapshot revisions must match");
        Assert.notNull(libraries, "Registry view Library index must not be null");
        final Map<String, Library> libraryCopies = new LinkedHashMap<>(libraries.size());
        libraries.forEach(
                (id, library) -> libraryCopies.put(
                        Assert.notBlank(id, "Registry view Library id must not be blank"),
                        copy(Assert.notNull(library, "Registry view Library must not be null"))));
        this.libraries = Map.copyOf(libraryCopies);
        Assert.notNull(workers, "Registry view Source worker index must not be null");
        final Map<Registry.Reference, SourceWorker> workerCopies = new LinkedHashMap<>(workers.size());
        workers.forEach(
                (reference, worker) -> workerCopies.put(
                        Assert.notNull(reference, "Registry view Source worker reference must not be null"),
                        Assert.notNull(worker, "Registry view Source worker must not be null")));
        this.workers = Map.copyOf(workerCopies);
    }

    /**
     * Deep-copies a mutable Library entity using the shared Bus serialization facility.
     *
     * @param library Library entity to detach
     * @return detached Library copy
     */
    private static Library copy(final Library library) {
        return Assert.notNull(SerializeKit.clone(library), "Registry view Library copy must not be null");
    }

    /**
     * Returns this view's immutable revision.
     *
     * @return committed view revision
     */
    @Override
    public Registry.Revision revision() {
        return revision;
    }

    /**
     * Returns the complete source snapshot retained by this compiled view.
     *
     * @return complete snapshot whose entity references are not deep-copied
     */
    @Override
    public Registry.Snapshot snapshot() {
        return snapshot;
    }

    /**
     * Returns a detached copy of an enabled Library so caller mutation cannot alter this view.
     *
     * @param id Library resource identifier
     * @return detached Library when present
     */
    @Override
    public Optional<Library> library(final String id) {
        Assert.notBlank(id, "Registry view Library lookup id must not be blank");
        final Library library = libraries.get(id);
        return Optional.ofNullable(library == null ? null : copy(library));
    }

    /**
     * Returns the compiled Source worker for an exact kind-safe reference.
     *
     * @param reference Source Registry reference
     * @return compiled Source worker when present
     */
    @Override
    public Optional<SourceWorker> worker(final Registry.Reference reference) {
        Assert.notNull(reference, "Registry view Source worker reference must not be null");
        return Optional.ofNullable(workers.get(reference));
    }

}
