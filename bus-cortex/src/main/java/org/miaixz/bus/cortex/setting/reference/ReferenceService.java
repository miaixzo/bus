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
import org.miaixz.bus.cortex.release.Manifest;
import org.miaixz.bus.cortex.setting.SettingEnforcer;
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
     * Mandatory permissions.
     */
    private final SettingEnforcer enforcer;

    /**
     * Creates relationship management.
     *
     * @param store     current relations
     * @param revisions exact published pins
     * @param codec     option parser
     * @param editor    overlay protection
     * @param enforcer  permissions
     */
    public ReferenceService(ReferenceStore store, RevisionStore revisions, ReferenceCodec codec, ItemEditor editor,
            SettingEnforcer enforcer) {
        this.store = Objects.requireNonNull(store);
        this.revisions = Objects.requireNonNull(revisions);
        this.codec = Objects.requireNonNull(codec);
        this.editor = Objects.requireNonNull(editor);
        this.enforcer = Objects.requireNonNull(enforcer);
    }

    /**
     * Reads current source relations.
     *
     * @param context trusted identity
     * @param item    source
     * @return current relations
     */
    public List<Reference> byItem(SettingEnforcer.Context context, String item) {
        enforcer.require(context, "read", "reference", item);
        return List.copyOf(store.byItem(context.tenant(), item));
    }

    /**
     * Reads current application relations.
     *
     * @param context trusted identity
     * @param app     application
     * @return current relations
     */
    public List<Reference> byApp(SettingEnforcer.Context context, String app) {
        enforcer.require(context, "read", "app", app);
        return List.copyOf(store.byApp(context.tenant(), app));
    }

    /**
     * Counts distinct target apps in one batch.
     *
     * @param context trusted identity
     * @param items   source set
     * @return counts
     */
    public Map<String, Long> countByItem(SettingEnforcer.Context context, Set<String> items) {
        for (String id : items)
            enforcer.require(context, "read", "item", id);
        return Map.copyOf(store.countByItem(context.tenant(), Set.copyOf(items)));
    }

    /**
     * Counts distinct current sources in one batch.
     *
     * @param context trusted identity
     * @param apps    app set
     * @return counts
     */
    public Map<String, Long> countByApp(SettingEnforcer.Context context, Set<String> apps) {
        for (String id : apps)
            enforcer.require(context, "read", "app", id);
        return Map.copyOf(store.countByApp(context.tenant(), Set.copyOf(apps)));
    }

    /**
     * Validates complete desired relationship membership and protects changed overlays only.
     *
     * @param context    trusted identity
     * @param source     protected source
     * @param candidates complete desired relation set
     * @return normalized protected relations
     */
    public List<Reference> prepare(SettingEnforcer.Context context, Item source, List<Reference> candidates) {
        enforcer.require(context, "associate", "item", source.getId());
        Map<String, Reference> existing = new HashMap<>();
        for (Reference r : store.byItem(context.tenant(), source.getId()))
            existing.put(r.getId(), r);
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
        ItemCodec.Document parsed = null;
        for (Reference input : candidates) {
            Reference next = JsonKit.toPojo(JsonKit.toJsonString(input), Reference.class);
            if (!context.tenant().equals(next.getTenant_id()) || !source.getId().equals(next.getSource_id())
                    || next.getTarget_id() == null || next.getTarget_id().isBlank())
                throw new IllegalArgumentException("Invalid relationship coordinates");
            enforcer.require(context, "associate", "app", next.getTarget_id());
            if (next.getId() == null)
                next.setId(ID.objectId());
            if (!recordIds.add(next.getId()))
                throw new IllegalArgumentException("Duplicate relationship identity");
            ReferenceCodec.Options usage = codec.decode(next);
            Reference prior = existing.get(next.getId());
            if (next.getRevision_id() != null) {
                Revision pin = pins.get(next.getRevision_id());
                if (pin == null || !context.tenant().equals(pin.getTenant_id())
                        || !source.getId().equals(pin.getItem_id()) || pin.getRelease_id() == null)
                    throw new IllegalArgumentException("Invalid published pin");
                Manifest.definition(pin);
            }
            String alias = usage.alias().isEmpty() ? source.getCode() : usage.alias();
            if (!identities.add(next.getTarget_id() + "\u0000" + alias))
                throw new IllegalArgumentException("Duplicate relationship alias");
            if (usage.type() == Reference.Type.ITEM_OWNER)
                owners++;
            if (usage.overlay() != null && !usage.overlay().values().isEmpty()
                    && (prior == null || !Objects.equals(next.getOverlay(), prior.getOverlay()))) {
                enforcer.require(context, "secret-edit", "reference", next.getId());
                if (parsed == null)
                    parsed = editor.open(source);
                usage = new ReferenceCodec.Options(usage.type(), usage.alias(), usage.prefix(), usage.override(),
                        usage.keys(), editor.protectOverlay(parsed, usage.overlay()));
            }
            result.add(codec.encode(usage, next));
        }
        if (owners > 1 || owners == 1 && result.size() != 1)
            throw new IllegalArgumentException("Owner/source sharing conflict");
        return List.copyOf(result);
    }

}
