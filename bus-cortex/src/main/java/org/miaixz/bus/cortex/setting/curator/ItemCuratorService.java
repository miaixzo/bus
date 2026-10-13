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
import org.miaixz.bus.cortex.setting.SettingEnforcer;
import org.miaixz.bus.cortex.setting.SettingPublisher;
import org.miaixz.bus.cortex.setting.item.*;
import org.miaixz.bus.cortex.setting.reference.ReferenceStore;
import org.miaixz.bus.cortex.setting.revision.Revision;
import org.miaixz.bus.cortex.setting.revision.RevisionStore;
import org.miaixz.bus.cortex.setting.rollout.Plan;
import org.miaixz.bus.cortex.setting.rollout.Rollout;
import org.miaixz.bus.cortex.setting.rollout.Rollout.Mode;
import org.miaixz.bus.cortex.setting.rollout.Rollout.State;
import org.miaixz.bus.cortex.setting.rollout.RolloutStore;
import org.miaixz.bus.extra.json.JsonKit;

/** Provides tenant-scoped configuration reads, publication, and revision history. */
public class ItemCuratorService {

    private final StoreBackedItemStore entryStore;
    private final RevisionStore revisionStore;
    private final ItemValueResolver resolver;
    private final SettingPublisher publisher;
    private final SettingEnforcer enforcer;
    private final CortexGuard cortexGuard;
    private final Keying<SettingSpec> keying;
    private final RolloutStore rolloutStore;

    @Deprecated(since = "8.8.0", forRemoval = true)
    public ItemCuratorService(StoreBackedItemStore entryStore, RevisionStore revisionStore,
            ReferenceStore referenceStore, ItemValueResolver resolver, SettingPublisher publisher) {
        this(entryStore, revisionStore, referenceStore, resolver, publisher, null, null, SettingGenerator.INSTANCE);
    }

    @Deprecated(since = "8.8.0", forRemoval = true)
    public ItemCuratorService(StoreBackedItemStore entryStore, RevisionStore revisionStore,
            ReferenceStore referenceStore, ItemValueResolver resolver, SettingPublisher publisher,
            SettingEnforcer enforcer, CortexGuard cortexGuard, Keying<SettingSpec> keying) {
        this(entryStore, revisionStore, resolver, publisher, null, enforcer, cortexGuard, keying);
    }

    public ItemCuratorService(StoreBackedItemStore entryStore, RevisionStore revisionStore, ItemValueResolver resolver,
            SettingPublisher publisher) {
        this(entryStore, revisionStore, resolver, publisher, null, null, null, SettingGenerator.INSTANCE);
    }

    public ItemCuratorService(StoreBackedItemStore entryStore, RevisionStore revisionStore, ItemValueResolver resolver,
            SettingPublisher publisher, RolloutStore rolloutStore, SettingEnforcer enforcer, CortexGuard cortexGuard,
            Keying<SettingSpec> keying) {
        if (entryStore == null || revisionStore == null || resolver == null || publisher == null) {
            throw new IllegalArgumentException("Item curator dependencies are required");
        }
        this.rolloutStore = rolloutStore;
        this.entryStore = entryStore;
        this.revisionStore = revisionStore;
        this.resolver = resolver;
        this.publisher = publisher;
        this.enforcer = enforcer;
        this.cortexGuard = cortexGuard;
        this.keying = keying == null ? SettingGenerator.INSTANCE : keying;
    }

    public Item publish(Item entry) {
        return publisher.publish(validateItem(entry));
    }

    public Item publishInline(Item entry, String content) {
        return publisher.publishInline(validateItem(entry), content);
    }

    public Item delete(Item coordinates) {
        requireAllowed("delete", coordinates);
        return publisher.delete(coordinates);
    }

    public Item rollback(Item coordinates, String revision) {
        requireAllowed("rollback", coordinates);
        return publisher.rollback(coordinates, revision);
    }

    /** Applies read authorization before looking up exact business coordinates. */
    public Item find(Item coordinates) {
        requireAllowed("read", coordinates);
        return entryStore.find(coordinates);
    }

    /** Resolves exact coordinates and enforces application-reference visibility before returning an item. */
    public Item find(ItemQuery query) {
        ItemQuery prepared = prepare(query);
        if (prepared == null || !allows(prepared)) {
            return null;
        }
        requireAllowed(
                "read",
                prepared.getTenant_id(),
                prepared.getApp_id(),
                prepared.getScope(),
                keying.key(SettingSpec.scope(prepared.coordinates())));
        Item item = entryStore.find(prepared.coordinates());
        return item != null && entryStore.visibleToApp(item, prepared.getApp_id()) ? item : null;
    }

    public List<Item> query(ItemQuery query) {
        ItemQuery prepared = prepare(query);
        if (prepared == null || !allows(prepared)) {
            return List.of();
        }
        requireAllowed("read", prepared.getTenant_id(), prepared.getApp_id(), prepared.getScope(), null);
        return entryStore.query(prepared);
    }

    public String resolve(ItemQuery query) {
        return resolve(query, null);
    }

    public String resolve(ItemQuery query, ItemExposure exposure) {
        ItemQuery prepared = prepare(query);
        if (prepared == null || !allows(prepared)) {
            return prepared == null ? null : prepared.getFallbackValue();
        }
        Item item = find(prepared);
        if (item == null) {
            return prepared.getFallbackValue();
        }
        if (Item.Scope.SHARED.name().equals(item.getScope()) && prepared.getApp_id() != null) {
            return prepared.getFallbackValue();
        }
        item = published(item, prepared.getRequestContext());
        if (item == null) {
            return prepared.getFallbackValue();
        }
        if (exposure != null && !exposure.name().equals(item.getExposure())) {
            return null;
        }
        String value = resolver.resolve(item);
        return value == null ? prepared.getFallbackValue() : value;
    }

    public Map<String, String> export(ItemScope scope) {
        return export(scope, null);
    }

    public Map<String, String> export(ItemScope scope, ItemExposure exposure) {
        ItemScope prepared = prepare(scope);
        if (prepared == null || !allows(prepared.getTenant_id(), prepared.getApp_id(), prepared.getScope())) {
            return Map.of();
        }
        requireAllowed("read", prepared.getTenant_id(), prepared.getApp_id(), prepared.getScope(), null);
        Map<String, String> result = new LinkedHashMap<>();
        for (Item item : entryStore.query(prepared)) {
            if (Item.Scope.SHARED.name().equals(item.getScope()) && prepared.getApp_id() != null) {
                continue;
            }
            Item active = published(item, null);
            if (active != null && (exposure == null || exposure.name().equals(active.getExposure()))) {
                result.put(keying.key(SettingSpec.export(item)), resolver.resolve(active));
            }
        }
        return result;
    }

    public ItemQuery prepare(ItemQuery query) {
        ItemQuery prepared = enforcer == null ? query : enforcer.validateQuery(query);
        validateScope(prepared);
        return prepared;
    }

    public ItemScope prepare(ItemScope scope) {
        ItemScope prepared = enforcer == null ? scope : enforcer.validateScope(scope);
        validateScope(prepared);
        return prepared;
    }

    private void validateScope(ItemScope scope) {
        if (scope != null) {
            ItemNormalizer.requireTenant(scope.getTenant_id());
            if (scope.getScope() != null) {
                Item.Scope.valueOf(scope.getScope());
            }
        }
    }

    public Item validateItem(Item item) {
        Item validated = enforcer == null ? item : enforcer.validateItem(item);
        ItemNormalizer.coordinates(validated);
        requireAllowed("publish", validated);
        return validated;
    }

    public boolean allows(ItemQuery query) {
        return query != null && allows(query.getTenant_id(), query.getApp_id(), query.getScope());
    }

    public boolean allows(String tenant_id, String app_id, String scope) {
        ItemNormalizer.requireTenant(tenant_id);
        return enforcer == null || enforcer.allows(tenant_id, app_id, scope);
    }

    public Revision revision(Item coordinates, String revision) {
        Item item = find(coordinates);
        return item == null ? null : revisionStore.find(item.getTenant_id(), item.getId(), revision);
    }

    public List<Revision> revisions(Item coordinates) {
        Item item = find(coordinates);
        return item == null ? List.of() : revisionStore.query(item.getTenant_id(), item.getId());
    }

    public Item refresh(Item coordinates) {
        requireAllowed("read", coordinates);
        return entryStore.refresh(coordinates);
    }

    public List<Item> rebuild(ItemScope scope) {
        ItemScope prepared = prepare(scope);
        if (prepared == null || !allows(prepared.getTenant_id(), prepared.getApp_id(), prepared.getScope())) {
            return List.of();
        }
        requireAllowed("read", prepared.getTenant_id(), prepared.getApp_id(), prepared.getScope(), null);
        return entryStore.rebuild(prepared);
    }

    public String preview(ItemQuery query) {
        ItemQuery prepared = prepare(query);
        if (prepared == null || !allows(prepared)) {
            return null;
        }
        Item item = find(prepared);
        return item == null ? null : resolver.preview(item);
    }

    public void evict(Item coordinates) {
        requireAllowed("evict", coordinates);
        entryStore.evict(coordinates);
    }

    public String watchKey(Item coordinates) {
        return keying.key(SettingSpec.watch(coordinates));
    }

    /** Resolves only immutable active snapshots; draft content never reaches runtime reads. */
    private Item published(Item item, GrayRequestContext context) {
        String id = item.getStable_id();
        if (item.getGray_id() != null && context != null) {
            if (context.getTenant() != null && !item.getTenant_id().equals(context.getTenant())) {
                throw new IllegalArgumentException("Gray context belongs to another tenant");
            }
            if (rolloutStore == null || item.getRollout_id() == null) {
                throw new IllegalStateException("Gray delivery requires a tenant-scoped rollout store");
            }
            Rollout rollout = rolloutStore
                    .find(Rollout.builder().tenant_id(item.getTenant_id()).id(item.getRollout_id()).build());
            if (rollout == null || !item.getTenant_id().equals(rollout.getTenant_id())
                    || !State.PUBLISHED.name().equals(rollout.getState()) || !Mode.GRAY.name().equals(rollout.getMode())
                    || rollout.getRule() == null) {
                throw new IllegalStateException("No active frozen gray publication exists");
            }
            Plan frozen = Plan.restore(rollout);
            if (!frozen.includes(item.getApp_id(), item.getId(), item.getGray_id())) {
                throw new IllegalStateException("Gray revision is outside the frozen application scope");
            }
            GrayRequestContext trusted = JsonKit.toPojo(JsonKit.toJsonString(context), GrayRequestContext.class);
            trusted.setTenant(item.getTenant_id());
            GrayRule rule = JsonKit.toPojo(rollout.getRule(), GrayRule.class);
            if (new GrayRuleMatcher().matches(rule, trusted)) {
                id = item.getGray_id();
            }
        }
        if (id == null) {
            return null;
        }
        Revision snapshot = revisionStore.identity(item.getTenant_id(), item.getId(), id);
        if (snapshot == null) {
            throw new IllegalStateException("Active configuration revision is missing");
        }
        if (Revision.Operation.DELETE.name().equals(snapshot.getOperation())) {
            return null;
        }
        Item active = Item.builder().id(item.getId()).tenant_id(item.getTenant_id()).scope(item.getScope())
                .app_id(item.getApp_id()).kind(item.getKind()).code(item.getCode()).content(snapshot.getContent())
                .format(snapshot.getFormat()).editor(snapshot.getEditor()).payload(snapshot.getPayload())
                .source(snapshot.getSource()).spec(snapshot.getSpec()).exposure(snapshot.getExposure())
                .encrypted(snapshot.getEncrypted()).labels(snapshot.getLabels()).extension(snapshot.getExtension())
                .checksum(snapshot.getChecksum()).build();
        return active;
    }

    private void requireAllowed(String action, Item coordinates) {
        ItemNormalizer.coordinates(coordinates);
        requireAllowed(
                action,
                coordinates.getTenant_id(),
                coordinates.getApp_id(),
                coordinates.getScope(),
                keying.key(SettingSpec.scope(coordinates)));
    }

    private void requireAllowed(String action, String tenant_id, String app_id, String scope, String resourceId) {
        if (!allows(tenant_id, app_id, scope)) {
            throw new IllegalArgumentException("Setting scope is not allowed");
        }
        if (cortexGuard != null) {
            GuardContext context = new GuardContext();
            context.setDomain("setting");
            context.setAction(action);
            context.setResourceType("ITEM");
            context.setResourceId(resourceId);
            context.tenant_id(tenant_id);
            context.setApp_id(app_id);
            context.putAttribute("scope", scope);
            cortexGuard.enforce(context);
        }
    }
}
