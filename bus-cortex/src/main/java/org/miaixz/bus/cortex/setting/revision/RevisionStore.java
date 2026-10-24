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
package org.miaixz.bus.cortex.setting.revision;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.cortex.setting.item.ItemNormalizer;

/**
 * Append-only store contract for immutable setting revisions within a mandatory tenant. A saved snapshot must never be
 * overwritten, including editor payload, reference manifests, or protection metadata. Rollbacks append new snapshots
 * instead of modifying historical snapshots.
 *
 * @author Kimi Liu
 */
public interface RevisionStore {

    /**
     * Appends one immutable revision.
     *
     * @param revision revision to append
     * @return persisted revision
     */
    Revision save(Revision revision);

    /**
     * Appends each non-null revision in encounter order.
     *
     * @param revisions revisions to append
     * @return persisted revisions
     */
    default List<Revision> saveAll(List<Revision> revisions) {
        if (revisions == null || revisions.isEmpty()) {
            return List.of();
        }
        List<Revision> result = new ArrayList<>(revisions.size());
        for (Revision revision : revisions) {
            if (revision != null) {
                result.add(save(revision));
            }
        }
        return result;
    }

    /**
     * Finds one immutable revision.
     *
     * @param tenant_id mandatory tenant identifier
     * @param item_id   item identifier
     * @param revision  item-scoped revision number
     * @return matching revision, or {@code null} when absent
     */
    Revision find(String tenant_id, String item_id, String revision);

    /**
     * Lists revisions for one item in newest-first order.
     *
     * @param tenant_id mandatory tenant identifier
     * @param item_id   item identifier
     * @return item revisions
     */
    List<Revision> query(String tenant_id, String item_id);

    /**
     * Finds an immutable snapshot by identifier without crossing the tenant or item boundary.
     *
     * @param tenant_id mandatory tenant identifier
     * @param item_id   required item identifier
     * @param id        required immutable snapshot identifier
     * @return matching snapshot, or null when absent
     * @throws IllegalArgumentException if tenant, item, or revision identity is missing
     */
    default Revision identity(String tenant_id, String item_id, String id) {
        ItemNormalizer.requireTenant(tenant_id);
        if (item_id == null || item_id.isBlank() || id == null || id.isBlank()) {
            throw new IllegalArgumentException("Revision identity coordinates are required");
        }
        return query(tenant_id, item_id).stream()
                .filter(
                        revision -> tenant_id.equals(revision.getTenant_id()) && item_id.equals(revision.getItem_id())
                                && id.equals(revision.getId()))
                .findFirst().orElse(null);
    }

    /**
     * Returns the latest revision for one item.
     *
     * @param tenant_id mandatory tenant identifier
     * @param item_id   item identifier
     * @return latest revision, or {@code null} when absent
     */
    default Revision latest(String tenant_id, String item_id) {
        List<Revision> revisions = query(tenant_id, item_id);
        return revisions == null || revisions.isEmpty() ? null : revisions.getFirst();
    }

    /**
     * Retains the newest revisions and every revision reachable from a stable/gray pointer, fixed reference, frozen
     * release, retained platform version, or watch. Unknown reachability must retain history.
     *
     * @param tenant_id    mandatory tenant identifier
     * @param item_id      item identifier
     * @param maxRevisions maximum number of revisions to retain
     */
    void retainLatest(String tenant_id, String item_id, int maxRevisions);

}
