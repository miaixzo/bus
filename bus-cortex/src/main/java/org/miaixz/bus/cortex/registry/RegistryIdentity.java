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
import org.miaixz.bus.cortex.magic.identity.CortexIdentity;

/**
 * Canonical identity rules shared by cortex persistence, cache, and runtime sync.
 * <p>
 * The primary asset identity is {@code namespace + app_id + type + method + version}. Helpers that omit {@code app_id}
 * only describe partial route fragments for broad scans or compatibility paths and are not the preferred identity path.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class RegistryIdentity {

    /**
     * Creates the registry-identity utility holder.
     */
    private RegistryIdentity() {

    }

    /**
     * Returns the canonical namespace, defaulting blank values to the shared default namespace.
     *
     * @param namespace raw namespace value
     * @return canonical namespace value
     */
    public static String namespace(String namespace) {
        return CortexIdentity.namespace(namespace);
    }

    /**
     * Returns the canonical application identifier.
     *
     * @param app_id raw application identifier
     * @return canonical application identifier or {@code null} when blank
     */
    public static String applicationId(String app_id) {
        return CortexIdentity.applicationId(app_id);
    }

    /**
     * Returns the effective registry type, explicitly defaulting null values to {@link Type#API}.
     *
     * @param type raw type key
     * @return canonical type
     */
    public static Type type(Integer type) {
        return type == null ? Type.API : Type.requireRegistryKey(type);
    }

    /**
     * Resolves an asset type using the same defaulting rules as {@link #type(Integer)}.
     *
     * @param asset asset whose type should be resolved
     * @return canonical asset type
     */
    public static Type type(Assets asset) {
        return asset == null ? Type.API : type(asset.getType());
    }

    /**
     * Builds a partial runtime route fragment from method and version.
     *
     * @param method  route method
     * @param version route version
     * @return concatenated route fragment or {@code null} when either part is missing
     */
    public static String routeKey(String method, String version) {
        if (method == null || version == null) {
            return null;
        }
        return method + version;
    }

    /**
     * Builds the runtime route key for an asset, including application scope.
     *
     * @param asset asset carrying method and version
     * @return concatenated route key or {@code null} when unavailable
     */
    public static String routeKey(Assets asset) {
        return asset == null ? null : routeKey(asset.getApp_id(), asset.getMethod(), asset.getVersion());
    }

    /**
     * Builds the runtime route key from application identifier, method, and version.
     *
     * @param app_id  application identifier
     * @param method  route method
     * @param version route version
     * @return concatenated route key or {@code null} when any required part is missing
     */
    public static String routeKey(String app_id, String method, String version) {
        if (applicationId(app_id) == null || method == null || version == null) {
            return null;
        }
        return app_id + ":" + method + ":" + version;
    }

    /**
     * Builds a type-qualified partial route fragment used in broad cross-type coordination.
     *
     * @param type    route type
     * @param method  route method
     * @param version route version
     * @return scoped route fragment or {@code null} when method or version is missing
     */
    public static String scopedRouteKey(Type type, String method, String version) {
        if (method == null || version == null) {
            return null;
        }
        Type effective = Type.requireRegistry(type);
        return effective.name() + ":" + method + ":" + version;
    }

    /**
     * Builds a type-qualified route key for an asset, including application scope.
     *
     * @param asset source asset
     * @return scoped route key or {@code null} when unavailable
     */
    public static String scopedRouteKey(Assets asset) {
        return asset == null ? null
                : scopedRouteKey(type(asset), asset.getApp_id(), asset.getMethod(), asset.getVersion());
    }

    /**
     * Builds a type-qualified route key used in cross-type coordination.
     *
     * @param type    route type
     * @param app_id  application identifier
     * @param method  route method
     * @param version route version
     * @return scoped route key or {@code null} when required parts are missing
     */
    public static String scopedRouteKey(Type type, String app_id, String method, String version) {
        if (applicationId(app_id) == null || method == null || version == null) {
            return null;
        }
        Type effective = Type.requireRegistry(type);
        return effective.name() + ":" + app_id + ":" + method + ":" + version;
    }

    /**
     * Normalizes namespace and type in place before the asset enters cache, persistence, or runtime state.
     *
     * @param asset        asset to normalize
     * @param fallbackType type used when the asset does not define one
     * @param <T>          concrete asset type
     * @return the same asset instance after normalization
     */
    public static <T extends Assets> T normalize(T asset, Type fallbackType) {
        if (asset == null) {
            return null;
        }
        asset.setNamespace_id(namespace(asset.getNamespace_id()));
        asset.setApp_id(applicationId(asset.getApp_id()));
        Integer rawType = asset.getType() == null ? fallbackType == null ? null : fallbackType.key() : asset.getType();
        asset.setType(type(rawType).key());
        return asset;
    }

}
