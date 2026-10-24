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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.cortex.registry.RegistryIdentity;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemQuery;

/**
 * Strategy for registry and tenant-qualified configuration identity keys.
 *
 * @param <S> domain-specific identity specification
 * @author Kimi Liu
 */
public interface Keying<S> {

    /**
     * Builds or returns the exact identity component for the current domain purpose.
     *
     * @param spec purpose-specific identity coordinates
     * @return generated identity key, or null when no key is available
     */
    String key(S spec);

    /**
     * Returns the generated key as a singleton list, or an empty list when no key is available.
     *
     * @param spec purpose-specific identity coordinates
     * @return generated identity keys
     */
    default List<String> keys(S spec) {
        String key = key(spec);
        return key == null ? List.of() : List.of(key);
    }

    /**
     * Returns the cache-scan prefix for the selected key purpose, when supported.
     *
     * @param spec purpose-specific identity coordinates
     * @return cache-scan prefix, or null when the purpose has no prefix
     */
    default String prefix(S spec) {
        return null;
    }

    /**
     * Registry identity coordinates for management entries, service relationships and routes.
     *
     * @param mode     identity purpose discriminator
     * @param tenantId tenant coordinate normalized according to the registry category
     * @param type     registry resource category
     * @param appId    application identity
     * @param id       exact record identity
     * @param method   registry method identity
     * @param version  registered service version
     * @param verb     HTTP verb discriminator
     * @param checksum stable runtime instance checksum
     * @author Kimi Liu
     */
    record RegistrySpec(int mode, String tenantId, Type type, String appId, String id, String method, String version,
            Integer verb, String checksum) {

        /**
         * Key-purpose discriminator for management cache entry.
         */
        public static final int ENTRY = Normal._1;

        /**
         * Key-purpose discriminator for a service-to-process relationship.
         */
        public static final int INSTANCE = Normal._2;

        /**
         * Key-purpose discriminator for registry route.
         */
        public static final int ROUTE = Normal._3;

        /**
         * Builds the exact registry management entry key specification.
         *
         * @param tenantId tenant coordinate normalized according to the registry category
         * @param type     registry resource category
         * @param id       exact record identity
         * @return management entry key specification
         */
        public static RegistrySpec entry(String tenantId, Type type, String id) {
            return new RegistrySpec(ENTRY, tenantId, type, null, id, null, null, null, null);
        }

        /**
         * Builds service relationship coordinates using the process checksum and application-qualified service route.
         *
         * @param tenantId tenant coordinate normalized according to the registry category
         * @param appId    application identity
         * @param method   registry method identity
         * @param version  registered service version
         * @param checksum stable runtime instance checksum
         * @return service relationship key specification
         */
        public static RegistrySpec instance(
                String tenantId,
                String appId,
                String method,
                String version,
                String checksum) {
            return new RegistrySpec(INSTANCE, tenantId, null, appId, null, method, version, null, checksum);
        }

        /**
         * Builds the normalized registry route key specification.
         *
         * @param tenantId tenant coordinate normalized according to the registry category
         * @param type     registry resource category
         * @param appId    application identity
         * @param method   registry method identity
         * @param version  registered service version
         * @param verb     HTTP verb discriminator
         * @return registry route key specification
         */
        public static RegistrySpec route(
                String tenantId,
                Type type,
                String appId,
                String method,
                String version,
                Integer verb) {
            return new RegistrySpec(ROUTE, tenantId, type, appId, null, method, version, verb, null);
        }

        /**
         * Builds the normalized registry route key specification.
         *
         * @param asset registry asset to project
         * @return registry route key specification
         */
        public static RegistrySpec route(Assets asset) {
            if (asset == null) {
                return route(null, null, null, null, null, null);
            }
            Type resolvedType = asset.getType() == null ? null
                    : Type.tryFromKey(asset.getType()).filter(Type::isRegistry).orElse(null);
            return route(
                    asset.getTenant_id(),
                    resolvedType,
                    asset.getApp_id(),
                    asset.getMethod(),
                    asset.getVersion(),
                    asset.getVerb());
        }

        /**
         * Returns the normalized tenant id identity component.
         *
         * @return normalized management-entry tenant, or null for other key purposes
         */
        public String tenantIdPart() {
            return mode == ENTRY ? RegistryIdentity.tenantId(typePart(), tenantId) : null;
        }

        /**
         * Returns the normalized type identity component.
         *
         * @return validated registry category, or null when no category was supplied
         */
        public Type typePart() {
            return type == null ? null : Type.requireRegistry(type);
        }

        /**
         * Returns the normalized type key identity component.
         *
         * @return decimal registry category code, or null when no category was supplied
         */
        public String typeKeyPart() {
            Type registryType = typePart();
            return registryType == null ? null : Integer.toString(registryType.key());
        }

        /**
         * Returns the normalized app id identity component.
         *
         * @return normalized application identifier
         */
        public String appIdPart() {
            return RegistryIdentity.applicationId(appId);
        }

        /**
         * Returns the normalized id identity component.
         *
         * @return trimmed record identifier, or null when absent or blank
         */
        public String idPart() {
            return id == null || id.isBlank() ? null : id.trim();
        }

        /**
         * Returns the normalized method identity component.
         *
         * @return trimmed service method, or null when absent or blank
         */
        public String methodPart() {
            return method == null || method.isBlank() ? null : method.trim();
        }

        /**
         * Returns the normalized version identity component.
         *
         * @return trimmed service version, or null when absent or blank
         */
        public String versionPart() {
            return version == null || version.isBlank() ? null : version.trim();
        }

        /**
         * Returns the normalized verb identity component.
         *
         * @return supplied HTTP verb code, or null when absent
         */
        public Integer verbPart() {
            return verb;
        }

        /**
         * Returns the normalized checksum identity component.
         *
         * @return trimmed runtime instance checksum, or null when absent or blank
         */
        public String checksumPart() {
            return checksum == null || checksum.isBlank() ? null : checksum.trim();
        }

        /**
         * Tests whether method, version and verb coordinates form a complete registry route.
         *
         * @return true when method, version and verb coordinates are all present
         */
        public boolean routable() {
            return methodPart() != null && versionPart() != null && verbPart() != null;
        }

    }

    /**
     * Purpose-specific source or application coordinates without entity ownership or publication state.
     *
     * @param mode        identity purpose discriminator
     * @param tenant_id   required tenant identity
     * @param item_id     exact source Item ID
     * @param app_id      application identity
     * @param code        application-local configuration alias
     * @param revision_id exact immutable Revision ID
     * @author Kimi Liu
     */
    record SettingSpec(int mode, String tenant_id, String item_id, String app_id, String code, String revision_id) {

        /**
         * Key-purpose discriminator for exact source identity.
         */
        public static final int ITEM_ID = Normal._11;
        /**
         * Key-purpose discriminator for source management scope.
         */
        public static final int SCOPE = Normal._12;
        /**
         * Key-purpose discriminator for application subscription.
         */
        public static final int WATCH = Normal._13;
        /**
         * Key-purpose discriminator for application export.
         */
        public static final int EXPORT = Normal._15;
        /**
         * Key-purpose discriminator for management cache entry.
         */
        public static final int ENTRY = Normal._16;
        /**
         * Key-purpose discriminator for exact immutable snapshot.
         */
        public static final int REVISION = Normal._17;

        /**
         * Builds tenant-qualified source coordinates after requiring an exact item identity.
         *
         * @param mode        identity purpose discriminator
         * @param item        source definition or exact coordinates
         * @param revision_id exact immutable Revision ID
         * @return source key specification for the requested purpose
         */
        private static SettingSpec of(int mode, Item item, String revision_id) {
            if (item == null || item.getId() == null || item.getId().isBlank()) {
                throw new IllegalArgumentException("Exact item identity is required");
            }
            if (item.getTenant_id() == null || item.getTenant_id().isBlank())
                throw new IllegalArgumentException("Tenant required");
            return new SettingSpec(mode, item.getTenant_id(), item.getId(), null, item.getCode(), revision_id);
        }

        /**
         * Builds application delivery coordinates with an exact source ID or alias.
         *
         * @param mode  identity purpose discriminator
         * @param query tenant-qualified request or store selector
         * @return application-qualified delivery key specification
         */
        private static SettingSpec application(int mode, ItemQuery query) {
            if (query == null || query.getApp_id() == null || query.getApp_id().isBlank()
                    || (query.getId() == null || query.getId().isBlank())
                            && (query.getCode() == null || query.getCode().isBlank())) {
                throw new IllegalArgumentException("Application and exact item or alias are required");
            }
            if (query.getTenant_id() == null || query.getTenant_id().isBlank())
                throw new IllegalArgumentException("Tenant required");
            return new SettingSpec(mode, query.getTenant_id(), query.getId(), query.getApp_id(), query.getCode(), null);
        }

        /**
         * Builds the tenant-qualified immutable source identity key specification.
         *
         * @param item source definition or exact coordinates
         * @return exact source identity key specification
         */
        public static SettingSpec itemId(Item item) {
            return of(ITEM_ID, item, null);
        }

        /**
         * Builds a source management scope identity without application ownership fields.
         *
         * @param item source definition or exact coordinates
         * @return source management scope key specification
         */
        public static SettingSpec scope(Item item) {
            return of(SCOPE, item, null);
        }

        /**
         * Builds the exact tenant-qualified configuration management entry key specification.
         *
         * @param item source definition or exact coordinates
         * @return management entry key specification
         */
        public static SettingSpec entry(Item item) {
            return of(ENTRY, item, null);
        }

        /**
         * Builds key coordinates for an exact published snapshot of the source item.
         *
         * @param item        source definition or exact coordinates
         * @param revision_id exact immutable Revision ID
         * @return published snapshot key specification
         */
        public static SettingSpec revision(Item item, String revision_id) {
            return of(REVISION, item, revision_id);
        }

        /**
         * Builds application-qualified subscription key coordinates without registering a subscription.
         *
         * @param query tenant-qualified request or store selector
         * @return application subscription key specification
         */
        public static SettingSpec watch(ItemQuery query) {
            return application(WATCH, query);
        }

        /**
         * Builds application-qualified export key coordinates without reading configuration data.
         *
         * @param query tenant-qualified request or store selector
         * @return application export key specification
         */
        public static SettingSpec export(ItemQuery query) {
            return application(EXPORT, query);
        }

    }

}
