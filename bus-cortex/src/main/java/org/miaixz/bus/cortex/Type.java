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
import java.util.Optional;

import org.miaixz.bus.core.lang.Normal;

/**
 * Supported Cortex resource types.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum Type {

    /**
     * API service definition.
     */
    API(Normal._1, "API service"),

    /**
     * MCP entry definition.
     */
    MCP(Normal._2, "MCP entry"),

    /**
     * Prompt entry definition.
     */
    PROMPT(Normal._3, "Prompt entry"),

    /**
     * Setting namespace directory resource.
     */
    NAMESPACE(4, "Setting namespace", Domain.SETTING),

    /**
     * Setting application directory resource.
     */
    APP(5, "Setting application", Domain.SETTING),

    /**
     * Setting profile directory resource.
     */
    PROFILE(6, "Setting profile", Domain.SETTING),

    /**
     * Current-state setting item resource.
     */
    ITEM(7, "Setting item", Domain.SETTING),

    /**
     * Historical setting item snapshot.
     */
    ITEM_REVISION(8, "Setting item revision", Domain.SETTING),

    /**
     * Setting resource binding row.
     */
    BINDING(9, "Setting binding", Domain.SETTING),

    /**
     * Version snapshot definition.
     */
    VERSION(Normal._10, "Version snapshot", Domain.VERSION);

    /**
     * Stable numeric identifier used for persistence, indexing and business comparison.
     */
    private final int key;

    /**
     * Human-readable description used only for display and diagnostics.
     */
    private final String desc;
    /**
     * Domain classification for boundary-sensitive type filtering.
     */
    private final Domain domain;

    /**
     * Creates a registry-domain resource type.
     *
     * @param key  stable numeric key
     * @param desc human-readable description
     */
    Type(int key, String desc) {
        this(key, desc, Domain.REGISTRY);
    }

    /**
     * Creates a resource type with an explicit domain.
     *
     * @param key    stable numeric key
     * @param desc   human-readable description
     * @param domain resource domain
     */
    Type(int key, String desc, Domain domain) {
        this.key = key;
        this.desc = desc;
        this.domain = domain;
    }

    /**
     * Returns the stable numeric key for compact persistence and indexing.
     *
     * @return stable numeric key
     */
    public int key() {
        return key;
    }

    /**
     * Returns the stable numeric key for bean/JSON access.
     *
     * @return stable numeric key
     */
    public int getKey() {
        return key;
    }

    /**
     * Returns the human-readable type description.
     *
     * @return type description
     */
    public String desc() {
        return desc;
    }

    /**
     * Returns the human-readable type description for bean/JSON access.
     *
     * @return type description
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Returns the cache-key segment used by registry and current version storage.
     *
     * @return cache-key segment
     */
    public String segment() {
        return switch (this) {
            case API -> "service";
            case MCP -> "mcp";
            case PROMPT -> "prompt";
            case VERSION -> "version";
            default -> throw new IllegalStateException("Setting types do not have cache key segments");
        };
    }

    /**
     * Returns whether this value is a registry asset type.
     *
     * @return {@code true} for API, MCP, and PROMPT
     */
    public boolean isRegistry() {
        return domain == Domain.REGISTRY;
    }

    /**
     * Returns whether this value is a setting-domain resource type.
     *
     * @return {@code true} for setting directory, item, revision, and binding resource types
     */
    public boolean isSetting() {
        return domain == Domain.SETTING;
    }

    /**
     * Returns whether this value is a version-domain resource type.
     *
     * @return {@code true} for VERSION
     */
    public boolean isVersion() {
        return domain == Domain.VERSION;
    }

    /**
     * Returns whether the supplied type has the same stable key.
     *
     * @param type candidate type
     * @return {@code true} when both types share the same key
     */
    public boolean is(Type type) {
        return type != null && key == type.key;
    }

    /**
     * Returns whether two types share the same stable key.
     *
     * @param left  first type
     * @param right second type
     * @return {@code true} when both types share the same key
     */
    public static boolean same(Type left, Type right) {
        return left != null && left.is(right);
    }

    /**
     * Resolves one type from its stable numeric key.
     *
     * @param key raw numeric key
     * @return resolved type
     * @throws IllegalArgumentException when the key is null or unknown
     */
    public static Type fromKey(Integer key) {
        return requireKnownKey(key);
    }

    /**
     * Attempts to resolve one type from its stable numeric key.
     *
     * @param key raw numeric key
     * @return optional resolved type
     */
    public static Optional<Type> tryFromKey(Integer key) {
        if (key == null) {
            return Optional.empty();
        }
        for (Type type : values()) {
            if (type.key == key) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    /**
     * Resolves one type from a persisted key, with text fallback for legacy metadata.
     *
     * @param value raw type value
     * @return resolved type
     * @throws IllegalArgumentException when the value is null, blank, or unknown
     */
    public static Type from(String value) {
        return requireKnown(value);
    }

    /**
     * Attempts to resolve one type from a persisted key, with text fallback for legacy metadata.
     *
     * @param value raw type value
     * @return optional resolved type
     */
    public static Optional<Type> tryFrom(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        if ("ITEM_BINDING".equalsIgnoreCase(value)) {
            return Optional.of(BINDING);
        }
        try {
            return tryFromKey(Integer.valueOf(value));
        } catch (NumberFormatException ignore) {
        }
        for (Type type : values()) {
            if (type.name().equalsIgnoreCase(value) || type.desc.equalsIgnoreCase(value)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    /**
     * Resolves one required type from its stable numeric key.
     *
     * @param key raw numeric key
     * @return resolved type
     * @throws IllegalArgumentException when the key is null or unknown
     */
    public static Type requireKnownKey(Integer key) {
        return tryFromKey(key).orElseThrow(() -> new IllegalArgumentException("Unknown Cortex type key: " + key));
    }

    /**
     * Resolves one required type from its persisted value.
     *
     * @param value raw type value
     * @return resolved type
     * @throws IllegalArgumentException when the value is null, blank, or unknown
     */
    public static Type requireKnown(String value) {
        return tryFrom(value).orElseThrow(() -> new IllegalArgumentException("Unknown Cortex type value: " + value));
    }

    /**
     * Returns the built-in registry asset types in their stable scan order.
     *
     * @return registry asset types
     */
    public static List<Type> registryTypes() {
        return List.of(API, MCP, PROMPT);
    }

    /**
     * Returns the built-in setting-domain resource types in their stable order.
     *
     * @return setting-domain resource types
     */
    public static List<Type> settingTypes() {
        return List.of(NAMESPACE, APP, PROFILE, ITEM, ITEM_REVISION, BINDING);
    }

    /**
     * Resolves one registry type from its stable key.
     *
     * @param key raw type key
     * @return registry type
     */
    public static Type registryFromKey(Integer key) {
        return requireRegistryKey(key);
    }

    /**
     * Resolves one required registry type from its stable key.
     *
     * @param key raw type key
     * @return registry type
     * @throws IllegalArgumentException when the key is null, unknown, or not a registry type
     */
    public static Type requireRegistryKey(Integer key) {
        return requireRegistry(requireKnownKey(key));
    }

    /**
     * Ensures the supplied type belongs to the registry asset type set.
     *
     * @param type candidate type
     * @return the supplied type
     * @throws IllegalArgumentException when the type is null or not a registry type
     */
    public static Type requireRegistry(Type type) {
        if (type == null || !type.isRegistry()) {
            throw new IllegalArgumentException("Unsupported registry type: " + type);
        }
        return type;
    }

    /**
     * Internal type-domain classifier used to keep registry, setting and version boundaries explicit.
     */
    private enum Domain {
        /**
         * Registry asset domain.
         */
        REGISTRY,
        /**
         * Setting resource domain.
         */
        SETTING,
        /**
         * Version release domain.
         */
        VERSION
    }

}
