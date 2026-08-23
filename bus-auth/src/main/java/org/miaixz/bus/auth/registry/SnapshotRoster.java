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

import org.miaixz.bus.auth.Blueprint;
import org.miaixz.bus.auth.Roster;
import org.miaixz.bus.core.lang.Assert;

/**
 * Implements one fixed revision-consistent Roster snapshot and its read-only Source indexes.
 *
 * @author Kimi Liu
 */
public class SnapshotRoster implements Roster {

    /**
     * Revision shared by the snapshot and every index.
     */
    private final Roster.Revision revision;

    /**
     * Complete source snapshot with a structurally frozen record list.
     */
    private final Roster.Snapshot snapshot;

    /**
     * Complete Source Blueprint entries grouped by Provider identifier.
     */
    private final Map<String, List<Blueprint.SourceEntry>> sourcesByProvider;

    /**
     * Enabled Source Blueprint entries grouped by Provider identifier.
     */
    private final Map<String, List<Blueprint.SourceEntry>> enabledSourcesByProvider;

    /**
     * Creates a detached fixed Roster after successful local compilation.
     *
     * @param revision compiled snapshot revision
     * @param snapshot complete source snapshot with the same revision
     * @throws IllegalArgumentException if a container, entry, or revision relationship is invalid
     */
    public SnapshotRoster(final Roster.Revision revision, final Roster.Snapshot snapshot) {
        this.revision = Assert.notNull(revision, "Roster snapshot revision must not be null");
        this.snapshot = Assert.notNull(snapshot, "Roster snapshot data must not be null");
        Assert.equals(revision, snapshot.revision(), "Roster snapshot revisions must match");
        final Map<String, List<Blueprint.SourceEntry>> all = new LinkedHashMap<>();
        final Map<String, List<Blueprint.SourceEntry>> enabled = new LinkedHashMap<>();
        for (Blueprint.Entry entry : snapshot.entries()) {
            if (entry instanceof Blueprint.SourceEntry sourceEntry) {
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
    private static Map<String, List<Blueprint.SourceEntry>> immutableLists(
            final Map<String, List<Blueprint.SourceEntry>> source) {
        final Map<String, List<Blueprint.SourceEntry>> copy = new LinkedHashMap<>(source.size());
        source.forEach((key, value) -> copy.put(key, List.copyOf(value)));
        return Map.copyOf(copy);
    }

    /**
     * Returns this Roster snapshot's immutable revision.
     *
     * @return Roster snapshot revision
     */
    @Override
    public Roster.Revision revision() {
        return revision;
    }

    /**
     * Returns the complete Blueprint snapshot retained by this fixed Roster.
     *
     * @return complete detached snapshot
     */
    @Override
    public Roster.Snapshot snapshot() {
        return snapshot;
    }

    /**
     * Returns all Source Blueprint entries owned by one Provider in snapshot order.
     *
     * @param providerId exact Provider identifier
     * @return immutable matching Source Blueprint entries
     */
    @Override
    public List<Blueprint.SourceEntry> sources(final String providerId) {
        Assert.notBlank(providerId, "Provider identifier must not be blank");
        return sourcesByProvider.getOrDefault(providerId, List.of());
    }

    /**
     * Returns enabled Source Blueprint entries owned by one Provider in snapshot order.
     *
     * @param providerId exact Provider identifier
     * @return immutable enabled Source Blueprint entries
     */
    @Override
    public List<Blueprint.SourceEntry> enabledSources(final String providerId) {
        Assert.notBlank(providerId, "Provider identifier must not be blank");
        return enabledSourcesByProvider.getOrDefault(providerId, List.of());
    }

}
