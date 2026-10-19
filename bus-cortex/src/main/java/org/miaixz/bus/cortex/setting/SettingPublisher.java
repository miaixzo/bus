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

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.Keying.SettingSpec;
import org.miaixz.bus.cortex.builtin.SettingGenerator;
import org.miaixz.bus.cortex.magic.watch.WatchManager;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemMetadata;
import org.miaixz.bus.cortex.setting.item.ItemNormalizer;
import org.miaixz.bus.cortex.setting.item.StoreBackedItemStore;
import org.miaixz.bus.cortex.setting.revision.Revision;
import org.miaixz.bus.cortex.setting.revision.RevisionNumbers;
import org.miaixz.bus.cortex.setting.revision.RevisionStore;
import org.miaixz.bus.cortex.setting.rollout.Plan;
import org.miaixz.bus.cortex.setting.rollout.Rollout;
import org.miaixz.bus.cortex.setting.secret.SecretCodec;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Stages configuration revisions; application release activation is committed by the host transaction.
 *
 * @author Kimi Liu
 */
public class SettingPublisher {

    /**
     * Store for current item state.
     */
    private final StoreBackedItemStore entryStore;
    /**
     * Store for immutable revision history.
     */
    private final RevisionStore revisionStore;
    /**
     * Codec used to protect and reveal sensitive content.
     */
    private final SecretCodec secretCodec;
    /**
     * Maximum number of revisions retained per item.
     */
    private final int maxRevisions;

    /**
     * Creates a publisher retaining ten revisions per item.
     *
     * @param entryStore    current-state item store
     * @param revisionStore immutable revision store
     * @param watchManager  retained compatibility dependency; activation notifications are host-owned
     * @param secretCodec   sensitive-content codec
     */
    public SettingPublisher(StoreBackedItemStore entryStore, RevisionStore revisionStore, WatchManager watchManager,
            SecretCodec secretCodec) {
        this(entryStore, revisionStore, watchManager, secretCodec, 10, SettingGenerator.INSTANCE);
    }

    /**
     * Creates a publisher with a configured revision-retention limit.
     *
     * @param entryStore    current-state item store
     * @param revisionStore immutable revision store
     * @param watchManager  retained compatibility dependency; activation notifications are host-owned
     * @param secretCodec   sensitive-content codec
     * @param maxRevisions  maximum revisions retained per item
     */
    public SettingPublisher(StoreBackedItemStore entryStore, RevisionStore revisionStore, WatchManager watchManager,
            SecretCodec secretCodec, int maxRevisions) {
        this(entryStore, revisionStore, watchManager, secretCodec, maxRevisions, SettingGenerator.INSTANCE);
    }

    /**
     * Creates a publisher with explicit retention and keying strategies.
     *
     * @param entryStore    current-state item store
     * @param revisionStore immutable revision store
     * @param watchManager  retained compatibility dependency; activation notifications are host-owned
     * @param secretCodec   sensitive-content codec
     * @param maxRevisions  maximum revisions retained per item
     * @param keying        retained compatibility key strategy
     */
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

    /**
     * Stages inline content using complete tenant-qualified item coordinates.
     *
     * @param entry   item coordinates and metadata
     * @param content plaintext inline content
     * @return staged current-state item
     */
    public Item publishInline(Item entry, String content) {
        Item copy = copy(entry);
        copy.setContent(content);
        return publish(copy);
    }

    /**
     * Saves a draft only; this compatibility method never changes live pointers or notifies clients.
     *
     * @param entry plaintext item to stage
     * @return staged current-state item
     */
    public Item publish(Item entry) {
        return publish(entry, null);
    }

    /**
     * Serializes draft publication against the current-state store.
     *
     * @param entry  item to stage
     * @param source historical source revision for a rollback, or {@code null}
     * @return stored current-state item
     */
    private Item publish(Item entry, Revision source) {
        synchronized (entryStore) {
            return stage(entry, source);
        }
    }

    /**
     * Builds, protects, persists, and retains one immutable draft revision.
     *
     * @param entry  item to stage
     * @param source historical source revision, or {@code null}
     * @return stored current-state item
     */
    private Item stage(Item entry, Revision source) {
        Item prepared = ItemNormalizer.normalize(copy(entry));
        prepared.setChecksum(ItemNormalizer.checksum(prepared));
        Item current = entryStore.find(prepared);
        Revision latest = current == null ? null : revisionStore.latest(current.getTenant_id(), current.getId());
        Revision draft = current == null || current.getDraft_id() == null ? null
                : revisionStore.identity(current.getTenant_id(), current.getId(), current.getDraft_id());
        if (source == null && current != null && draft != null
                && Revision.Operation.UPSERT.name().equals(draft.getOperation()) && current.getChecksum() != null
                && sameDraft(current, prepared)) {
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
        prepared.setState(
                current == null || current.getStable_id() == null ? Item.State.UNPUBLISHED.name()
                        : Item.State.CHANGED.name());
        prepared.setStatus(current == null ? 1 : current.getStatus());
        Map<String, JsonValue> metadata = source == null ? metadata(prepared.getExtension()) : Map.of();
        String reference = source == null ? freeze(metadata, "sources", "references") : source.getReference();
        String secrets = source == null ? freeze(metadata, "protection", "keys") : source.getSecrets();
        if (ItemNormalizer.isEncryptedFlagEnabled(prepared.getEncrypted())) {
            if (!ItemNormalizer.INLINE_SOURCE.equals(prepared.getSource())) {
                throw new IllegalArgumentException("Protected drafts require an inline materialized snapshot");
            }
            prepared.setContent(secretCodec.protect(prepared.getContent()));
            prepared.setPayload(secretCodec.protect(prepared.getPayload()));
            prepared.setSpec(secretCodec.protect(prepared.getSpec()));
            prepared.setExtension(secretCodec.protect(prepared.getExtension()));
            if (source == null) {
                reference = reference == null ? null : secretCodec.protect(reference);
                secrets = secrets == null ? null : secretCodec.protect(secrets);
            }
            prepared.setChecksum(ItemNormalizer.checksum(prepared));
        }
        Revision revision = snapshot(
                prepared,
                revisionNo,
                source == null ? null : source.getId(),
                Revision.Operation.UPSERT);
        revision.setReference(reference);
        revision.setSecrets(secrets);
        prepared.setDraft_id(revision.getId());
        revisionStore.save(revision);
        Item stored = entryStore.save(prepared);
        revisionStore.retainLatest(stored.getTenant_id(), stored.getId(), maxRevisions);
        return stored;
    }

    /**
     * Stages a deletion tombstone. Published content remains available until the selected release commits.
     *
     * @param coordinates exact item coordinates
     * @return staged item, or null when absent
     */
    public Item delete(Item coordinates) {
        synchronized (entryStore) {
            return stageDeletion(coordinates);
        }
    }

    /**
     * Builds and persists a deletion tombstone for an existing item.
     *
     * @param coordinates item coordinates
     * @return updated current-state item, or {@code null} when absent
     */
    private Item stageDeletion(Item coordinates) {
        Item current = entryStore.find(coordinates);
        if (current == null) {
            return null;
        }
        Item staged = copy(current);
        staged.setEdition(next(current.getEdition()));
        staged.setState(current.getStable_id() == null ? Item.State.UNPUBLISHED.name() : Item.State.CHANGED.name());
        Revision latest = revisionStore.latest(current.getTenant_id(), current.getId());
        Revision tombstone = snapshot(
                staged,
                RevisionNumbers.next(latest == null ? null : latest.getRevision()),
                null,
                Revision.Operation.DELETE);
        tombstone.setContent(null);
        tombstone.setPayload(null);
        Revision previous = current.getDraft_id() == null ? null
                : revisionStore.identity(current.getTenant_id(), current.getId(), current.getDraft_id());
        if (previous != null) {
            tombstone.setReference(previous.getReference());
            tombstone.setSecrets(previous.getSecrets());
        }
        staged.setDraft_id(tombstone.getId());
        revisionStore.save(tombstone);
        Item deleted = entryStore.save(staged);
        revisionStore.retainLatest(current.getTenant_id(), current.getId(), maxRevisions);
        return deleted;
    }

    /**
     * Stages a new revision from immutable history without activating it or modifying the source.
     *
     * @param coordinates exact item coordinates
     * @param revision    historical revision identifier
     * @return staged item, or null when unavailable
     */
    public Item rollback(Item coordinates, String revision) {
        synchronized (entryStore) {
            return restore(coordinates, revision);
        }
    }

    /**
     * Copies immutable historical content into a new draft revision.
     *
     * @param coordinates item coordinates
     * @param revision    historical revision identifier
     * @return restored current-state item, or {@code null} when unavailable
     */
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
        Item restored = Item.builder().id(current.getId()).tenant_id(current.getTenant_id()).scope(current.getScope())
                .app_id(current.getApp_id()).kind(current.getKind()).code(current.getCode())
                .rollout_id(current.getRollout_id()).editor(source.getEditor()).payload(source.getPayload())
                .status(current.getStatus()).creator(current.getCreator()).created(current.getCreated())
                .modifier(current.getModifier()).modified(current.getModified()).build();
        restored.setContent(
                ItemNormalizer.isEncryptedFlagEnabled(source.getEncrypted()) && source.getContent() != null
                        ? secretCodec.decrypt(source.getContent())
                        : source.getContent());
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

    /**
     * Captures all immutable item fields in a revision snapshot.
     *
     * @param item       normalized item
     * @param revisionNo monotonic revision number
     * @param source_id  historical source identifier, or {@code null}
     * @param operation  revision operation
     * @return immutable revision snapshot
     */
    private Revision snapshot(Item item, String revisionNo, String source_id, Revision.Operation operation) {
        return Revision.builder().id(ID.objectId()).tenant_id(item.getTenant_id()).item_id(item.getId())
                .rollout_id(item.getRollout_id()).source_id(source_id).revision(revisionNo).edition(item.getEdition())
                .generation(item.getGeneration()).content(item.getContent()).format(item.getFormat())
                .editor(item.getEditor()).payload(item.getPayload()).source(item.getSource()).spec(item.getSpec())
                .exposure(item.getExposure()).encrypted(item.getEncrypted()).labels(item.getLabels())
                .extension(item.getExtension()).checksum(item.getChecksum()).operation(operation.name())
                .description(item.getDescription()).status(item.getStatus()).creator(item.getCreator())
                .created(item.getCreated()).modifier(item.getModifier()).modified(item.getModified()).build();
    }

    /**
     * Prepares one application-owned pointer change from an approved frozen plan, without persistence or notification.
     * The host must atomically commit all selected pointers, version, rollout state, and outbox; never loop-save them.
     * Source snapshots and the current item must be loaded in that same transaction and protected by edition checks.
     *
     * @param current  transaction-reserved effective item
     * @param revision exact effective revision
     * @param rollout  approved publishing rollout
     * @param plan     frozen approval plan
     * @return detached pointer update for the host to commit atomically
     */
    public Item prepareActivation(Item current, Revision revision, Rollout rollout, Plan plan) {
        requireSelected(current, revision, rollout, plan);
        if (!Rollout.State.PUBLISHING.name().equals(rollout.getState())) {
            throw new IllegalArgumentException("Activation requires a publishing approved plan");
        }
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
            activated.setState(
                    pending ? Item.State.CHANGED.name()
                            : Revision.Operation.DELETE.name().equals(revision.getOperation())
                                    ? Item.State.ARCHIVED.name()
                                    : Item.State.PUBLISHED.name());
        }
        return activated;
    }

    /**
     * Prepares a source stable pointer only when an approved effective snapshot freezes that exact source revision.
     * Gray publication never approves source pointers. The host reserves source rows and persists all updates in the
     * same transaction as effective activation; this method neither saves rows nor resolves live relationships.
     *
     * @param current   reserved source item
     * @param revision  exact immutable source revision
     * @param effective selected effective item
     * @param snapshot  exact approved effective revision
     * @param rollout   approved publishing rollout
     * @param plan      immutable approval plan
     * @return detached source pointer update, unchanged when an equal or newer source revision is already approved
     */
    public Item prepareSourceActivation(
            Item current,
            Revision revision,
            Item effective,
            Revision snapshot,
            Rollout rollout,
            Plan plan) {
        requireSelected(effective, snapshot, rollout, plan);
        if (!Rollout.State.PUBLISHING.name().equals(rollout.getState())
                || !Rollout.Mode.FORMAL.name().equals(rollout.getMode())) {
            throw new IllegalArgumentException("Source activation requires formal publication");
        }
        return sourceActivation(current, revision, effective, snapshot, rollout);
    }

    /**
     * Prepares the same source approval when the host promotes an already approved gray release to stable. No new plan
     * or approval is introduced; the host must atomically commit the original promote operation.
     *
     * @param current   reserved source item
     * @param revision  exact immutable source revision
     * @param effective current effective item still owned by the original gray release
     * @param snapshot  approved effective revision
     * @param rollout   original published gray rollout
     * @param plan      original immutable gray approval plan
     * @return detached monotonic source pointer update
     */
    public Item prepareSourcePromotion(
            Item current,
            Revision revision,
            Item effective,
            Revision snapshot,
            Rollout rollout,
            Plan plan) {
        requireSelected(effective, snapshot, rollout, plan);
        if (!Rollout.State.PUBLISHED.name().equals(rollout.getState())
                || !Rollout.Mode.GRAY.name().equals(rollout.getMode())
                || !Objects.equals(effective.getGray_id(), snapshot.getId())
                || !Objects.equals(effective.getRollout_id(), rollout.getId())) {
            throw new IllegalArgumentException("Source promotion requires the original active approved gray target");
        }
        return sourceActivation(current, revision, effective, snapshot, rollout);
    }

    /**
     * Checks selected effective identity through the same frozen plan for ordinary activation and gray promotion.
     *
     * @param current  selected effective item
     * @param revision exact effective revision
     * @param rollout  original approved rollout
     * @param plan     original immutable plan
     */
    private void requireSelected(Item current, Revision revision, Rollout rollout, Plan plan) {
        if (plan == null)
            throw new IllegalArgumentException("Frozen publication plan is required");
        plan.verify(rollout);
        if (revision == null || current == null || !plan.getTenant_id().equals(current.getTenant_id())
                || !current.getTenant_id().equals(revision.getTenant_id())
                || !current.getId().equals(revision.getItem_id())
                || !Item.Scope.APPLICATION.name().equals(current.getScope())
                || !Item.Kind.EFFECTIVE.name().equals(current.getKind())
                || !plan.includes(current.getApp_id(), current.getId(), revision.getId())) {
            throw new IllegalArgumentException("Activation must match the frozen selected application revision");
        }
        Revision.Operation.valueOf(revision.getOperation());
    }

    /**
     * Applies the unique monotonic source-pointer algorithm after the caller's frozen effective approval check.
     *
     * @param current   reserved source item
     * @param revision  exact approved source revision
     * @param effective selected effective owner
     * @param snapshot  exact approved effective snapshot
     * @param rollout   original approved release
     * @return detached source pointer update
     */
    private Item sourceActivation(Item current, Revision revision, Item effective, Revision snapshot, Rollout rollout) {
        if (current == null || revision == null || !Item.Kind.SOURCE.name().equals(current.getKind())
                || !Objects.equals(current.getTenant_id(), effective.getTenant_id())
                || !Objects.equals(current.getTenant_id(), revision.getTenant_id())
                || !Objects.equals(current.getId(), revision.getItem_id())
                || !Revision.Operation.UPSERT.name().equals(revision.getOperation())
                || !Revision.Operation.UPSERT.name().equals(snapshot.getOperation())
                || Item.Scope.APPLICATION.name().equals(current.getScope())
                        && !Objects.equals(current.getApp_id(), effective.getApp_id())
                || !Item.Scope.APPLICATION.name().equals(current.getScope())
                        && !Item.Scope.SHARED.name().equals(current.getScope())) {
            throw new IllegalArgumentException("Source activation requires a frozen formal effective revision");
        }
        JsonValue manifest = snapshot.getReference() == null ? null
                : parse(decode(snapshot.getReference(), snapshot.getEncrypted()));
        JsonValue frozen = manifest instanceof JsonValue.ObjectValue root ? root.values().get("sources") : null;
        JsonValue selected = frozen instanceof JsonValue.ObjectValue sources ? sources.values().get("revisions") : null;
        JsonValue expected = selected instanceof JsonValue.ObjectValue revisions
                ? revisions.values().get(current.getId())
                : null;
        if (!(expected instanceof JsonValue.StringValue identity) || !identity.value().equals(revision.getId())) {
            throw new IllegalArgumentException("Source revision is absent from the approved effective snapshot");
        }
        if (current.getStable_id() != null) {
            Revision stable = revisionStore.identity(current.getTenant_id(), current.getId(), current.getStable_id());
            if (stable == null)
                throw new IllegalArgumentException("Approved source baseline is unavailable");
            if (RevisionNumbers.sortKey(stable.getRevision()) >= RevisionNumbers.sortKey(revision.getRevision())) {
                return copy(current);
            }
        }
        Item activated = copy(current);
        activated.setStable_id(revision.getId());
        activated.setRollout_id(rollout.getId());
        activated.setGeneration(next(current.getGeneration()));
        activated.setEdition(next(current.getEdition()));
        activated.setState(
                Objects.equals(current.getDraft_id(), revision.getId()) ? Item.State.PUBLISHED.name()
                        : Item.State.CHANGED.name());
        return activated;
    }

    /**
     * Compares semantic plaintext snapshots so randomized encryption does not create duplicate drafts.
     *
     * @param current  stored current item
     * @param prepared normalized incoming plaintext
     * @return whether all checksum-owned values and the editor agree
     */
    private boolean sameDraft(Item current, Item prepared) {
        Item plaintext = copy(current);
        plaintext.setContent(decode(current.getContent(), current.getEncrypted()));
        plaintext.setPayload(decode(current.getPayload(), current.getEncrypted()));
        plaintext.setSpec(decode(current.getSpec(), current.getEncrypted()));
        plaintext.setExtension(decode(current.getExtension(), current.getEncrypted()));
        return Objects.equals(ItemNormalizer.checksum(plaintext), prepared.getChecksum());
    }

    /**
     * Reads the existing envelope before encryption; no client-controlled provenance is created here.
     *
     * @param extension trusted host-validated plaintext extension
     * @return immutable service metadata, empty for absent metadata
     */
    private Map<String, JsonValue> metadata(String extension) {
        return ItemMetadata.services(extension == null || extension.isBlank() ? null : parse(extension));
    }

    /**
     * Freezes only the requested metadata members without embedding source values into the public manifest.
     *
     * @param metadata validated service metadata
     * @param names    fixed member names owned by the snapshot
     * @return canonical metadata text, or null when absent
     */
    private String freeze(Map<String, JsonValue> metadata, String... names) {
        Map<String, JsonValue> frozen = new TreeMap<>();
        for (String name : names) {
            if (metadata.containsKey(name))
                frozen.put(name, metadata.get(name));
        }
        return frozen.isEmpty() ? null
                : new String(JsonKit.writeValue(new JsonValue.ObjectValue(frozen)), Charset.UTF_8);
    }

    /**
     * Parses an already host-bounded immutable envelope with duplicate-name rejection.
     *
     * @param value immutable metadata text
     * @return exact provider-neutral JSON tree
     */
    private JsonValue parse(String value) {
        return JsonKit.readValue(value.getBytes(Charset.UTF_8), 256, true);
    }

    /**
     * Reveals a protected value when its encrypted flag is enabled.
     *
     * @param value     stored value
     * @param encrypted encrypted flag
     * @return decoded value
     */
    private String decode(String value, Integer encrypted) {
        return value != null && ItemNormalizer.isEncryptedFlagEnabled(encrypted) ? secretCodec.decrypt(value) : value;
    }

    /**
     * Creates a detached item copy through the configured JSON representation.
     *
     * @param item item to copy
     * @return detached copy
     */
    private Item copy(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item is required");
        }
        return JsonKit.toPojo(JsonKit.toJsonString(item), Item.class);
    }

    /**
     * Advances a nullable monotonic counter.
     *
     * @param value current counter value
     * @return one for a missing counter, otherwise the next value
     */
    private long next(Long value) {
        return value == null ? 1L : Math.incrementExact(value);
    }

}
