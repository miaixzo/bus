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

import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Type;

/**
 * Route identity used by registry batch lookup strategies.
 *
 * @author Kimi Liu
 * @param namespace_id namespace identifier
 * @param app_id       application identifier
 * @param type         registry type key
 * @param method       route method
 * @param version      route version
 * @param verb         HTTP verb code
 * @since Java 21+
 */
/**
 * The type field.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
/**
 * The type field.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
/**
 * The type field.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public record RegistryRouteKey(String namespace_id, String app_id, Integer type, String method, String version,
        Integer verb) {

    /**
     * Creates a route key from one registry asset.
     *
     * @param asset source asset
     * @return route key or {@code null}
     */
    public static RegistryRouteKey of(Assets asset) {
        if (asset == null || asset.getMethod() == null || asset.getVersion() == null) {
            return null;
        }
        Type type = RegistryIdentity.type(asset);
        return new RegistryRouteKey(RegistryIdentity.namespace(asset.getNamespace_id()), asset.getApp_id(), type.key(),
                asset.getMethod(), asset.getVersion(), asset.getVerb());
    }

}
