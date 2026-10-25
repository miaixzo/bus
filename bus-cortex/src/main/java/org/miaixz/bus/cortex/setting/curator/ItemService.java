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
package org.miaixz.bus.cortex.setting.curator;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.cortex.guard.CortexGuard;
import org.miaixz.bus.cortex.release.ReleaseCodec;
import org.miaixz.bus.cortex.setting.SettingProvider;
import org.miaixz.bus.cortex.setting.SettingStore;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemCodec;
import org.miaixz.bus.cortex.setting.item.ItemScope;
import org.miaixz.bus.cortex.setting.item.ItemStore;
import org.miaixz.bus.cortex.setting.revision.Revision;
import org.miaixz.bus.cortex.setting.revision.RevisionStore;

/**
 * Current-state management reads and edit preparation; actual writes belong to SettingService.
 *
 * @author Kimi Liu
 */
public final class ItemService {

    /**
     * Current-state source.
     */
    private final ItemStore store;
    /**
     * Published recovery source only.
     */
    private final RevisionStore revisions;
    /**
     * Sensitive member editor.
     */
    private final ItemEditor editor;
    /**
     * Optional host access policy.
     */
    private final CortexGuard guard;

    /**
     * Creates management preparation.
     *
     * @param store     current state
     * @param revisions published content
     * @param editor    sensitive editor
     * @param guard     optional host access policy
     */
    public ItemService(ItemStore store, RevisionStore revisions, ItemEditor editor, CortexGuard guard) {
        this.store = Objects.requireNonNull(store);
        this.revisions = Objects.requireNonNull(revisions);
        this.editor = Objects.requireNonNull(editor);
        this.guard = guard;
    }

    /**
     * Finds exact protected current state for internal management orchestration.
     *
     * @param context operation coordinates
     * @param item    exact source
     * @return protected entity or absent
     */
    public Item find(SettingProvider.Context context, String item) {
        context.check(guard, "read", "item", item);
        Item found = store.find(context.tenant(), item);
        if (found != null && (!context.tenant().equals(found.getTenant_id()) || !item.equals(found.getId())))
            throw new IllegalStateException("Source identity mismatch");
        return found;
    }

    /**
     * Queries bounded current management state.
     *
     * @param context operation coordinates
     * @param scope   page and filters
     * @return protected current entities
     */
    public List<Item> query(SettingProvider.Context context, ItemScope scope) {
        context.check(guard, "read", "item", scope.getId());
        if (!context.tenant().equals(scope.getTenant_id()) || scope.getLimit() < 1 || scope.getLimit() > 1000
                || scope.getOffset() < 0)
            throw new IllegalArgumentException("Invalid management scope");
        List<Item> items = store.query(scope);
        for (Item item : items)
            if (!context.tenant().equals(item.getTenant_id()))
                throw new IllegalStateException("Tenant mismatch");
        return List.copyOf(items);
    }

    /**
     * Returns a safe already-parsed management projection.
     *
     * @param context operation coordinates
     * @param item    exact source
     * @return safe definition or absent
     */
    public ItemCodec.Document view(SettingProvider.Context context, String item) {
        Item value = find(context, item);
        return value == null ? null : editor.view(value);
    }

    /**
     * Prepares only current configuration data.
     *
     * @param context   operation coordinates
     * @param expected  current state
     * @param candidate edit
     * @param actions   exact sensitive intent
     * @param level     validation
     * @return protected candidate
     */
    public Item prepare(
            SettingProvider.Context context,
            Item expected,
            Item candidate,
            Map<String, ItemEditor.SecretAction> actions,
            ItemCodec.ValidationLevel level) {
        context.check(guard, "save", "item", candidate.getId());
        if (!context.tenant().equals(candidate.getTenant_id()))
            throw new IllegalArgumentException("Tenant mismatch");
        if (actions != null && !actions.isEmpty())
            context.check(guard, "secret-edit", "item", candidate.getId());
        return editor.prepare(expected, candidate, actions, level);
    }

    /**
     * Prepares current state from exact published content; caller commits through SettingService.
     *
     * @param context           operation coordinates
     * @param item              exact current source
     * @param edition           expected editing CAS
     * @param publishedRevision exact published snapshot
     * @return uncommitted current-state edit for submission through SettingService.save
     */
    public SettingStore.Change restore(
            SettingProvider.Context context,
            String item,
            long edition,
            String publishedRevision) {
        context.check(guard, "restore", "item", item);
        Item current = find(context, item);
        if (current == null || !Objects.equals(current.getEdition(), edition))
            throw new IllegalStateException("Current edition changed");
        Revision revision = revisions.find(context.tenant(), publishedRevision);
        if (revision == null || !context.tenant().equals(revision.getTenant_id()) || !item.equals(revision.getItem_id())
                || revision.getRelease_id() == null)
            throw new IllegalArgumentException("Published source snapshot required");
        if (Integer.valueOf(1).equals(current.getEncrypted()) || Integer.valueOf(1).equals(revision.getEncrypted()))
            context.check(guard, "secret-edit", "item", item);
        ItemEditor.Restoration restored = editor
                .restore(current, ReleaseCodec.definition(revision).item(context.tenant()));
        return new SettingStore.Change(item, restored.candidate(), edition, restored.actions(),
                ItemCodec.ValidationLevel.FULL, null);
    }

}
