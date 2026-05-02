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

import java.util.List;
import java.util.Map;

import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.cortex.Suite;
import org.miaixz.bus.cortex.Trait;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.Vector;

/**
 * Durable store contract for registry entries and runtime instances.
 *
 * @param <T> stored asset type
 * @author Kimi Liu
 * @since Java 21+
 */
public interface RegistryStore<T extends Assets> {

    /**
     * Saves a single registry entry.
     *
     * @param entry registry entry
     */
    void save(T entry);

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
     * Saves a registry entry together with a runtime instance snapshot.
     *
     * @param entry    registry entry
     * @param instance runtime instance
     */
    default void save(T entry, Instance instance) {
        save(entry);
    }

    /**
     * Deletes an entry by type, namespace and ID.
     *
     * @param type      asset type
     * @param namespace namespace
     * @param id        entry identifier
     */
    void delete(Type type, String namespace, String id);

    /**
     * Deletes a runtime instance by method/version/fingerprint.
     *
     * @param type        asset type
     * @param namespace   namespace
     * @param app_id      application identifier
     * @param method      method
     * @param version     version
     * @param fingerprint instance fingerprint
     */
    default void deleteInstance(
            Type type,
            String namespace,
            String app_id,
            String method,
            String version,
            String fingerprint) {

    }

    /**
     * Finds an entry by type, namespace and ID.
     *
     * @param type      asset type
     * @param namespace namespace
     * @param id        entry identifier
     * @return persisted entry or {@code null}
     */
    T find(Type type, String namespace, String id);

    /**
     * Finds an entry by method and version.
     *
     * @param type      asset type
     * @param namespace namespace
     * @param app_id    application identifier
     * @param method    method
     * @param version   version
     * @return persisted entry or {@code null}
     */
    default T findByMethodVersion(Type type, String namespace, String app_id, String method, String version) {
        return null;
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
     * @param type      asset type
     * @param namespace namespace
     * @param app_id    application identifier
     * @param method    method
     * @param version   version
     * @return matching entries
     */
    default List<T> queryByRoute(Type type, String namespace, String app_id, String method, String version) {
        Vector vector = new Vector();
        vector.setNamespace_id(namespace);
        vector.setApp_id(app_id);
        vector.setType(type == null ? null : type.key());
        vector.setMethod(method);
        vector.setVersion(version);
        return query(type, vector);
    }

    /**
     * Queries runtime instances for the given service identity.
     *
     * @param type      asset type
     * @param namespace namespace
     * @param app_id    application identifier
     * @param method    method
     * @param version   version
     * @return matching runtime instances
     */
    default List<Instance> queryInstances(Type type, String namespace, String app_id, String method, String version) {
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

    /**
     * Returns store capability hints using legacy string keys.
     *
     * @return capability flags
     */
    default Map<String, Boolean> capabilities() {
        return storeCapabilities().asMap();
    }

}
