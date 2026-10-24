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

import org.miaixz.bus.cortex.setting.curator.ItemService;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.reference.ReferenceService;

/**
 * Only public current-state write orchestration; no snapshots or formal versions are created.
 *
 * @author Kimi Liu
 */
public final class SettingService {

    /**
     * Definition preparation.
     */
    private final ItemService items;
    /**
     * Relationship preparation.
     */
    private final ReferenceService references;
    /**
     * Mandatory host joint transaction.
     */
    private final SettingStore store;
    /**
     * Mandatory permissions.
     */
    private final SettingEnforcer enforcer;

    /**
     * Creates current management orchestration.
     *
     * @param items      definition preparation
     * @param references relation preparation
     * @param store      atomic host boundary
     * @param enforcer   permissions
     */
    public SettingService(ItemService items, ReferenceService references, SettingStore store,
            SettingEnforcer enforcer) {
        this.items = Objects.requireNonNull(items);
        this.references = Objects.requireNonNull(references);
        this.store = Objects.requireNonNull(store);
        this.enforcer = Objects.requireNonNull(enforcer);
    }

    /**
     * Saves current content and explicit relationship changes atomically.
     *
     * @param context trusted identity
     * @param change  requested current-state edit
     * @return committed detached state
     */
    public SettingStore.Result save(SettingEnforcer.Context context, SettingStore.Change change) {
        enforcer.require(context, "save", "item", change.item());
        Item expected = items.find(context, change.item());
        if (expected == null ? change.expectedEdition() != null
                : !Objects.equals(expected.getEdition(), change.expectedEdition()))
            throw new IllegalStateException("Current edition conflict");
        if (change.next() == null && change.relations() == null)
            throw new IllegalArgumentException("No edit submitted");
        Item next = null;
        if (change.next() != null) {
            if (!change.item().equals(change.next().getId()))
                throw new IllegalArgumentException("Source identity mismatch");
            next = items.prepare(context, expected, change.next(), change.actions(), change.validation());
        }
        Item source = next == null ? expected : next;
        if (source == null)
            throw new IllegalArgumentException("Source not found");
        SettingStore.RelationChange relations = change.relations() == null ? null
                : new SettingStore.RelationChange(references.prepare(context, source, change.relations().next()),
                        change.relations().expected());
        return store.commit(
                context,
                new SettingStore.Change(change.item(), next, change.expectedEdition(), Map.of(), change.validation(),
                        relations));
    }

    /**
     * Deletes only current state and its relations.
     *
     * @param context            trusted identity
     * @param item               exact source
     * @param edition            expected CAS
     * @param expectedReferences complete relationship editions
     */
    public void delete(
            SettingEnforcer.Context context,
            String item,
            long edition,
            Map<String, Long> expectedReferences) {
        enforcer.require(context, "delete", "item", item);
        store.delete(context, item, edition, Map.copyOf(expectedReferences));
    }

}
