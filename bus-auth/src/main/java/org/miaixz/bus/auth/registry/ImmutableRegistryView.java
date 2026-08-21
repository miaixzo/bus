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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.auth.Registration;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;

/**
 * Stores one immutable revision-consistent Source-worker index and its detached registration snapshot.
 * <p>
 * Source workers are public framework contracts held behind an unmodifiable map for generation consistency.
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
     * Compiled enabled Source workers indexed by kind-safe reference.
     */
    private final Map<Registry.Reference, SourceWorker> workers;

    /**
     * Complete Source registrations grouped by Provider identifier.
     */
    private final Map<String, List<Registration.SourceEntry>> sourcesByProvider;

    /**
     * Enabled Source registrations grouped by Provider identifier.
     */
    private final Map<String, List<Registration.SourceEntry>> enabledSourcesByProvider;

    private final Object lifecycle = new Object();

    private boolean retired;

    private boolean closed;

    private int leases;

    /**
     * Creates a detached immutable view after successful local compilation.
     *
     * @param revision compiled snapshot revision
     * @param snapshot complete source snapshot with the same revision
     * @param workers  enabled compiled Source-worker index
     * @throws IllegalArgumentException if a container, entry, key, or revision relationship is invalid
     */
    public ImmutableRegistryView(final Registry.Revision revision, final Registry.Snapshot snapshot,
            final Map<Registry.Reference, SourceWorker> workers) {
        this.revision = Assert.notNull(revision, "Registry view revision must not be null");
        this.snapshot = Assert.notNull(snapshot, "Registry view snapshot must not be null");
        Assert.equals(revision, snapshot.revision(), "Registry view and snapshot revisions must match");
        Assert.notNull(workers, "Registry view Source worker index must not be null");
        final Map<Registry.Reference, SourceWorker> workerCopies = new LinkedHashMap<>(workers.size());
        workers.forEach(
                (reference, worker) -> workerCopies.put(
                        Assert.notNull(reference, "Registry view Source worker reference must not be null"),
                        Assert.notNull(worker, "Registry view Source worker must not be null")));
        this.workers = Map.copyOf(workerCopies);
        final Map<String, List<Registration.SourceEntry>> all = new LinkedHashMap<>();
        final Map<String, List<Registration.SourceEntry>> enabled = new LinkedHashMap<>();
        for (Registration.Entry entry : snapshot.records()) {
            if (entry instanceof Registration.SourceEntry sourceEntry) {
                final String providerId = sourceEntry.resource().getProvider_id();
                all.computeIfAbsent(providerId, ignored -> new ArrayList<>()).add(sourceEntry);
                if (sourceEntry.enabled()) {
                    enabled.computeIfAbsent(providerId, ignored -> new ArrayList<>()).add(sourceEntry);
                }
            }
        }
        this.sourcesByProvider = immutableLists(all);
        this.enabledSourcesByProvider = immutableLists(enabled);
    }

    private static Map<String, List<Registration.SourceEntry>> immutableLists(
            final Map<String, List<Registration.SourceEntry>> source) {
        final Map<String, List<Registration.SourceEntry>> copy = new LinkedHashMap<>(source.size());
        source.forEach((key, value) -> copy.put(key, List.copyOf(value)));
        return Map.copyOf(copy);
    }

    @Override
    public Lease acquire() {
        synchronized (lifecycle) {
            if (retired) {
                return null;
            }
            leases++;
            return new GenerationLease(this);
        }
    }

    @Override
    public void retire() {
        final boolean close;
        synchronized (lifecycle) {
            retired = true;
            close = leases == 0 && !closed;
            if (close) {
                closed = true;
            }
        }
        if (close) {
            closeWorkers();
        }
    }

    private void release() {
        final boolean close;
        synchronized (lifecycle) {
            if (leases > 0) {
                leases--;
            }
            close = retired && leases == 0 && !closed;
            if (close) {
                closed = true;
            }
        }
        if (close) {
            closeWorkers();
        }
    }

    private void closeWorkers() {
        for (SourceWorker worker : workers.values()) {
            try {
                worker.close();
            } catch (RuntimeException ignored) {
                // Retirement must continue so one faulty worker cannot leak every remaining worker.
            }
        }
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
     * @return complete detached snapshot
     */
    @Override
    public Registry.Snapshot snapshot() {
        return snapshot;
    }

    @Override
    public List<Registration.SourceEntry> sources(final String providerId) {
        Assert.notBlank(providerId, "Provider identifier must not be blank");
        return sourcesByProvider.getOrDefault(providerId, List.of());
    }

    @Override
    public List<Registration.SourceEntry> enabledSources(final String providerId) {
        Assert.notBlank(providerId, "Provider identifier must not be blank");
        return enabledSourcesByProvider.getOrDefault(providerId, List.of());
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

    private static final class GenerationLease implements Lease {

        private final ImmutableRegistryView view;

        private final AtomicBoolean closed = new AtomicBoolean();

        private GenerationLease(final ImmutableRegistryView view) {
            this.view = view;
        }

        @Override
        public RegistryView view() {
            return view;
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                view.release();
            }
        }
    }

}
