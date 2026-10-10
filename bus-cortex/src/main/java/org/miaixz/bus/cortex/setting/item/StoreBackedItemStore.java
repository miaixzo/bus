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
package org.miaixz.bus.cortex.setting.item;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.Keying.SettingSpec;
import org.miaixz.bus.cortex.builtin.SettingGenerator;
import org.miaixz.bus.cortex.setting.reference.Reference;
import org.miaixz.bus.cortex.setting.reference.ReferenceStore;

/**
 * Durable item store with a read-through cache.
 *
 * @author Kimi Liu
 */
public class StoreBackedItemStore {

    /**
     * Cache used for coordinate-based item reads.
     */
    private final CacheX<String, Object> cacheX;

    /**
     * Durable current-state store.
     */
    private final ItemStore store;

    /**
     * Relationship store used to enforce application visibility.
     */
    private final ReferenceStore referenceStore;

    /**
     * Setting-domain key strategy.
     */
    private final Keying<SettingSpec> keying;

    /**
     * Creates a cached store with the default key strategy.
     *
     * @param cacheX         item cache
     * @param store          durable item store
     * @param referenceStore durable relationship store
     */
    public StoreBackedItemStore(CacheX<String, Object> cacheX, ItemStore store, ReferenceStore referenceStore) {
        this(cacheX, store, referenceStore, SettingGenerator.INSTANCE);
    }

    /**
     * Creates a cached store with an explicit key strategy.
     *
     * @param cacheX         item cache
     * @param store          durable item store
     * @param referenceStore durable relationship store
     * @param keying         setting-domain key strategy
     */
    public StoreBackedItemStore(CacheX<String, Object> cacheX, ItemStore store, ReferenceStore referenceStore,
            Keying<SettingSpec> keying) {
        if (cacheX == null || store == null || referenceStore == null) {
            throw new IllegalArgumentException("Cache, ItemStore and ReferenceStore are required");
        }
        this.cacheX = cacheX;
        this.store = store;
        this.referenceStore = referenceStore;
        this.keying = keying == null ? SettingGenerator.INSTANCE : keying;
    }

    /**
     * Normalizes, persists, and caches one item.
     *
     * @param entry item to save
     * @return persisted item
     */
    public Item save(Item entry) {
        Item stored = store.save(ItemNormalizer.normalize(entry));
        cache(stored);
        return stored;
    }

    /**
     * Normalizes, persists, and caches a collection of items.
     *
     * @param entries items to save
     * @return persisted items
     */
    public List<Item> saveAll(List<Item> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<Item> normalized = new ArrayList<>(entries.size());
        for (Item entry : entries) {
            if (entry != null) {
                normalized.add(ItemNormalizer.normalize(entry));
            }
        }
        List<Item> stored = store.saveAll(normalized);
        if (stored != null) {
            stored.forEach(this::cache);
        }
        return stored == null ? List.of() : stored;
    }

    /**
     * Deletes one item and evicts its coordinate cache entry.
     *
     * @param tenant_id tenant identifier, when available
     * @param id        item identifier
     * @return deleted item, or {@code null} when absent
     */
    public Item delete(String tenant_id, String id) {
        Item deleted = store.delete(tenant_id, id);
        if (deleted != null) {
            evict(
                    deleted.getTenant_id(),
                    deleted.getSpace_id(),
                    deleted.getGroup(),
                    deleted.getCode(),
                    deleted.getProfile_id());
        }
        return deleted;
    }

    /**
     * Finds one item through the read-through cache.
     *
     * @param tenant_id  tenant identifier, when available
     * @param space_id   space identifier
     * @param profile_id profile identifier, when applicable
     * @param group      item group
     * @param code       item code
     * @return matching item, or {@code null} when absent
     */
    public Item find(String tenant_id, String space_id, String profile_id, String group, String code) {
        String key = entryKey(tenant_id, space_id, group, code, profile_id);
        Object cached = cacheX.read(key);
        if (cached instanceof Item item) {
            return item;
        }
        Item loaded = store.find(tenant_id, space_id, profile_id, group, code);
        cache(loaded);
        return loaded;
    }

    /**
     * Finds one item without a tenant coordinate.
     *
     * @param space_id   space identifier
     * @param group      item group
     * @param code       item code
     * @param profile_id profile identifier, when applicable
     * @return matching item, or {@code null} when absent
     */
    public Item find(String space_id, String group, String code, String profile_id) {
        return find(null, space_id, profile_id, group, code);
    }

    /**
     * Queries visible items and refreshes their cache entries.
     *
     * @param query item query
     * @return visible matching items
     */
    public List<Item> query(ItemQuery query) {
        if (query == null) {
            return List.of();
        }
        List<Item> loaded = store.query(query);
        if (loaded == null || loaded.isEmpty()) {
            return List.of();
        }
        List<Item> result = new ArrayList<>(loaded.size());
        for (Item item : loaded) {
            if (item != null && visibleToApp(item, query.getApp_id())) {
                result.add(cache(item));
            }
        }
        return result;
    }

    /**
     * Converts a shared item scope to a durable item query.
     *
     * @param scope shared item scope
     * @return visible matching items
     */
    public List<Item> query(ItemScope scope) {
        ItemQuery query = new ItemQuery();
        if (scope != null) {
            query.setSpace_id(scope.getSpace_id());
            query.setProfile_id(scope.getProfile_id());
            query.setApp_id(scope.getApp_id());
            query.setGroup(scope.getGroup());
            query.setLabels(scope.getLabels());
            query.setSelectors(scope.getSelectors());
            query.setRequestId(scope.getRequestId());
            query.setIncludeDeleted(scope.isIncludeDeleted());
            query.setLimit(scope.getLimit());
            query.setOffset(scope.getOffset());
        }
        return query(query);
    }

    /**
     * Evicts and reloads one item by its full coordinates.
     *
     * @param tenant_id  tenant identifier, when available
     * @param space_id   space identifier
     * @param profile_id profile identifier, when applicable
     * @param group      item group
     * @param code       item code
     * @return reloaded item, or {@code null} when absent
     */
    public Item refresh(String tenant_id, String space_id, String profile_id, String group, String code) {
        evict(tenant_id, space_id, group, code, profile_id);
        return find(tenant_id, space_id, profile_id, group, code);
    }

    /**
     * Evicts and reloads one item without a tenant coordinate.
     *
     * @param space_id   space identifier
     * @param group      item group
     * @param code       item code
     * @param profile_id profile identifier, when applicable
     * @return reloaded item, or {@code null} when absent
     */
    public Item refresh(String space_id, String group, String code, String profile_id) {
        return refresh(null, space_id, profile_id, group, code);
    }

    /**
     * Rebuilds cache entries for all items matching one scope.
     *
     * @param scope shared item scope
     * @return cached items
     */
    public List<Item> rebuild(ItemScope scope) {
        List<Item> entries = query(scope);
        entries.forEach(this::cache);
        return entries;
    }

    /**
     * Evicts one full-coordinate cache entry.
     *
     * @param tenant_id  tenant identifier, when available
     * @param space_id   space identifier
     * @param group      item group
     * @param code       item code
     * @param profile_id profile identifier, when applicable
     */
    public void evict(String tenant_id, String space_id, String group, String code, String profile_id) {
        cacheX.remove(entryKey(tenant_id, space_id, group, code, profile_id));
    }

    /**
     * Evicts one cache entry without a tenant coordinate.
     *
     * @param space_id   space identifier
     * @param group      item group
     * @param code       item code
     * @param profile_id profile identifier, when applicable
     */
    public void evict(String space_id, String group, String code, String profile_id) {
        evict(null, space_id, group, code, profile_id);
    }

    /**
     * Writes one item to its coordinate cache entry.
     *
     * @param item item to cache
     * @return the supplied item
     */
    private Item cache(Item item) {
        if (item != null) {
            cacheX.write(
                    entryKey(
                            item.getTenant_id(),
                            item.getSpace_id(),
                            item.getGroup(),
                            item.getCode(),
                            item.getProfile_id()),
                    item,
                    0L);
        }
        return item;
    }

    /**
     * Tests whether one item is visible to the requested application.
     *
     * @param item   item to inspect
     * @param app_id application identifier, when filtering is required
     * @return {@code true} when the item is visible
     */
    private boolean visibleToApp(Item item, String app_id) {
        if (app_id == null || app_id.isBlank()) {
            return true;
        }
        List<Reference> references = referenceStore
                .outgoing(item.getTenant_id(), item.getId(), Reference.Type.ITEM_APP.name());
        if (references == null || references.isEmpty()) {
            return false;
        }
        return references.stream().anyMatch(reference -> app_id.equals(reference.getTarget_id()));
    }

    /**
     * Builds the tenant-qualified cache key for one item coordinate.
     *
     * @param tenant_id  tenant identifier, when available
     * @param space_id   space identifier
     * @param group      item group
     * @param code       item code
     * @param profile_id profile identifier, when applicable
     * @return cache key
     */
    private String entryKey(String tenant_id, String space_id, String group, String code, String profile_id) {
        return value(tenant_id) + ':' + keying.key(SettingSpec.entry(space_id, group, code, profile_id));
    }

    /**
     * Normalizes a nullable cache-key segment.
     *
     * @param value segment value
     * @return normalized segment
     */
    private String value(String value) {
        return value == null ? "_" : value;
    }
}
