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
 * API registry identity is global. MCP and Prompt entries retain explicit tenant identity for storage and lookup. Route
 * coordinates and the separate setting-domain tenant rules remain unchanged.
 * </p>
 *
 * @author Kimi Liu
 */
public class RegistryIdentity {

    /**
     * Keeps registry identity handling on the static API.
     */
    public RegistryIdentity() {
        // No initialization required.
    }

    /**
     * Validates the existing explicit-tenant compatibility contract.
     *
     * @param tenantId explicit tenant identifier
     * @return canonical nonblank tenant identifier
     */
    public static String tenantId(String tenantId) {
        return CortexIdentity.tenantId(tenantId);
    }

    /**
     * Resolves storage identity by registry type without changing global route lookup coordinates.
     *
     * @param type     registry type; null retains the existing API default
     * @param tenantId explicit tenant for MCP or Prompt, ignored for API
     * @return null for API, otherwise the validated tenant
     */
    public static String tenantId(Type type, String tenantId) {
        Type effective = type == null ? Type.API : Type.requireRegistry(type);
        return effective == Type.API ? null : CortexIdentity.tenantId(tenantId);
    }

    /**
     * Rejects a stored registry value outside its selected non-API tenant.
     *
     * @param type     existing registry type
     * @param expected requested tenant
     * @param actual   stored tenant
     */
    public static void requireTenant(Type type, String expected, String actual) {
        if (Type.requireRegistry(type) != Type.API) {
            CortexIdentity.requireTenant(expected, actual);
        }
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
     * Normalizes API to global identity while retaining explicit MCP and Prompt storage tenants.
     *
     * @param asset        asset to normalize
     * @param <T>          concrete asset type
     * @param fallbackType type used when the asset does not define one
     * @return the same asset instance after normalization
     */
    public static <T extends Assets> T normalize(T asset, Type fallbackType) {
        if (asset == null) {
            return null;
        }
        Integer rawType = asset.getType() == null ? fallbackType == null ? null : fallbackType.key() : asset.getType();
        Type effective = type(rawType);
        asset.setTenant_id(tenantId(effective, asset.getTenant_id()));
        asset.setApp_id(applicationId(asset.getApp_id()));
        asset.setType(effective.key());
        return asset;
    }

}
