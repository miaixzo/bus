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
package org.miaixz.bus.cortex.setting.item;

import jakarta.persistence.Lob;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;

/**
 * Current state of one setting item.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Item extends Tenant {

    /**
     * Owning application identifier; required for application items and absent for shared items.
     */
    private String app_id;
    /**
     * Current stable revision identifier.
     */
    private String stable_id;
    /**
     * Latest pending revision, including staged deletion; never a runtime-delivery pointer.
     */
    private String draft_id;
    /**
     * Current gray revision identifier.
     */
    private String gray_id;
    /**
     * Active gray rollout identifier.
     */
    private String rollout_id;
    /**
     * Configuration category from org.miaixz.bus.cortex.setting.item.Item.Type; unrelated to the Cortex resource
     * category.
     */
    private Integer type;
    /**
     * Ownership scope: SHARED for tenant-wide items or APPLICATION for application-owned items.
     */
    private String scope;
    /**
     * Persisted item kind.
     */
    private String kind;
    /**
     * Stable setting code.
     */
    private String code;

    /**
     * Editable source content.
     */
    @Lob
    private String content;
    /**
     * Content format.
     */
    private String format;
    /**
     * Content source type.
     */
    private String source;

    /**
     * Whole-content JSON Schema, independent from external source parameters.
     */
    @Lob
    private String schema;
    /**
     * Encrypted-content flag.
     */
    private Integer encrypted;
    /**
     * Labels stored as text.
     */
    @Lob
    private String labels;
    /**
     * External source descriptor.
     */
    @Lob
    private String descriptor;
    /**
     * Digest of immutable item coordinates.
     */
    private String fingerprint;
    /**
     * Source extension stored as text.
     */
    @Lob
    private String extension;
    /**
     * Semantic content checksum.
     */
    private String checksum;
    /**
     * Optimistic editing edition.
     */
    private Long edition;
    /**
     * Runtime delivery generation.
     */
    private Long generation;
    /**
     * Optional item description.
     */
    private String description;

    /**
     * Creates an empty setting item.
     */
    public Item() {
        // No initialization required.
    }

    /**
     * Supported configuration ownership scopes.
     *
     * @author Kimi Liu
     */
    public enum Scope {
        /**
         * Configuration shared within a tenant.
         */
        SHARED,
        /**
         * Configuration maintained by one application.
         */
        APPLICATION
    }

    /**
     * Configuration content kind.
     */
    public enum Kind {
        /**
         * User-authored source configuration.
         */
        SOURCE,
        /**
         * Materialized effective configuration.
         */
        EFFECTIVE
    }

    /**
     * Supported content editing modes.
     *
     * @author Kimi Liu
     */
    public enum Editor {
        /**
         * Source-text editing mode.
         */
        SOURCE,
        /**
         * Structured key-value editing mode.
         */
        KEY_VALUE
    }

    /**
     * Supported protected-value mutations.
     *
     * @author Kimi Liu
     */
    public enum SecretAction {
        /**
         * Keeps the existing protected value.
         */
        KEEP,
        /**
         * Replaces the existing protected value.
         */
        REPLACE,
        /**
         * Deletes the existing protected value.
         */
        DELETE
    }

    /**
     * Supported validation depths.
     *
     * @author Kimi Liu
     */
    public enum ValidationLevel {
        /**
         * Performs syntax validation only.
         */
        SYNTAX,
        /**
         * Performs syntax and business validation.
         */
        FULL
    }

    /**
     * Stable persisted configuration categories.
     */
    public enum Type {

        /**
         * Ordinary configuration content, stored with category code {@code 1}.
         */
        CONFIG_MAP(1),

        /**
         * Protected configuration content, stored with category code {@code 2}.
         */
        SECRET(2);

        /**
         * Stable numeric code stored in configuration records.
         */
        private final int key;

        /**
         * Creates an enum value with its stable storage code.
         *
         * @param key persisted numeric code
         */
        Type(int key) {
            this.key = key;
        }

        /**
         * Resolves a configuration category from its stable numeric code.
         *
         * @param key persisted numeric code
         * @return matching enum value
         * @throws IllegalArgumentException if the code is null or unsupported
         */
        public static Type of(Integer key) {
            for (Type value : values())
                if (key != null && value.key == key)
                    return value;
            throw new IllegalArgumentException("Unsupported configuration type");
        }

        /**
         * Returns the stable storage code.
         *
         * @return persisted numeric code
         */
        public int key() {
            return key;
        }
    }

    /**
     * Stable persisted lifecycle states for configuration items.
     */
    public enum Status {

        /**
         * Logically deleted item ({@code -1}).
         */
        DELETED(-1),

        /**
         * Disabled item ({@code 0}).
         */
        DISABLED(0),

        /**
         * Draft item not yet published ({@code 10}).
         */
        UNPUBLISHED(10),

        /**
         * Published item ({@code 20}).
         */
        PUBLISHED(20),

        /**
         * Item with staged changes ({@code 30}).
         */
        CHANGED(30),

        /**
         * Item participating in a gray publication ({@code 40}).
         */
        GRAY(40),

        /**
         * Archived item ({@code 50}).
         */
        ARCHIVED(50);

        /**
         * Stable numeric code stored in configuration records.
         */
        private final int key;

        /**
         * Creates an enum value with its stable storage code.
         *
         * @param key persisted numeric code
         */
        Status(int key) {
            this.key = key;
        }

        /**
         * Resolves a configuration lifecycle state from its stable numeric code.
         *
         * @param key persisted numeric code
         * @return matching enum value
         * @throws IllegalArgumentException if the code is null or unsupported
         */
        public static Status of(Integer key) {
            for (Status value : values())
                if (key != null && value.key == key)
                    return value;
            throw new IllegalArgumentException("Unsupported configuration status");
        }

        /**
         * Returns the stable storage code.
         *
         * @return persisted numeric code
         */
        public int key() {
            return key;
        }
    }

    /**
     * Supported content-resolution sources for configuration items.
     */
    public enum Source {
        /**
         * Content supplied directly by the item.
         */
        INLINE,

        /**
         * Content resolved from the configured environment source.
         */
        ENV,

        /**
         * Content resolved through an authorized external Secret provider.
         */
        SECRET
    }

}
