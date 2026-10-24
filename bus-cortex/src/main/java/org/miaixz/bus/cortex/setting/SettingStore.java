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
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.miaixz.bus.cortex.setting.app.App;
import org.miaixz.bus.cortex.setting.curator.ItemEditor;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemCodec;
import org.miaixz.bus.cortex.setting.reference.Reference;
import org.miaixz.bus.cortex.setting.rollout.Plan;

/**
 * Host current-state transaction: Item and relationship changes commit together, never with Revision.
 *
 * @author Kimi Liu
 */
public interface SettingStore {

    /**
     * Atomically validates identity, tenant, CAS, complete relation membership and target/pin constraints, then commits
     * already protected input. Only definition changes advance Item.edition.
     *
     * @param context authenticated operation identity
     * @param change  protected definition and relationship changes with concurrency expectations
     * @return detached committed current state
     */
    Result commit(SettingEnforcer.Context context, Change change);

    /**
     * Deletes current definition and relationships atomically; must preserve all formal Release/Revision data.
     *
     * @param context            authenticated operation identity
     * @param item               exact current item identifier
     * @param edition            expected definition concurrency counter
     * @param expectedReferences complete expected relationship identifier-to-edition map
     */
    void delete(SettingEnforcer.Context context, String item, long edition, Map<String, Long> expectedReferences);

    /**
     * Reads a consistent source graph and complete concurrency read-set for the requested applications.
     *
     * @param tenant exact tenant identifier
     * @param apps   application identifiers whose complete source graph is required
     * @return consistent source graph and its concurrency read-set
     */
    View read(String tenant, Set<String> apps);

    /**
     * Tests a read-set for diagnostics; final freeze must atomically compare it in RolloutStore.submit.
     *
     * @param tenant  exact tenant identifier
     * @param readSet complete source graph expectations captured during the read
     * @return whether all read-set expectations still match
     */
    boolean matches(String tenant, Plan.ReadSet readSet);

    /**
     * A single current-state edit.
     *
     * @param item            exact target
     * @param next            definition edit or null for relationships only
     * @param expectedEdition current edition, null only for creation
     * @param actions         sensitive intent
     * @param validation      requested validation
     * @param relations       explicit relation edit or null to preserve
     * @author Kimi Liu
     */
    record Change(String item, Item next, Long expectedEdition, Map<String, ItemEditor.SecretAction> actions,
            ItemCodec.ValidationLevel validation, RelationChange relations) {

        /**
         * Freezes command members; null relation edit differs from an empty replacement set.
         *
         * @param item            exact target
         * @param next            definition edit
         * @param expectedEdition expected CAS
         * @param actions         sensitive intent
         * @param validation      validation
         * @param relations       relationship edit
         */
        public Change {
            if (item == null || item.isBlank())
                throw new IllegalArgumentException("Exact item required");
            actions = actions == null ? Map.of() : Map.copyOf(actions);
            validation = Objects.requireNonNull(validation);
        }

    }

    /**
     * Complete desired relationship set.
     *
     * @param next     desired relations
     * @param expected exact existing ID to edition map
     * @author Kimi Liu
     */
    record RelationChange(List<Reference> next, Map<String, Long> expected) {

        /**
         * Freezes relationship membership expectations.
         *
         * @param next     desired relations
         * @param expected complete expected set
         */
        public RelationChange {
            next = List.copyOf(next);
            expected = Map.copyOf(expected);
        }

    }

    /**
     * Detached committed current state.
     *
     * @param item       committed definition
     * @param references committed relationships
     * @author Kimi Liu
     */
    record Result(Item item, List<Reference> references) {

        /**
         * Freezes relationships.
         *
         * @param item       committed definition
         * @param references committed relationships
         */
        public Result {
            references = List.copyOf(references);
        }

    }

    /**
     * Consistent candidate source graph.
     *
     * @param apps       applications
     * @param items      current definitions
     * @param references current relations
     * @param readSet    exact concurrency proof
     * @author Kimi Liu
     */
    record View(List<App> apps, List<Item> items, List<Reference> references, Plan.ReadSet readSet) {

        /**
         * Freezes graph membership.
         *
         * @param apps       applications
         * @param items      definitions
         * @param references relationships
         * @param readSet    read proof
         */
        public View {
            apps = List.copyOf(apps);
            items = List.copyOf(items);
            references = List.copyOf(references);
            Objects.requireNonNull(readSet);
        }

    }

}
