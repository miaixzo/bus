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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Suite;
import org.miaixz.bus.cortex.Trait;
import org.miaixz.bus.cortex.builtin.MetadataMatcher;
import org.miaixz.bus.cortex.magic.identity.CortexIdentity;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;

/**
 * Store-backed current-state setting coordinator.
 *
 * <p>
 * This mirrors {@code StoreBackedRegistry}: a durable {@link ItemStore} is optional, while cache projection is kept
 * inside this coordinator instead of being exposed as another store abstraction.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class StoreBackedItemStore {

    /**
     * Shared cache used for current-state projection.
     */
    private final CacheX<String, Object> cacheX;
    /**
     * Optional durable current-state store.
     */
    private final ItemStore store;

    /**
     * Creates a StoreBackedItemStore.
     *
     * @param cacheX shared cache backend
     * @param store  durable current-state store, or {@code null} for cache-only fallback
     */
    public StoreBackedItemStore(CacheX<String, Object> cacheX, ItemStore store) {
        this.cacheX = cacheX;
        this.store = store;
    }

    /**
     * Saves the entry to durable storage first, then updates the cache projection.
     *
     * @param entry entry to store
     * @return stored entry snapshot
     */
    public Item save(Item entry) {
        if (entry == null) {
            return null;
        }
        Item prepared = ItemNormalizer.normalize(entry);
        Item stored = durable() ? store.save(prepared) : prepared;
        if (store != null && !durable()) {
            capabilityFallback("save", Trait.DURABLE, "cache write");
        }
        return cache(stored == null ? prepared : stored);
    }

    /**
     * Saves a batch of entries.
     *
     * @param entries entries to store
     * @return stored entry snapshots
     */
    public List<Item> saveAll(List<Item> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<Item> result = new ArrayList<>(entries.size());
        for (Item entry : entries) {
            if (entry != null) {
                result.add(save(entry));
            }
        }
        return result;
    }

    /**
     * Deletes from durable storage and evicts the cache projection.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return deleted entry snapshot, or {@code null} when absent
     */
    public Item delete(String namespace, String group, String data_id, String profile) {
        Item existing = find(namespace, group, data_id, profile);
        if (StringKit.isNotEmpty(profile) && !matchesExactProfile(existing, profile)) {
            existing = null;
        }
        Item deleted = durable() ? store.delete(namespace, group, data_id, profile) : existing;
        if (store != null && !durable()) {
            capabilityFallback("delete", Trait.DURABLE, "cache evict");
        }
        evict(namespace, group, data_id, profile);
        return deleted == null ? existing : deleted;
    }

    /**
     * Finds from cache first and falls back to durable storage.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return current entry or {@code null}
     */
    public Item find(String namespace, String group, String data_id, String profile) {
        Item cached = cached(namespace, group, data_id, profile);
        if (cached != null || store == null || !durable()) {
            if (cached == null && store != null && !durable()) {
                capabilityFallback("find", Trait.DURABLE, "cache query");
            }
            return cached != null ? cached : first(queryByCoordinates(namespace, group, data_id, profile));
        }
        Item loaded = store.find(namespace, group, data_id, profile);
        if (loaded == null) {
            return null;
        }
        return cache(loaded);
    }

    /**
     * Queries durable storage first and warms the cache projection with the result set.
     *
     * @param query query filter
     * @return matching entries
     */
    public List<Item> query(ItemQuery query) {
        if (store == null || !queryable()) {
            if (store != null) {
                capabilityFallback("query", Trait.QUERY, "cache query");
            }
            return queryCache(query);
        }
        List<Item> entries = store.query(query);
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<Item> result = new ArrayList<>(entries.size());
        for (Item entry : entries) {
            if (entry != null) {
                result.add(cache(entry));
            }
        }
        return result;
    }

    /**
     * Queries setting entries for one general scope.
     *
     * @param scope scope filter
     * @return matching entries
     */
    public List<Item> query(ItemScope scope) {
        return query(toQuery(scope));
    }

    /**
     * Reloads one entry from durable storage into cache.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return refreshed entry or {@code null}
     */
    public Item refresh(String namespace, String group, String data_id, String profile) {
        if (store == null || !durable()) {
            if (store != null) {
                capabilityFallback("refresh", Trait.DURABLE, "cache read");
            }
            return cached(namespace, group, data_id, profile);
        }
        Item loaded = store.find(namespace, group, data_id, profile);
        if (loaded == null) {
            evict(namespace, group, data_id, profile);
            return null;
        }
        return cache(loaded);
    }

    /**
     * Rebuilds cache projection from durable storage for the provided scope.
     *
     * @param scope rebuild scope
     * @return rebuilt entries
     */
    public List<Item> rebuild(ItemScope scope) {
        if (store == null || !durable() || !queryable()) {
            if (store != null) {
                capabilityFallback("rebuild", Trait.DURABLE, "cache query");
            }
            return queryCache(toQuery(scope));
        }
        evict(scope);
        ItemQuery query = toQuery(scope);
        List<Item> entries = store.query(query);
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<Item> result = new ArrayList<>(entries.size());
        for (Item entry : entries) {
            if (entry != null) {
                result.add(cache(entry));
            }
        }
        return result;
    }

    /**
     * Evicts one setting entry from cache without touching durable state.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     */
    public void evict(String namespace, String group, String data_id, String profile) {
        if (StringKit.isNotEmpty(profile)) {
            cacheX.remove(ItemKeys.entryKeyForScope(namespace, group, data_id, profile));
            return;
        }
        List<String> keys = new ArrayList<>();
        String sharedKey = ItemKeys.entryKeyForScope(namespace, group, data_id, null);
        keys.add(sharedKey);
        Map<String, Object> profileEntries = cacheX.scan(sharedKey + ":");
        if (profileEntries != null && !profileEntries.isEmpty()) {
            keys.addAll(profileEntries.keySet());
        }
        cacheX.remove(keys.toArray(String[]::new));
    }

    /**
     * Returns effective current-state capability hints.
     *
     * @return capability flags
     */
    public Suite storeCapabilities() {
        return Suite.of(Trait.BATCH, Trait.QUERY, Trait.CACHE, Trait.EVICT, Trait.REBUILD)
                .with(Trait.DURABLE, store != null);
    }

    /**
     * Returns effective current-state capability hints using legacy string keys.
     *
     * @return capability flags
     */
    public Map<String, Boolean> capabilities() {
        return storeCapabilities().asMap();
    }

    /**
     * Normalizes and writes one current-state setting entry into the cache projection.
     *
     * @param entry setting entry
     * @return cached normalized entry
     */
    private Item cache(Item entry) {
        if (entry == null) {
            return null;
        }
        Item prepared = ItemNormalizer.normalize(entry);
        String json = JsonKit.toJsonString(prepared);
        for (String key : cacheKeys(prepared)) {
            cacheX.write(key, json, 0L);
        }
        return prepared;
    }

    /**
     * Loads one current-state setting entry from the cache projection.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return cached setting entry or {@code null}
     */
    private Item cached(String namespace, String group, String data_id, String profile) {
        if (StringKit.isNotEmpty(profile)) {
            Item scoped = readCached(ItemKeys.entryKeyForScope(namespace, group, data_id, profile));
            if (matchesExactProfile(scoped, profile)) {
                return scoped;
            }
        }
        Item shared = readCached(ItemKeys.entryKeyForScope(namespace, group, data_id, null));
        return ItemBindingProjection.matchesProfileBinding(shared, profile) ? shared : null;
    }

    /**
     * Scans and filters the cache projection using the same selector semantics as the durable store.
     *
     * @param query cache query
     * @return filtered cache snapshot
     */
    private List<Item> queryCache(ItemQuery query) {
        ItemQuery criteria = query != null ? query : new ItemQuery();
        String namespace = CortexIdentity.namespace(criteria.getNamespace_id());
        Map<String, Object> entries = cacheX.scan(ItemKeys.entryPrefix(namespace));
        Map<String, Item> result = new LinkedHashMap<>();
        for (Object value : entries.values()) {
            if (!(value instanceof String json)) {
                continue;
            }
            Item entry = JsonKit.toPojo(json, Item.class);
            if (entry == null || !matches(entry, criteria)) {
                continue;
            }
            result.putIfAbsent(cacheIdentity(entry), entry);
        }
        List<Item> page = new ArrayList<>(result.values());
        int offset = Math.max(criteria.getOffset(), 0);
        int limit = criteria.getLimit() > 0 ? criteria.getLimit() : page.size();
        if (offset >= page.size()) {
            return List.of();
        }
        int toIndex = limit == 0 ? page.size() : Math.min(page.size(), offset + limit);
        return page.subList(offset, toIndex);
    }

    /**
     * Evicts one or more cache entries described by the supplied scope.
     *
     * @param scope cache-eviction scope
     */
    private void evict(ItemScope scope) {
        ItemQuery query = toQuery(scope);
        String namespace = CortexIdentity.namespace(query.getNamespace_id());
        if (StringKit.isNotEmpty(query.getData_id())) {
            evict(namespace, query.getGroup(), query.getData_id(), query.getProfile_id());
            return;
        }
        List<Item> entries = queryCache(query);
        if (entries.isEmpty()) {
            return;
        }
        List<String> keys = new ArrayList<>(entries.size());
        for (Item entry : entries) {
            keys.addAll(cacheKeys(entry));
        }
        cacheX.remove(keys.toArray(String[]::new));
    }

    /**
     * Converts one broad setting scope into the query model used by cache and store scans.
     *
     * @param scope source scope
     * @return equivalent query selector
     */
    private ItemQuery toQuery(ItemScope scope) {
        ItemQuery query = new ItemQuery();
        if (scope == null) {
            return query;
        }
        query.setNamespace_id(CortexIdentity.namespace(scope.getNamespace_id()));
        query.setGroup(scope.getGroup());
        query.setApp_id(scope.getApp_id());
        query.setProfile_id(scope.getProfile_id());
        query.setLabels(scope.getLabels());
        query.setSelectors(scope.getSelectors());
        query.setLimit(scope.getLimit());
        query.setOffset(scope.getOffset());
        query.setIncludeDeleted(scope.isIncludeDeleted());
        query.setRequestId(scope.getRequestId());
        if (scope instanceof ItemQuery itemQuery) {
            query.setData_id(itemQuery.getData_id());
            query.setFallbackValue(itemQuery.getFallbackValue());
            query.setRequestContext(itemQuery.getRequestContext());
        }
        return query;
    }

    /**
     * Returns whether one setting entry satisfies the supplied query selector.
     *
     * @param entry    setting entry
     * @param criteria query selector
     * @return {@code true} when the entry matches
     */
    private boolean matches(Item entry, ItemQuery criteria) {
        if (!criteria.isIncludeDeleted() && entry.getStatus() != null && entry.getStatus() < 0) {
            return false;
        }
        if (StringKit.isNotEmpty(criteria.getNamespace_id())
                && !Objects.equals(CortexIdentity.namespace(criteria.getNamespace_id()), entry.getNamespace_id())) {
            return false;
        }
        if (StringKit.isNotEmpty(criteria.getGroup()) && !Objects.equals(criteria.getGroup(), entry.getGroup())) {
            return false;
        }
        if (StringKit.isNotEmpty(criteria.getData_id()) && !Objects.equals(criteria.getData_id(), entry.getData_id())) {
            return false;
        }
        if (!ItemBindingProjection.matchesProfileBinding(entry, criteria.getProfile_id())) {
            return false;
        }
        if (StringKit.isNotEmpty(criteria.getApp_id())
                && !ItemBindingProjection.bindsToApp(entry, criteria.getApp_id())) {
            return false;
        }
        return MetadataMatcher.matches(entry.getLabels(), criteria.getLabels(), criteria.getSelectors());
    }

    /**
     * Queries setting items by their storage coordinates.
     *
     * @param namespace namespace identifier
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   profile identifier
     * @return matching setting items
     */
    private List<Item> queryByCoordinates(String namespace, String group, String data_id, String profile) {
        ItemQuery query = new ItemQuery();
        query.setNamespace_id(namespace);
        query.setGroup(group);
        query.setData_id(data_id);
        query.setProfile_id(profile);
        return store == null || !queryable() ? queryCache(query) : store.query(query);
    }

    /**
     * Returns the first setting item from a list.
     *
     * @param entries setting items
     * @return first item or {@code null}
     */
    private Item first(List<Item> entries) {
        return entries == null || entries.isEmpty() ? null : entries.getFirst();
    }

    /**
     * Builds all cache keys that should point to one item entry.
     *
     * @param entry setting item
     * @return cache keys
     */
    private List<String> cacheKeys(Item entry) {
        List<String> profiles = ItemBindingProjection.normalizedProfileIds(entry);
        if (profiles == null || profiles.isEmpty()) {
            return List
                    .of(ItemKeys.entryKeyForScope(entry.getNamespace_id(), entry.getGroup(), entry.getData_id(), null));
        }
        List<String> keys = new ArrayList<>(profiles.size());
        for (String profile : profiles) {
            String key = ItemKeys
                    .entryKeyForScope(entry.getNamespace_id(), entry.getGroup(), entry.getData_id(), profile);
            if (!keys.contains(key)) {
                keys.add(key);
            }
        }
        return keys;
    }

    /**
     * Reads one item from the local cache.
     *
     * @param key cache key
     * @return cached item or {@code null}
     */
    private Item readCached(String key) {
        Object raw = cacheX.read(key);
        return raw instanceof String json ? JsonKit.toPojo(json, Item.class) : null;
    }

    /**
     * Builds a cache identity for diagnostics and change events.
     *
     * @param entry setting item
     * @return cache identity
     */
    private String cacheIdentity(Item entry) {
        List<String> profiles = ItemBindingProjection.normalizedProfileIds(entry);
        return ItemKeys.profileScope(
                entry.getNamespace_id(),
                entry.getGroup(),
                entry.getData_id(),
                profiles == null || profiles.isEmpty() ? null : String.join(",", profiles));
    }

    /**
     * Returns whether an item is bound exactly to the supplied profile.
     *
     * @param entry   setting item
     * @param profile profile identifier
     * @return {@code true} when the item is bound to the supplied profile only
     */
    private boolean matchesExactProfile(Item entry, String profile) {
        if (entry == null) {
            return false;
        }
        List<String> profiles = ItemBindingProjection.normalizedProfileIds(entry);
        if (StringKit.isEmpty(profile)) {
            return profiles == null || profiles.isEmpty();
        }
        return profiles != null && profiles.contains(profile.trim().toLowerCase());
    }

    /**
     * Returns whether the backing store provides durable persistence.
     *
     * @return {@code true} when the store supports durable writes
     */
    private boolean durable() {
        return store != null && store.storeCapabilities().supports(Trait.DURABLE);
    }

    /**
     * Returns whether the backing store can execute queries.
     *
     * @return {@code true} when the store supports query operations
     */
    private boolean queryable() {
        return store != null && store.storeCapabilities().supports(Trait.QUERY);
    }

    /**
     * Logs that an operation is falling back because a store trait is missing.
     *
     * @param operation  operation name
     * @param capability missing store trait
     * @param fallback   fallback behavior description
     */
    private void capabilityFallback(String operation, Trait capability, String fallback) {
        Logger.warn(
                "Setting item store capability missing [operation={}, capability={}], using {}",
                operation,
                capability == null ? null : capability.key(),
                fallback);
    }

}
