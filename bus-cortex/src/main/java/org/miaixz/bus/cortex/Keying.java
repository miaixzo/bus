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
package org.miaixz.bus.cortex;

import java.util.List;

import org.miaixz.bus.cortex.registry.RegistryIdentity;

/**
 * Generic key-strategy abstraction shared by the registry and setting domains.
 * <p>
 * The interface intentionally keeps only three generic operations:
 * </p>
 * <ul>
 * <li>{@link #key(Object)} for the strongest or canonical key of one specification</li>
 * <li>{@link #keys(Object)} for ordered key candidates when the domain supports fallback lookup</li>
 * <li>{@link #prefix(Object)} for scan prefixes when the domain supports range-style queries</li>
 * </ul>
 * <p>
 * Concrete domains contribute their own specification type through the generic parameter {@code S}. In this module,
 * registry/runtime routing uses {@link RegistrySpec} while the setting domain uses {@link SettingSpec}.
 * </p>
 *
 * @param <S> domain-owned key specification type
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Keying<S> {

    /**
     * Builds the strongest or primary key represented by the supplied specification.
     *
     * @param spec key specification
     * @return primary key or {@code null} when the specification is incomplete
     */
    String key(S spec);

    /**
     * Builds the ordered candidate-key list represented by the supplied specification.
     *
     * @param spec key specification
     * @return ordered candidate-key list
     */
    default List<String> keys(S spec) {
        String key = key(spec);
        return key == null ? List.of() : List.of(key);
    }

    /**
     * Builds one scanning prefix represented by the supplied specification.
     *
     * @param spec key specification
     * @return key prefix or {@code null} when the specification has no prefix form
     */
    default String prefix(S spec) {
        return null;
    }

    /**
     * Registry-domain key specification.
     * <p>
     * One {@code RegistrySpec} can describe three different registry key families:
     * </p>
     * <ul>
     * <li>{@link #ENTRY}: durable registry-entry keys</li>
     * <li>{@link #INSTANCE}: runtime-instance keys</li>
     * <li>{@link #ROUTE}: runtime route lookup keys and their ordered fallback chain</li>
     * </ul>
     * <p>
     * Optional dimensions such as {@code namespace}, {@code type}, and {@code appId} are preserved as supplied.
     * Route-side generation intentionally does not auto-fill these dimensions with persistence defaults.
     * </p>
     *
     * @author Kimi Liu
     * @param mode        registry key mode
     * @param namespace   namespace
     * @param type        registry type
     * @param appId       application identifier
     * @param id          logical entry identifier
     * @param method      route method
     * @param version     route version
     * @param verb        numeric verb code
     * @param fingerprint runtime instance fingerprint
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
    record RegistrySpec(int mode, String namespace, Type type, String appId, String id, String method, String version,
            Integer verb, String fingerprint) {

        /**
         * Mode flag for durable registry-entry keys.
         */
        public static final int ENTRY = 1;

        /**
         * Mode flag for runtime-instance keys.
         */
        public static final int INSTANCE = 2;

        /**
         * Mode flag for runtime route keys and route candidate chains.
         */
        public static final int ROUTE = 3;

        /**
         * Creates one registry-entry specification.
         *
         * @param namespace namespace
         * @param type      registry type
         * @param id        logical entry identifier
         * @return registry-entry specification
         */
        public static RegistrySpec entry(String namespace, Type type, String id) {
            return new RegistrySpec(ENTRY, namespace, type, null, id, null, null, null, null);
        }

        /**
         * Creates one runtime-instance specification.
         *
         * @param namespace   namespace
         * @param appId       application identifier
         * @param method      route method
         * @param version     route version
         * @param fingerprint runtime instance fingerprint
         * @return runtime-instance specification
         */
        public static RegistrySpec instance(
                String namespace,
                String appId,
                String method,
                String version,
                String fingerprint) {
            return new RegistrySpec(INSTANCE, namespace, null, appId, null, method, version, null, fingerprint);
        }

        /**
         * Creates one runtime-route specification.
         *
         * @param namespace namespace, optional
         * @param type      registry type, optional
         * @param appId     application identifier, optional
         * @param method    route method
         * @param version   route version
         * @param verb      numeric verb code
         * @return runtime-route specification
         */
        public static RegistrySpec route(
                String namespace,
                Type type,
                String appId,
                String method,
                String version,
                Integer verb) {
            return new RegistrySpec(ROUTE, namespace, type, appId, null, method, version, verb, null);
        }

        /**
         * Creates one runtime-route specification from one asset.
         *
         * @param asset source asset
         * @return runtime-route specification
         */
        public static RegistrySpec route(Assets asset) {
            if (asset == null) {
                return route(null, null, null, null, null, null);
            }
            Type resolvedType = asset.getType() == null ? null
                    : Type.tryFromKey(asset.getType()).filter(Type::isRegistry).orElse(null);
            return route(
                    asset.getNamespace_id(),
                    resolvedType,
                    asset.getApp_id(),
                    asset.getMethod(),
                    asset.getVersion(),
                    asset.getVerb());
        }

        /**
         * Returns the optional namespace part without introducing route-side defaults.
         *
         * @return namespace part or {@code null}
         */
        public String namespacePart() {
            return namespace == null || namespace.isBlank() ? null : namespace.trim();
        }

        /**
         * Returns the optional registry type.
         *
         * @return registry type or {@code null}
         */
        public Type typePart() {
            return type == null ? null : Type.requireRegistry(type);
        }

        /**
         * Returns the optional stable numeric type part.
         *
         * @return numeric type part or {@code null}
         */
        public String typeKeyPart() {
            Type registryType = typePart();
            return registryType == null ? null : Integer.toString(registryType.key());
        }

        /**
         * Returns the optional application identifier part.
         *
         * @return application identifier part or {@code null}
         */
        public String appIdPart() {
            return RegistryIdentity.applicationId(appId);
        }

        /**
         * Returns the logical entry identifier part.
         *
         * @return entry identifier part or {@code null}
         */
        public String idPart() {
            return id == null || id.isBlank() ? null : id.trim();
        }

        /**
         * Returns the route method part.
         *
         * @return route method part or {@code null}
         */
        public String methodPart() {
            return method == null || method.isBlank() ? null : method.trim();
        }

        /**
         * Returns the route version part.
         *
         * @return route version part or {@code null}
         */
        public String versionPart() {
            return version == null || version.isBlank() ? null : version.trim();
        }

        /**
         * Returns the numeric verb part.
         *
         * @return numeric verb part or {@code null}
         */
        public Integer verbPart() {
            return verb;
        }

        /**
         * Returns the runtime instance fingerprint part.
         *
         * @return fingerprint part or {@code null}
         */
        public String fingerprintPart() {
            return fingerprint == null || fingerprint.isBlank() ? null : fingerprint.trim();
        }

        /**
         * Returns whether this specification can produce runtime route keys.
         *
         * @return {@code true} when method, version, and verb are all present
         */
        public boolean routable() {
            return methodPart() != null && versionPart() != null && verbPart() != null;
        }

    }

    /**
     * Setting-domain key specification.
     * <p>
     * One {@code SettingSpec} covers the logical identifiers and cache/storage keys used by the setting subsystem,
     * including item identity, profile scopes, runtime watch/overlay keys, current-state entry keys, and revision
     * history keys.
     * </p>
     *
     * @param mode      setting key mode
     * @param namespace namespace
     * @param group     group name
     * @param dataId    data identifier
     * @param profile   runtime profile
     * @param revision  revision number
     * @author Kimi Liu
     * @since Java 21+
     */
    record SettingSpec(int mode, String namespace, String group, String dataId, String profile, String revision) {

        /**
         * Mode flag for the logical item identifier.
         */
        public static final int ITEM_ID = 11;

        /**
         * Mode flag for the logical profile scope.
         */
        public static final int PROFILE_SCOPE = 12;

        /**
         * Mode flag for runtime watch keys.
         */
        public static final int WATCH = 13;

        /**
         * Mode flag for runtime overlay keys.
         */
        public static final int OVERLAY = 14;

        /**
         * Mode flag for exported logical setting identifiers.
         */
        public static final int EXPORT = 15;

        /**
         * Mode flag for current-state entry keys.
         */
        public static final int ENTRY = 16;

        /**
         * Mode flag for revision keys.
         */
        public static final int REVISION = 17;

        /**
         * Mode flag for revision-sequence keys.
         */
        public static final int SEQUENCE = 18;

        /**
         * Creates one logical-item specification.
         *
         * @param namespace namespace
         * @param group     group
         * @param dataId    data identifier
         * @return item-id specification
         */
        public static SettingSpec itemId(String namespace, String group, String dataId) {
            return new SettingSpec(ITEM_ID, namespace, group, dataId, null, null);
        }

        /**
         * Creates one profile-scope specification.
         *
         * @param namespace namespace
         * @param group     group
         * @param dataId    data identifier
         * @param profile   optional profile
         * @return profile-scope specification
         */
        public static SettingSpec profileScope(String namespace, String group, String dataId, String profile) {
            return new SettingSpec(PROFILE_SCOPE, namespace, group, dataId, profile, null);
        }

        /**
         * Creates one watch-key specification.
         *
         * @param namespace namespace
         * @param group     group
         * @param dataId    data identifier
         * @param profile   optional profile
         * @return watch-key specification
         */
        public static SettingSpec watch(String namespace, String group, String dataId, String profile) {
            return new SettingSpec(WATCH, namespace, group, dataId, profile, null);
        }

        /**
         * Creates one overlay-key specification.
         *
         * @param namespace namespace
         * @param group     group
         * @param dataId    data identifier
         * @param profile   optional profile
         * @return overlay-key specification
         */
        public static SettingSpec overlay(String namespace, String group, String dataId, String profile) {
            return new SettingSpec(OVERLAY, namespace, group, dataId, profile, null);
        }

        /**
         * Creates one export-key specification.
         *
         * @param namespace namespace
         * @param group     group
         * @param dataId    data identifier
         * @param profile   optional profile
         * @return export-key specification
         */
        public static SettingSpec export(String namespace, String group, String dataId, String profile) {
            return new SettingSpec(EXPORT, namespace, group, dataId, profile, null);
        }

        /**
         * Creates one current-state entry specification.
         *
         * @param namespace namespace
         * @param group     group
         * @param dataId    data identifier
         * @param profile   optional profile
         * @return current-state entry specification
         */
        public static SettingSpec entry(String namespace, String group, String dataId, String profile) {
            return new SettingSpec(ENTRY, namespace, group, dataId, profile, null);
        }

        /**
         * Creates one revision specification.
         *
         * @param namespace namespace
         * @param group     group
         * @param dataId    data identifier
         * @param profile   optional profile
         * @param revision  revision number
         * @return revision specification
         */
        public static SettingSpec revision(
                String namespace,
                String group,
                String dataId,
                String profile,
                String revision) {
            return new SettingSpec(REVISION, namespace, group, dataId, profile, revision);
        }

        /**
         * Creates one revision-sequence specification.
         *
         * @param namespace namespace
         * @param group     group
         * @param dataId    data identifier
         * @return revision-sequence specification
         */
        public static SettingSpec sequence(String namespace, String group, String dataId) {
            return new SettingSpec(SEQUENCE, namespace, group, dataId, null, null);
        }

        /**
         * Returns the optional group part.
         *
         * @return group part or {@code null}
         */
        public String groupPart() {
            return group == null || group.isBlank() ? null : group.trim();
        }

        /**
         * Returns the optional data identifier part.
         *
         * @return data identifier part or {@code null}
         */
        public String dataIdPart() {
            return dataId == null || dataId.isBlank() ? null : dataId.trim();
        }

        /**
         * Returns the optional normalized profile part.
         * <p>
         * Profiles are normalized to lower case so runtime watch, overlay, entry, and revision keys share the same
         * canonical profile segment.
         * </p>
         *
         * @return profile part or {@code null}
         */
        public String profilePart() {
            return profile == null || profile.isBlank() ? null : profile.trim().toLowerCase();
        }

        /**
         * Returns the optional revision part.
         *
         * @return revision part or {@code null}
         */
        public String revisionPart() {
            return revision == null || revision.isBlank() ? null : revision.trim();
        }

    }

}
