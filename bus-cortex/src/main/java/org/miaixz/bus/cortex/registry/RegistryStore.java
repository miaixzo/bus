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
package org.miaixz.bus.cortex.registry;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.cortex.*;

/**
 * Durable store contract for registry entries and runtime instances.
 *
 * @param <T> stored asset type
 * @author Kimi Liu
 */
public interface RegistryStore<T extends Assets> {

    /**
     * Executes cache and event effects after the owning host transaction commits. Stores without host transactions
     * retain immediate execution.
     *
     * @param action cache or event effect to run after successful commit
     */
    default void afterCommit(Runnable action) {
        action.run();
    }

    /**
     * Saves a single registry entry.
     *
     * @param entry registry entry
     */
    void save(T entry);

    /**
     * Saves one entry using an existing snapshot supplied by the mutation coordinator.
     * <p>
     * Stores that do not need the snapshot retain the ordinary save behavior.
     * </p>
     *
     * @param entry    registry entry
     * @param existing existing snapshot or {@code null} when the entry is absent
     */
    default void save(T entry, T existing) {
        save(entry);
    }

    /**
     * Saves a batch of entries.
     *
     * @param entries registry entries
     */
    default void saveAll(List<T> entries) {
        if (entries == null) {
            return;
        }
        for (T entry : entries) {
            if (entry != null) {
                save(entry);
            }
        }
    }

    /**
     * Atomically saves a service and its instance to the shared process store. The host must preserve the existing
     * process identity and current session; registry writes must not replace them with service coordinates. The host
     * owns authoritative process persistence and session maintenance. Assign or reuse its exact process identifier in
     * the supplied snapshot before returning so committed projections expose that same identity.
     *
     * @param entry    registry entry
     * @param instance shared process snapshot associated with the supplied service
     */
    void save(T entry, Instance instance);

    /**
     * Atomically updates only the observation of an existing runtime registration. The service definition and expected
     * instance must still match within the host transaction; stale, removed, disabled or replaced snapshots return
     * null. Implementations must not delegate to ordinary registration, recreate rows, or publish side effects.
     *
     * @param service     exact service snapshot captured before probing
     * @param expected    exact runtime snapshot captured before probing
     * @param observation completed probe result
     * @param started     probe start in epoch milliseconds
     * @param finished    probe completion in epoch milliseconds
     * @return updated runtime snapshot, or null when the observation is stale
     */
    Instance updateHealth(T service, Instance expected, Status observation, long started, long finished);

    /**
     * Publishes a committed runtime snapshot only while its exact definition and association remain authoritative.
     * Hosts must exclude a concurrent replacement through the action and discard stale snapshots without dispatch.
     *
     * @param entry    exact captured definition
     * @param instance exact captured runtime association
     * @param previous retired runtime when publishing a removal, or null
     * @param action   event construction and dispatch after the originating transaction committed
     */
    void publish(T entry, Instance instance, Instance previous, Runnable action);

    /**
     * Reads runtime registrations for one complete service identity, including its verb. Implementations retain every
     * association snapshot and perform no cache writes or process-level deduplication.
     *
     * @param service exact service definition
     * @return current runtime association snapshots for the supplied service
     */
    List<Instance> queryInstances(T service);

    /**
     * Deletes an entry by type, tenantId and ID.
     *
     * @param type     asset type
     * @param tenantId tenantId
     * @param id       entry identifier
     */
    void delete(Type type, String tenantId, String id);

    /**
     * Deletes one entry using an existing snapshot supplied by the mutation coordinator.
     *
     * @param type     asset type
     * @param tenantId tenantId
     * @param id       entry identifier
     * @param existing existing snapshot
     */
    default void delete(Type type, String tenantId, String id, T existing) {
        delete(type, tenantId, id);
    }

    /**
     * Deletes one service relationship by route and process checksum; never deletes the shared process.
     *
     * @param type     asset type
     * @param tenantId tenantId
     * @param app_id   application identifier
     * @param method   method
     * @param version  version
     * @param checksum instance checksum
     */
    default void deleteInstance(
            Type type,
            String tenantId,
            String app_id,
            String method,
            String version,
            String checksum) {

    }

    /**
     * Finds an entry by type, tenantId and ID.
     *
     * @param type     asset type
     * @param tenantId tenantId
     * @param id       entry identifier
     * @return persisted entry or {@code null}
     */
    T find(Type type, String tenantId, String id);

    /**
     * Finds an entry by method and version.
     *
     * @param type     asset type
     * @param tenantId tenantId
     * @param app_id   application identifier
     * @param method   method
     * @param version  version
     * @return persisted entry or {@code null}
     */
    default T findByMethodVersion(Type type, String tenantId, String app_id, String method, String version) {
        return null;
    }

    /**
     * Finds entries by complete registry route keys.
     *
     * @param type   asset type
     * @param routes route keys
     * @return existing entries keyed by route
     */
    default Map<RegistryRouteKey, T> findByRoutes(Type type, Collection<RegistryRouteKey> routes) {
        return Map.of();
    }

    /**
     * Queries entries for the given selector.
     *
     * @param type   asset type
     * @param vector selector
     * @return matching entries
     */
    List<T> query(Type type, Vector vector);

    /**
     * Queries entries for the given registry-specific selector.
     *
     * @param type  asset type
     * @param query registry query
     * @return matching entries
     */
    default List<T> query(Type type, RegistryQuery query) {
        return query(type, RegistryScopeMapping.toVector(query));
    }

    /**
     * Queries entries by exact route identity.
     *
     * @param type     asset type
     * @param tenantId tenantId
     * @param app_id   application identifier
     * @param method   method
     * @param version  version
     * @return matching entries
     */
    default List<T> queryByRoute(Type type, String tenantId, String app_id, String method, String version) {
        Vector vector = new Vector();
        vector.setTenant_id(tenantId);
        vector.setApp_id(app_id);
        vector.setType(type == null ? null : type.key());
        vector.setMethod(method);
        vector.setVersion(version);
        return query(type, vector);
    }

    /**
     * Queries shared processes using service associations maintained by this store.
     *
     * @param type     asset type
     * @param tenantId tenantId
     * @param app_id   application identifier
     * @param method   method
     * @param version  version
     * @return matching shared runtime instances
     */
    default List<Instance> queryInstances(Type type, String tenantId, String app_id, String method, String version) {
        return List.of();
    }

    /**
     * Returns strongly typed store capability hints.
     *
     * @return capability flags
     */
    default Suite storeCapabilities() {
        return Suite.of(Trait.BATCH, Trait.QUERY, Trait.DURABLE).with(Trait.ROUTE_QUERY, false)
                .with(Trait.INSTANCES, false);
    }

}
