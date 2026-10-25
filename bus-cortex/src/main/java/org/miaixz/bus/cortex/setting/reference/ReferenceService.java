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
package org.miaixz.bus.cortex.setting.reference;

import java.util.*;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.cortex.guard.CortexGuard;
import org.miaixz.bus.cortex.release.ReleaseCodec;
import org.miaixz.bus.cortex.setting.SettingProvider;
import org.miaixz.bus.cortex.setting.SettingStore;
import org.miaixz.bus.cortex.setting.curator.ItemEditor;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemCodec;
import org.miaixz.bus.cortex.setting.revision.Revision;
import org.miaixz.bus.cortex.setting.revision.RevisionStore;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Current relationship validation and counts; no publication or duplicate policy storage.
 *
 * @author Kimi Liu
 */
public final class ReferenceService {

    /**
     * Current relationships.
     */
    private final ReferenceStore store;
    /**
     * Exact published pin validation.
     */
    private final RevisionStore revisions;
    /**
     * Relationship-only parser.
     */
    private final ReferenceCodec codec;
    /**
     * Sensitive overlay protection.
     */
    private final ItemEditor editor;
    /**
     * Optional host access policy.
     */
    private final CortexGuard guard;

    /**
     * Creates relationship management.
     *
     * @param store     current relations
     * @param revisions exact published pins
     * @param codec     option parser
     * @param editor    overlay protection
     * @param guard     optional host access policy
     */
    public ReferenceService(ReferenceStore store, RevisionStore revisions, ReferenceCodec codec, ItemEditor editor,
            CortexGuard guard) {
        this.store = Objects.requireNonNull(store);
        this.revisions = Objects.requireNonNull(revisions);
        this.codec = Objects.requireNonNull(codec);
        this.editor = Objects.requireNonNull(editor);
        this.guard = guard;
    }

    /**
     * Reads current source relations.
     *
     * @param context operation coordinates
     * @param item    source
     * @return current relations
     */
    public List<Reference> byItem(SettingProvider.Context context, String item) {
        context.check(guard, "read", "reference", item);
        return List.copyOf(store.byItem(context.tenant(), item));
    }

    /**
     * Reads current application relations.
     *
     * @param context operation coordinates
     * @param app     application
     * @return current relations
     */
    public List<Reference> byApp(SettingProvider.Context context, String app) {
        context.check(guard, "read", "app", app);
        return List.copyOf(store.byApp(context.tenant(), app));
    }

    /**
     * Counts distinct target apps in one batch.
     *
     * @param context operation coordinates
     * @param items   source set
     * @return counts
     */
    public Map<String, Long> countByItem(SettingProvider.Context context, Set<String> items) {
        for (String id : items)
            context.check(guard, "read", "item", id);
        return Map.copyOf(store.countByItem(context.tenant(), Set.copyOf(items)));
    }

    /**
     * Counts distinct current sources in one batch.
     *
     * @param context operation coordinates
     * @param apps    app set
     * @return counts
     */
    public Map<String, Long> countByApp(SettingProvider.Context context, Set<String> apps) {
        for (String id : apps)
            context.check(guard, "read", "app", id);
        return Map.copyOf(store.countByApp(context.tenant(), Set.copyOf(apps)));
    }

    /**
     * Validates complete desired relationships and protects nonempty overlays according to the selected current or
     * fixed definition, including unchanged plaintext overlays whose sensitivity requires protection.
     *
     * @param context    operation coordinates
     * @param source     protected source
     * @param candidates complete desired relation set
     * @return normalized protected relations
     */
    public List<Reference> prepare(SettingProvider.Context context, Item source, List<Reference> candidates) {
        if (source == null || !context.tenant().equals(source.getTenant_id()) || source.getId() == null
                || source.getId().isBlank())
            throw new IllegalArgumentException("Invalid source coordinates");
        context.check(guard, "associate", "item", source.getId());
        Map<String, Reference> existing = new HashMap<>();
        for (Reference r : store.byItem(context.tenant(), source.getId()))
            existing.put(r.getId(), r);
        return prepare(context, source, candidates, existing, false);
    }

    /**
     * Prepares retained overlays against current or fixed definitions, preserving the complete relationship CAS
     * read-set. Already protected or empty overlays require neither decryption nor additional relationship
     * authorization.
     *
     * @param context current-state operation coordinates
     * @param source  prepared protected definition
     * @return exact protected relationship replacement, or null when no overlay requires a change
     */
    public SettingStore.RelationChange protectRetained(SettingProvider.Context context, Item source) {
        if (source == null || !context.tenant().equals(source.getTenant_id()) || source.getId() == null
                || source.getId().isBlank())
            throw new IllegalArgumentException("Invalid source coordinates");
        List<Reference> retained = store.byItem(context.tenant(), source.getId());
        Map<String, Reference> existing = new HashMap<>();
        Map<String, Long> expected = new HashMap<>();
        boolean pending = false;
        for (Reference reference : retained) {
            if (!context.tenant().equals(reference.getTenant_id()) || !source.getId().equals(reference.getSource_id())
                    || reference.getId() == null || reference.getEdition() == null
                    || existing.put(reference.getId(), reference) != null)
                throw new IllegalStateException("Invalid retained relationship snapshot");
            expected.put(reference.getId(), reference.getEdition());
            ReferenceCodec.Options usage = codec.decode(reference);
            pending |= (Integer.valueOf(1).equals(source.getEncrypted()) || reference.getRevision_id() != null)
                    && usage.overlay() != null && !usage.overlay().values().isEmpty()
                    && !editor.isProtectedOverlay(usage.overlay());
        }
        if (!pending)
            return null;
        List<Reference> prepared = prepare(context, source, retained, existing, true);
        for (Reference reference : prepared)
            if (!Objects.equals(reference.getOverlay(), existing.get(reference.getId()).getOverlay()))
                return new SettingStore.RelationChange(prepared, expected);
        return null;
    }

    /**
     * Shares relationship validation while authorizing retained-overlay maintenance only when protection changes.
     *
     * @param context    operation coordinates
     * @param source     prepared current source
     * @param candidates desired complete relationship set
     * @param existing   exact previously read relationships
     * @param retained   whether only existing overlay protection may change
     * @return normalized protected relationships
     */
    private List<Reference> prepare(
            SettingProvider.Context context,
            Item source,
            List<Reference> candidates,
            Map<String, Reference> existing,
            boolean retained) {
        Set<String> pinIds = new TreeSet<>();
        for (Reference relation : candidates)
            if (relation.getRevision_id() != null)
                pinIds.add(relation.getRevision_id());
        Map<String, Revision> pins = new TreeMap<>();
        if (!pinIds.isEmpty())
            for (Revision revision : revisions.findAll(context.tenant(), pinIds)) {
                if (!pinIds.contains(revision.getId()) || pins.put(revision.getId(), revision) != null)
                    throw new IllegalStateException("Invalid pin batch");
            }
        List<Reference> result = new ArrayList<>();
        Set<String> identities = new HashSet<>(), recordIds = new HashSet<>();
        int owners = 0;
        Map<String, ItemCodec.Document> parsed = new HashMap<>();
        for (Reference input : candidates) {
            Reference next = JsonKit.toPojo(JsonKit.toJsonString(input), Reference.class);
            if (!context.tenant().equals(next.getTenant_id()) || !source.getId().equals(next.getSource_id())
                    || next.getTarget_id() == null || next.getTarget_id().isBlank())
                throw new IllegalArgumentException("Invalid relationship coordinates");
            if (!retained)
                context.check(guard, "associate", "app", next.getTarget_id());
            if (next.getId() == null)
                next.setId(ID.objectId());
            if (!recordIds.add(next.getId()))
                throw new IllegalArgumentException("Duplicate relationship identity");
            ReferenceCodec.Options usage = codec.decode(next);
            Reference prior = existing.get(next.getId());
            Item selected = source;
            if (next.getRevision_id() != null) {
                Revision pin = pins.get(next.getRevision_id());
                if (pin == null || !context.tenant().equals(pin.getTenant_id())
                        || !source.getId().equals(pin.getItem_id()) || pin.getRelease_id() == null)
                    throw new IllegalArgumentException("Invalid published pin");
                selected = ReleaseCodec.definition(pin).item(context.tenant());
            }
            String alias = usage.alias().isEmpty() ? source.getCode() : usage.alias();
            if (!identities.add(next.getTarget_id() + "\u0000" + alias))
                throw new IllegalArgumentException("Duplicate relationship alias");
            if (usage.type() == Reference.Type.ITEM_OWNER)
                owners++;
            if (usage.overlay() != null && !usage.overlay().values().isEmpty()) {
                boolean changed = prior == null || !Objects.equals(next.getOverlay(), prior.getOverlay());
                if (changed || !editor.isProtectedOverlay(usage.overlay())) {
                    ItemCodec.Document document = parsed.get(next.getRevision_id());
                    if (document == null) {
                        document = editor.open(selected);
                        parsed.put(next.getRevision_id(), document);
                    }
                    if (changed || editor.needsOverlayProtection(document, usage.overlay())) {
                        if (retained) {
                            context.check(guard, "associate", "item", source.getId());
                            context.check(guard, "associate", "app", next.getTarget_id());
                        }
                        context.check(guard, "secret-edit", "reference", next.getId());
                        usage = new ReferenceCodec.Options(usage.type(), usage.alias(), usage.prefix(),
                                usage.override(), usage.keys(), editor.protectOverlay(document, usage.overlay()));
                    }
                }
            }
            result.add(codec.encode(usage, next));
        }
        if (owners > 1 || owners == 1 && result.size() != 1)
            throw new IllegalArgumentException("Owner/source sharing conflict");
        return List.copyOf(result);
    }

}
