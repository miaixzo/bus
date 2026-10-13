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

import java.util.Objects;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.Keying.SettingSpec;
import org.miaixz.bus.cortex.builtin.SettingGenerator;
import org.miaixz.bus.cortex.magic.watch.WatchManager;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemNormalizer;
import org.miaixz.bus.cortex.setting.item.StoreBackedItemStore;
import org.miaixz.bus.cortex.setting.revision.Revision;
import org.miaixz.bus.cortex.setting.revision.RevisionNumbers;
import org.miaixz.bus.cortex.setting.revision.RevisionStore;
import org.miaixz.bus.cortex.setting.secret.SecretCodec;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.cortex.setting.rollout.Plan;
import org.miaixz.bus.cortex.setting.rollout.Rollout;

/** Stages configuration revisions; application release activation is committed by the host transaction. */
public class SettingPublisher {

    private final StoreBackedItemStore entryStore;
    private final RevisionStore revisionStore;
    private final SecretCodec secretCodec;
    private final int maxRevisions;

    public SettingPublisher(StoreBackedItemStore entryStore, RevisionStore revisionStore, WatchManager watchManager,
            SecretCodec secretCodec) {
        this(entryStore, revisionStore, watchManager, secretCodec, 10, SettingGenerator.INSTANCE);
    }

    public SettingPublisher(StoreBackedItemStore entryStore, RevisionStore revisionStore, WatchManager watchManager,
            SecretCodec secretCodec, int maxRevisions) {
        this(entryStore, revisionStore, watchManager, secretCodec, maxRevisions, SettingGenerator.INSTANCE);
    }

    public SettingPublisher(StoreBackedItemStore entryStore, RevisionStore revisionStore, WatchManager watchManager,
            SecretCodec secretCodec, int maxRevisions, Keying<SettingSpec> keying) {
        if (entryStore == null || revisionStore == null || secretCodec == null) {
            throw new IllegalArgumentException("Setting publisher dependencies are required");
        }
        this.entryStore = entryStore;
        this.revisionStore = revisionStore;
        this.secretCodec = secretCodec;
        this.maxRevisions = Math.max(maxRevisions, 1);
    }

    /** Stages inline content using complete tenant-qualified item coordinates. */
    public Item publishInline(Item entry, String content) {
        Item copy = copy(entry);
        copy.setContent(content);
        return publish(copy);
    }

    /** Saves a draft only; this compatibility method never changes live pointers or notifies clients. */
    public Item publish(Item entry) {
        return publish(entry, null);
    }

    private Item publish(Item entry, Revision source) {
        synchronized (entryStore) {
            return stage(entry, source);
        }
    }

    private Item stage(Item entry, Revision source) {
        Item prepared = ItemNormalizer.normalize(copy(entry));
        prepared.setChecksum(ItemNormalizer.checksum(prepared));
        Item current = entryStore.find(prepared);
        Revision latest = current == null ? null : revisionStore.latest(current.getTenant_id(), current.getId());
        Revision draft = current == null || current.getDraft_id() == null ? null
                : revisionStore.identity(current.getTenant_id(), current.getId(), current.getDraft_id());
        if (source == null && current != null && draft != null
                && Revision.Operation.UPSERT.name().equals(draft.getOperation())
                && current.getChecksum() != null
                && current.getChecksum().equals(prepared.getChecksum())
                && Objects.equals(current.getEditor(), prepared.getEditor())
                && Objects.equals(current.getPayload(), prepared.getPayload())) {
            return current;
        }
        if (current != null) {
            prepared.setId(current.getId());
            prepared.setStable_id(current.getStable_id());
            prepared.setGray_id(current.getGray_id());
            prepared.setRollout_id(current.getRollout_id());
        } else {
            prepared.setStable_id(null);
            prepared.setGray_id(null);
            prepared.setRollout_id(null);
        }
        String revisionNo = RevisionNumbers.next(latest == null ? null : latest.getRevision());
        prepared.setEdition(next(current == null ? null : current.getEdition()));
        prepared.setGeneration(current == null || current.getGeneration() == null ? 0L : current.getGeneration());
        prepared.setState(current == null || current.getStable_id() == null
                ? Item.State.UNPUBLISHED.name() : Item.State.CHANGED.name());
        prepared.setStatus(current == null ? 1 : current.getStatus());
        if (ItemNormalizer.isEncryptedFlagEnabled(prepared.getEncrypted())) {
            if (!ItemNormalizer.INLINE_SOURCE.equals(prepared.getSource())) {
                throw new IllegalArgumentException("Protected drafts require an inline materialized snapshot");
            }
            prepared.setContent(secretCodec.protect(prepared.getContent()));
            prepared.setPayload(secretCodec.protect(prepared.getPayload()));
            prepared.setSpec(secretCodec.protect(prepared.getSpec()));
            prepared.setExtension(secretCodec.protect(prepared.getExtension()));
            prepared.setChecksum(ItemNormalizer.checksum(prepared));
        }
        Revision revision = snapshot(prepared, revisionNo, source == null ? null : source.getId(),
                Revision.Operation.UPSERT);
        if (source != null) {
            // Preserve the frozen manifest and protection metadata without consulting live relationships.
            revision.setReference(source.getReference());
            revision.setSecrets(source.getSecrets());
        }
        prepared.setDraft_id(revision.getId());
        revisionStore.save(revision);
        Item stored = entryStore.save(prepared);
        revisionStore.retainLatest(stored.getTenant_id(), stored.getId(), maxRevisions);
        return stored;
    }

    /** Stages a deletion tombstone. Published content remains available until the selected release commits. */
    public Item delete(Item coordinates) {
        synchronized (entryStore) {
            return stageDeletion(coordinates);
        }
    }

    private Item stageDeletion(Item coordinates) {
        Item current = entryStore.find(coordinates);
        if (current == null) {
            return null;
        }
        Item staged = copy(current);
        staged.setEdition(next(current.getEdition()));
        staged.setState(current.getStable_id() == null ? Item.State.UNPUBLISHED.name() : Item.State.CHANGED.name());
        Revision latest = revisionStore.latest(current.getTenant_id(), current.getId());
        Revision tombstone = snapshot(staged,
                RevisionNumbers.next(latest == null ? null : latest.getRevision()), null, Revision.Operation.DELETE);
        tombstone.setContent(null);
        tombstone.setPayload(null);
        staged.setDraft_id(tombstone.getId());
        revisionStore.save(tombstone);
        Item deleted = entryStore.save(staged);
        revisionStore.retainLatest(current.getTenant_id(), current.getId(), maxRevisions);
        return deleted;
    }

    /** Stages a new revision from immutable history without activating it or modifying the source. */
    public Item rollback(Item coordinates, String revision) {
        synchronized (entryStore) {
            return restore(coordinates, revision);
        }
    }

    private Item restore(Item coordinates, String revision) {
        Item current = entryStore.find(coordinates);
        if (current == null) {
            return null;
        }
        Revision source = revisionStore.find(current.getTenant_id(), current.getId(), revision);
        if (source == null) {
            return null;
        }
        if (Revision.Operation.DELETE.name().equals(source.getOperation())) {
            throw new IllegalArgumentException("A deletion revision cannot be restored as item content");
        }
        // Use a fresh item so failed publication cannot mutate a cached current state.
        Item restored = Item.builder().id(current.getId()).tenant_id(current.getTenant_id())
                .scope(current.getScope()).app_id(current.getApp_id()).kind(current.getKind()).code(current.getCode())
                .rollout_id(current.getRollout_id()).editor(source.getEditor()).payload(source.getPayload())
                .status(current.getStatus()).creator(current.getCreator()).created(current.getCreated())
                .modifier(current.getModifier()).modified(current.getModified()).build();
        restored.setContent(ItemNormalizer.isEncryptedFlagEnabled(source.getEncrypted()) && source.getContent() != null
                ? secretCodec.decrypt(source.getContent()) : source.getContent());
        restored.setPayload(decode(source.getPayload(), source.getEncrypted()));
        restored.setFormat(source.getFormat());
        restored.setSource(source.getSource());
        restored.setSpec(decode(source.getSpec(), source.getEncrypted()));
        restored.setExposure(source.getExposure());
        restored.setEncrypted(source.getEncrypted());
        restored.setLabels(source.getLabels());
        restored.setExtension(decode(source.getExtension(), source.getEncrypted()));
        restored.setDescription(source.getDescription());
        return publish(restored, source);
    }

    private Revision snapshot(Item item, String revisionNo, String source_id, Revision.Operation operation) {
        return Revision.builder().id(ID.objectId()).tenant_id(item.getTenant_id()).item_id(item.getId())
                .rollout_id(item.getRollout_id()).source_id(source_id).revision(revisionNo).edition(item.getEdition())
                .generation(item.getGeneration()).content(item.getContent()).format(item.getFormat())
                .editor(item.getEditor()).payload(item.getPayload())
                .source(item.getSource()).spec(item.getSpec()).exposure(item.getExposure())
                .encrypted(item.getEncrypted()).labels(item.getLabels()).extension(item.getExtension())
                .checksum(item.getChecksum()).operation(operation.name()).description(item.getDescription())
                .status(item.getStatus()).creator(item.getCreator()).created(item.getCreated())
                .modifier(item.getModifier()).modified(item.getModified()).build();
    }

    /**
     * Prepares one application-owned pointer change from an approved frozen plan, without persistence or notification.
     * The host must atomically commit all selected pointers, version, rollout state, and outbox; never loop-save them.
     * Source snapshots and the current item must be loaded in that same transaction and protected by edition checks.
     */
    public Item prepareActivation(Item current, Revision revision, Rollout rollout, Plan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("Frozen publication plan is required");
        }
        plan.verify(rollout);
        if (!Rollout.State.PUBLISHING.name().equals(rollout.getState()) || revision == null
                || current == null || !plan.getTenant_id().equals(current.getTenant_id())
                || !current.getTenant_id().equals(revision.getTenant_id())
                || !current.getId().equals(revision.getItem_id())
                || !Item.Scope.APPLICATION.name().equals(current.getScope())
                || !plan.includes(current.getApp_id(), current.getId(), revision.getId())) {
            throw new IllegalArgumentException("Activation must match the frozen selected application revision");
        }
        Revision.Operation.valueOf(revision.getOperation());
        Item activated = copy(current);
        activated.setGeneration(next(current.getGeneration()));
        activated.setEdition(next(current.getEdition()));
        activated.setRollout_id(rollout.getId());
        if (Rollout.Mode.GRAY.name().equals(rollout.getMode())) {
            activated.setGray_id(revision.getId());
            activated.setState(Item.State.GRAY.name());
        } else {
            activated.setStable_id(revision.getId());
            activated.setGray_id(null);
            boolean pending = current.getDraft_id() != null && !current.getDraft_id().equals(revision.getId());
            activated.setState(pending ? Item.State.CHANGED.name()
                    : Revision.Operation.DELETE.name().equals(revision.getOperation())
                            ? Item.State.ARCHIVED.name() : Item.State.PUBLISHED.name());
        }
        return activated;
    }

    private String decode(String value, Integer encrypted) {
        return value != null && ItemNormalizer.isEncryptedFlagEnabled(encrypted) ? secretCodec.decrypt(value) : value;
    }

    private Item copy(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item is required");
        }
        return JsonKit.toPojo(JsonKit.toJsonString(item), Item.class);
    }

    private long next(Long value) {
        return value == null ? 1L : value + 1L;
    }
}
