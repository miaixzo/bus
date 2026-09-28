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

import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.Keying.SettingSpec;
import org.miaixz.bus.cortex.builtin.SettingGenerator;
import org.miaixz.bus.cortex.guard.CortexGuard;
import org.miaixz.bus.cortex.guard.GuardContext;
import org.miaixz.bus.cortex.magic.identity.CortexIdentity;
import org.miaixz.bus.cortex.setting.SettingEnforcer;
import org.miaixz.bus.cortex.setting.SettingPublisher;
import org.miaixz.bus.cortex.setting.item.*;
import org.miaixz.bus.cortex.setting.reference.Reference;
import org.miaixz.bus.cortex.setting.reference.ReferenceStore;
import org.miaixz.bus.cortex.setting.revision.Revision;
import org.miaixz.bus.cortex.setting.revision.RevisionStore;

/**
 * Application service for setting reads, publication and revision history.
 *
 * @author Kimi Liu
 */
public class ItemCuratorService {

    /**
     * Current item store.
     */
    private final StoreBackedItemStore entryStore;
    /**
     * Immutable revision store.
     */
    private final RevisionStore revisionStore;
    /**
     * Resource relationship store.
     */
    private final ReferenceStore referenceStore;
    /**
     * Item content resolver.
     */
    private final ItemValueResolver resolver;
    /**
     * Durable item publisher.
     */
    private final SettingPublisher publisher;
    /**
     * Optional scope and input enforcer.
     */
    private final SettingEnforcer enforcer;
    /**
     * Optional authorization guard.
     */
    private final CortexGuard cortexGuard;
    /**
     * Setting-domain key strategy.
     */
    private final Keying<SettingSpec> keying;

    /**
     * Creates an item curator with required dependencies and default policy components.
     *
     * @param entryStore     current item store
     * @param revisionStore  immutable revision store
     * @param referenceStore resource relationship store
     * @param resolver       content resolver
     * @param publisher      durable publisher
     */
    public ItemCuratorService(StoreBackedItemStore entryStore, RevisionStore revisionStore,
            ReferenceStore referenceStore, ItemValueResolver resolver, SettingPublisher publisher) {
        this(entryStore, revisionStore, referenceStore, resolver, publisher, null, null, SettingGenerator.INSTANCE);
    }

    /**
     * Creates an item curator with explicit policy and key components.
     *
     * @param entryStore     current item store
     * @param revisionStore  immutable revision store
     * @param referenceStore resource relationship store
     * @param resolver       content resolver
     * @param publisher      durable publisher
     * @param enforcer       optional scope enforcer
     * @param cortexGuard    optional authorization guard
     * @param keying         setting-domain key strategy
     */
    public ItemCuratorService(StoreBackedItemStore entryStore, RevisionStore revisionStore,
            ReferenceStore referenceStore, ItemValueResolver resolver, SettingPublisher publisher,
            SettingEnforcer enforcer, CortexGuard cortexGuard, Keying<SettingSpec> keying) {
        if (entryStore == null || revisionStore == null || referenceStore == null || resolver == null
                || publisher == null) {
            throw new IllegalArgumentException("Item curator dependencies are required");
        }
        this.entryStore = entryStore;
        this.revisionStore = revisionStore;
        this.referenceStore = referenceStore;
        this.resolver = resolver;
        this.publisher = publisher;
        this.enforcer = enforcer;
        this.cortexGuard = cortexGuard;
        this.keying = keying == null ? SettingGenerator.INSTANCE : keying;
    }

    /**
     * Validates and publishes one item.
     *
     * @param entry item to publish
     * @return persisted item
     */
    public Item publish(Item entry) {
        return publisher.publish(validateItem(entry));
    }

    /**
     * Publishes inline content for one item coordinate.
     *
     * @param space   space identifier
     * @param group   item group
     * @param code    item code
     * @param content source content
     * @return persisted item
     */
    public Item publishInline(String space, String group, String code, String content) {
        Item entry = new Item();
        entry.setSpace_id(CortexIdentity.space(space));
        entry.setGroup(group);
        entry.setCode(code);
        entry.setContent(content);
        return publish(entry);
    }

    /**
     * Deletes one authorized item.
     *
     * @param space   space identifier
     * @param group   item group
     * @param code    item code
     * @param profile optional profile identifier
     * @return deleted item, or {@code null} when absent
     */
    public Item delete(String space, String group, String code, String profile) {
        requireAllowed("delete", space, null, profile, profileScope(space, group, code, profile));
        return publisher.delete(space, group, code, profile);
    }

    /**
     * Rolls an item back to one historical revision.
     *
     * @param space    space identifier
     * @param group    item group
     * @param code     item code
     * @param profile  optional profile identifier
     * @param revision item-scoped revision number
     * @return republished item, or {@code null} when absent
     */
    public Item rollback(String space, String group, String code, String profile, String revision) {
        requireAllowed("rollback", space, null, profile, profileScope(space, group, code, profile));
        return publisher.rollback(space, group, code, profile, revision);
    }

    /**
     * Finds one current item by business coordinates.
     *
     * @param space   space identifier
     * @param group   item group
     * @param code    item code
     * @param profile optional profile identifier
     * @return matching item, or {@code null} when absent
     */
    public Item find(String space, String group, String code, String profile) {
        return entryStore.find(null, CortexIdentity.space(space), profile, group, code);
    }

    /**
     * Queries items after scope validation.
     *
     * @param query item query
     * @return matching authorized items
     */
    public List<Item> query(ItemQuery query) {
        ItemQuery prepared = prepare(query);
        if (prepared == null || !allows(prepared)) {
            return List.of();
        }
        return entryStore.query(prepared);
    }

    /**
     * Resolves item content without an exposure filter.
     *
     * @param query item query
     * @return resolved content or the configured fallback
     */
    public String resolve(ItemQuery query) {
        return resolve(query, null);
    }

    /**
     * Resolves item content with an optional exposure filter.
     *
     * @param query    item query
     * @param exposure required exposure, when present
     * @return resolved content or the configured fallback
     */
    public String resolve(ItemQuery query, ItemExposure exposure) {
        ItemQuery prepared = prepare(query);
        if (prepared == null || !allows(prepared)) {
            return prepared == null ? null : prepared.getFallbackValue();
        }
        Item item = find(prepared.getSpace_id(), prepared.getGroup(), prepared.getCode(), prepared.getProfile_id());
        if (item == null || !visibleToApp(item, prepared.getApp_id())) {
            return prepared.getFallbackValue();
        }
        if (exposure != null && !exposure.name().equals(item.getExposure())) {
            return null;
        }
        String value = resolver.resolve(item, prepared.getRequestContext());
        return value == null ? prepared.getFallbackValue() : value;
    }

    /**
     * Exports all visible item values in one scope.
     *
     * @param scope item scope
     * @return values keyed by canonical export keys
     */
    public Map<String, String> export(ItemScope scope) {
        return export(scope, null);
    }

    /**
     * Exports visible values matching an optional exposure filter.
     *
     * @param scope    item scope
     * @param exposure required exposure, when present
     * @return values keyed by canonical export keys
     */
    public Map<String, String> export(ItemScope scope, ItemExposure exposure) {
        ItemScope prepared = prepare(scope);
        if (prepared == null || !allows(prepared.getSpace_id(), prepared.getApp_id(), prepared.getProfile_id())) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (Item item : entryStore.query(prepared)) {
            if (exposure == null || exposure.name().equals(item.getExposure())) {
                result.put(
                        keying.key(
                                SettingSpec.export(
                                        item.getSpace_id(),
                                        item.getGroup(),
                                        item.getCode(),
                                        item.getProfile_id())),
                        resolver.resolve(item, null));
            }
        }
        return result;
    }

    /**
     * Applies query validation when an enforcer is configured.
     *
     * @param query item query
     * @return validated query
     */
    public ItemQuery prepare(ItemQuery query) {
        return enforcer == null ? query : enforcer.validateQuery(query);
    }

    /**
     * Applies scope validation when an enforcer is configured.
     *
     * @param scope item scope
     * @return validated scope
     */
    public ItemScope prepare(ItemScope scope) {
        return enforcer == null ? scope : enforcer.validateScope(scope);
    }

    /**
     * Validates item input and enforces publish authorization.
     *
     * @param item item to validate
     * @return validated item
     */
    public Item validateItem(Item item) {
        Item validated = enforcer == null ? item : enforcer.validateItem(item);
        if (validated != null) {
            requireAllowed(
                    "publish",
                    validated.getSpace_id(),
                    null,
                    validated.getProfile_id(),
                    profileScope(
                            validated.getSpace_id(),
                            validated.getGroup(),
                            validated.getCode(),
                            validated.getProfile_id()));
        }
        return validated;
    }

    /**
     * Tests whether one query scope is allowed.
     *
     * @param query item query
     * @return {@code true} when the query is allowed
     */
    public boolean allows(ItemQuery query) {
        return query != null && allows(query.getSpace_id(), query.getApp_id(), query.getProfile_id());
    }

    /**
     * Tests whether one explicit scope is allowed.
     *
     * @param space_id   space identifier
     * @param app_id     application identifier, when present
     * @param profile_id profile identifier, when present
     * @return {@code true} when the scope is allowed
     */
    public boolean allows(String space_id, String app_id, String profile_id) {
        return enforcer == null || enforcer.allows(space_id, app_id, profile_id);
    }

    /**
     * Finds one immutable item revision.
     *
     * @param space    space identifier
     * @param group    item group
     * @param code     item code
     * @param profile  optional profile identifier
     * @param revision item-scoped revision number
     * @return matching revision, or {@code null} when absent
     */
    public Revision revision(String space, String group, String code, String profile, String revision) {
        Item item = find(space, group, code, profile);
        return item == null ? null : revisionStore.find(item.getTenant_id(), item.getId(), revision);
    }

    /**
     * Lists immutable revisions for one item.
     *
     * @param space   space identifier
     * @param group   item group
     * @param code    item code
     * @param profile optional profile identifier
     * @return item revisions
     */
    public List<Revision> revisions(String space, String group, String code, String profile) {
        Item item = find(space, group, code, profile);
        return item == null ? List.of() : revisionStore.query(item.getTenant_id(), item.getId());
    }

    /**
     * Evicts and reloads one current item.
     *
     * @param space   space identifier
     * @param group   item group
     * @param code    item code
     * @param profile optional profile identifier
     * @return reloaded item, or {@code null} when absent
     */
    public Item refresh(String space, String group, String code, String profile) {
        return entryStore.refresh(null, CortexIdentity.space(space), profile, group, code);
    }

    /**
     * Rebuilds cache entries for one item scope.
     *
     * @param scope item scope
     * @return cached items
     */
    public List<Item> rebuild(ItemScope scope) {
        return entryStore.rebuild(scope);
    }

    /**
     * Resolves one source value without applying runtime delivery routing.
     *
     * @param query item query
     * @return preview content, or {@code null} when absent
     */
    public String preview(ItemQuery query) {
        if (query == null) {
            return null;
        }
        return resolver.preview(find(query.getSpace_id(), query.getGroup(), query.getCode(), query.getProfile_id()));
    }

    /**
     * Evicts one current item from the coordinate cache.
     *
     * @param space   space identifier
     * @param group   item group
     * @param code    item code
     * @param profile optional profile identifier
     */
    public void evict(String space, String group, String code, String profile) {
        entryStore.evict(null, CortexIdentity.space(space), group, code, profile);
    }

    /**
     * Builds the canonical watch key for one item coordinate.
     *
     * @param space   space identifier
     * @param group   item group
     * @param code    item code
     * @param profile optional profile identifier
     * @return watch key
     */
    public String watchKey(String space, String group, String code, String profile) {
        return keying.key(SettingSpec.watch(space, group, code, profile));
    }

    /**
     * Tests application visibility through persisted item relationships.
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
        return references != null && references.stream().anyMatch(reference -> app_id.equals(reference.getTarget_id()));
    }

    /**
     * Enforces an action against one item scope.
     *
     * @param action     action name
     * @param space_id   space identifier
     * @param app_id     application identifier, when present
     * @param profile_id profile identifier, when present
     * @param resourceId canonical resource identifier
     */
    private void requireAllowed(String action, String space_id, String app_id, String profile_id, String resourceId) {
        if (cortexGuard != null) {
            GuardContext context = new GuardContext();
            context.setDomain("setting");
            context.setAction(action);
            context.setResourceType("ITEM");
            context.setResourceId(resourceId);
            context.space_id(space_id);
            context.setApp_id(app_id);
            context.setProfile_id(profile_id);
            cortexGuard.enforce(context);
        } else if (!allows(space_id, app_id, profile_id)) {
            throw new IllegalArgumentException("Setting scope is not allowed");
        }
    }

    /**
     * Builds the canonical profile-scope resource identifier.
     *
     * @param space   space identifier
     * @param group   item group
     * @param code    item code
     * @param profile optional profile identifier
     * @return profile-scope key
     */
    private String profileScope(String space, String group, String code, String profile) {
        return keying.key(SettingSpec.profileScope(space, group, code, profile));
    }
}
