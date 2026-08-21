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
import java.util.Set;

import org.miaixz.bus.auth.protocol.Conformance;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Declares the immutable identity, protocol, capabilities, conformance and management shape of one authentication or
 * authorization scheme.
 * <p>
 * A scheme is descriptive only. It does not register itself, decode persistence data, resolve credentials, compile a
 * worker, invoke protocol operations, or perform security and audit processing.
 * </p>
 *
 * @param <O> exact immutable configuration value accepted by this scheme
 * @author Kimi Liu
 */
public interface Scheme<O extends Options<?>> {

    /**
     * Returns the stable identifier used by Source type lookup.
     *
     * @return non-blank stable scheme identifier
     */
    String id();

    /**
     * Returns the exact protocol classification represented by this scheme.
     *
     * @return protocol classification
     */
    Protocol protocol();

    /**
     * Returns every protocol accepted by this scheme.
     * <p>
     * Ordinary schemes accept only their primary protocol. Aggregate schemes may override this method while retaining
     * {@link #protocol()} as their management classification.
     * </p>
     *
     * @return immutable non-empty accepted protocol set
     */
    default Set<Protocol> protocols() {
        return Set.of(protocol());
    }

    /**
     * Returns only the capabilities implemented by the matching runtime.
     *
     * @return immutable capability manifest
     */
    Capability.Manifest manifest();

    /**
     * Returns the formal standard basis implemented by this scheme.
     *
     * @return conformance declaration, or empty when no formal standard applies
     */
    Optional<Conformance> conformance();

    /**
     * Returns the external management form describing this scheme's options.
     *
     * @return immutable management form
     */
    Form form();

    /**
     * Returns optional non-sensitive default options.
     *
     * @return defaults, or empty when deployment input is required
     */
    Optional<O> defaults();

    /**
     * Describes immutable management input fields for a scheme without rendering, binding, decoding or executing them.
     *
     * @param sections ordered management form sections
     * @author Kimi Liu
     */
    record Form(List<Section> sections) {

        /**
         * Creates an immutable management form.
         *
         * @param sections ordered form sections
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
         * Enumerates management presentation types without redefining protocol data types.
         *
         * @author Kimi Liu
         */
        public enum Type {

            /** Single-line textual input. */
            TEXT,
            /** Secret input that must remain redacted. */
            SECRET,
            /** Absolute or relative URL input. */
            URL,
            /** Boolean toggle input. */
            BOOLEAN,
            /** Numeric input. */
            NUMBER,
            /** Single-choice selection input. */
            SELECT,
            /** Multiple-choice selection input. */
            MULTI_SELECT

        }

        /**
         * Groups related management fields.
         *
         * @param key    stable section key
         * @param title  section title
         * @param fields ordered fields
         * @author Kimi Liu
         */
        public record Section(String key, String title, List<Field> fields) {

            /** Validates and freezes one management form section. */
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
         * @param key          formal protocol or Vendor field key
         * @param label        display label
         * @param type         presentation type
         * @param required     whether input is required
         * @param defaultValue optional non-sensitive default value
         * @param constraints  ordered validation declarations
         * @author Kimi Liu
         */
        public record Field(String key, String label, Type type, boolean required, Optional<JsonValue> defaultValue,
                List<Constraint> constraints) {

            /** Validates and freezes one management input description. */
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
         * Declares one external management validation and its immutable provider-neutral arguments.
         *
         * @param validator stable external validator identifier
         * @param arguments immutable validator arguments
         * @author Kimi Liu
         */
        public record Constraint(String validator, JsonValue.ObjectValue arguments) {

            /** Validates and detaches one management field constraint. */
            public Constraint {
                Assert.notBlank(validator, "Form constraint validator must not be blank");
                Assert.notNull(arguments, "Form constraint arguments must not be null");
                arguments = new JsonValue.ObjectValue(arguments.values());
            }

        }

    }

}
