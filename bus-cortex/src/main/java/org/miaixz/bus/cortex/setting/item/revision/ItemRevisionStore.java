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
package org.miaixz.bus.cortex.setting.item.revision;

import java.util.List;
import java.util.Map;

import org.miaixz.bus.cortex.Suite;
import org.miaixz.bus.cortex.Trait;

/**
 * History store abstraction for {@code setting.item.revision} snapshots.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface ItemRevisionStore {

    /**
     * Persists one {@code setting.item.revision} snapshot.
     *
     * @param revision revision snapshot
     * @return stored revision
     */
    ItemRevision save(ItemRevision revision);

    /**
     * Saves a batch of revision snapshots.
     *
     * @param revisions revision snapshots
     * @return stored revision snapshots
     */
    default List<ItemRevision> saveAll(List<ItemRevision> revisions) {
        if (revisions == null || revisions.isEmpty()) {
            return List.of();
        }
        java.util.ArrayList<ItemRevision> result = new java.util.ArrayList<>(revisions.size());
        for (ItemRevision revision : revisions) {
            if (revision != null) {
                result.add(save(revision));
            }
        }
        return result;
    }

    /**
     * Deletes one concrete revision snapshot. Implementations should make this operation idempotent so callers can use
     * it as publish compensation after a partially failed current-state update.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @param revision  revision number
     * @return deleted revision snapshot, or {@code null} when absent
     */
    ItemRevision delete(String namespace, String group, String data_id, String profile, String revision);

    /**
     * Finds one concrete revision.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @param revision  revision number
     * @return matching revision or {@code null}
     */
    ItemRevision find(String namespace, String group, String data_id, String profile, String revision);

    /**
     * Queries all known {@code setting.item.revision} snapshots for one entry.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return revisions ordered from newest to oldest
     */
    List<ItemRevision> query(String namespace, String group, String data_id, String profile);

    /**
     * Lists a page of revisions.
     *
     * @param namespace namespace
     * @param group     group
     * @param data_id   data_id
     * @param profile   profile
     * @param offset    offset
     * @param limit     page size
     * @return paged revisions
     */
    default List<ItemRevision> list(
            String namespace,
            String group,
            String data_id,
            String profile,
            int offset,
            int limit) {
        List<ItemRevision> revisions = query(namespace, group, data_id, profile);
        if (revisions == null || revisions.isEmpty()) {
            return List.of();
        }
        int from = Math.max(offset, 0);
        if (from >= revisions.size()) {
            return List.of();
        }
        int size = limit > 0 ? limit : revisions.size();
        return revisions.subList(from, Math.min(revisions.size(), from + size));
    }

    /**
     * Trims history so that only the latest revisions remain.
     *
     * @param namespace    namespace
     * @param group        setting group
     * @param data_id      setting data identifier
     * @param profile      optional profile
     * @param maxRevisions max revisions to keep
     */
    void retainLatest(String namespace, String group, String data_id, String profile, int maxRevisions);

    /**
     * Marks one revision as a rollback of another revision and persists the updated metadata.
     *
     * <p>
     * ItemRevision stores are primarily append-oriented, but rollback metadata is assigned only after the rollback
     * publish succeeds. Implementations must therefore update the already-written revision snapshot atomically when
     * their backing storage supports it, or fail without mutating state when it does not.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @param revision  revision number to update
     * @param revert    source revision number
     * @return updated revision, or {@code null} when the revision does not exist
     */
    default ItemRevision markRollback(
            String namespace,
            String group,
            String data_id,
            String profile,
            String revision,
            String revert) {
        ItemRevision snapshot = find(namespace, group, data_id, profile, revision);
        if (snapshot == null) {
            return null;
        }
        snapshot.setRevert(revert);
        return save(snapshot);
    }

    /**
     * Returns the latest revision for one setting entry.
     *
     * @param namespace namespace
     * @param group     group
     * @param data_id   data_id
     * @param profile   profile
     * @return latest revision or {@code null}
     */
    default ItemRevision latest(String namespace, String group, String data_id, String profile) {
        List<ItemRevision> revisions = list(namespace, group, data_id, profile, 0, 1);
        return revisions.isEmpty() ? null : revisions.getFirst();
    }

    /**
     * Returns strongly typed revision-store capability hints.
     *
     * @return capability flags
     */
    default Suite storeCapabilities() {
        return Suite.of(Trait.DURABLE, Trait.DELETE, Trait.ROLLBACK_METADATA);
    }

    /**
     * Returns revision-store capability hints using legacy string keys.
     *
     * @return capability flags
     */
    default Map<String, Boolean> capabilities() {
        return storeCapabilities().asMap();
    }

}
