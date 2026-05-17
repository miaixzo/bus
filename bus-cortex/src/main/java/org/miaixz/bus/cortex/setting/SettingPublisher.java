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
package org.miaixz.bus.cortex.setting;

import java.util.List;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.Keying.SettingSpec;
import org.miaixz.bus.cortex.builtin.SettingGenerator;
import org.miaixz.bus.cortex.magic.event.CortexChangeLogStore;
import org.miaixz.bus.cortex.magic.event.CortexChangeRecord;
import org.miaixz.bus.cortex.magic.identity.CortexIdentity;
import org.miaixz.bus.cortex.magic.watch.WatchManager;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemBindingProjection;
import org.miaixz.bus.cortex.setting.item.ItemNormalizer;
import org.miaixz.bus.cortex.setting.item.ItemRevisionNumbers;
import org.miaixz.bus.cortex.setting.item.StoreBackedItemStore;
import org.miaixz.bus.cortex.setting.item.revision.ItemRevision;
import org.miaixz.bus.cortex.setting.item.revision.ItemRevisionStore;
import org.miaixz.bus.cortex.setting.secret.SecretCodec;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;

/**
 * Setting publisher responsible for current-state updates and item revision history.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SettingPublisher {

    /**
     * Watch-event source name used for durable setting mutations.
     */
    public static final String SETTING_DURABLE_SOURCE = "setting-durable";

    /**
     * Watch-event type emitted after a durable publish.
     */
    private static final String DURABLE_PUBLISH_EVENT = "durable-publish";

    /**
     * Watch-event type emitted after a rollback publish.
     */
    private static final String ROLLBACK_EVENT = "rollback";

    /**
     * Watch-event type emitted after durable deletion.
     */
    private static final String DURABLE_DELETE_EVENT = "durable-delete";

    /**
     * Current-state setting store.
     */
    private final StoreBackedItemStore entryStore;

    /**
     * ItemRevision history store.
     */
    private final ItemRevisionStore revisionStore;

    /**
     * Watch manager notified after publish and delete operations.
     */
    private final WatchManager watchManager;

    /**
     * Secret codec used when the entry stores encrypted content.
     */
    private final SecretCodec secretCodec;

    /**
     * Maximum number of retained revisions per setting entry.
     */
    private final int maxRevisions;

    /**
     * Optional reliable change log used as a first-stage outbox.
     */
    private final CortexChangeLogStore changeLogStore;

    /**
     * Setting-domain key strategy.
     */
    private final Keying<SettingSpec> keying;

    /**
     * Creates a SettingPublisher with default history retention.
     *
     * @param entryStore    current-state setting store
     * @param revisionStore revision history store
     * @param watchManager  watch manager
     * @param secretCodec   secret codec
     */
    public SettingPublisher(StoreBackedItemStore entryStore, ItemRevisionStore revisionStore, WatchManager watchManager,
            SecretCodec secretCodec) {
        this(entryStore, revisionStore, watchManager, secretCodec, 10, SettingGenerator.INSTANCE, null);
    }

    /**
     * Creates a SettingPublisher with explicit history retention.
     *
     * @param entryStore    current-state setting store
     * @param revisionStore revision history store
     * @param watchManager  watch manager
     * @param secretCodec   secret codec
     * @param maxRevisions  max revisions to retain
     */
    public SettingPublisher(StoreBackedItemStore entryStore, ItemRevisionStore revisionStore, WatchManager watchManager,
            SecretCodec secretCodec, int maxRevisions) {
        this(entryStore, revisionStore, watchManager, secretCodec, maxRevisions, SettingGenerator.INSTANCE, null);
    }

    /**
     * Creates a SettingPublisher with explicit history retention and optional outbox recording.
     *
     * @param entryStore     current-state setting store
     * @param revisionStore  revision history store
     * @param watchManager   watch manager
     * @param secretCodec    secret codec
     * @param maxRevisions   max revisions to retain
     * @param changeLogStore optional outbox store
     */
    public SettingPublisher(StoreBackedItemStore entryStore, ItemRevisionStore revisionStore, WatchManager watchManager,
            SecretCodec secretCodec, int maxRevisions, CortexChangeLogStore changeLogStore) {
        this(entryStore, revisionStore, watchManager, secretCodec, maxRevisions, SettingGenerator.INSTANCE,
                changeLogStore);
    }

    /**
     * Creates a SettingPublisher with explicit history retention, key strategy, and optional outbox recording.
     *
     * @param entryStore     current-state setting store
     * @param revisionStore  revision history store
     * @param watchManager   watch manager
     * @param secretCodec    secret codec
     * @param maxRevisions   max revisions to retain
     * @param keying         setting-domain key strategy
     * @param changeLogStore optional outbox store
     */
    public SettingPublisher(StoreBackedItemStore entryStore, ItemRevisionStore revisionStore, WatchManager watchManager,
            SecretCodec secretCodec, int maxRevisions, Keying<SettingSpec> keying,
            CortexChangeLogStore changeLogStore) {
        this.entryStore = entryStore;
        this.revisionStore = revisionStore;
        this.watchManager = watchManager;
        this.secretCodec = secretCodec;
        this.maxRevisions = maxRevisions;
        this.changeLogStore = changeLogStore;
        this.keying = keying == null ? SettingGenerator.INSTANCE : keying;
    }

    /**
     * Publishes a plain inline setting value.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param content   inline content
     * @return stored setting entry
     */
    public Item publish(String namespace, String group, String data_id, String content) {
        Item entry = new Item();
        entry.setNamespace_id(CortexIdentity.namespace(namespace));
        entry.setGroup(group);
        entry.setData_id(data_id);
        entry.setContent(content);
        entry.setSource(ItemNormalizer.INLINE_SOURCE);
        return publish(entry);
    }

    /**
     * Publishes a setting entry, assigns a new revision number, records history and notifies watchers.
     *
     * @param entry setting entry to publish
     * @return stored setting entry
     */
    public Item publish(Item entry) {
        return publish(entry, false, SETTING_DURABLE_SOURCE, DURABLE_PUBLISH_EVENT, "Setting published");
    }

    /**
     * Publishes a setting entry and optionally forces a new revision even when content checksum matches current state.
     *
     * @param entry         setting entry to publish
     * @param forceRevision whether checksum idempotency should be bypassed
     * @param source        logical event source recorded for watchers
     * @param eventType     logical event type recorded for watchers
     * @param summary       human-readable event summary recorded for watchers
     * @return stored setting entry
     */
    private Item publish(Item entry, boolean forceRevision, String source, String eventType, String summary) {
        Item prepared = prepare(entry);
        String profile = ItemBindingProjection.firstProfileId(prepared);
        Logger.info(
                true,
                "Cortex",
                "Setting publish started: namespace={}, group={}, dataId={}, profile={}, forceRevision={}, contentChars={}",
                prepared.getNamespace_id(),
                prepared.getGroup(),
                prepared.getData_id(),
                profile,
                forceRevision,
                prepared.getContent() == null ? 0 : prepared.getContent().length());
        Item current = entryStore.find(prepared.getNamespace_id(), prepared.getGroup(), prepared.getData_id(), profile);
        if (!matchesExactProfile(current, profile)) {
            current = null;
        }
        if (!forceRevision && current != null && current.getChecksum() != null
                && current.getChecksum().equals(prepared.getChecksum())) {
            Logger.info(
                    false,
                    "Cortex",
                    "Setting publish skipped: namespace={}, group={}, dataId={}, profile={}, reason={}, revision={}",
                    prepared.getNamespace_id(),
                    prepared.getGroup(),
                    prepared.getData_id(),
                    profile,
                    "checksumUnchanged",
                    current.getRevision());
            return current;
        }
        prepared.setRevision(ItemRevisionNumbers.next(current == null ? null : current.getRevision()));
        String notifyContent = prepared.getContent();
        if (ItemNormalizer.isEncryptedFlagEnabled(prepared.getEncrypted()) && prepared.getContent() != null) {
            prepared.setContent(secretCodec.encrypt(prepared.getContent()));
        }
        Item stored = entryStore.save(prepared);
        ItemRevision previous = current == null ? null
                : revisionStore.latest(current.getNamespace_id(), current.getGroup(), current.getData_id(), profile);
        ItemRevision revision = toRevision(stored, previous);
        try {
            revisionStore.save(revision);
            revisionStore.retainLatest(
                    stored.getNamespace_id(),
                    stored.getGroup(),
                    stored.getData_id(),
                    profile,
                    maxRevisions);
        } catch (RuntimeException | Error e) {
            Logger.error(
                    false,
                    "Cortex",
                    e,
                    "Setting revision persistence failed: namespace={}, group={}, dataId={}, profile={}, revision={}, exception={}",
                    stored.getNamespace_id(),
                    stored.getGroup(),
                    stored.getData_id(),
                    profile,
                    stored.getRevision(),
                    e.getClass().getSimpleName());
            compensateCurrentState(current, stored, revision, e);
            throw e;
        }
        appendChangeLog("publish", stored, revision);
        List<String> profiles = ItemBindingProjection.normalizedProfileIds(stored);
        if (profiles == null || profiles.isEmpty()) {
            watchManager.notifySetting(
                    watchKey(stored.getNamespace_id(), stored.getGroup(), stored.getData_id(), null),
                    notifyContent,
                    source,
                    eventType,
                    summary);
        } else {
            for (String profileId : profiles) {
                watchManager.notifySetting(
                        watchKey(stored.getNamespace_id(), stored.getGroup(), stored.getData_id(), profileId),
                        notifyContent,
                        source,
                        eventType,
                        summary);
            }
        }
        Logger.info(
                false,
                "Cortex",
                "Setting publish completed: namespace={}, group={}, dataId={}, profileCount={}, revision={}, encrypted={}",
                stored.getNamespace_id(),
                stored.getGroup(),
                stored.getData_id(),
                profiles == null ? 0 : profiles.size(),
                stored.getRevision(),
                stored.getEncrypted());
        return stored;
    }

    /**
     * Deletes one setting entry and notifies watchers.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return deleted entry or {@code null}
     */
    public Item delete(String namespace, String group, String data_id, String profile) {
        Logger.info(
                true,
                "Cortex",
                "Setting delete started: namespace={}, group={}, dataId={}, profile={}",
                namespace,
                group,
                data_id,
                profile);
        Item deleted = entryStore.delete(namespace, group, data_id, profile);
        if (deleted != null) {
            appendChangeLog("delete", deleted, null);
            watchManager.notifySetting(
                    watchKey(namespace, group, data_id, profile),
                    null,
                    SETTING_DURABLE_SOURCE,
                    DURABLE_DELETE_EVENT,
                    "Setting deleted");
        }
        Logger.info(
                false,
                "Cortex",
                "Setting delete completed: namespace={}, group={}, dataId={}, profile={}, deleted={}",
                namespace,
                group,
                data_id,
                profile,
                deleted != null);
        return deleted;
    }

    /**
     * Re-publishes the contents of a historical revision as the latest current state.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @param revision  historical revision
     * @return newly stored current entry or {@code null} when the revision does not exist
     */
    public Item rollback(String namespace, String group, String data_id, String profile, String revision) {
        Logger.info(
                true,
                "Cortex",
                "Setting rollback started: namespace={}, group={}, dataId={}, profile={}, revision={}",
                namespace,
                group,
                data_id,
                profile,
                revision);
        ItemRevision snapshot = revisionStore.find(namespace, group, data_id, profile, revision);
        if (snapshot == null) {
            Logger.warn(
                    false,
                    "Cortex",
                    "Setting rollback skipped: namespace={}, group={}, dataId={}, profile={}, revision={}, reason={}",
                    namespace,
                    group,
                    data_id,
                    profile,
                    revision,
                    "missingRevision");
            return null;
        }
        Item entry = new Item();
        entry.setNamespace_id(snapshot.getNamespace_id());
        entry.setGroup(snapshot.getGroup());
        entry.setData_id(snapshot.getData_id());
        ItemBindingProjection.normalizeProfileIdsInto(entry, ItemBindingProjection.normalizedProfileIds(snapshot));
        ItemBindingProjection.normalizeAppIdsInto(entry, ItemBindingProjection.normalizedAppIds(snapshot));
        entry.setContent(snapshot.getContent());
        entry.setSource(snapshot.getSource());
        entry.setSpec(snapshot.getSpec());
        ItemBindingProjection.copyExtensionInto(entry, snapshot.getExtension());
        entry.setFormat(snapshot.getFormat());
        entry.setExposure(snapshot.getExposure());
        entry.setEncrypted(snapshot.getEncrypted());
        entry.setRule(snapshot.getRule());
        entry.setChecksum(snapshot.getChecksum());
        if (ItemNormalizer.isEncryptedFlagEnabled(entry.getEncrypted()) && entry.getContent() != null) {
            entry.setContent(secretCodec.decrypt(entry.getContent()));
        }
        Item prepared = ItemNormalizer.normalize(entry);
        Item published = publish(prepared, true, SETTING_DURABLE_SOURCE, ROLLBACK_EVENT, "Setting rolled back");
        if (published != null) {
            ItemRevision latest = revisionStore.latest(namespace, group, data_id, profile);
            if (latest != null) {
                revisionStore.markRollback(namespace, group, data_id, profile, latest.getRevision(), revision);
            }
        }
        Logger.info(
                false,
                "Cortex",
                "Setting rollback completed: namespace={}, group={}, dataId={}, profile={}, fromRevision={}, newRevision={}",
                namespace,
                group,
                data_id,
                profile,
                revision,
                published == null ? null : published.getRevision());
        return published;
    }

    /**
     * Restores the previous current-state snapshot when revision persistence fails after current-state save.
     *
     * @param previous previous current-state entry
     * @param stored   newly stored current-state entry
     * @param revision revision snapshot that failed to complete
     * @param failure  original publish failure
     */
    private void compensateCurrentState(Item previous, Item stored, ItemRevision revision, Throwable failure) {
        try {
            if (stored != null && revision != null) {
                revisionStore.delete(
                        revision.getNamespace_id(),
                        revision.getGroup(),
                        revision.getData_id(),
                        ItemBindingProjection.firstProfileId(revision),
                        revision.getRevision());
            }
            if (previous != null) {
                entryStore.save(previous);
            } else if (stored != null) {
                entryStore.delete(
                        stored.getNamespace_id(),
                        stored.getGroup(),
                        stored.getData_id(),
                        ItemBindingProjection.firstProfileId(stored));
            }
        } catch (RuntimeException | Error compensationFailure) {
            Logger.error(
                    false,
                    "Cortex",
                    compensationFailure,
                    "Setting publish compensation failed: namespace={}, group={}, dataId={}, profile={}, exception={}",
                    stored == null ? null : stored.getNamespace_id(),
                    stored == null ? null : stored.getGroup(),
                    stored == null ? null : stored.getData_id(),
                    stored == null ? null : ItemBindingProjection.firstProfileId(stored),
                    compensationFailure.getClass().getSimpleName());
            failure.addSuppressed(compensationFailure);
        }
    }

    /**
     * Applies publisher defaults before the entry is written to current state.
     *
     * @param entry incoming setting entry
     * @return normalized entry ready for persistence
     */
    private Item prepare(Item entry) {
        return ItemNormalizer.normalize(entry, keying);
    }

    /**
     * Converts the current stored entry into a revision snapshot.
     *
     * @param entry    current setting entry
     * @param previous previous recorded revision, or {@code null} for the initial revision
     * @return revision snapshot recorded for history and rollback
     */
    private ItemRevision toRevision(Item entry, ItemRevision previous) {
        ItemRevision revision = ItemRevision.builder().item_id(entry.getId()).namespace_id(entry.getNamespace_id())
                .group(entry.getGroup()).data_id(entry.getData_id())
                .profile_ids(ItemBindingProjection.normalizedProfileIds(entry))
                .app_ids(ItemBindingProjection.normalizedAppIds(entry)).content(entry.getContent())
                .source(entry.getSource()).spec(entry.getSpec()).extension(entry.getExtension())
                .format(entry.getFormat()).exposure(entry.getExposure()).encrypted(entry.getEncrypted())
                .rule(entry.getRule()).checksum(entry.getChecksum()).status(entry.getStatus())
                .diff(previous == null ? "initial" : diff(previous, entry)).created(System.currentTimeMillis()).build();
        revision.setRevision(entry.getRevision());
        return revision;
    }

    /**
     * Produces a lightweight diff summary against the previous revision.
     *
     * @param previous previous revision
     * @param current  current entry
     * @return diff summary
     */
    private String diff(ItemRevision previous, Item current) {
        if (previous == null || current == null) {
            return "initial";
        }
        if (!StringKit.equals(previous.getChecksum(), current.getChecksum())) {
            return "content";
        }
        if (!StringKit.equals(previous.getSource(), current.getSource())
                || !StringKit.equals(previous.getSpec(), current.getSpec())) {
            return "source";
        }
        if (!StringKit.equals(previous.getRule(), current.getRule())) {
            return "gray";
        }
        return "metadata";
    }

    /**
     * Appends one setting-domain change record to the optional change log.
     *
     * @param action   setting event action
     * @param item     current setting item
     * @param revision current setting revision
     */
    private void appendChangeLog(String action, Item item, ItemRevision revision) {
        if (changeLogStore == null || item == null) {
            return;
        }
        CortexChangeRecord record = new CortexChangeRecord();
        record.setDomain("setting");
        record.setAction(action);
        record.setResourceType("ITEM");
        record.setResourceId(
                profileScope(
                        item.getNamespace_id(),
                        item.getGroup(),
                        item.getData_id(),
                        ItemBindingProjection.firstProfileId(item)));
        record.setNamespace_id(item.getNamespace_id());
        record.setPayload(JsonKit.toJsonString(revision == null ? item : revision));
        record.setSequence(ItemRevisionNumbers.sortKey(item.getRevision()));
        record.setIdempotencyKey("setting:" + action + ":" + record.getResourceId() + ":" + item.getRevision());
        changeLogStore.append(record);
    }

    /**
     * Returns whether an item is bound exactly to the supplied profile.
     *
     * @param entry   setting item
     * @param profile normalized profile identifier
     * @return {@code true} when the item is bound to the profile only
     */
    private boolean matchesExactProfile(Item entry, String profile) {
        if (entry == null) {
            return false;
        }
        List<String> profiles = ItemBindingProjection.normalizedProfileIds(entry);
        if (StringKit.isEmpty(profile)) {
            return profiles == null || profiles.isEmpty();
        }
        return profiles != null && profiles.contains(profile.trim().toLowerCase());
    }

    /**
     * Builds one watch key.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param dataId    setting data identifier
     * @param profile   optional profile
     * @return watch key
     */
    private String watchKey(String namespace, String group, String dataId, String profile) {
        return keying.key(SettingSpec.watch(namespace, group, dataId, profile));
    }

    /**
     * Builds one logical profile scope.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param dataId    setting data identifier
     * @param profile   optional profile
     * @return profile scope key
     */
    private String profileScope(String namespace, String group, String dataId, String profile) {
        return keying.key(SettingSpec.profileScope(namespace, group, dataId, profile));
    }

}
