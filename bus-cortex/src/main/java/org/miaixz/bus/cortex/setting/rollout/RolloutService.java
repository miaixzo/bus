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
package org.miaixz.bus.cortex.setting.rollout;

import java.util.*;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.cortex.guard.CortexGuard;
import org.miaixz.bus.cortex.release.Release;
import org.miaixz.bus.cortex.release.ReleaseCodec;
import org.miaixz.bus.cortex.release.ReleaseStore;
import org.miaixz.bus.cortex.setting.SettingProvider;
import org.miaixz.bus.cortex.setting.SettingStore;
import org.miaixz.bus.cortex.setting.approval.ApprovalStore;
import org.miaixz.bus.cortex.setting.curator.ItemEditor;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemCodec;
import org.miaixz.bus.cortex.setting.item.ItemMerger;
import org.miaixz.bus.cortex.setting.reference.Reference;
import org.miaixz.bus.cortex.setting.reference.ReferenceCodec;
import org.miaixz.bus.cortex.setting.revision.Revision;
import org.miaixz.bus.cortex.setting.revision.RevisionStore;

/**
 * Draft CRUD and consistent candidate freezing; never allocates a formal version.
 *
 * @author Kimi Liu
 */
public final class RolloutService {

    /**
     * Workflow transaction boundary.
     */
    private final RolloutStore store;
    /**
     * Consistent current-state graph.
     */
    private final SettingStore settings;
    /**
     * Formal baseline authority.
     */
    private final ReleaseStore releases;
    /**
     * Exact published fixed-content reads.
     */
    private final RevisionStore revisions;
    /**
     * Source integrity and protected content decoding.
     */
    private final ItemEditor editor;
    /**
     * Unique relation parser.
     */
    private final ReferenceCodec references;
    /**
     * Host approval policy registry, independent of access checks.
     */
    private final ApprovalStore approvals;
    /**
     * Optional host access policy.
     */
    private final CortexGuard guard;

    /**
     * Creates workflow management.
     *
     * @param store      workflows
     * @param settings   current graph
     * @param releases   formal baselines
     * @param revisions  exact snapshots
     * @param editor     source verification
     * @param references option parser
     * @param approvals  approval policy registry
     * @param guard      optional host access policy
     */
    public RolloutService(RolloutStore store, SettingStore settings, ReleaseStore releases, RevisionStore revisions,
            ItemEditor editor, ReferenceCodec references, ApprovalStore approvals, CortexGuard guard) {
        this.store = Objects.requireNonNull(store);
        this.settings = Objects.requireNonNull(settings);
        this.releases = Objects.requireNonNull(releases);
        this.revisions = Objects.requireNonNull(revisions);
        this.editor = Objects.requireNonNull(editor);
        this.references = Objects.requireNonNull(references);
        this.approvals = Objects.requireNonNull(approvals);
        this.guard = guard;
    }

    /**
     * Creates only workflow input.
     *
     * @param context     operation coordinates
     * @param title       name
     * @param description purpose
     * @param selection   explicit apps
     * @return draft
     */
    public Rollout create(SettingProvider.Context context, String title, String description, Plan.Selection selection) {
        context.requireRequest();
        context.check(guard, "create", "rollout", null);
        approvals.validatePolicy(context, selection.policy());
        return store.saveDraft(
                Rollout.builder().tenant_id(context.tenant()).id(ID.objectId()).title(title).description(description)
                        .status(Rollout.State.DRAFT.key()).manifest(Plan.selection(selection)).build(),
                null);
    }

    /**
     * Updates only an editable draft.
     *
     * @param context     operation coordinates
     * @param rollout     workflow
     * @param edition     expected CAS
     * @param title       name
     * @param description purpose
     * @param selection   apps
     * @return draft
     */
    public Rollout update(
            SettingProvider.Context context,
            String rollout,
            long edition,
            String title,
            String description,
            Plan.Selection selection) {
        context.requireRequest();
        context.check(guard, "edit", "rollout", rollout);
        Rollout next = draft(context, rollout, edition);
        approvals.validatePolicy(context, selection.policy());
        next.setTitle(title);
        next.setDescription(description);
        next.setManifest(Plan.selection(selection));
        next.setChecksum(null);
        return store.saveDraft(next, edition);
    }

    /**
     * Deletes only a draft; Store must repeat state/CAS checks atomically.
     *
     * @param context operation coordinates
     * @param rollout workflow
     * @param edition expected CAS
     */
    public void delete(SettingProvider.Context context, String rollout, long edition) {
        context.requireRequest();
        context.check(guard, "delete", "rollout", rollout);
        draft(context, rollout, edition);
        store.deleteDraft(context.tenant(), rollout, edition);
    }

    /**
     * Finds an exact workflow.
     *
     * @param context operation coordinates
     * @param rollout workflow
     * @return detached workflow
     */
    public Rollout find(SettingProvider.Context context, String rollout) {
        context.check(guard, "read", "rollout", rollout);
        Rollout value = store.find(context.tenant(), rollout);
        if (value != null && (!context.tenant().equals(value.getTenant_id()) || !rollout.equals(value.getId())))
            throw new IllegalStateException("Workflow identity mismatch");
        return value;
    }

    /**
     * Queries workflow management state.
     *
     * @param context operation coordinates
     * @param offset  page offset
     * @param limit   page size
     * @return workflows
     */
    public List<Rollout> query(SettingProvider.Context context, int offset, int limit) {
        context.check(guard, "read", "rollout", null);
        if (offset < 0 || limit < 1 || limit > 1000)
            throw new IllegalArgumentException("Invalid workflow page");
        return List.copyOf(store.query(context.tenant(), offset, limit));
    }

    /**
     * Freezes consistent current definitions/relationships and exact approved baseline.
     *
     * @param context operation coordinates
     * @param rollout draft identity
     * @param edition expected CAS
     * @return immutable review input
     */
    public Rollout submit(SettingProvider.Context context, String rollout, long edition) {
        context.requireRequest();
        context.check(guard, "submit", "rollout", rollout);
        Rollout draft = draft(context, rollout, edition);
        Plan.Selection selection = Plan.selection(draft.getManifest());
        approvals.validatePolicy(context, selection.policy());
        for (String app : selection.applications())
            context.check(guard, "publish", "app", app);
        SettingStore.View view = settings.read(context.tenant(), selection.applications());
        Set<String> apps = new HashSet<>();
        view.apps().forEach(app -> {
            if (!context.tenant().equals(app.getTenant_id()))
                throw new IllegalStateException("Application tenant mismatch");
            apps.add(app.getId());
        });
        if (!apps.containsAll(selection.applications()))
            throw new IllegalStateException("Application is missing or unavailable");
        Release baseline = view.readSet().baseline() == null ? null
                : releases.byId(context.tenant(), view.readSet().baseline());
        if (view.readSet().baseline() != null && baseline == null)
            throw new IllegalStateException("Baseline missing");
        Map<String, Plan.Definition> definitions = new TreeMap<>();
        Map<String, List<Plan.Binding>> graph = new TreeMap<>();
        Map<Plan.Definition, String> definitionKeys = new IdentityHashMap<>();
        if (baseline != null) {
            ReleaseCodec.Document previous = ReleaseCodec.decode(baseline);
            Set<String> ids = new TreeSet<>();
            previous.applications().values().forEach(list -> list.forEach(b -> ids.add(b.source())));
            Map<String, Revision> snapshots = new TreeMap<>();
            for (Revision revision : revisions.findAll(context.tenant(), ids))
                if (snapshots.put(revision.getId(), revision) != null)
                    throw new IllegalStateException("Duplicate snapshot");
            ReleaseCodec.verify(baseline, snapshots);
            Map<String, Plan.Definition> publishedDefinitions = new HashMap<>();
            previous.applications().forEach((app, bindings) -> {
                if (selection.applications().contains(app))
                    return;
                List<Plan.Binding> inherited = new ArrayList<>();
                for (ReleaseCodec.Binding binding : bindings) {
                    Plan.Definition definition = publishedDefinitions.computeIfAbsent(
                            binding.source(),
                            source -> ReleaseCodec.definition(snapshots.get(source)));
                    String key = definitionKeys.computeIfAbsent(definition, Plan.Definition::key);
                    definitions.put(key, definition);
                    inherited.add(new Plan.Binding(key, binding.options(), null));
                }
                graph.put(app, inherited);
            });
        }
        for (String app : selection.applications())
            graph.put(app, new ArrayList<>());
        Map<String, Item> current = new TreeMap<>();
        for (Item item : view.items()) {
            if (!context.tenant().equals(item.getTenant_id()) || current.put(item.getId(), item) != null)
                throw new IllegalStateException("Invalid source graph");
            if (!Objects.equals(item.getEdition(), view.readSet().items().get(item.getId())))
                throw new IllegalStateException("Source read-set mismatch");
        }
        Set<String> pinIds = new TreeSet<>();
        for (Reference relation : view.references())
            if (relation.getRevision_id() != null)
                pinIds.add(relation.getRevision_id());
        Map<String, Revision> pins = new TreeMap<>();
        for (Revision revision : revisions.findAll(context.tenant(), pinIds))
            pins.put(revision.getId(), revision);
        if (!pins.keySet().equals(pinIds))
            throw new IllegalStateException("Pinned snapshot missing");
        // Reuse verified definitions, digests and plaintext only within this submission; each binding retains its
        // checks.
        Map<String, ItemCodec.Document> parsed = new HashMap<>();
        Map<String, Plan.Definition> currentDefinitions = new HashMap<>();
        Map<String, Plan.Definition> pinnedDefinitions = new HashMap<>();
        for (Reference relation : view.references()) {
            if (!context.tenant().equals(relation.getTenant_id())
                    || !selection.applications().contains(relation.getTarget_id())
                    || !Objects.equals(relation.getEdition(), view.readSet().references().get(relation.getId())))
                throw new IllegalStateException("Relationship read-set mismatch");
            Item item = current.get(relation.getSource_id());
            if (item == null)
                throw new IllegalStateException("Source missing");
            if (!Integer.valueOf(Item.Status.ACTIVE.key()).equals(item.getStatus()))
                continue;
            Plan.Definition definition;
            if (relation.getRevision_id() == null)
                definition = currentDefinitions.computeIfAbsent(item.getId(), ignored -> Plan.Definition.of(item));
            else {
                Revision pin = pins.get(relation.getRevision_id());
                if (!context.tenant().equals(pin.getTenant_id()) || !item.getId().equals(pin.getItem_id())
                        || pin.getRelease_id() == null)
                    throw new IllegalStateException("Pinned source mismatch");
                definition = pinnedDefinitions.computeIfAbsent(pin.getId(), ignored -> ReleaseCodec.definition(pin));
            }
            String key = definitionKeys.computeIfAbsent(definition, Plan.Definition::key);
            ItemCodec.Document document = parsed
                    .computeIfAbsent(key, ignored -> editor.open(definition.item(context.tenant())));
            ReferenceCodec.Options options = references.decode(relation);
            if (editor.needsOverlayProtection(document, options.overlay()))
                throw new IllegalStateException("Sensitive relationship overlay requires protected storage");
            definitions.put(key, definition);
            graph.get(relation.getTarget_id()).add(new Plan.Binding(key, options, relation.getRevision_id()));
        }
        Plan.Frozen frozen = new Plan.Frozen(view.readSet().baseline(), selection, definitions, graph);
        // Configuration aliases have independent namespaces; validate each frozen binding independently.
        for (String app : selection.applications())
            for (Plan.Binding binding : frozen.applications().get(app)) {
                ReferenceCodec.Options options = binding.options();
                ReferenceCodec.Options clear = new ReferenceCodec.Options(options.type(), options.alias(),
                        options.prefix(), options.override(), options.keys(), editor.openOverlay(options.overlay()));
                ItemMerger.merge(
                        List.of(new ItemMerger.Source(binding.source(), parsed.get(binding.source()))),
                        List.of(clear),
                        64,
                        100000);
            }
        return store.submit(context, rollout, edition, frozen, view.readSet());
    }

    /**
     * Requires exact editable draft state before preparation.
     *
     * @param context operation coordinates
     * @param id      workflow
     * @param edition expected CAS
     * @return detached draft
     */
    private Rollout draft(SettingProvider.Context context, String id, long edition) {
        Rollout value = find(context, id);
        if (value == null || !Objects.equals(Rollout.State.DRAFT.key(), value.getStatus())
                || !Objects.equals(edition, value.getEdition()))
            throw new IllegalStateException("Only the current DRAFT may be edited");
        return value;
    }

}
