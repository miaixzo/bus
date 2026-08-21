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

import org.miaixz.bus.auth.Registration;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.core.lang.Assert;

/**
 * Implements one fixed revision-consistent Registry snapshot and its read-only Source indexes.
 *
 * @author Kimi Liu
 */
public final class SnapshotRegistry implements Registry {

    /**
     * Revision shared by the snapshot and every index.
     */
    private final Registry.Revision revision;

    /**
     * Complete source snapshot with a structurally frozen record list.
     */
    private final Registry.Snapshot snapshot;

    /**
     * Complete Source registrations grouped by Provider identifier.
     */
    private final Map<String, List<Registration.SourceEntry>> sourcesByProvider;

    /**
     * Enabled Source registrations grouped by Provider identifier.
     */
    private final Map<String, List<Registration.SourceEntry>> enabledSourcesByProvider;

    /**
     * Creates a detached fixed Registry after successful local compilation.
     *
     * @param revision compiled snapshot revision
     * @param snapshot complete source snapshot with the same revision
     * @throws IllegalArgumentException if a container, entry, or revision relationship is invalid
     */
    public SnapshotRegistry(final Registry.Revision revision, final Registry.Snapshot snapshot) {
        this.revision = Assert.notNull(revision, "Snapshot Registry revision must not be null");
        this.snapshot = Assert.notNull(snapshot, "Snapshot Registry data must not be null");
        Assert.equals(revision, snapshot.revision(), "Snapshot Registry revisions must match");
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

    /**
     * Detaches and freezes a Provider-to-Source multimap.
     *
     * @param source mutable source multimap
     * @return immutable detached multimap
     */
    private static Map<String, List<Registration.SourceEntry>> immutableLists(
            final Map<String, List<Registration.SourceEntry>> source) {
        final Map<String, List<Registration.SourceEntry>> copy = new LinkedHashMap<>(source.size());
        source.forEach((key, value) -> copy.put(key, List.copyOf(value)));
        return Map.copyOf(copy);
    }

    /**
     * Returns this Snapshot Registry's immutable revision.
     *
     * @return Snapshot Registry revision
     */
    @Override
    public Registry.Revision revision() {
        return revision;
    }

    /**
     * Returns the complete registration snapshot retained by this fixed Registry.
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

}
