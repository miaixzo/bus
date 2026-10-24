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

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.core.lang.EnumValue;
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

/**
 * Version-domain publishing workflow.
 *
 * @author Kimi Liu
 */
public class ReleasePublisher {

    /**
     * Change event emitted when a release draft is saved.
     */
    public static final String RELEASE_DRAFT_EVENT = "release-draft";
    /**
     * Change event emitted when a release is published.
     */
    public static final String RELEASE_PUBLISH_EVENT = "release-publish";
    /**
     * Change event emitted when a release is rolled back to a prior version.
     */
    public static final String RELEASE_ROLLBACK_EVENT = "release-rollback";
    /**
     * Change event emitted when a release is deprecated.
     */
    public static final String RELEASE_DEPRECATE_EVENT = "release-deprecate";
    /**
     * Change event emitted when a release is deleted.
     */
    public static final String RELEASE_DELETE_EVENT = "release-delete";
    /**
     * Watch source identifier used for version-domain notifications.
     */
    public static final String RELEASE_SOURCE = "release-center";
    /**
     * Process-local monotonic clock; persistent stores must additionally enforce tenant/track/version uniqueness.
     */
    private static final AtomicLong RELEASE_CLOCK = new AtomicLong();
    /**
     * Durable version store.
     */
    private final ReleaseStore store;
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
    public ReleasePublisher(ReleaseStore store) {
        this(store, null, null, null);
    }

    /**
     * Creates a publisher with change-log integration.
     *
     * @param store          durable version store
     * @param changeLogStore optional change-log store
     */
    public ReleasePublisher(ReleaseStore store, CortexChangeLogStore changeLogStore) {
        this(store, changeLogStore, null, null);
    }

    /**
     * Creates a publisher with change-log and watch integration.
     *
     * @param store          durable version store
     * @param changeLogStore optional change-log store
     * @param watchManager   optional watch manager
     */
    public ReleasePublisher(ReleaseStore store, CortexChangeLogStore changeLogStore, WatchManager watchManager) {
        this(store, changeLogStore, null, watchManager);
    }

    /**
     * Creates a publisher with change-log and guard integration.
     *
     * @param store          durable version store
     * @param changeLogStore optional change-log store
     * @param cortexGuard    optional Cortex guard
     */
    public ReleasePublisher(ReleaseStore store, CortexChangeLogStore changeLogStore, CortexGuard cortexGuard) {
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
    public ReleasePublisher(ReleaseStore store, CortexChangeLogStore changeLogStore, CortexGuard cortexGuard,
            WatchManager watchManager) {
        this.store = store;
        this.changeLogStore = changeLogStore;
        this.cortexGuard = cortexGuard;
        this.watchManager = watchManager;
    }

    /**
     * Generates a version from the server clock; callers cannot choose a version through this operation.
     *
     * @param clock server-owned clock
     * @return monotonically generated release version
     */
    private static String generateVersion(Clock clock) {
        Objects.requireNonNull(clock, "Server clock");
        long instant = RELEASE_CLOCK.updateAndGet(previous -> Math.max(clock.millis(), previous + 1));
        return DateTimeFormatter.ofPattern("'v'uuuu.MM.dd.HHmmss.SSS").withZone(clock.getZone())
                .format(Instant.ofEpochMilli(instant));
    }

    /**
     * Creates a server-versioned release draft. The supplied creator must be the authenticated operator, and the host
     * must not expose caller-controlled creator or version fields.
     *
     * @param record tenant-qualified release record to validate and insert
     * @return persisted release record
     * @throws IllegalArgumentException if a new draft supplies a version rather than using the host allocator
     */
    public Release create(Release record) {
        if (record == null || record.getVersion() != null) {
            throw new IllegalArgumentException("A new draft cannot supply its own version");
        }
        Release draft = JsonKit.toPojo(JsonKit.toJsonString(record), Release.class);
        draft.setVersion(generateVersion(Clock.systemDefaultZone()));
        draft = prepare(draft, EnumValue.Phase.DRAFT, false);
        enforce("draft", draft);
        Release saved = store.create(draft);
        emit(RELEASE_DRAFT_EVENT, saved);
        return saved;
    }

    /**
     * Saves a release draft without making it current.
     *
     * @param record release record
     * @return persisted draft record
     */
    public Release draft(Release record) {
        Release prepared = prepare(record, EnumValue.Phase.DRAFT, false);
        enforce("draft", prepared);
        Release saved = store.save(prepared);
        emit(RELEASE_DRAFT_EVENT, saved);
        return saved;
    }

    /**
     * Prepares the platform version for a frozen approved batch, without changing the active version. The host must
     * atomically persist it with selected application pointers and its durable notification outbox.
     *
     * @param rollout rollout containing the frozen, approved publication manifest
     * @return release record prepared from the verified rollout
     * @throws IllegalArgumentException if approved publication intent or authenticated creator is missing
     * @throws IllegalStateException if the active baseline changed or the allocated version conflicts
     */
    public Release preparePublication(Rollout rollout) {
        Plan plan = Plan.restore(rollout);
        if (!State.PUBLISHING.name().equals(rollout.getState())) {
            throw new IllegalArgumentException("Publication requires an approved execution intent");
        }
        Release active = store.current(plan.getTenant_id(), EnumValue.Track.normalize(null));
        if (!Objects.equals(plan.getBaseline(), active == null ? null : active.getVersion())) {
            throw new IllegalStateException("Publication baseline changed; review a newly frozen plan");
        }
        if (rollout.getCreator() == null || rollout.getCreator().isBlank()) {
            throw new IllegalArgumentException("Authenticated rollout creator is required");
        }
        Release existing = store.find(plan.getTenant_id(), plan.getVersion(), EnumValue.Track.normalize(null));
        if (existing != null && (!EnumValue.Phase.DRAFT.matches(existing.getStatus())
                || !Objects.equals(existing.getCreator(), rollout.getCreator())
                || existing.getMetadata() != null && !existing.getMetadata().equals(plan.toJson()))) {
            throw new IllegalStateException("Publication version conflicts with the existing draft");
        }
        Release record = existing == null ? new Release()
                : JsonKit.toPojo(JsonKit.toJsonString(existing), Release.class);
        record.setTenant_id(plan.getTenant_id());
        record.setVersion(plan.getVersion());
        record.setTrack(EnumValue.Track.normalize(null));
        record.setStatus(EnumValue.Phase.DRAFT.key());
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
     * Deletes a release record.
     *
     * @param tenantId release tenantId
     * @param track    release track
     * @param version  release version
     * @return deleted release record or {@code null}
     */
    public Release delete(String tenantId, String track, String version) {
        enforce("delete", tenantId, track, version, null);
        Release deleted = store.delete(tenantId, version, track);
        emit(RELEASE_DELETE_EVENT, deleted);
        return deleted;
    }

    /**
     * Resolves the current release for a tenantId and track.
     *
     * @param tenantId release tenantId
     * @param track    release track
     * @return current release record or {@code null}
     */
    public Release resolveCurrent(String tenantId, String track) {
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
    private Release prepare(Release record, EnumValue.Phase status, boolean published) {
        Release prepared = record == null ? new Release() : JsonKit.toPojo(JsonKit.toJsonString(record), Release.class);
        prepared.setTrack(EnumValue.Track.normalize(prepared.getTrack()));
        prepared.setStatus(status.key());
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
    private void emit(String action, Release record) {
        appendChangeLog(action, record);
        publishWatch(action, record);
    }

    /**
     * Appends one version-domain event to the change log.
     *
     * @param action event action
     * @param record release record
     */
    private void appendChangeLog(String action, Release record) {
        if (changeLogStore == null || record == null) {
            return;
        }
        CortexChangeRecord change = new CortexChangeRecord();
        change.setDomain("version");
        change.setAction(action);
        change.setResourceType("VERSION");
        change.setResourceId(EnumValue.Track.normalize(record.getTrack()) + Symbol.COLON + record.getVersion());
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
    private void publishWatch(String action, Release record) {
        if (watchManager == null || record == null) {
            return;
        }
        watchManager.notifyTyped(
                watchKey(record),
                toAssets(record),
                action,
                "watch:version:" + EnumValue.Track.normalize(record.getTrack()),
                RELEASE_SOURCE,
                summary(action));
    }

    /**
     * Converts a release record to a version asset for watch notifications.
     *
     * @param record release record
     * @return version asset
     */
    private ReleaseAssets toAssets(Release record) {
        ReleaseAssets asset = new ReleaseAssets();
        asset.setId(record.getId() == null || record.getId().isBlank() ? record.getVersion() : record.getId());
        asset.setTenant_id(record.getTenant_id());
        asset.setVersion(record.getVersion());
        asset.setName(record.getTitle());
        asset.setDescription(record.getDescription());
        asset.metadata(record.getMetadata());
        asset.semver(record.getVersion());
        asset.setStatus(record.getStatus());
        asset.releasedAt(record.getPublished());
        return asset;
    }

    /**
     * Builds the watch key for one release record.
     *
     * @param record release record
     * @return watch key
     */
    private String watchKey(Release record) {
        return "version:" + record.getTenant_id() + Symbol.COLON + EnumValue.Track.normalize(record.getTrack())
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
            case RELEASE_DRAFT_EVENT -> "Release drafted";
            case RELEASE_PUBLISH_EVENT -> "Release published";
            case RELEASE_ROLLBACK_EVENT -> "Release rolled back";
            case RELEASE_DEPRECATE_EVENT -> "Release deprecated";
            case RELEASE_DELETE_EVENT -> "Release deleted";
            default -> "Version changed";
        };
    }

    /**
     * Applies guard policy to one release record.
     *
     * @param action guarded action
     * @param record release record
     */
    private void enforce(String action, Release record) {
        if (record == null) {
            enforce(action, null, null, null, null);
            return;
        }
        enforce(action, record.getTenant_id(), record.getTrack(), record.getVersion(), record.getStatus());
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
    private void enforce(String action, String tenantId, String track, String version, Integer status) {
        if (cortexGuard == null) {
            return;
        }
        GuardContext context = new GuardContext();
        context.setDomain("version");
        context.setAction(action);
        context.setResourceType("VERSION");
        context.setResourceId(EnumValue.Track.normalize(track) + Symbol.COLON + version);
        context.tenant_id(tenantId);
        context.putAttribute("track", EnumValue.Track.normalize(track));
        context.putAttribute("version", version);
        context.putAttribute("status", status);
        cortexGuard.enforce(context);
    }

}
