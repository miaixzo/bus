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
package org.miaixz.bus.cortex.release;

import java.util.List;
import java.util.Objects;

import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.cortex.*;
import org.miaixz.bus.cortex.magic.identity.CortexIdentity;
import org.miaixz.bus.cortex.magic.watch.WatchManager;

/**
 * Read-only compatibility adapter exposing version records through the legacy registry-shaped facade.
 *
 * @author Kimi Liu
 */
public class ReleaseRegistry implements Registry<ReleaseAssets> {

    /**
     * Durable version store.
     */
    private final ReleaseStore store;

    /**
     * Optional shared watch manager.
     */
    private final WatchManager watchManager;

    /**
     * Creates a read-only formal version registry; configuration commands use workflow services.
     *
     * @param store        authoritative formal versions
     * @param watchManager notification transport
     */
    public ReleaseRegistry(ReleaseStore store, WatchManager watchManager) {
        this.store = Objects.requireNonNull(store);
        this.watchManager = watchManager;
    }

    /**
     * Returns the durable version store.
     *
     * @return version store
     */
    public ReleaseStore store() {
        return store;
    }

    /**
     * Rejects direct version publication through the read-only registry.
     * <p>
     * Platform versions must be published through an approved rollout and atomically activated by the durable host.
     * </p>
     *
     * @param entry ignored version asset
     * @throws UnsupportedOperationException always; direct publication must use an approved rollout
     */
    @Override
    public void register(ReleaseAssets entry) {
        throw new UnsupportedOperationException(
                "Direct version registration is not supported; publish an approved rollout instead");
    }

    /**
     * Rejects direct version publication through the runtime-registration overload.
     *
     * @param service  ignored version asset
     * @param instance ignored runtime instance
     * @throws UnsupportedOperationException always; direct publication must use an approved rollout
     */
    @Override
    public void register(ReleaseAssets service, Instance instance) {
        register(service);
    }

    /**
     * Rejects physical deletion of formal release history.
     *
     * @param tenantId owning tenant
     * @param id       formal version identity
     * @throws IllegalStateException formal releases are immutable
     */
    @Override
    public void deregister(String tenantId, String id) {
        throw new IllegalStateException("Formal release history cannot be deleted through the registry");
    }

    /**
     * Queries version assets for a tenantId and optional track.
     *
     * @param vector shared query vector
     * @return matching version assets
     */
    @Override
    public List<ReleaseAssets> query(Vector vector) {
        String tenantId = CortexIdentity.tenantId(vector == null ? null : vector.getTenant_id());
        String track = vector == null ? null : vector.getMethod();
        return store.list(tenantId, track).stream().map(this::toAssets).toList();
    }

    /**
     * Subscribes to version events when a watch manager is configured.
     *
     * @param vector   shared query vector
     * @param listener version event listener
     * @return watch identifier
     * @throws UnsupportedOperationException if no version-watch manager is configured
     */
    @Override
    public String watch(Vector vector, Listener<Watch<ReleaseAssets>> listener) {
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
     * Converts a release record into a registry-shaped version asset.
     *
     * @param record release record
     * @return version asset
     */
    private ReleaseAssets toAssets(Release record) {
        ReleaseAssets asset = new ReleaseAssets();
        asset.setId(record.getId() == null || record.getId().isBlank() ? record.getVersion() : record.getId());
        asset.setTenant_id(record.getTenant_id());
        asset.setType(Type.VERSION.key());
        asset.setVersion(record.getVersion());
        asset.setMethod(EnumValue.Track.normalize(record.getTrack()));
        asset.setName(record.getTitle());
        asset.setDescription(record.getDescription());
        asset.metadata(record.getMetadata());
        asset.semver(record.getVersion());
        asset.setStatus(record.getStatus());
        asset.releasedAt(record.getPublished());
        return asset;
    }

    /**
     * Finds a release by stable-track version first, then by any matching version or asset identifier.
     *
     * @param tenantId release tenantId
     * @param id       release version or asset identifier
     * @return matching release record or {@code null}
     */
    private Release findByVersionOrId(String tenantId, String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        Release stable = store.find(tenantId, id, EnumValue.Track.STABLE.key());
        if (stable != null) {
            return stable;
        }
        return store.list(tenantId, null).stream()
                .filter(record -> id.equals(record.getVersion()) || id.equals(record.getId())).findFirst().orElse(null);
    }

}
