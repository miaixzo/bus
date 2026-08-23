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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Aggregates the immutable configuration, protocol, capability, conformance, and management descriptions of one
 * authentication or authorization scheme.
 * <p>
 * A scheme is descriptive only. It does not register itself, decode persistence data, resolve credentials, compile a
 * worker, invoke protocol operations, or perform security and audit processing.
 * </p>
 *
 * @param <O> exact immutable configuration value accepted by this scheme
 * @author Kimi Liu
 */
public interface Scheme<O extends Scheme.Options<?>> {

    /**
     * Returns the stable identifier used by Source type lookup.
     *
     * @return non-blank stable scheme identifier
     */
    String id();

    /**
     * Returns the management presentation metadata shared by every selection described by this scheme.
     *
     * @return immutable scheme metadata
     */
    Metadata metadata();

    /**
     * Returns every protocol accepted by this scheme.
     *
     * @return immutable non-empty accepted protocol set
     */
    Set<Protocol> protocols();

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
     * Marks an immutable decoded configuration value accepted by one {@link Scheme}.
     * <p>
     * An options value contains deployment input and external credential references only. It does not describe
     * management presentation, select a protocol, decode persistence JSON, compile workers, or execute authentication
     * operations.
     * </p>
     * <p>
     * Persistence and transport serialization belong to the integrating project. This runtime contract deliberately
     * does not require Java object serialization.
     * </p>
     *
     * @param <O> exact immutable implementation type
     * @author Kimi Liu
     */
    interface Options<O extends Options<O>> {

        /**
         * Returns this immutable configuration implementation type.
         *
         * @return exact options implementation class
         */
        Class<O> type();

        /**
         * Returns an immutable detached value safe to retain in one compiled runtime container.
         * <p>
         * Immutable value implementations may return {@code this}. Mutable project implementations must return a
         * detached immutable copy. The framework does not serialize deployment options to manufacture a snapshot.
         * </p>
         *
         * @return non-null immutable options snapshot
         */
        O snapshot();

    }

    /**
     * Declares the exact formal standard version, profile, and normative citations implemented by one protocol profile.
     * <p>
     * This descriptive metadata is not protocol discovery or wire content and does not repeat capability declarations.
     * Vendor-specific behavior without a formal standard must use an explicit vendor-deviation declaration rather than
     * fabricate conformance metadata.
     * </p>
     *
     * @param protocol  existing Bus protocol identifier
     * @param version   formal protocol version
     * @param citations non-empty set of normative standard locations
     * @param profile   exact formal standard profile name
     * @author Kimi Liu
     */
    record Conformance(Protocol protocol, Version version, Set<Citation> citations, String profile) {

        /**
         * Validates and freezes formal conformance metadata.
         *
         * @throws IllegalArgumentException if a component or citation is {@code null} or text is blank
         * @throws ValidateException        if no normative citation is supplied
         */
        public Conformance {
            Assert.notNull(protocol, "Conformance protocol must not be null");
            Assert.notNull(version, "Conformance version must not be null");
            Assert.notNull(citations, "Conformance citations must not be null");
            citations = Set.copyOf(citations);
            if (citations.isEmpty()) {
                throw new ValidateException("Conformance must cite at least one formal standard section");
            }
            for (Citation citation : citations) {
                Assert.notNull(citation, "Conformance citation must not be null");
            }
            Assert.notBlank(profile, "Conformance standard profile must not be blank");
        }

        /**
         * Identifies one normative section of a formal specification without copying specification content into
         * runtime.
         *
         * @param standardUrl absolute stable HTTP(S) specification URL without query or fragment
         * @param section     exact normative section identifier
         * @author Kimi Liu
         */
        public record Citation(String standardUrl, String section) {

            /**
             * Validates the stable formal-standard location and section identifier.
             *
             * @throws IllegalArgumentException if text is blank
             * @throws ValidateException        if the URL is not an absolute credential-free HTTP(S) specification URL
             */
            public Citation {
                Assert.notBlank(standardUrl, "Conformance standard URL must not be blank");
                Assert.notBlank(section, "Conformance standard section must not be blank");
                try {
                    final URI uri = new URI(standardUrl);
                    if (!uri.isAbsolute() || uri.getHost() == null
                            || !(Protocol.HTTP.name.equalsIgnoreCase(uri.getScheme())
                                    || Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()))
                            || uri.getRawUserInfo() != null || uri.getRawQuery() != null
                            || uri.getRawFragment() != null) {
                        throw new ValidateException(
                                "Conformance standard URL must be an absolute HTTP(S) URL without credentials, query, or fragment");
                    }
                } catch (URISyntaxException cause) {
                    throw new ValidateException("Conformance standard URL is malformed", cause);
                }
            }

        }

    }

    /**
     * Carries implementation-neutral management presentation data for one scheme or exact Source selection.
     *
     * @param name        non-blank display name
     * @param description non-null human-readable description, which may be empty
     * @param icon        non-null stable icon reference, which may be empty
     * @author Kimi Liu
     */
    record Metadata(String name, String description, String icon) {

        /**
         * Validates one immutable presentation metadata value without interpreting its icon reference.
         */
        public Metadata {
            Assert.notBlank(name, "Scheme metadata name must not be blank");
            Assert.notNull(description, "Scheme metadata description must not be null");
            Assert.notNull(icon, "Scheme metadata icon must not be null");
        }

    }

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

            /**
             * Single-line textual input.
             */
            TEXT,
            /**
             * Secret input that must remain redacted.
             */
            SECRET,
            /**
             * Absolute or relative URL input.
             */
            URL,
            /**
             * Boolean toggle input.
             */
            BOOLEAN,
            /**
             * Numeric input.
             */
            NUMBER,
            /**
             * Single-choice selection input.
             */
            SELECT,
            /**
             * Multiple-choice selection input.
             */
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

            /**
             * Validates and freezes one management form section.
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

            /**
             * Validates and freezes one management input description.
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
         * Declares one external management validation and its immutable implementation-neutral arguments.
         *
         * @param validator stable external validator identifier
         * @param arguments immutable validator arguments
         * @author Kimi Liu
         */
        public record Constraint(String validator, JsonValue.ObjectValue arguments) {

            /**
             * Validates and detaches one management field constraint.
             */
            public Constraint {
                Assert.notBlank(validator, "Form constraint validator must not be blank");
                Assert.notNull(arguments, "Form constraint arguments must not be null");
                arguments = new JsonValue.ObjectValue(arguments.values());
            }

        }

    }

}
