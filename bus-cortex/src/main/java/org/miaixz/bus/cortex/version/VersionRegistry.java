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
package org.miaixz.bus.cortex.version;

import java.util.List;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.cortex.Listener;
import org.miaixz.bus.cortex.Registry;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.Vector;
import org.miaixz.bus.cortex.Watch;
import org.miaixz.bus.cortex.guard.CortexGuard;
import org.miaixz.bus.cortex.magic.event.CortexChangeLogStore;
import org.miaixz.bus.cortex.magic.identity.CortexIdentity;
import org.miaixz.bus.cortex.magic.watch.WatchManager;

/**
 * Compatibility adapter exposing version records through the legacy registry-shaped facade.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class VersionRegistry implements Registry<VersionAssets> {

    /**
     * Durable version store.
     */
    private final VersionStore store;
    /**
     * Version publishing workflow facade.
     */
    private final VersionPublisher publisher;
    /**
     * Optional shared watch manager.
     */
    private final WatchManager watchManager;

    /**
     * Creates a version registry without watch or change-log integration.
     *
     * @param store durable version store
     */
    public VersionRegistry(VersionStore store) {
        this(store, null);
    }

    /**
     * Creates a version registry with optional watch integration.
     *
     * @param store        durable version store
     * @param watchManager optional watch manager
     */
    public VersionRegistry(VersionStore store, WatchManager watchManager) {
        this(store, watchManager, null, null);
    }

    /**
     * Creates a version registry with watch and change-log integration.
     *
     * @param store          durable version store
     * @param watchManager   optional watch manager
     * @param changeLogStore optional change-log store
     */
    public VersionRegistry(VersionStore store, WatchManager watchManager, CortexChangeLogStore changeLogStore) {
        this(store, watchManager, changeLogStore, null);
    }

    /**
     * Creates a version registry with watch and guard integration.
     *
     * @param store        durable version store
     * @param watchManager optional watch manager
     * @param cortexGuard  optional Cortex guard
     */
    public VersionRegistry(VersionStore store, WatchManager watchManager, CortexGuard cortexGuard) {
        this(store, watchManager, null, cortexGuard);
    }

    /**
     * Creates a version registry with optional watch, change-log, and guard integration.
     *
     * @param store          durable version store
     * @param watchManager   optional watch manager
     * @param changeLogStore optional change-log store
     * @param cortexGuard    optional Cortex guard
     */
    public VersionRegistry(VersionStore store, WatchManager watchManager, CortexChangeLogStore changeLogStore,
            CortexGuard cortexGuard) {
        this.store = store;
        this.publisher = new VersionPublisher(store, changeLogStore, cortexGuard, watchManager);
        this.watchManager = watchManager;
    }

    /**
     * Creates a version registry backed by a cache store unless a store is supplied.
     *
     * @param cacheX       cache facade used for fallback persistence
     * @param watchManager optional watch manager
     * @param store        optional dedicated version store
     */
    public VersionRegistry(CacheX<String, Object> cacheX, WatchManager watchManager, VersionStore store) {
        this(cacheX, watchManager, store, null);
    }

    /**
     * Creates a version registry backed by a cache store unless a guarded store is supplied.
     *
     * @param cacheX       cache facade used for fallback persistence
     * @param watchManager optional watch manager
     * @param store        optional dedicated version store
     * @param cortexGuard  optional Cortex guard
     */
    public VersionRegistry(CacheX<String, Object> cacheX, WatchManager watchManager, VersionStore store,
            CortexGuard cortexGuard) {
        this(store == null ? new CacheVersionStore(cacheX) : store, watchManager, cortexGuard);
    }

    /**
     * Returns the version publishing workflow facade.
     *
     * @return version publisher
     */
    public VersionPublisher publisher() {
        return publisher;
    }

    /**
     * Returns the durable version store.
     *
     * @return version store
     */
    public VersionStore store() {
        return store;
    }

    /**
     * Registers a version asset by publishing its release record.
     *
     * @param entry version asset
     */
    @Override
    public void register(VersionAssets entry) {
        publisher.publish(toRecord(entry));
    }

    /**
     * Registers a version asset and ignores runtime-instance data.
     *
     * @param service  version asset
     * @param instance ignored runtime instance
     */
    @Override
    public void register(VersionAssets service, Instance instance) {
        register(service);
    }

    /**
     * Deletes a version asset by version or identifier.
     *
     * @param namespace version namespace
     * @param id        version identifier
     */
    @Override
    public void deregister(String namespace, String id) {
        VersionRecord target = findByVersionOrId(CortexIdentity.namespace(namespace), id);
        if (target == null) {
            publisher.delete(namespace, ReleaseTrack.STABLE.key(), id);
            return;
        }
        publisher.delete(target.getNamespace_id(), target.getTrack(), target.getVersion());
    }

    /**
     * Queries version assets for a namespace and optional track.
     *
     * @param vector compatibility query vector
     * @return matching version assets
     */
    @Override
    public List<VersionAssets> query(Vector vector) {
        String namespace = CortexIdentity.namespace(vector == null ? null : vector.getNamespace_id());
        String track = vector == null ? null : vector.getMethod();
        return store.list(namespace, track).stream().map(this::toAssets).toList();
    }

    /**
     * Subscribes to version events when a watch manager is configured.
     *
     * @param vector   compatibility query vector
     * @param listener version event listener
     * @return watch identifier
     */
    @Override
    public String watch(Vector vector, Listener<Watch<VersionAssets>> listener) {
        if (watchManager == null) {
            throw new UnsupportedOperationException("Version watch is not configured");
        }
        return watchManager.add(vector, listener);
    }

    /**
     * Cancels a version watch subscription.
     *
     * @param watch_id watch identifier
     */
    @Override
    public void unwatch(String watch_id) {
        if (watchManager != null) {
            watchManager.remove(watch_id);
        }
    }

    /**
     * Returns whether version registry supports runtime instances.
     *
     * @return always {@code false}
     */
    @Override
    public boolean supportsInstances() {
        return false;
    }

    /**
     * Returns whether version watch support is available.
     *
     * @return {@code true} when a watch manager is configured
     */
    @Override
    public boolean supportsWatch() {
        return watchManager != null;
    }

    /**
     * Returns whether version refresh support is available.
     *
     * @return always {@code false}
     */
    @Override
    public boolean supportsRefresh() {
        return false;
    }

    /**
     * Converts a registry-shaped version asset into a release record.
     *
     * @param asset version asset
     * @return release record
     */
    private VersionRecord toRecord(VersionAssets asset) {
        VersionAssets source = asset == null ? new VersionAssets() : asset;
        VersionRecord record = new VersionRecord();
        record.setId(source.getId());
        record.setNamespace_id(CortexIdentity.namespace(source.getNamespace_id()));
        record.setVersion(source.semver() != null ? source.semver() : source.getVersion());
        record.setTrack(
                source.getMethod() == null || source.getMethod().isBlank() ? ReleaseTrack.STABLE.key()
                        : source.getMethod());
        record.setVersionStatus(source.versionStatus() == null ? VersionStatus.ACTIVE : source.versionStatus());
        record.setTitle(source.getName());
        record.setDescription(source.getDescription());
        record.setMetadata(source.getMetadata());
        record.setPublished(source.releasedAt());
        return record;
    }

    /**
     * Converts a release record into a registry-shaped version asset.
     *
     * @param record release record
     * @return version asset
     */
    private VersionAssets toAssets(VersionRecord record) {
        VersionAssets asset = new VersionAssets();
        asset.setId(record.getId() == null || record.getId().isBlank() ? record.getVersion() : record.getId());
        asset.setNamespace_id(record.getNamespace_id());
        asset.setType(Type.VERSION.key());
        asset.setVersion(record.getVersion());
        asset.setMethod(ReleaseTrack.normalize(record.getTrack()));
        asset.setName(record.getTitle());
        asset.setDescription(record.getDescription());
        asset.metadata(record.getMetadata());
        asset.semver(record.getVersion());
        asset.versionStatus(record.getVersionStatus());
        asset.releasedAt(record.getPublished());
        return asset;
    }

    /**
     * Finds a release by stable-track version first, then by any matching version or asset identifier.
     *
     * @param namespace release namespace
     * @param id        release version or asset identifier
     * @return matching release record or {@code null}
     */
    private VersionRecord findByVersionOrId(String namespace, String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        VersionRecord stable = store.find(namespace, id, ReleaseTrack.STABLE.key());
        if (stable != null) {
            return stable;
        }
        return store.list(namespace, null).stream()
                .filter(record -> id.equals(record.getVersion()) || id.equals(record.getId())).findFirst().orElse(null);
    }

}
