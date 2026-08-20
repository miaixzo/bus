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
package org.miaixz.bus.auth;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Describes immutable management input fields for a Provider, Source, or Vendor profile.
 * <p>
 * Field keys retain their formal protocol or official Vendor names. Forms guide an external management interface and
 * never participate in protocol wire encoding, credential storage, or runtime Registry invocation.
 * </p>
 *
 * @param sections ordered management form sections
 * @author Kimi Liu
 */
public record Form(List<Section> sections) {

    /**
     * Creates an immutable management form.
     *
     * @param sections ordered form sections
     * @throws IllegalArgumentException if the list or any section is {@code null}
     */
    public Form {
        Assert.notNull(sections, "Form sections must not be null");
        final List<Section> copy = new ArrayList<>(sections.size());
        for (Section section : sections) {
            copy.add(Assert.notNull(section, "Form section must not be null"));
        }
        sections = List.copyOf(copy);
    }

    /**
     * Enumerates management input presentation types without redefining protocol data types.
     *
     * @author Kimi Liu
     */
    public enum Type {

        /**
         * Single-line non-sensitive text input.
         */
        TEXT,

        /**
         * Masked credential-reference input; submitted plaintext is handled outside persistent settings.
         */
        SECRET,

        /**
         * URL lexical-value input.
         */
        URL,

        /**
         * Boolean input.
         */
        BOOLEAN,

        /**
         * Numeric input.
         */
        NUMBER,

        /**
         * Single-choice input.
         */
        SELECT,

        /**
         * Multiple-choice input.
         */
        MULTI_SELECT

    }

    /**
     * Groups related management fields while preserving their display order.
     *
     * @param key    stable section key used by external management interfaces
     * @param title  human-readable or localized-resource section title
     * @param fields ordered fields in this section
     * @author Kimi Liu
     */
    public record Section(String key, String title, List<Field> fields) {

        /**
         * Creates an immutable management form section.
         *
         * @param key    non-blank stable section key
         * @param title  non-blank section title
         * @param fields ordered section fields
         * @throws IllegalArgumentException if text is blank, the list is {@code null}, or a field is {@code null}
         */
        public Section {
            Assert.notBlank(key, "Form section key must not be blank");
            Assert.notBlank(title, "Form section title must not be blank");
            Assert.notNull(fields, "Form section fields must not be null");
            final List<Field> copy = new ArrayList<>(fields.size());
            for (Field field : fields) {
                copy.add(Assert.notNull(field, "Form field must not be null"));
            }
            fields = List.copyOf(copy);
        }

    }

    /**
     * Describes one management input without carrying submitted secret material.
     *
     * @param key          formal protocol or official Vendor field key
     * @param label        human-readable or localized-resource label
     * @param type         management input presentation type
     * @param required     whether management input is required
     * @param defaultValue optional non-sensitive default value
     * @param constraints  ordered generic management validation declarations
     * @author Kimi Liu
     */
    public record Field(String key, String label, Type type, boolean required, Optional<JsonValue> defaultValue,
            List<Constraint> constraints) {

        /**
         * Creates an immutable management field declaration.
         *
         * @param key          formal protocol or official Vendor field key
         * @param label        non-blank display label
         * @param type         input presentation type
         * @param required     whether input is required
         * @param defaultValue optional non-sensitive default value
         * @param constraints  ordered validation declarations
         * @throws IllegalArgumentException if required text is blank, a container is {@code null}, or a constraint is
         *                                  {@code null}
         */
        public Field {
            Assert.notBlank(key, "Form field key must not be blank");
            Assert.notBlank(label, "Form field label must not be blank");
            Assert.notNull(type, "Form field type must not be null");
            Assert.notNull(defaultValue, "Form field default container must not be null");
            Assert.notNull(constraints, "Form field constraints must not be null");
            defaultValue = Optional.ofNullable(defaultValue.getOrNull());
            final List<Constraint> copy = new ArrayList<>(constraints.size());
            for (Constraint constraint : constraints) {
                copy.add(Assert.notNull(constraint, "Form field constraint must not be null"));
            }
            constraints = List.copyOf(copy);
        }

    }

    /**
     * Declares one generic management validation and its immutable provider-neutral arguments.
     *
     * @param validator stable Bus Validator identifier understood by the external management implementation
     * @param arguments immutable validator arguments
     * @author Kimi Liu
     */
    public record Constraint(String validator, JsonValue.ObjectValue arguments) {

        /**
         * Creates an immutable validation declaration.
         *
         * @param validator non-blank Validator identifier
         * @param arguments provider-neutral validation arguments
         * @throws IllegalArgumentException if the identifier is blank or arguments are {@code null}
         */
        public Constraint {
            Assert.notBlank(validator, "Form constraint validator must not be blank");
            Assert.notNull(arguments, "Form constraint arguments must not be null");
            arguments = new JsonValue.ObjectValue(arguments.values());
        }

    }

}
