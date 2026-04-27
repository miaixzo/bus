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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.guard.CortexGuard;
import org.miaixz.bus.cortex.guard.GuardContext;
import org.miaixz.bus.cortex.setting.SettingEnforcer;
import org.miaixz.bus.cortex.setting.SettingPublisher;
import org.miaixz.bus.cortex.setting.item.*;
import org.miaixz.bus.cortex.setting.item.revision.ItemRevision;
import org.miaixz.bus.cortex.magic.identity.CortexIdentity;
import org.miaixz.bus.cortex.setting.item.revision.ItemRevisionStore;

/**
 * Application service for the setting domain.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ItemCuratorService {

    /**
     * Current-state store for setting entries.
     */
    private final StoreBackedItemStore entryStore;
    /**
     * ItemRevision-history store used for rollback and audits.
     */
    private final ItemRevisionStore revisionStore;
    /**
     * Effective-value resolver for external and internal reads.
     */
    private final ItemValueResolver resolver;
    /**
     * Publisher that coordinates current-state writes and history snapshots.
     */
    private final SettingPublisher publisher;
    /**
     * Optional centralized setting enforcer.
     */
    private final SettingEnforcer enforcer;
    /**
     * Optional shared Cortex guard.
     */
    private final CortexGuard cortexGuard;

    /**
     * Creates an ItemCuratorService.
     *
     * @param entryStore    current-state store
     * @param revisionStore revision-history store
     * @param resolver      effective-value resolver
     * @param publisher     publisher responsible for revision-tracked updates
     */
    public ItemCuratorService(StoreBackedItemStore entryStore, ItemRevisionStore revisionStore,
            ItemValueResolver resolver, SettingPublisher publisher) {
        this(entryStore, revisionStore, resolver, publisher, null);
    }

    /**
     * Creates an ItemCuratorService with an optional setting enforcer.
     *
     * @param entryStore    current-state store
     * @param revisionStore revision-history store
     * @param resolver      effective-value resolver
     * @param publisher     publisher responsible for revision-tracked updates
     * @param enforcer      optional centralized setting enforcer
     */
    public ItemCuratorService(StoreBackedItemStore entryStore, ItemRevisionStore revisionStore,
            ItemValueResolver resolver, SettingPublisher publisher, SettingEnforcer enforcer) {
        this(entryStore, revisionStore, resolver, publisher, enforcer, null);
    }

    /**
     * Creates an ItemCuratorService with optional setting and shared guards.
     *
     * @param entryStore    current-state store
     * @param revisionStore revision-history store
     * @param resolver      resolver
     * @param publisher     publisher
     * @param enforcer      setting enforcer
     * @param cortexGuard   shared guard
     */
    public ItemCuratorService(StoreBackedItemStore entryStore, ItemRevisionStore revisionStore,
            ItemValueResolver resolver, SettingPublisher publisher, SettingEnforcer enforcer, CortexGuard cortexGuard) {
        this.entryStore = entryStore;
        this.revisionStore = revisionStore;
        this.resolver = resolver;
        this.publisher = publisher;
        this.enforcer = enforcer;
        this.cortexGuard = cortexGuard;
    }

    /**
     * Publishes a setting entry through the revision-tracked publisher.
     *
     * @param entry setting entry
     * @return stored entry
     */
    public Item publish(Item entry) {
        return publisher.publish(validateItem(entry));
    }

    /**
     * Publishes a plain inline setting item.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param content   inline content
     * @return stored entry
     */
    public Item publishInline(String namespace, String group, String data_id, String content) {
        Item entry = new Item();
        entry.setNamespace_id(CortexIdentity.namespace(namespace));
        entry.setGroup(group);
        entry.setData_id(data_id);
        entry.setContent(content);
        return publish(entry);
    }

    /**
     * Deletes one setting entry from the current state store.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return deleted entry or {@code null}
     */
    public Item delete(String namespace, String group, String data_id, String profile) {
        requireAllowed("delete", namespace, null, profile, ItemKeys.profileScope(namespace, group, data_id, profile));
        return publisher.delete(namespace, group, data_id, profile);
    }

    /**
     * Rolls back the current setting entry to a previous revision.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @param revision  historical revision
     * @return newly published current entry after rollback, or {@code null}
     */
    public Item rollback(String namespace, String group, String data_id, String profile, String revision) {
        requireAllowed("rollback", namespace, null, profile, ItemKeys.profileScope(namespace, group, data_id, profile));
        return publisher.rollback(namespace, group, data_id, profile, revision);
    }

    /**
     * Finds the current setting entry.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return current entry
     */
    public Item find(String namespace, String group, String data_id, String profile) {
        return entryStore.find(namespace, group, data_id, profile);
    }

    /**
     * Queries current setting entries.
     *
     * @param query query filter
     * @return matching entries
     */
    public List<Item> query(ItemQuery query) {
        query = prepare(query);
        if (!allows(query)) {
            return List.of();
        }
        return filterByApp(entryStore.query(query), query == null ? null : query.getApp_id());
    }

    /**
     * Resolves one effective setting value.
     *
     * @param query query filter
     * @return resolved value
     */
    public String resolve(ItemQuery query) {
        return resolve(query, null);
    }

    /**
     * Resolves one effective setting value with an optional exposure guard.
     *
     * @param query            query filter
     * @param requiredExposure required exposure policy, or {@code null} to disable filtering
     * @return resolved value
     */
    public String resolve(ItemQuery query, ItemExposure requiredExposure) {
        query = prepare(query);
        if (query == null) {
            return null;
        }
        if (!allows(query)) {
            return query.getFallbackValue();
        }
        Item entry = entryStore
                .find(query.getNamespace_id(), query.getGroup(), query.getData_id(), query.getProfile_id());
        if (entry != null && !ItemBindingProjection.bindsToApp(entry, query.getApp_id())) {
            return query.getFallbackValue();
        }
        if (requiredExposure != null && (entry == null || !requiredExposure.name().equals(entry.getExposure()))) {
            return null;
        }
        String resolved = resolver.resolve(entry, query.getRequestContext());
        if (resolved == null && query.getFallbackValue() != null) {
            return query.getFallbackValue();
        }
        return resolved;
    }

    /**
     * Exports resolved setting values for a scope.
     *
     * @param scope export scope
     * @return resolved values keyed by logical setting identifier
     */
    public Map<String, String> export(ItemScope scope) {
        return export(scope, null);
    }

    /**
     * Exports resolved setting values for a scope with an optional exposure filter.
     *
     * @param scope            export scope
     * @param requiredExposure required exposure policy, or {@code null} to export all entries
     * @return resolved values keyed by logical setting identifier
     */
    public Map<String, String> export(ItemScope scope, ItemExposure requiredExposure) {
        scope = prepare(scope);
        if (!allows(
                scope == null ? null : scope.getNamespace_id(),
                scope == null ? null : scope.getApp_id(),
                scope == null ? null : scope.getProfile_id())) {
            return Map.of();
        }
        ItemQuery query = new ItemQuery();
        if (scope != null) {
            query.setNamespace_id(scope.getNamespace_id());
            query.setGroup(scope.getGroup());
            query.setProfile_id(scope.getProfile_id());
            query.setApp_id(scope.getApp_id());
            query.setLabels(scope.getLabels());
            query.setSelectors(scope.getSelectors());
            query.setLimit(scope.getLimit());
            query.setOffset(scope.getOffset());
        }
        List<Item> entries = query(query);
        Map<String, String> result = new LinkedHashMap<>();
        GrayRequestContext requestContext = new GrayRequestContext();
        for (Item entry : entries) {
            if (entry == null || requiredExposure != null && !requiredExposure.name().equals(entry.getExposure())) {
                continue;
            }
            result.put(
                    exportKey(entry, scope == null ? null : scope.getProfile_id()),
                    resolver.resolve(entry, requestContext));
        }
        return result;
    }

    /**
     * Applies the optional relation guard to one query.
     *
     * @param query query to validate
     * @return validated query
     */
    public ItemQuery prepare(ItemQuery query) {
        return enforcer == null ? query : enforcer.validateQuery(query);
    }

    /**
     * Applies the optional relation guard to one scope.
     *
     * @param scope scope to validate
     * @return validated scope
     */
    public ItemScope prepare(ItemScope scope) {
        return enforcer == null ? scope : enforcer.validateScope(scope);
    }

    /**
     * Applies the optional write guard to one item.
     *
     * @param entry entry to validate
     * @return validated entry
     */
    public Item validateItem(Item entry) {
        Item validated = enforcer == null ? entry : enforcer.validateItem(entry);
        if (validated != null) {
            requireAllowed(
                    "publish",
                    validated.getNamespace_id(),
                    first(ItemBindingProjection.normalizedAppIds(validated)),
                    first(ItemBindingProjection.normalizedProfileIds(validated)),
                    ItemKeys.profileScope(
                            validated.getNamespace_id(),
                            validated.getGroup(),
                            validated.getData_id(),
                            first(ItemBindingProjection.normalizedProfileIds(validated))));
        }
        return validated;
    }

    /**
     * Returns whether the supplied query is allowed by the centralized relation guard.
     *
     * @param query query
     * @return {@code true} when the relation is allowed
     */
    public boolean allows(ItemQuery query) {
        return allows(
                query == null ? null : query.getNamespace_id(),
                query == null ? null : query.getApp_id(),
                query == null ? null : query.getProfile_id());
    }

    /**
     * Returns whether the supplied app/profile relation is allowed.
     *
     * @param namespace_id namespace identifier
     * @param app_id       application identifier
     * @param profile_id   profile identifier
     * @return {@code true} when the relation is allowed
     */
    public boolean allows(String namespace_id, String app_id, String profile_id) {
        return enforcer == null || enforcer.allows(namespace_id, app_id, profile_id);
    }

    /**
     * Enforces setting scope access for one curator action.
     *
     * @param action       guarded action
     * @param namespace_id namespace identifier
     * @param app_id       application identifier
     * @param profile_id   profile identifier
     * @param resourceId   guarded resource identifier
     */
    private void requireAllowed(
            String action,
            String namespace_id,
            String app_id,
            String profile_id,
            String resourceId) {
        if (cortexGuard != null) {
            GuardContext context = new GuardContext();
            context.setDomain("setting");
            context.setAction(action);
            context.setResourceType("ITEM");
            context.setResourceId(resourceId);
            context.namespace_id(namespace_id);
            context.setApp_id(app_id);
            context.setProfile_id(profile_id);
            cortexGuard.enforce(context);
            return;
        }
        if (!allows(namespace_id, app_id, profile_id)) {
            throw new IllegalArgumentException("Setting scope is not allowed");
        }
    }

    /**
     * Returns the first value from a list.
     *
     * @param values values to inspect
     * @return first value or {@code null}
     */
    private String first(List<String> values) {
        return values == null || values.isEmpty() ? null : values.getFirst();
    }

    /**
     * Loads a historical revision.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @param revision  historical revision
     * @return matching revision or {@code null}
     */
    public ItemRevision revision(String namespace, String group, String data_id, String profile, String revision) {
        return revisionStore.find(namespace, group, data_id, profile, revision);
    }

    /**
     * Lists {@code setting.item.revision} snapshots for one entry.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return revisions from newest to oldest
     */
    public List<ItemRevision> revisions(String namespace, String group, String data_id, String profile) {
        return revisionStore.query(namespace, group, data_id, profile);
    }

    /**
     * Refreshes one current entry from durable state.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return refreshed entry
     */
    public Item refresh(String namespace, String group, String data_id, String profile) {
        return entryStore.refresh(namespace, group, data_id, profile);
    }

    /**
     * Rebuilds setting cache for a scope.
     *
     * @param scope rebuild scope
     * @return rebuilt entries
     */
    public List<Item> rebuild(ItemScope scope) {
        return entryStore.rebuild(scope);
    }

    /**
     * Resolves one preview value using the adapter preview path.
     *
     * @param query query filter
     * @return preview value
     */
    public String preview(ItemQuery query) {
        if (query == null) {
            return null;
        }
        Item entry = find(query.getNamespace_id(), query.getGroup(), query.getData_id(), query.getProfile_id());
        return resolver.preview(entry);
    }

    /**
     * Evicts one current entry from cache.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     */
    public void evict(String namespace, String group, String data_id, String profile) {
        entryStore.evict(namespace, group, data_id, profile);
    }

    /**
     * Builds the logical export key for one resolved setting entry.
     *
     * @param entry setting entry being exported
     * @return export key including profile when present
     */
    private String exportKey(Item entry, String profile) {
        List<String> profiles = ItemBindingProjection.normalizedProfileIds(entry);
        String resolvedProfile = StringKit.isNotEmpty(profile) ? profile
                : profiles == null || profiles.isEmpty() ? null : profiles.getFirst();
        return ItemKeys
                .exportKeyForScope(entry.getNamespace_id(), entry.getGroup(), entry.getData_id(), resolvedProfile);
    }

    /**
     * Returns the logical watch key used for setting notifications.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return logical watch key
     */
    public String watchKey(String namespace, String group, String data_id, String profile) {
        return ItemKeys.watchKeyForScope(namespace, group, data_id, profile);
    }

    /**
     * Filters queried settings by application binding.
     *
     * @param entries queried settings
     * @param app_id  application identifier
     * @return settings visible to the application
     */
    private List<Item> filterByApp(List<Item> entries, String app_id) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        if (app_id == null || app_id.isBlank()) {
            return entries;
        }
        java.util.ArrayList<Item> result = new java.util.ArrayList<>(entries.size());
        for (Item entry : entries) {
            if (entry != null && ItemBindingProjection.bindsToApp(entry, app_id)) {
                result.add(entry);
            }
        }
        return result;
    }

}
