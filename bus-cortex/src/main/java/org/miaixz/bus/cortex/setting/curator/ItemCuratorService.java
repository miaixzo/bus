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

import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.lang.exception.ForbiddenException;
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.Keying.SettingSpec;
import org.miaixz.bus.cortex.builtin.SettingGenerator;
import org.miaixz.bus.cortex.guard.CortexGuard;
import org.miaixz.bus.cortex.guard.GuardContext;
import org.miaixz.bus.cortex.setting.SettingEnforcer;
import org.miaixz.bus.cortex.setting.SettingPublisher;
import org.miaixz.bus.cortex.setting.item.*;
import org.miaixz.bus.cortex.setting.revision.Revision;
import org.miaixz.bus.cortex.setting.revision.RevisionStore;
import org.miaixz.bus.cortex.setting.rollout.Plan;
import org.miaixz.bus.cortex.setting.rollout.Rollout;
import org.miaixz.bus.cortex.setting.rollout.Rollout.Mode;
import org.miaixz.bus.cortex.setting.rollout.Rollout.State;
import org.miaixz.bus.cortex.setting.rollout.RolloutStore;
import org.miaixz.bus.cortex.setting.secret.SecretContext;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Provides tenant-scoped configuration reads, publication, and revision history.
 *
 * @author Kimi Liu
 */
public class ItemCuratorService {

    /**
     * Store for current item state and cache coordination.
     */
    private final StoreBackedItemStore entryStore;
    /**
     * Store for immutable item revisions.
     */
    private final RevisionStore revisionStore;
    /**
     * Resolver for materializing published item content.
     */
    private final ItemValueResolver resolver;
    /**
     * Publisher that stages item revisions.
     */
    private final SettingPublisher publisher;
    /**
     * Optional business-rule enforcer.
     */
    private final SettingEnforcer enforcer;
    /**
     * Optional authorization guard.
     */
    private final CortexGuard cortexGuard;
    /**
     * Strategy for deriving setting-domain keys.
     */
    private final Keying<SettingSpec> keying;
    /**
     * Optional store used to validate frozen gray rollouts.
     */
    private final RolloutStore rolloutStore;

    /**
     * Creates an item curator with default keying and no optional enforcement services.
     *
     * @param entryStore    current-state item store
     * @param revisionStore immutable revision store
     * @param resolver      published-value resolver
     * @param publisher     revision publisher
     */
    public ItemCuratorService(StoreBackedItemStore entryStore, RevisionStore revisionStore, ItemValueResolver resolver,
            SettingPublisher publisher) {
        this(entryStore, revisionStore, resolver, publisher, null, null, null, SettingGenerator.INSTANCE);
    }

    /**
     * Creates an item curator with rollout, business, authorization, and keying integrations.
     *
     * @param entryStore current-state item store
     * @param revisionStore immutable revision store
     * @param resolver published-value resolver
     * @param publisher revision publisher
     * @param rolloutStore rollout store, or {@code null} when gray delivery is disabled
     * @param enforcer business-rule enforcer, or {@code null}
     * @param cortexGuard authorization guard, or {@code null}
     * @param keying key strategy, or {@code null} for the default strategy
     * @throws IllegalArgumentException if a required curation dependency is absent
     */
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

    /**
     * Validates and stages an item revision.
     *
     * @param entry item to stage
     * @return stored current-state item
     */
    public Item publish(Item entry) {
        return publisher.publish(validateItem(entry));
    }

    /**
     * Validates an item and stages supplied inline content.
     *
     * @param entry   item metadata
     * @param content inline content
     * @return stored current-state item
     */
    public Item publishInline(Item entry, String content) {
        return publisher.publishInline(validateItem(entry), content);
    }

    /**
     * Authorizes and stages an item deletion tombstone.
     *
     * @param coordinates item coordinates
     * @return updated current-state item, or {@code null} when absent
     */
    public Item delete(Item coordinates) {
        requireAllowed("delete", coordinates);
        return publisher.delete(coordinates);
    }

    /**
     * Authorizes and stages content copied from an immutable historical revision.
     *
     * @param coordinates item coordinates
     * @param revision    historical revision identifier
     * @return restored current-state item, or {@code null} when unavailable
     */
    public Item rollback(Item coordinates, String revision) {
        requireAllowed("rollback", coordinates);
        return publisher.rollback(coordinates, revision);
    }

    /**
     * Applies read authorization before looking up exact business coordinates.
     * @param coordinates tenant-qualified business coordinates to authorize
     * @return matching item, or null when absent
     */
    public Item find(Item coordinates) {
        requireAllowed("read", coordinates);
        return entryStore.find(coordinates);
    }

    /**
     * Resolves exact coordinates and enforces application-reference visibility before returning an item.
     * @param query tenant-qualified query to normalize and authorize
     * @return matching visible item, or null when unavailable
     */
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

    /**
     * Queries visible items after scope validation and authorization.
     *
     * @param query item query
     * @return matching items, or an empty list when the scope is unavailable
     */
    public List<Item> query(ItemQuery query) {
        ItemQuery prepared = prepare(query);
        if (prepared == null || !allows(prepared)) {
            return List.of();
        }
        requireAllowed("read", prepared.getTenant_id(), prepared.getApp_id(), prepared.getScope(), null);
        return entryStore.query(prepared);
    }

    /**
     * Resolves a published item without restricting its exposure class.
     *
     * @param query item query
     * @return resolved value or query fallback
     */
    public String resolve(ItemQuery query) {
        return resolve(query, null);
    }

    /**
     * Resolves a published item only when its configuration category matches the requested type.
     *
     * @param query item query
     * @param type expected configuration category
     * @return resolved value, query fallback, or {@code null} for an category mismatch
     */
    public String resolve(ItemQuery query, Item.Type type) {
        return resolve(query, type, null);
    }

    /**
     * Resolves a published item under configuration access and category rules, passing trusted context to SECRET
     * resolution. Missing or inaccessible query results retain existing fallback behavior.
     *
     * @param query setting query evaluated against the existing access and publication rules
     * @param type expected configuration category
     * @param context authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                reference
     * @return resolved string, configured fallback where applicable, or null when no value is available
     * @throws ForbiddenException if access is denied or the supplied credential representation is not permitted
     * @throws CryptoException if deadline, lifecycle, backend or payload checks fail; diagnostics are sanitized
     */
    public String resolve(ItemQuery query, Item.Type type, SecretContext context) {
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
        if (type != null && !Integer.valueOf(type.key()).equals(item.getType())) {
            return null;
        }
        String value = resolver.resolve(item, context);
        return value == null ? prepared.getFallbackValue() : value;
    }

    /**
     * Exports all published values visible in a scope.
     *
     * @param scope export scope
     * @return canonical keys mapped to resolved values
     */
    public Map<String, String> export(ItemScope scope) {
        return export(scope, null);
    }

    /**
     * Exports published values whose configuration category matches the requested type.
     *
     * @param scope export scope
     * @param type expected configuration category
     * @return canonical keys mapped to resolved values
     */
    public Map<String, String> export(ItemScope scope, Item.Type type) {
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
            if (active != null && !ItemNormalizer.isSecretReference(active)
                    && (type == null || Integer.valueOf(type.key()).equals(active.getType()))) {
                result.put(keying.key(SettingSpec.export(item)), resolver.resolve(active));
            }
        }
        return result;
    }

    /**
     * Applies business validation and validates a query scope.
     *
     * @param query query to prepare
     * @return prepared query
     */
    public ItemQuery prepare(ItemQuery query) {
        ItemQuery prepared = enforcer == null ? query : enforcer.validateQuery(query);
        validateScope(prepared);
        return prepared;
    }

    /**
     * Applies business validation and validates an export scope.
     *
     * @param scope scope to prepare
     * @return prepared scope
     */
    public ItemScope prepare(ItemScope scope) {
        ItemScope prepared = enforcer == null ? scope : enforcer.validateScope(scope);
        validateScope(prepared);
        return prepared;
    }

    /**
     * Validates tenant identity and the optional ownership-scope value.
     *
     * @param scope scope to validate
     */
    private void validateScope(ItemScope scope) {
        if (scope != null) {
            ItemNormalizer.requireTenant(scope.getTenant_id());
            if (scope.getScope() != null) {
                Item.Scope.valueOf(scope.getScope());
            }
        }
    }

    /**
     * Applies business validation, validates coordinates, and authorizes publication.
     *
     * @param item item to validate
     * @return validated item
     */
    public Item validateItem(Item item) {
        Item validated = enforcer == null ? item : enforcer.validateItem(item);
        ItemNormalizer.coordinates(validated);
        requireAllowed("publish", validated);
        return validated;
    }

    /**
     * Tests whether a prepared query is allowed by the business enforcer.
     *
     * @param query query to test
     * @return {@code true} when the scope is allowed
     */
    public boolean allows(ItemQuery query) {
        return query != null && allows(query.getTenant_id(), query.getApp_id(), query.getScope());
    }

    /**
     * Tests access to an application and ownership scope within a tenant.
     *
     * @param tenant_id tenant identifier
     * @param app_id    application identifier
     * @param scope     ownership scope
     * @return {@code true} when access is allowed
     */
    public boolean allows(String tenant_id, String app_id, String scope) {
        ItemNormalizer.requireTenant(tenant_id);
        return enforcer == null || enforcer.allows(tenant_id, app_id, scope);
    }

    /**
     * Loads one immutable revision after item-read authorization.
     *
     * @param coordinates item coordinates
     * @param revision    revision identifier
     * @return revision, or {@code null} when unavailable
     */
    public Revision revision(Item coordinates, String revision) {
        Item item = find(coordinates);
        return item == null ? null : revisionStore.find(item.getTenant_id(), item.getId(), revision);
    }

    /**
     * Lists immutable revisions for an authorized item.
     *
     * @param coordinates item coordinates
     * @return item revision history
     */
    public List<Revision> revisions(Item coordinates) {
        Item item = find(coordinates);
        return item == null ? List.of() : revisionStore.query(item.getTenant_id(), item.getId());
    }

    /**
     * Reloads an authorized item from durable storage into the cache.
     *
     * @param coordinates item coordinates
     * @return refreshed item, or {@code null} when absent
     */
    public Item refresh(Item coordinates) {
        requireAllowed("read", coordinates);
        return entryStore.refresh(coordinates);
    }

    /**
     * Rebuilds cached item state for an authorized scope.
     *
     * @param scope scope to rebuild
     * @return rebuilt items
     */
    public List<Item> rebuild(ItemScope scope) {
        ItemScope prepared = prepare(scope);
        if (prepared == null || !allows(prepared.getTenant_id(), prepared.getApp_id(), prepared.getScope())) {
            return List.of();
        }
        requireAllowed("read", prepared.getTenant_id(), prepared.getApp_id(), prepared.getScope(), null);
        return entryStore.rebuild(prepared);
    }

    /**
     * Returns a safe preview of the current item content.
     *
     * @param query item query
     * @return preview text, or {@code null} when unavailable
     */
    public String preview(ItemQuery query) {
        ItemQuery prepared = prepare(query);
        if (prepared == null || !allows(prepared)) {
            return null;
        }
        Item item = find(prepared);
        return item == null ? null : resolver.preview(item);
    }

    /**
     * Authorizes and evicts one item from the cache.
     *
     * @param coordinates item coordinates
     */
    public void evict(Item coordinates) {
        requireAllowed("evict", coordinates);
        entryStore.evict(coordinates);
    }

    /**
     * Builds the subscription key for one item coordinate.
     *
     * @param coordinates item coordinates
     * @return canonical watch key
     */
    public String watchKey(Item coordinates) {
        return keying.key(SettingSpec.watch(coordinates));
    }

    /**
     * Resolves only immutable active snapshots; draft content never reaches runtime reads.
     *
     * @param item current item carrying stable and gray revision pointers
     * @param context trusted gray-selection context, or null to use the stable pointer
     * @return detached active snapshot, or null when no active revision is available
     * @throws IllegalArgumentException if the gray context belongs to another tenant
     * @throws IllegalStateException if the frozen gray publication or its active snapshot is missing or inconsistent
     */
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
                .type(Revision.category(snapshot)).format(snapshot.getFormat()).source(snapshot.getSource())
                .descriptor(Revision.descriptor(snapshot)).schema(Revision.schema(snapshot))
                .encrypted(snapshot.getEncrypted()).labels(snapshot.getLabels()).extension(snapshot.getExtension())
                .checksum(snapshot.getChecksum()).build();
        return active;
    }

    /**
     * Validates coordinates and enforces an action for one item.
     *
     * @param action      action name
     * @param coordinates item coordinates
     */
    private void requireAllowed(String action, Item coordinates) {
        ItemNormalizer.coordinates(coordinates);
        requireAllowed(
                action,
                coordinates.getTenant_id(),
                coordinates.getApp_id(),
                coordinates.getScope(),
                keying.key(SettingSpec.scope(coordinates)));
    }

    /**
     * Enforces business-scope and guard authorization for an action.
     *
     * @param action action name
     * @param tenant_id tenant identifier
     * @param app_id application identifier
     * @param scope ownership scope
     * @param resourceId canonical resource identifier
     * @throws IllegalArgumentException if the configuration scope is denied
     */
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
