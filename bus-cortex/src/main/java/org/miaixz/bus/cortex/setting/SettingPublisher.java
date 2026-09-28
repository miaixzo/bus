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

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.Keying.SettingSpec;
import org.miaixz.bus.cortex.builtin.SettingGenerator;
import org.miaixz.bus.cortex.magic.identity.CortexIdentity;
import org.miaixz.bus.cortex.magic.watch.WatchManager;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemNormalizer;
import org.miaixz.bus.cortex.setting.item.StoreBackedItemStore;
import org.miaixz.bus.cortex.setting.revision.Revision;
import org.miaixz.bus.cortex.setting.revision.RevisionNumbers;
import org.miaixz.bus.cortex.setting.revision.RevisionStore;
import org.miaixz.bus.cortex.setting.secret.SecretCodec;

/**
 * Coordinates current-state changes, immutable revisions and notifications.
 *
 * @author Kimi Liu
 */
public class SettingPublisher {

    /**
     * Store used for durable current item state.
     */
    private final StoreBackedItemStore entryStore;

    /**
     * Append-only revision store.
     */
    private final RevisionStore revisionStore;

    /**
     * Manager notified after successful state changes.
     */
    private final WatchManager watchManager;

    /**
     * Codec used to protect encrypted content.
     */
    private final SecretCodec secretCodec;

    /**
     * Maximum retained revisions per item.
     */
    private final int maxRevisions;

    /**
     * Key strategy used for setting watch notifications.
     */
    private final Keying<SettingSpec> keying;

    /**
     * Creates a publisher with default revision retention and key strategy.
     *
     * @param entryStore    current item store
     * @param revisionStore immutable revision store
     * @param watchManager  watch notification manager
     * @param secretCodec   protected-content codec
     */
    public SettingPublisher(StoreBackedItemStore entryStore, RevisionStore revisionStore, WatchManager watchManager,
            SecretCodec secretCodec) {
        this(entryStore, revisionStore, watchManager, secretCodec, 10, SettingGenerator.INSTANCE);
    }

    /**
     * Creates a publisher with explicit revision retention.
     *
     * @param entryStore    current item store
     * @param revisionStore immutable revision store
     * @param watchManager  watch notification manager
     * @param secretCodec   protected-content codec
     * @param maxRevisions  maximum retained revisions per item
     */
    public SettingPublisher(StoreBackedItemStore entryStore, RevisionStore revisionStore, WatchManager watchManager,
            SecretCodec secretCodec, int maxRevisions) {
        this(entryStore, revisionStore, watchManager, secretCodec, maxRevisions, SettingGenerator.INSTANCE);
    }

    /**
     * Creates a publisher with explicit retention and key strategy.
     *
     * @param entryStore    current item store
     * @param revisionStore immutable revision store
     * @param watchManager  watch notification manager
     * @param secretCodec   protected-content codec
     * @param maxRevisions  maximum retained revisions per item
     * @param keying        setting key strategy
     */
    public SettingPublisher(StoreBackedItemStore entryStore, RevisionStore revisionStore, WatchManager watchManager,
            SecretCodec secretCodec, int maxRevisions, Keying<SettingSpec> keying) {
        if (entryStore == null || revisionStore == null || watchManager == null || secretCodec == null) {
            throw new IllegalArgumentException("Setting publisher dependencies are required");
        }
        this.entryStore = entryStore;
        this.revisionStore = revisionStore;
        this.watchManager = watchManager;
        this.secretCodec = secretCodec;
        this.maxRevisions = Math.max(maxRevisions, 1);
        this.keying = keying == null ? SettingGenerator.INSTANCE : keying;
    }

    /**
     * Publishes inline content for one setting coordinate.
     *
     * @param space   space identifier
     * @param group   item group
     * @param code    item code
     * @param content source content
     * @return persisted current item
     */
    public Item publish(String space, String group, String code, String content) {
        Item entry = new Item();
        entry.setSpace_id(CortexIdentity.space(space));
        entry.setGroup(group);
        entry.setCode(code);
        entry.setContent(content);
        return publish(entry);
    }

    /**
     * Publishes one fully described item.
     *
     * @param entry item to publish
     * @return persisted current item
     */
    public Item publish(Item entry) {
        return publish(entry, null);
    }

    /**
     * Publishes one item derived from an optional source revision.
     *
     * @param entry     item to publish
     * @param source_id source revision identifier, when applicable
     * @return persisted current item
     */
    private Item publish(Item entry, String source_id) {
        Item prepared = ItemNormalizer.normalize(entry);
        requireCoordinates(prepared);
        Item current = entryStore.find(
                prepared.getTenant_id(),
                prepared.getSpace_id(),
                prepared.getProfile_id(),
                prepared.getGroup(),
                prepared.getCode());
        if (current != null && current.getChecksum() != null && current.getChecksum().equals(prepared.getChecksum())) {
            return current;
        }
        Revision latest = current == null ? null : revisionStore.latest(current.getTenant_id(), current.getId());
        String revisionNo = RevisionNumbers.next(latest == null ? null : latest.getRevision());
        prepared.setEdition(next(current == null ? null : current.getEdition()));
        prepared.setGeneration(next(current == null ? null : current.getGeneration()));
        if (prepared.getId() == null) {
            prepared.setId(ID.objectId());
        }
        String plainContent = prepared.getContent();
        if (ItemNormalizer.isEncryptedFlagEnabled(prepared.getEncrypted()) && plainContent != null) {
            prepared.setContent(secretCodec.encrypt(plainContent));
            prepared.setChecksum(ItemNormalizer.checksum(prepared));
        }
        Revision revision = snapshot(prepared, revisionNo, source_id, Revision.Operation.UPSERT);
        prepared.setStable_id(revision.getId());
        revisionStore.save(revision);
        Item stored = entryStore.save(prepared);
        revisionStore.retainLatest(stored.getTenant_id(), stored.getId(), maxRevisions);
        notify(stored, plainContent, "durable-publish", "Setting published");
        return stored;
    }

    /**
     * Deletes one current item after appending a tombstone revision.
     *
     * @param space   space identifier
     * @param group   item group
     * @param code    item code
     * @param profile optional profile identifier
     * @return deleted item, or {@code null} when absent
     */
    public Item delete(String space, String group, String code, String profile) {
        Item current = entryStore.find(null, CortexIdentity.space(space), profile, group, code);
        if (current == null) {
            return null;
        }
        Revision latest = revisionStore.latest(current.getTenant_id(), current.getId());
        Revision tombstone = snapshot(
                current,
                RevisionNumbers.next(latest == null ? null : latest.getRevision()),
                null,
                Revision.Operation.DELETE);
        tombstone.setContent(null);
        revisionStore.save(tombstone);
        Item deleted = entryStore.delete(current.getTenant_id(), current.getId());
        revisionStore.retainLatest(current.getTenant_id(), current.getId(), maxRevisions);
        notify(current, null, "durable-delete", "Setting deleted");
        return deleted;
    }

    /**
     * Republishes content from one historical revision.
     *
     * @param space    space identifier
     * @param group    item group
     * @param code     item code
     * @param profile  optional profile identifier
     * @param revision item-scoped revision number
     * @return republished item, or {@code null} when the item or revision is absent
     */
    public Item rollback(String space, String group, String code, String profile, String revision) {
        Item current = entryStore.find(null, CortexIdentity.space(space), profile, group, code);
        if (current == null) {
            return null;
        }
        Revision source = revisionStore.find(current.getTenant_id(), current.getId(), revision);
        if (source == null) {
            return null;
        }
        current.setContent(source.getContent());
        current.setFormat(source.getFormat());
        current.setSource(source.getSource());
        current.setSpec(source.getSpec());
        current.setExposure(source.getExposure());
        current.setEncrypted(source.getEncrypted());
        current.setLabels(source.getLabels());
        current.setExtension(source.getExtension());
        current.setDescription(source.getDescription());
        current.setChecksum(null);
        return publish(current, source.getId());
    }

    /**
     * Creates an immutable snapshot from the supplied current item state.
     *
     * @param item       current item
     * @param revisionNo item-scoped revision number
     * @param source_id  source revision identifier, when applicable
     * @param operation  snapshot operation
     * @return immutable revision snapshot
     */
    private Revision snapshot(Item item, String revisionNo, String source_id, Revision.Operation operation) {
        return Revision.builder().id(ID.objectId()).tenant_id(item.getTenant_id()).item_id(item.getId())
                .rollout_id(item.getRollout_id()).source_id(source_id).revision(revisionNo).edition(item.getEdition())
                .baseline(item.getGeneration()).content(item.getContent()).format(item.getFormat())
                .source(item.getSource()).spec(item.getSpec()).exposure(item.getExposure())
                .encrypted(item.getEncrypted()).labels(item.getLabels()).extension(item.getExtension())
                .checksum(item.getChecksum()).operation(operation.name()).description(item.getDescription())
                .status(item.getStatus()).creator(item.getCreator()).created(item.getCreated())
                .modifier(item.getModifier()).modified(item.getModified()).build();
    }

    /**
     * Notifies subscribers after a committed item change.
     *
     * @param item      changed item
     * @param content   delivered plain content
     * @param eventType notification event type
     * @param summary   notification summary
     */
    private void notify(Item item, String content, String eventType, String summary) {
        watchManager.notifySetting(
                watchKey(item.getSpace_id(), item.getGroup(), item.getCode(), item.getProfile_id()),
                content,
                "setting-center",
                eventType,
                summary);
    }

    /**
     * Builds the watch key for one item coordinate.
     *
     * @param space   space identifier
     * @param group   item group
     * @param code    item code
     * @param profile optional profile identifier
     * @return watch key
     */
    private String watchKey(String space, String group, String code, String profile) {
        return keying.key(SettingSpec.watch(space, group, code, profile));
    }

    /**
     * Advances a nullable counter.
     *
     * @param value current value
     * @return {@code 1} for {@code null}, otherwise the incremented value
     */
    private long next(Long value) {
        return value == null ? 1L : value + 1L;
    }

    /**
     * Verifies that the required item coordinates are present.
     *
     * @param item item to validate
     */
    private void requireCoordinates(Item item) {
        if (item.getSpace_id() == null || item.getGroup() == null || item.getCode() == null) {
            throw new IllegalArgumentException("Item space_id, group and code are required");
        }
    }
}
