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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cortex.Suite;
import org.miaixz.bus.cortex.Trait;
import org.miaixz.bus.cortex.setting.item.ItemBindingProjection;
import org.miaixz.bus.cortex.setting.item.ItemKeys;
import org.miaixz.bus.cortex.setting.item.ItemRevisionNumbers;
import org.miaixz.bus.cortex.setting.item.revision.ItemRevision;
import org.miaixz.bus.cortex.setting.item.revision.ItemRevisionStore;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Cache-backed {@code setting.item.revision} store.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CacheItemRevisionStore implements ItemRevisionStore {

    /**
     * Shared cache that stores serialized {@code setting.item.revision} snapshots.
     */
    private final CacheX<String, Object> cacheX;

    /**
     * Creates a CacheItemRevisionStore.
     *
     * @param cacheX shared cache backend
     */
    public CacheItemRevisionStore(CacheX<String, Object> cacheX) {
        this.cacheX = cacheX;
    }

    /**
     * Stores one {@code setting.item.revision} snapshot in the backing cache.
     *
     * @param revision {@code setting.item.revision} snapshot
     * @return stored revision
     */
    @Override
    public ItemRevision save(ItemRevision revision) {
        if (revision == null) {
            return null;
        }
        String json = JsonKit.toJsonString(revision);
        for (String key : revisionKeys(revision)) {
            cacheX.write(key, json, 0L);
        }
        return revision;
    }

    /**
     * Finds one {@code setting.item.revision} snapshot by its logical revision key.
     *
     * @param namespace  namespace
     * @param group      setting group
     * @param data_id    setting data identifier
     * @param profile    optional profile
     * @param revisionNo revision number
     * @return matching revision or {@code null}
     */
    @Override
    public ItemRevision find(String namespace, String group, String data_id, String profile, String revisionNo) {
        Object raw = cacheX.read(ItemKeys.revisionKeyForScope(namespace, group, data_id, profile, revisionNo));
        if (raw instanceof String json) {
            return JsonKit.toPojo(json, ItemRevision.class);
        }
        return null;
    }

    /**
     * Deletes one {@code setting.item.revision} snapshot from the backing cache.
     *
     * @param namespace  namespace
     * @param group      setting group
     * @param data_id    setting data identifier
     * @param profile    optional profile
     * @param revisionNo revision number
     */
    @Override
    public ItemRevision delete(String namespace, String group, String data_id, String profile, String revisionNo) {
        ItemRevision revision = find(namespace, group, data_id, profile, revisionNo);
        if (revision == null) {
            cacheX.remove(ItemKeys.revisionKeyForScope(namespace, group, data_id, profile, revisionNo));
            return null;
        }
        cacheX.remove(revisionKeys(revision).toArray(String[]::new));
        return revision;
    }

    /**
     * Queries all {@code setting.item.revision} snapshots for one logical setting entry.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return revisions ordered from newest to oldest
     */
    @Override
    public List<ItemRevision> query(String namespace, String group, String data_id, String profile) {
        Map<String, Object> entries = cacheX.scan(ItemKeys.revisionPrefixForScope(namespace, group, data_id, profile));
        List<ItemRevision> result = new ArrayList<>();
        for (Object value : entries.values()) {
            if (value instanceof String json) {
                ItemRevision revision = JsonKit.toPojo(json, ItemRevision.class);
                if (revision != null) {
                    result.add(revision);
                }
            }
        }
        result.sort(
                Comparator.comparingLong((ItemRevision revision) -> ItemRevisionNumbers.sortKey(revision.getRevision()))
                        .reversed());
        return result;
    }

    /**
     * Retains only the most recent revision snapshots for one logical setting entry.
     *
     * @param namespace    namespace
     * @param group        setting group
     * @param data_id      setting data identifier
     * @param profile      optional profile
     * @param maxRevisions maximum revisions to retain
     */
    @Override
    public void retainLatest(String namespace, String group, String data_id, String profile, int maxRevisions) {
        if (maxRevisions <= 0) {
            return;
        }
        List<ItemRevision> revisions = query(namespace, group, data_id, profile);
        if (revisions.size() <= maxRevisions) {
            return;
        }
        List<String> expiredKeys = new ArrayList<>();
        for (int i = maxRevisions; i < revisions.size(); i++) {
            expiredKeys.addAll(revisionKeys(revisions.get(i)));
        }
        if (!expiredKeys.isEmpty()) {
            cacheX.remove(expiredKeys.toArray(String[]::new));
        }
    }

    /**
     * Persists rollback metadata on an existing revision snapshot.
     *
     * @param namespace  namespace
     * @param group      setting group
     * @param data_id    setting data identifier
     * @param profile    optional profile
     * @param revisionNo revision number to update
     * @param revert     source revision number
     * @return updated revision or {@code null}
     */
    @Override
    public ItemRevision markRollback(
            String namespace,
            String group,
            String data_id,
            String profile,
            String revisionNo,
            String revert) {
        ItemRevision revision = find(namespace, group, data_id, profile, revisionNo);
        if (revision == null) {
            return null;
        }
        revision.setRevert(revert);
        return save(revision);
    }

    /**
     * Returns strongly typed capability flags for the fallback cache-backed revision store.
     *
     * @return capability flags
     */
    @Override
    public Suite storeCapabilities() {
        return Suite.of(Trait.DELETE, Trait.ROLLBACK_METADATA).with(Trait.DURABLE, false);
    }

    /**
     * Returns capability flags for the fallback cache-backed revision store using legacy string keys.
     *
     * @return capability flags
     */
    @Override
    public Map<String, Boolean> capabilities() {
        return storeCapabilities().asMap();
    }

    /**
     * Builds all cache keys that should point to the supplied revision.
     *
     * @param revision item revision
     * @return revision cache keys
     */
    private List<String> revisionKeys(ItemRevision revision) {
        List<String> profiles = ItemBindingProjection.normalizedProfileIds(revision);
        if (profiles == null || profiles.isEmpty()) {
            return List.of(
                    ItemKeys.revisionKeyForScope(
                            revision.getNamespace_id(),
                            revision.getGroup(),
                            revision.getData_id(),
                            null,
                            revision.getRevision()));
        }
        List<String> keys = new ArrayList<>(profiles.size());
        for (String profile : profiles) {
            String key = ItemKeys.revisionKeyForScope(
                    revision.getNamespace_id(),
                    revision.getGroup(),
                    revision.getData_id(),
                    profile,
                    revision.getRevision());
            if (!keys.contains(key)) {
                keys.add(key);
            }
        }
        return keys;
    }

}
