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

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.cortex.guard.CortexGuard;
import org.miaixz.bus.cortex.guard.GuardContext;
import org.miaixz.bus.cortex.magic.event.CortexChangeLogStore;
import org.miaixz.bus.cortex.magic.event.CortexChangeRecord;
import org.miaixz.bus.cortex.magic.watch.WatchManager;
import org.miaixz.bus.cortex.setting.rollout.Plan;
import org.miaixz.bus.cortex.setting.rollout.Rollout;
import org.miaixz.bus.cortex.setting.rollout.Rollout.State;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;

/**
 * Version-domain publishing workflow.
 *
 * @author Kimi Liu
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
     * Creates a server-versioned release draft. The supplied creator must be the authenticated operator, and the host
     * must not expose caller-controlled creator or version fields.
     */
    public VersionRecord create(VersionRecord record) {
        if (record == null || record.getVersion() != null) {
            throw new IllegalArgumentException("A new draft cannot supply its own version");
        }
        VersionRecord draft = JsonKit.toPojo(JsonKit.toJsonString(record), VersionRecord.class);
        draft.setVersion(VersionRecord.generate(Clock.systemDefaultZone()));
        draft = prepare(draft, VersionStatus.DRAFT, false);
        enforce("draft", draft);
        VersionRecord saved = store.create(draft);
        emit(VERSION_DRAFT_EVENT, saved);
        return saved;
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
     * Prepares the platform version for a frozen approved batch, without changing the active version. The host must
     * atomically persist it with selected application pointers and its durable notification outbox.
     */
    public VersionRecord preparePublication(Rollout rollout) {
        Plan plan = Plan.restore(rollout);
        if (!State.PUBLISHING.name().equals(rollout.getState())) {
            throw new IllegalArgumentException("Publication requires an approved execution intent");
        }
        VersionRecord active = store.current(plan.getTenant_id(), ReleaseTrack.normalize(null));
        if (!Objects.equals(plan.getBaseline(), active == null ? null : active.getVersion())) {
            throw new IllegalStateException("Publication baseline changed; review a newly frozen plan");
        }
        if (rollout.getCreator() == null || rollout.getCreator().isBlank()) {
            throw new IllegalArgumentException("Authenticated rollout creator is required");
        }
        VersionRecord existing = store.find(plan.getTenant_id(), plan.getVersion(), ReleaseTrack.normalize(null));
        if (existing != null && (existing.getVersionStatus() != VersionStatus.DRAFT
                || !Objects.equals(existing.getCreator(), rollout.getCreator())
                || existing.getMetadata() != null && !existing.getMetadata().equals(plan.toJson()))) {
            throw new IllegalStateException("Publication version conflicts with the existing draft");
        }
        VersionRecord record = existing == null ? new VersionRecord()
                : JsonKit.toPojo(JsonKit.toJsonString(existing), VersionRecord.class);
        record.setTenant_id(plan.getTenant_id());
        record.setVersion(plan.getVersion());
        record.setTrack(ReleaseTrack.normalize(null));
        record.setVersionStatus(VersionStatus.DRAFT);
        record.setTitle(rollout.getTitle());
        record.setDescription(rollout.getDescription());
        record.setCreator(rollout.getCreator());
        if (existing == null) {
            record.setCreated(rollout.getCreated());
        }
        record.setMetadata(plan.toJson());
        enforce("publish", record);
        return record;
    }

    /**
     * Publishes a release and makes it current for its track. Compatibility workflow only; governed application batches
     * use preparePublication and atomic host activation.
     *
     * @param record release record
     * @return persisted active release record
     */
    @Deprecated(since = "8.8.0")
    public VersionRecord publish(VersionRecord record) {
        VersionRecord prepared = prepare(record, VersionStatus.ACTIVE, true);
        enforce("publish", prepared);
        VersionRecord previous = store.current(prepared.getTenant_id(), prepared.getTrack());
        store.save(prepared);
        VersionRecord current = store.setCurrent(prepared.getTenant_id(), prepared.getTrack(), prepared.getVersion());
        VersionRecord saved = current == null ? prepared : current;
        emitDeprecated(previous, saved);
        emit(VERSION_PUBLISH_EVENT, saved);
        return saved;
    }

    /**
     * Publishes a new version containing historical artifacts without rewriting the historical target.
     *
     * @param tenantId release tenantId
     * @param track    release track
     * @param version  target release version
     * @return persisted rollback target or {@code null}
     */
    @Deprecated(since = "8.8.0")
    public VersionRecord rollback(String tenantId, String track, String version) {
        String normalizedTrack = ReleaseTrack.normalize(track);
        VersionRecord target = store.find(tenantId, version, normalizedTrack);
        enforce("rollback", tenantId, normalizedTrack, version, target == null ? null : target.getVersionStatus());
        if (target == null) {
            return null;
        }
        VersionRecord previous = store.current(tenantId, normalizedTrack);
        VersionRecord restored = JsonKit.toPojo(JsonKit.toJsonString(target), VersionRecord.class);
        restored.setId(null);
        restored.setPublished(null);
        restored.setCreated(System.currentTimeMillis());
        attachRollbackMetadata(restored, previous);
        restored.setVersion(VersionRecord.generate(Clock.systemDefaultZone()));
        restored.setVersionStatus(VersionStatus.DRAFT);
        store.create(restored);
        VersionRecord saved = store.setCurrent(tenantId, normalizedTrack, restored.getVersion());
        emitDeprecated(previous, saved);
        emit(VERSION_ROLLBACK_EVENT, saved);
        return saved;
    }

    /**
     * Deletes a release record.
     *
     * @param tenantId release tenantId
     * @param track    release track
     * @param version  release version
     * @return deleted release record or {@code null}
     */
    public VersionRecord delete(String tenantId, String track, String version) {
        enforce("delete", tenantId, track, version, null);
        VersionRecord deleted = store.delete(tenantId, version, track);
        emit(VERSION_DELETE_EVENT, deleted);
        return deleted;
    }

    /**
     * Resolves the current release for a tenantId and track.
     *
     * @param tenantId release tenantId
     * @param track    release track
     * @return current release record or {@code null}
     */
    public VersionRecord resolveCurrent(String tenantId, String track) {
        return store.current(tenantId, track);
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
        VersionRecord prepared = record == null ? new VersionRecord()
                : JsonKit.toPojo(JsonKit.toJsonString(record), VersionRecord.class);
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
        change.setResourceId(ReleaseTrack.normalize(record.getTrack()) + Symbol.COLON + record.getVersion());
        change.setTenant_id(record.getTenant_id());
        change.setPayload(JsonKit.toJsonString(record));
        change.setSequence(
                record.getPublished() == null ? record.getCreated() == null ? 0L : record.getCreated()
                        : record.getPublished());
        change.setIdempotencyKey(
                "version:" + action + Symbol.COLON + change.getTenant_id() + Symbol.COLON + change.getResourceId());
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
                .find(previous.getTenant_id(), previous.getVersion(), ReleaseTrack.normalize(previous.getTrack()));
        if (deprecated != null && deprecated.getVersionStatus() == VersionStatus.DEPRECATED) {
            emit(VERSION_DEPRECATE_EVENT, deprecated);
        }
    }

    /**
     * Adds rollback provenance to a new release copied from the historical target.
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
            Map<String, Object> root = JsonKit.toPojo(metadata, Map.class);
            return root == null ? new LinkedHashMap<>() : new LinkedHashMap<>(root);
        } catch (Exception e) {
            Logger.warn(
                    false,
                    "Cortex",
                    e,
                    "Cortex operation failed: provider={}, recoverable={}, exception={}",
                    "VersionPublisher",
                    false,
                    e.getClass().getSimpleName());
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
        asset.setTenant_id(record.getTenant_id());
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
        return "version:" + record.getTenant_id() + Symbol.COLON + ReleaseTrack.normalize(record.getTrack())
                + Symbol.COLON + record.getVersion();
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
        enforce(action, record.getTenant_id(), record.getTrack(), record.getVersion(), record.getVersionStatus());
    }

    /**
     * Applies guard policy to one version-domain action.
     *
     * @param action   guarded action
     * @param tenantId release tenantId
     * @param track    release track
     * @param version  release version
     * @param status   release status
     */
    private void enforce(String action, String tenantId, String track, String version, VersionStatus status) {
        if (cortexGuard == null) {
            return;
        }
        GuardContext context = new GuardContext();
        context.setDomain("version");
        context.setAction(action);
        context.setResourceType("VERSION");
        context.setResourceId(ReleaseTrack.normalize(track) + Symbol.COLON + version);
        context.tenant_id(tenantId);
        context.putAttribute("track", ReleaseTrack.normalize(track));
        context.putAttribute("version", version);
        context.putAttribute("status", status == null ? null : status.name());
        cortexGuard.enforce(context);
    }

}
