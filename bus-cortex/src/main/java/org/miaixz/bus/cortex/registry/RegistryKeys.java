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

import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.Type;

/**
 * Shared cache key layout for cortex registry state.
 * <p>
 * Application-aware keys that include {@code app_id} are the canonical layout for asset uniqueness and instance
 * routing. Namespace-only overloads remain available for broad scans and compatibility flows.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class RegistryKeys {

    /**
     * Creates the registry-key utility holder.
     */
    private RegistryKeys() {

    }

    /**
     * Builds the cache key for a single registry entry.
     *
     * @param namespace entry namespace
     * @param type      entry type
     * @param id        entry identifier
     * @return full cache key for the entry
     */
    public static String entry(String namespace, Type type, String id) {
        return Builder.REG_PREFIX + RegistryIdentity.namespace(namespace) + ":" + segment(type) + ":" + id;
    }

    /**
     * Builds the cache key prefix used to scan all entries in the same namespace/type partition.
     *
     * @param namespace entry namespace
     * @param type      entry type
     * @return full entry prefix
     */
    public static String entryPrefix(String namespace, Type type) {
        return Builder.REG_PREFIX + RegistryIdentity.namespace(namespace) + ":" + segment(type) + ":";
    }

    /**
     * Builds a namespace-wide unique-route key used only for broad scans or compatibility flows.
     *
     * @param namespace entry namespace
     * @param type      entry type
     * @param method    route method
     * @param version   route version
     * @return namespace-wide route key
     */
    public static String unique(String namespace, Type type, String method, String version) {
        Type registryType = Type.requireRegistry(type);
        return Builder.REG_PREFIX + RegistryIdentity.namespace(namespace) + ":unique:"
                + registryType.name().toLowerCase() + ":" + method + ":" + version;
    }

    /**
     * Builds the unique-route key used to guard method/version collisions within a namespace, application, and asset
     * type.
     *
     * @param namespace entry namespace
     * @param app_id    application identifier
     * @param type      entry type
     * @param method    route method
     * @param version   route version
     * @return unique route key
     */
    public static String unique(String namespace, String app_id, Type type, String method, String version) {
        Type registryType = Type.requireRegistry(type);
        return Builder.REG_PREFIX + RegistryIdentity.namespace(namespace) + ":unique:"
                + registryType.name().toLowerCase() + ":" + RegistryIdentity.applicationId(app_id) + ":" + method + ":"
                + version;
    }

    /**
     * Builds a namespace-wide cache key for a concrete runtime instance of a route.
     *
     * @param namespace   entry namespace
     * @param method      route method
     * @param version     route version
     * @param fingerprint instance fingerprint
     * @return namespace-wide instance key
     */
    public static String instance(String namespace, String method, String version, String fingerprint) {
        return Builder.REG_PREFIX + RegistryIdentity.namespace(namespace) + ":instance:" + method + ":" + version + ":"
                + fingerprint;
    }

    /**
     * Builds the cache key for a concrete runtime instance of a route.
     *
     * @param namespace   entry namespace
     * @param app_id      application identifier
     * @param method      route method
     * @param version     route version
     * @param fingerprint instance fingerprint
     * @return full instance key
     */
    public static String instance(String namespace, String app_id, String method, String version, String fingerprint) {
        return Builder.REG_PREFIX + RegistryIdentity.namespace(namespace) + ":instance:"
                + RegistryIdentity.applicationId(app_id) + ":" + method + ":" + version + ":" + fingerprint;
    }

    /**
     * Builds the prefix used to scan namespace-wide runtime instances for a method or a specific method/version pair.
     *
     * @param namespace entry namespace
     * @param method    route method, optional
     * @param version   route version, optional when method is provided
     * @return instance key prefix
     */
    public static String instancePrefix(String namespace, String method, String version) {
        StringBuilder builder = new StringBuilder();
        builder.append(Builder.REG_PREFIX).append(RegistryIdentity.namespace(namespace)).append(":instance:");
        if (method != null) {
            builder.append(method).append(":");
            if (version != null) {
                builder.append(version).append(":");
            }
        }
        return builder.toString();
    }

    /**
     * Builds the prefix used to scan runtime instances for a specific application or route partition.
     *
     * @param namespace entry namespace
     * @param app_id    application identifier, optional
     * @param method    route method, optional
     * @param version   route version, optional when method is provided
     * @return instance key prefix
     */
    public static String instancePrefix(String namespace, String app_id, String method, String version) {
        StringBuilder builder = new StringBuilder();
        builder.append(Builder.REG_PREFIX).append(RegistryIdentity.namespace(namespace)).append(":instance:");
        if (app_id != null) {
            builder.append(RegistryIdentity.applicationId(app_id)).append(":");
            if (method != null) {
                builder.append(method).append(":");
                if (version != null) {
                    builder.append(version).append(":");
                }
            }
        }
        return builder.toString();
    }

    /**
     * Resolves the cache-key segment for a registry type.
     *
     * @param type registry type
     * @return cache-key segment
     */
    private static String segment(Type type) {
        return Type.requireRegistry(type).segment();
    }

}
