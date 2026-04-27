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
package org.miaixz.bus.vortex.registry;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.registry.RegistryIdentity;

/**
 * A specialized, in-memory registry for managing route {@link Assets}.
 * <p>
 * This class extends the generic {@link AbstractRegistry} and configures it to specifically handle {@code Assets}
 * objects. Its primary responsibility is to define the unique key used to store and retrieve each asset across
 * namespace, type, method, and version dimensions.
 *
 * @author Kimi Liu
 * @see AbstractRegistry
 * @see Assets
 * @since Java 21+
 */
public class AssetsRegistry extends AbstractRegistry<Assets> {

    /**
     * Constructs an {@code AssetsRegistry} and configures its key generation strategy.
     * <p>
     * The unique key for each asset is defined as the concatenation of its namespace, type key, method name, and
     * version (e.g., "default:1:user.getProfile:1.0.0"). This composite key avoids collisions across namespaces and
     * asset types while still allowing quick lookups during request processing.
     */
    public AssetsRegistry() {
        setKeyGenerator(
                asset -> buildKey(
                        RegistryIdentity.type(asset.getType()),
                        asset.getNamespace_id(),
                        asset.getMethod(),
                        asset.getVersion()));
    }

    /**
     * A convenience method to retrieve an asset based on its type, namespace, method name, and version.
     * <p>
     * This method constructs the composite key and delegates the lookup to the underlying map in the parent class.
     *
     * @param type      The logical asset type.
     * @param namespace The logical namespace of the asset.
     * @param method    The method name of the asset.
     * @param version   The version string of the asset.
     * @return The matching {@link Assets} object, or {@code null} if no asset is found for the given combination.
     */
    public Assets get(Type type, String namespace, String method, String version) {
        return get(buildKey(type, namespace, method, version));
    }

    /**
     * A convenience method to retrieve an API asset based on its namespace, method name, and version.
     *
     * @param namespace The logical namespace of the asset.
     * @param method    The method name of the asset.
     * @param version   The version string of the asset.
     * @return The matching {@link Assets} object, or {@code null} if no asset is found for the given combination.
     */
    public Assets get(String namespace, String method, String version) {
        return get(Type.API, namespace, method, version);
    }

    /**
     * A convenience method to retrieve an API asset from the default namespace based on its method name and version.
     *
     * @param method  The method name of the asset.
     * @param version The version string of the asset.
     * @return The matching {@link Assets} object, or {@code null} if no asset is found for the given combination.
     */
    public Assets get(String method, String version) {
        return get(Type.API, Normal.DEFAULT, method, version);
    }

    /**
     * Builds the in-memory registry key for one asset lookup.
     *
     * @param type      The logical asset type.
     * @param namespace The logical namespace of the asset.
     * @param method    The method name of the asset.
     * @param version   The version string of the asset.
     * @return Composite registry key.
     */
    private String buildKey(Type type, String namespace, String method, String version) {
        Type effective = type == null ? Type.API : type;
        return RegistryIdentity.namespace(namespace) + Symbol.COLON + effective.key() + Symbol.COLON + method
                + Symbol.COLON + version;
    }

}
