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

import org.miaixz.bus.cortex.magic.identity.CortexIdentity;
import org.miaixz.bus.cortex.registry.RegistryIdentity;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemNormalizer;

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
     * Tenant identity is mandatory for entry and instance keys, but is not used for route keys. Optional {@code type}
     * and {@code appId} route dimensions are preserved as supplied and may be omitted by fallback candidates.
     * </p>
     *
     * @param mode        registry key mode
     * @param tenantId    tenantId
     * @param type        registry type
     * @param appId       application identifier
     * @param id          logical entry identifier
     * @param method      route method
     * @param version     route version
     * @param verb        numeric verb code
     * @param fingerprint runtime instance fingerprint
     */
    record RegistrySpec(int mode, String tenantId, Type type, String appId, String id, String method, String version,
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
         * @param tenantId tenantId
         * @param type     registry type
         * @param id       logical entry identifier
         * @return registry-entry specification
         */
        public static RegistrySpec entry(String tenantId, Type type, String id) {
            return new RegistrySpec(ENTRY, tenantId, type, null, id, null, null, null, null);
        }

        /**
         * Creates one runtime-instance specification.
         *
         * @param tenantId    tenantId
         * @param appId       application identifier
         * @param method      route method
         * @param version     route version
         * @param fingerprint runtime instance fingerprint
         * @return runtime-instance specification
         */
        public static RegistrySpec instance(
                String tenantId,
                String appId,
                String method,
                String version,
                String fingerprint) {
            return new RegistrySpec(INSTANCE, tenantId, null, appId, null, method, version, null, fingerprint);
        }

        /**
         * Creates one runtime-route specification.
         *
         * @param tenantId tenant metadata, not used for route-key encoding or lookup
         * @param type     registry type, optional
         * @param appId    application identifier, optional
         * @param method   route method
         * @param version  route version
         * @param verb     numeric verb code
         * @return runtime-route specification
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
                    asset.getTenant_id(),
                    resolvedType,
                    asset.getApp_id(),
                    asset.getMethod(),
                    asset.getVersion(),
                    asset.getVerb());
        }

        /**
         * Requires and returns the tenant identifier without introducing a default.
         *
         * @return nonblank tenant identifier
         */
        public String tenantIdPart() {
            return CortexIdentity.tenantId(tenantId);
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
     * Configuration key specification qualified by tenant, ownership scope, application, code, and kind.
     *
     * @param mode      key purpose
     * @param tenant_id tenant identifier
     * @param scope     configuration ownership scope
     * @param app_id    application identifier
     * @param code      configuration code
     * @param kind      configuration content kind
     * @param revision  historical revision
     */
    record SettingSpec(int mode, String tenant_id, String scope, String app_id, String code, String kind,
            String revision) {

        /**
         * Mode for the stable item identifier key.
         */
        public static final int ITEM_ID = 11;
        /**
         * Mode for the item-scope authorization key.
         */
        public static final int SCOPE = 12;
        /**
         * Mode for the item watch channel key.
         */
        public static final int WATCH = 13;
        /**
         * Mode for the runtime overlay key.
         */
        public static final int OVERLAY = 14;
        /**
         * Mode for the exported item key.
         */
        public static final int EXPORT = 15;
        /**
         * Mode for the durable item-entry key.
         */
        public static final int ENTRY = 16;
        /**
         * Mode for an immutable revision key.
         */
        public static final int REVISION = 17;
        /**
         * Mode for the revision-sequence key.
         */
        public static final int SEQUENCE = 18;

        /**
         * Creates a key specification for the supplied item and purpose.
         */
        private static SettingSpec of(int mode, Item item, String revision) {
            ItemNormalizer.coordinates(item);
            return new SettingSpec(mode, item.getTenant_id(), item.getScope(), item.getApp_id(), item.getCode(),
                    item.getKind(), revision);
        }

        /**
         * Creates a stable item-identifier specification.
         *
         * @param item setting item
         * @return item-identifier specification
         */
        public static SettingSpec itemId(Item item) {
            return of(ITEM_ID, item, null);
        }

        /**
         * Creates an authorization-scope specification.
         *
         * @param item setting item
         * @return scope specification
         */
        public static SettingSpec scope(Item item) {
            return of(SCOPE, item, null);
        }

        /**
         * Creates a watch-channel specification.
         *
         * @param item setting item
         * @return watch specification
         */
        public static SettingSpec watch(Item item) {
            return of(WATCH, item, null);
        }

        /**
         * Creates a runtime-overlay specification.
         *
         * @param item setting item
         * @return overlay specification
         */
        public static SettingSpec overlay(Item item) {
            return of(OVERLAY, item, null);
        }

        /**
         * Creates an exported-key specification.
         *
         * @param item setting item
         * @return export specification
         */
        public static SettingSpec export(Item item) {
            return of(EXPORT, item, null);
        }

        /**
         * Creates a durable entry-key specification.
         *
         * @param item setting item
         * @return entry specification
         */
        public static SettingSpec entry(Item item) {
            return of(ENTRY, item, null);
        }

        /**
         * Creates an immutable revision-key specification.
         *
         * @param item     setting item
         * @param revision revision identifier
         * @return revision specification
         */
        public static SettingSpec revision(Item item, String revision) {
            return of(REVISION, item, revision);
        }

        /**
         * Creates a revision-sequence specification.
         *
         * @param item setting item
         * @return sequence specification
         */
        public static SettingSpec sequence(Item item) {
            return of(SEQUENCE, item, null);
        }
    }
}
