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
import java.util.Objects;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.Keying.SettingSpec;
import org.miaixz.bus.cortex.builtin.SettingGenerator;
import org.miaixz.bus.cortex.setting.reference.Reference;
import org.miaixz.bus.cortex.setting.reference.ReferenceStore;
import org.miaixz.bus.extra.json.JsonKit;

/** Persistent read-through cache isolated by tenant and item coordinates. */
public class StoreBackedItemStore {

    private final CacheX<String, Object> cacheX;
    private final ItemStore store;
    private final ReferenceStore referenceStore;
    private final Keying<SettingSpec> keying;

    public StoreBackedItemStore(CacheX<String, Object> cacheX, ItemStore store, ReferenceStore referenceStore) {
        this(cacheX, store, referenceStore, SettingGenerator.INSTANCE);
    }

    public StoreBackedItemStore(CacheX<String, Object> cacheX, ItemStore store, ReferenceStore referenceStore,
            Keying<SettingSpec> keying) {
        if (cacheX == null || store == null || referenceStore == null) {
            throw new IllegalArgumentException("The item cache, persistent store, and reference store are required");
        }
        this.cacheX = cacheX;
        this.store = store;
        this.referenceStore = referenceStore;
        this.keying = keying == null ? SettingGenerator.INSTANCE : keying;
    }

    public Item save(Item entry) {
        return cache(store.save(ItemNormalizer.normalize(copy(entry))));
    }

    public List<Item> saveAll(List<Item> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<Item> normalized = entries.stream().filter(Objects::nonNull).map(this::copy).map(ItemNormalizer::normalize)
                .toList();
        List<Item> stored = store.saveAll(normalized);
        if (stored == null) {
            return List.of();
        }
        stored.forEach(this::cache);
        return stored;
    }

    public Item delete(String tenant_id, String id) {
        ItemNormalizer.requireTenant(tenant_id);
        Item deleted = store.delete(tenant_id, id);
        if (deleted != null) {
            if (!tenant_id.equals(deleted.getTenant_id())) {
                throw new IllegalStateException("The item store returned data belonging to another tenant");
            }
            evict(deleted);
        }
        return deleted;
    }

    /** Reads complete business coordinates, caching source and effective items separately. */
    public Item find(Item coordinates) {
        String key = entryKey(coordinates);
        Object cached = cacheX.read(key);
        Item snapshot = cached instanceof String json ? JsonKit.toPojo(json, Item.class)
                : cached instanceof Item item ? copy(item) : null;
        if (snapshot != null && key.equals(entryKey(snapshot))) {
            return snapshot;
        }
        Item loaded = store.find(coordinates);
        if (loaded != null && !key.equals(entryKey(loaded))) {
            throw new IllegalStateException("The returned item does not match the requested coordinates");
        }
        return cache(loaded);
    }

    /**
     * Queries application-owned items and referenced shared items. Persistent implementations must filter by tenant and
     * retain shared candidates when filtering by application.
     */
    public List<Item> query(ItemQuery query) {
        if (query == null) {
            return List.of();
        }
        ItemNormalizer.requireTenant(query.getTenant_id());
        if (query.getScope() != null) {
            Item.Scope.valueOf(query.getScope());
        }
        String kind = query.getKind() == null ? Item.Kind.SOURCE.name() : query.getKind();
        Item.Kind.valueOf(kind);
        query.setKind(kind);
        List<Item> loaded = store.query(query);
        if (loaded == null) {
            return List.of();
        }
        List<Item> result = new ArrayList<>();
        for (Item item : loaded) {
            if (item == null || !query.getTenant_id().equals(item.getTenant_id()) || !kind.equals(item.getKind())
                    || query.getScope() != null && !query.getScope().equals(item.getScope())
                    || query.getCode() != null && !query.getCode().equals(item.getCode())
                    || !query.isIncludeDeleted() && Integer.valueOf(-1).equals(item.getStatus())) {
                continue;
            }
            if (visibleToApp(item, query.getApp_id())) {
                result.add(cache(item));
            }
        }
        return result;
    }

    public List<Item> query(ItemScope scope) {
        if (scope == null) {
            return List.of();
        }
        ItemQuery query = new ItemQuery();
        query.setTenant_id(scope.getTenant_id());
        query.setScope(scope.getScope());
        query.setKind(scope.getKind());
        query.setApp_id(scope.getApp_id());
        query.setLabels(scope.getLabels());
        query.setSelectors(scope.getSelectors());
        query.setRequestId(scope.getRequestId());
        query.setIncludeDeleted(scope.isIncludeDeleted());
        query.setLimit(scope.getLimit());
        query.setOffset(scope.getOffset());
        return query(query);
    }

    public Item refresh(Item coordinates) {
        evict(coordinates);
        return find(coordinates);
    }

    public List<Item> rebuild(ItemScope scope) {
        return query(scope);
    }

    public void evict(Item coordinates) {
        cacheX.remove(entryKey(coordinates));
    }

    private Item cache(Item item) {
        if (item != null) {
            cacheX.write(entryKey(item), JsonKit.toJsonString(item), 0L);
        }
        return copy(item);
    }

    private Item copy(Item item) {
        return item == null ? null : JsonKit.toPojo(JsonKit.toJsonString(item), Item.class);
    }

    /** Checks ownership for application items and reference-based visibility for shared items. */
    public boolean visibleToApp(Item item, String app_id) {
        ItemNormalizer.coordinates(item);
        if (app_id == null || app_id.isBlank()) {
            return true;
        }
        if (Item.Scope.APPLICATION.name().equals(item.getScope())) {
            return app_id.equals(item.getApp_id());
        }
        if (item.getId() == null || item.getId().isBlank()) {
            return false;
        }
        Reference query = Reference.builder().tenant_id(item.getTenant_id()).source_id(item.getId()).target_id(app_id)
                .type(Reference.Type.ITEM_APP.name()).build();
        List<Reference> references = referenceStore.outgoing(query);
        return references != null && references.stream().anyMatch(
                reference -> reference != null && item.getTenant_id().equals(reference.getTenant_id())
                        && item.getId().equals(reference.getSource_id())
                        && Reference.Type.ITEM_APP.name().equals(reference.getType())
                        && !Integer.valueOf(-1).equals(reference.getStatus())
                        && app_id.equals(reference.getTarget_id()));
    }

    private String entryKey(Item item) {
        return keying.key(SettingSpec.entry(item));
    }
}
