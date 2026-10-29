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

import java.io.Serial;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import jakarta.persistence.Lob;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;
import org.miaixz.bus.core.xyz.EnumKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Current editable configuration definition; application relationships and publication state belong elsewhere.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Item extends Tenant {

    @Serial
    private static final long serialVersionUID = 2558909106509L;

    /**
     * name.
     */
    private String name;

    /**
     * Code identifying the current configuration definition.
     */
    private String code;

    /**
     * Configuration category stored using the stable code from {@link Type}.
     */
    private Integer type;

    /**
     * Current definition with extensible property attributes, including {@code value}, {@code default} and
     * {@code desc}; sensitive members must be protected.
     */
    @Lob
    private String content;

    /**
     * Delivery and export format; the definition body is always canonical JSON.
     */
    private String format;

    /**
     * JSON Schema used to validate the complete effective configuration.
     */
    @Lob
    private String schema;

    /**
     * Storage protection flag; protection is mandatory for {@link Type#SECRET}.
     */
    private Integer encrypted;

    /**
     * Optimistic concurrency counter for the current definition, independent of publication versions.
     */
    private Long edition;

    /**
     * Validated definition rules and protection metadata.
     */
    @Lob
    private String extension;

    /**
     * Checksum of the canonical protected definition.
     */
    private String checksum;

    /**
     * Description of the complete configuration.
     */
    private String description;

    /**
     * Creates an empty configuration definition.
     */
    public Item() {
        // No initialization required.
    }

    /**
     * Immutable attributes of one configuration property, independent of storage and publication state. Java null
     * denotes an absent attribute; JSON null remains an explicit value.
     *
     * @param attributes complete attribute collection
     * @author Kimi Liu
     */
    public static record Property(Map<String, JsonValue> attributes) {

        /**
         * Copies attributes without retaining mutable caller collections.
         *
         * @param attributes complete non-null attribute collection
         */
        public Property {
            TreeMap<String, JsonValue> copy = new TreeMap<>(Objects.requireNonNull(attributes));
            copy.forEach((name, value) -> {
                Objects.requireNonNull(name);
                Objects.requireNonNull(value);
            });
            attributes = Collections.unmodifiableMap(copy);
        }

        /**
         * Tests presence independently from JSON null or false values.
         *
         * @param name exact attribute name
         * @return whether the attribute exists
         */
        public boolean contains(String name) {
            return attributes.containsKey(name);
        }

        /**
         * Reads an exact attribute without interpreting its business meaning.
         *
         * @param name exact attribute name
         * @return the node, or Java null when absent
         */
        public JsonValue get(String name) {
            return attributes.get(name);
        }

        /**
         * Returns a changed property; Java null removes the member while JSON null remains present.
         *
         * @param name  exact attribute name
         * @param value replacement node, or Java null for removal
         * @return a new immutable property
         */
        public Property with(String name, JsonValue value) {
            Map<String, JsonValue> copy = new TreeMap<>(attributes);
            if (value == null)
                copy.remove(Objects.requireNonNull(name));
            else
                copy.put(Objects.requireNonNull(name), value);
            return new Property(copy);
        }

        /**
         * Returns a property with one exact attribute removed.
         *
         * @param name exact attribute name
         * @return a new immutable property
         */
        public Property without(String name) {
            return with(name, null);
        }

        /**
         * Reads the explicit business value without materializing defaults.
         *
         * @return the value node, or Java null when absent
         */
        public JsonValue value() {
            return get("value");
        }

        /**
         * Reads the existing wire attribute named default.
         *
         * @return the fallback node, or Java null when absent
         */
        public JsonValue defaultValue() {
            return get("default");
        }

        /**
         * Reads the display description after Codec type validation.
         *
         * @return description text, or Java null when absent
         */
        public String desc() {
            JsonValue value = get("desc");
            return value == null ? null : ((JsonValue.StringValue) value).value();
        }

        /**
         * Reports whether an explicit business value is required during user editing.
         *
         * @return whether an explicit required flag is true
         */
        public boolean required() {
            return get("required") instanceof JsonValue.BooleanValue flag && flag.value();
        }

    }

    /**
     * Configuration categories, independent of Cortex resource categories.
     *
     * @author Kimi Liu
     */
    public enum Type {

        /**
         * Ordinary configuration values.
         */
        CONFIG_MAP(1),
        /**
         * Sensitive configuration values requiring storage protection.
         */
        SECRET(2);

        /**
         * Stable persistence code.
         */
        private final int key;

        /**
         * Creates an enum value with its stable persistence code.
         *
         * @param key stable persistence code
         */
        Type(int key) {
            this.key = key;
        }

        /**
         * Resolves a supported persistence code without fallback.
         *
         * @param key persistence code to resolve
         * @return matching enum value
         * @throws IllegalArgumentException if the code is null or unsupported
         */
        public static Type of(Integer key) {
            Type value = EnumKit.getBy(Type.class, candidate -> key != null && candidate.key == key);
            if (value == null) {
                throw new IllegalArgumentException("Unsupported Type code");
            }
            return value;
        }

        /**
         * Returns the stable persistence code.
         *
         * @return stable persistence code
         */
        public int key() {
            return key;
        }

    }

    /**
     * Current definition states, independent of publication workflow states.
     *
     * @author Kimi Liu
     */
    public enum Status {

        /**
         * Deleted current definition.
         */
        DELETED(-1),
        /**
         * Disabled current definition.
         */
        DISABLED(0),
        /**
         * Active current definition.
         */
        ACTIVE(1);

        /**
         * Stable persistence code.
         */
        private final int key;

        /**
         * Creates an enum value with its stable persistence code.
         *
         * @param key stable persistence code
         */
        Status(int key) {
            this.key = key;
        }

        /**
         * Resolves a supported persistence code without fallback.
         *
         * @param key persistence code to resolve
         * @return matching enum value
         * @throws IllegalArgumentException if the code is null or unsupported
         */
        public static Status of(Integer key) {
            Status value = EnumKit.getBy(Status.class, candidate -> key != null && candidate.key == key);
            if (value == null) {
                throw new IllegalArgumentException("Unsupported Status code");
            }
            return value;
        }

        /**
         * Returns the stable persistence code.
         *
         * @return stable persistence code
         */
        public int key() {
            return key;
        }

    }

}
