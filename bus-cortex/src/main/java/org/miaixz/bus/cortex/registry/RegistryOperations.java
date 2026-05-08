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

import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.Type;

/**
 * Controlled registry operations exposed to pluggable strategies.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface RegistryOperations {

    /**
     * Returns route keying strategy.
     *
     * @return keying strategy
     */
    Keying<Keying.RegistrySpec> keying();

    /**
     * Returns supported registry types.
     *
     * @return supported types
     */
    List<Type> supportedTypes();

    /**
     * Builds a route key for one registry entry.
     *
     * @param entry registry entry
     * @return route key or {@code null}
     */
    RegistryRouteKey routeKey(Assets entry);

    /**
     * Normalizes one asset.
     *
     * @param asset asset
     * @return normalized asset
     */
    Assets normalize(Assets asset);

    /**
     * Applies registry guard.
     *
     * @param action guarded action
     * @param type   registry type
     * @param asset  asset
     */
    void enforce(String action, Type type, Assets asset);

    /**
     * Resolves the existing asset for one entry.
     *
     * @param entry entry
     * @return existing asset or {@code null}
     */
    Assets resolveExisting(Assets entry);

    /**
     * Upserts one entry.
     *
     * @param entry entry
     * @return stored asset
     */
    Assets upsert(Assets entry);

    /**
     * Deletes one entry.
     *
     * @param type      registry type
     * @param namespace namespace
     * @param id        asset id
     */
    void delete(Type type, String namespace, String id);

    /**
     * Returns the concrete registry for a type.
     *
     * @param type registry type
     * @return registry
     */
    StoreBackedRegistry<? extends Assets> registry(Type type);

    /**
     * Returns durable store for a type.
     *
     * @param type registry type
     * @return durable store or {@code null}
     */
    RegistryStore<? extends Assets> store(Type type);

}
