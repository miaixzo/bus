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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.cortex.guard.CortexGuard;
import org.miaixz.bus.cortex.guard.GuardContext;
import org.miaixz.bus.cortex.magic.event.CortexChangeLogStore;
import org.miaixz.bus.cortex.magic.event.CortexChangeRecord;
import org.miaixz.bus.cortex.magic.watch.WatchManager;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Version-domain publishing workflow.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class VersionPublisher {

    /**
     * Change event emitted when a release draft is saved.
     */
    public static final String VERSION_DRAFT_EVENT = "version-draft";
    /**
     * Change event emitted when a release is published.
     */
    public static final String VERSION_PUBLISH_EVENT = "version-publish";
    /**
     * Change event emitted when a release is rolled back to a prior version.
     */
    public static final String VERSION_ROLLBACK_EVENT = "version-rollback";
    /**
     * Change event emitted when a release is deprecated.
     */
    public static final String VERSION_DEPRECATE_EVENT = "version-deprecate";
    /**
     * Change event emitted when a release is deleted.
     */
    public static final String VERSION_DELETE_EVENT = "version-delete";
    /**
     * Watch source identifier used for version-domain notifications.
     */
    public static final String VERSION_SOURCE = "version-center";

    /**
     * Durable version store.
     */
    private final VersionStore store;
    /**
     * Optional change-log store used for publication events.
     */
    private final CortexChangeLogStore changeLogStore;
    /**
     * Optional guard used to enforce version-domain policy.
     */
    private final CortexGuard cortexGuard;
    /**
     * Optional watch manager used to publish version notifications.
     */
    private final WatchManager watchManager;

    /**
     * Creates a publisher with store-only persistence.
     *
     * @param store durable version store
     */
    public VersionPublisher(VersionStore store) {
        this(store, null, null, null);
    }

    /**
     * Creates a publisher with change-log integration.
     *
     * @param store          durable version store
     * @param changeLogStore optional change-log store
     */
    public VersionPublisher(VersionStore store, CortexChangeLogStore changeLogStore) {
        this(store, changeLogStore, null, null);
    }

    /**
     * Creates a publisher with change-log and watch integration.
     *
     * @param store          durable version store
     * @param changeLogStore optional change-log store
     * @param watchManager   optional watch manager
     */
    public VersionPublisher(VersionStore store, CortexChangeLogStore changeLogStore, WatchManager watchManager) {
        this(store, changeLogStore, null, watchManager);
    }

    /**
     * Creates a publisher with change-log and guard integration.
     *
     * @param store          durable version store
     * @param changeLogStore optional change-log store
     * @param cortexGuard    optional Cortex guard
     */
    public VersionPublisher(VersionStore store, CortexChangeLogStore changeLogStore, CortexGuard cortexGuard) {
        this(store, changeLogStore, cortexGuard, null);
    }

    /**
     * Creates a publisher with optional change-log, guard, and watch integration.
     *
     * @param store          durable version store
     * @param changeLogStore optional change-log store
     * @param cortexGuard    optional Cortex guard
     * @param watchManager   optional watch manager
     */
    public VersionPublisher(VersionStore store, CortexChangeLogStore changeLogStore, CortexGuard cortexGuard,
            WatchManager watchManager) {
        this.store = store;
        this.changeLogStore = changeLogStore;
        this.cortexGuard = cortexGuard;
        this.watchManager = watchManager;
    }

    /**
     * Saves a release draft without making it current.
     *
     * @param record release record
     * @return persisted draft record
     */
    public VersionRecord draft(VersionRecord record) {
        VersionRecord prepared = prepare(record, VersionStatus.DRAFT, false);
        enforce("draft", prepared);
        VersionRecord saved = store.save(prepared);
        emit(VERSION_DRAFT_EVENT, saved);
        return saved;
    }

    /**
     * Publishes a release and makes it current for its track.
     *
     * @param record release record
     * @return persisted active release record
     */
    public VersionRecord publish(VersionRecord record) {
        VersionRecord prepared = prepare(record, VersionStatus.ACTIVE, true);
        enforce("publish", prepared);
        VersionRecord previous = store.current(prepared.getNamespace_id(), prepared.getTrack());
        store.save(prepared);
        VersionRecord current = store
                .setCurrent(prepared.getNamespace_id(), prepared.getTrack(), prepared.getVersion());
        VersionRecord saved = current == null ? prepared : current;
        emitDeprecated(previous, saved);
        emit(VERSION_PUBLISH_EVENT, saved);
        return saved;
    }

    /**
     * Rolls the current release pointer back to a prior version on the same track.
     *
     * @param namespace release namespace
     * @param track     release track
     * @param version   target release version
     * @return persisted rollback target or {@code null}
     */
    public VersionRecord rollback(String namespace, String track, String version) {
        String normalizedTrack = ReleaseTrack.normalize(track);
        VersionRecord target = store.find(namespace, version, normalizedTrack);
        enforce("rollback", namespace, normalizedTrack, version, target == null ? null : target.getVersionStatus());
        if (target == null) {
            return null;
        }
        VersionRecord previous = store.current(namespace, normalizedTrack);
        attachRollbackMetadata(target, previous);
        store.save(target);
        VersionRecord saved = store.setCurrent(namespace, normalizedTrack, version);
        emitDeprecated(previous, saved);
        emit(VERSION_ROLLBACK_EVENT, saved);
        return saved;
    }

    /**
     * Deletes a release record.
     *
     * @param namespace release namespace
     * @param track     release track
     * @param version   release version
     * @return deleted release record or {@code null}
     */
    public VersionRecord delete(String namespace, String track, String version) {
        enforce("delete", namespace, track, version, null);
        VersionRecord deleted = store.delete(namespace, version, track);
        emit(VERSION_DELETE_EVENT, deleted);
        return deleted;
    }

    /**
     * Resolves the current release for a namespace and track.
     *
     * @param namespace release namespace
     * @param track     release track
     * @return current release record or {@code null}
     */
    public VersionRecord resolveCurrent(String namespace, String track) {
        return store.current(namespace, track);
    }

    /**
     * Normalizes release fields before a publish or draft operation.
     *
     * @param record    release record
     * @param status    status to apply
     * @param published whether the publication timestamp should be refreshed
     * @return prepared release record
     */
    private VersionRecord prepare(VersionRecord record, VersionStatus status, boolean published) {
        VersionRecord prepared = record == null ? new VersionRecord() : record;
        prepared.setTrack(ReleaseTrack.normalize(prepared.getTrack()));
        prepared.setVersionStatus(status);
        if (published) {
            prepared.setPublished(System.currentTimeMillis());
        }
        return prepared;
    }

    /**
     * Emits the change-log and watch notification for one version event.
     *
     * @param action event action
     * @param record release record
     */
    private void emit(String action, VersionRecord record) {
        appendChangeLog(action, record);
        publishWatch(action, record);
    }

    /**
     * Appends one version-domain event to the change log.
     *
     * @param action event action
     * @param record release record
     */
    private void appendChangeLog(String action, VersionRecord record) {
        if (changeLogStore == null || record == null) {
            return;
        }
        CortexChangeRecord change = new CortexChangeRecord();
        change.setDomain("version");
        change.setAction(action);
        change.setResourceType("VERSION");
        change.setResourceId(ReleaseTrack.normalize(record.getTrack()) + ":" + record.getVersion());
        change.setNamespace_id(record.getNamespace_id());
        change.setPayload(JsonKit.toJsonString(record));
        change.setSequence(
                record.getPublished() == null ? record.getCreated() == null ? 0L : record.getCreated()
                        : record.getPublished());
        change.setIdempotencyKey("version:" + action + ":" + change.getNamespace_id() + ":" + change.getResourceId());
        changeLogStore.append(change);
    }

    /**
     * Publishes one version-domain watch notification.
     *
     * @param action event action
     * @param record release record
     */
    private void publishWatch(String action, VersionRecord record) {
        if (watchManager == null || record == null) {
            return;
        }
        watchManager.notifyTyped(
                watchKey(record),
                toAssets(record),
                action,
                "watch:version:" + ReleaseTrack.normalize(record.getTrack()),
                VERSION_SOURCE,
                summary(action));
    }

    /**
     * Emits a deprecation event for the previous current release when publication changed the current pointer.
     *
     * @param previous previous current release
     * @param current  new current release
     */
    private void emitDeprecated(VersionRecord previous, VersionRecord current) {
        if (previous == null || current == null || sameRelease(previous, current)) {
            return;
        }
        VersionRecord deprecated = store
                .find(previous.getNamespace_id(), previous.getVersion(), ReleaseTrack.normalize(previous.getTrack()));
        if (deprecated != null && deprecated.getVersionStatus() == VersionStatus.DEPRECATED) {
            emit(VERSION_DEPRECATE_EVENT, deprecated);
        }
    }

    /**
     * Adds rollback metadata to the target release.
     *
     * @param target   rollback target release
     * @param previous previous current release
     */
    private void attachRollbackMetadata(VersionRecord target, VersionRecord previous) {
        Map<String, Object> root = metadata(target.getMetadata());
        Map<String, Object> rollback = new LinkedHashMap<>();
        rollback.put("rollbackFrom", previous == null ? null : previous.getVersion());
        rollback.put("rollbackTo", target.getVersion());
        rollback.put("track", ReleaseTrack.normalize(target.getTrack()));
        rollback.put("rollbackAt", System.currentTimeMillis());
        root.put("rollback", rollback);
        target.setMetadata(JsonKit.toJsonString(root));
    }

    /**
     * Parses a metadata JSON object, falling back to an empty mutable map.
     *
     * @param metadata raw metadata JSON
     * @return mutable metadata map
     */
    private Map<String, Object> metadata(String metadata) {
        if (metadata == null || metadata.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            Map<String, Object> root = JsonKit.toMap(metadata);
            return root == null ? new LinkedHashMap<>() : new LinkedHashMap<>(root);
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    /**
     * Converts a release record to a version asset for watch notifications.
     *
     * @param record release record
     * @return version asset
     */
    private VersionAssets toAssets(VersionRecord record) {
        VersionAssets asset = new VersionAssets();
        asset.setId(record.getId() == null || record.getId().isBlank() ? record.getVersion() : record.getId());
        asset.setNamespace_id(record.getNamespace_id());
        asset.setVersion(record.getVersion());
        asset.setName(record.getTitle());
        asset.setDescription(record.getDescription());
        asset.metadata(record.getMetadata());
        asset.semver(record.getVersion());
        asset.versionStatus(record.getVersionStatus());
        asset.releasedAt(record.getPublished());
        return asset;
    }

    /**
     * Builds the watch key for one release record.
     *
     * @param record release record
     * @return watch key
     */
    private String watchKey(VersionRecord record) {
        return "version:" + record.getNamespace_id() + ":" + ReleaseTrack.normalize(record.getTrack()) + ":"
                + record.getVersion();
    }

    /**
     * Builds a short human-readable summary for one event action.
     *
     * @param action event action
     * @return event summary
     */
    private String summary(String action) {
        return switch (action) {
            case VERSION_DRAFT_EVENT -> "Version drafted";
            case VERSION_PUBLISH_EVENT -> "Version published";
            case VERSION_ROLLBACK_EVENT -> "Version rolled back";
            case VERSION_DEPRECATE_EVENT -> "Version deprecated";
            case VERSION_DELETE_EVENT -> "Version deleted";
            default -> "Version changed";
        };
    }

    /**
     * Returns whether two release records describe the same track and version.
     *
     * @param left  first release record
     * @param right second release record
     * @return {@code true} when both records describe the same release
     */
    private boolean sameRelease(VersionRecord left, VersionRecord right) {
        return left != null && right != null
                && Objects.equals(ReleaseTrack.normalize(left.getTrack()), ReleaseTrack.normalize(right.getTrack()))
                && Objects.equals(left.getVersion(), right.getVersion());
    }

    /**
     * Applies guard policy to one release record.
     *
     * @param action guarded action
     * @param record release record
     */
    private void enforce(String action, VersionRecord record) {
        if (record == null) {
            enforce(action, null, null, null, null);
            return;
        }
        enforce(action, record.getNamespace_id(), record.getTrack(), record.getVersion(), record.getVersionStatus());
    }

    /**
     * Applies guard policy to one version-domain action.
     *
     * @param action    guarded action
     * @param namespace release namespace
     * @param track     release track
     * @param version   release version
     * @param status    release status
     */
    private void enforce(String action, String namespace, String track, String version, VersionStatus status) {
        if (cortexGuard == null) {
            return;
        }
        GuardContext context = new GuardContext();
        context.setDomain("version");
        context.setAction(action);
        context.setResourceType("VERSION");
        context.setResourceId(ReleaseTrack.normalize(track) + ":" + version);
        context.namespace_id(namespace);
        context.putAttribute("track", ReleaseTrack.normalize(track));
        context.putAttribute("version", version);
        context.putAttribute("status", status == null ? null : status.name());
        cortexGuard.enforce(context);
    }

}
