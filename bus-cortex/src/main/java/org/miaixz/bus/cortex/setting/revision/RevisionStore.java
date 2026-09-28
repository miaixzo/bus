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

/**
 * Append-only store contract for immutable setting revisions.
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
     * @param tenant_id tenant identifier, when available
     * @param item_id   item identifier
     * @param revision  item-scoped revision number
     * @return matching revision, or {@code null} when absent
     */
    Revision find(String tenant_id, String item_id, String revision);

    /**
     * Lists revisions for one item in newest-first order.
     *
     * @param tenant_id tenant identifier, when available
     * @param item_id   item identifier
     * @return item revisions
     */
    List<Revision> query(String tenant_id, String item_id);

    /**
     * Returns the latest revision for one item.
     *
     * @param tenant_id tenant identifier, when available
     * @param item_id   item identifier
     * @return latest revision, or {@code null} when absent
     */
    default Revision latest(String tenant_id, String item_id) {
        List<Revision> revisions = query(tenant_id, item_id);
        return revisions == null || revisions.isEmpty() ? null : revisions.getFirst();
    }

    /**
     * Retains at most the requested number of newest revisions.
     *
     * @param tenant_id    tenant identifier, when available
     * @param item_id      item identifier
     * @param maxRevisions maximum number of revisions to retain
     */
    void retainLatest(String tenant_id, String item_id, int maxRevisions);
}
