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

import java.util.List;
import java.util.Map;

import jakarta.persistence.Transient;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.cortex.Setting;
import org.miaixz.bus.cortex.Type;

/**
 * Full current-state setting item model.
 * <p>
 * This resource owns the durable item coordinates, source metadata, content, gray rule, and runtime application/profile
 * bindings directly. It is no longer a thin runtime patch on top of {@link Setting}; instead it is the primary resource
 * model for current-state setting entries.
 * </p>
 *
 * @author Kimi Liu
 */
@Setter
@Getter
@SuperBuilder
public class Item extends Setting {

    /**
     * Setting data identifier within the group.
     */
    private String data_id;

    /**
     * Setting group name within the space.
     */
    private String group;

    /**
     * Source type used to resolve the effective value.
     */
    private String source;

    /**
     * Source-specific descriptor such as an environment variable name or external resource address.
     */
    private String spec;

    /**
     * Optional gray-release rule.
     */
    private String rule;

    /**
     * Logical content format.
     */
    private String format;

    /**
     * Current logical setting value.
     */
    private String content;

    /**
     * Delivery exposure policy.
     */
    private String exposure;

    /**
     * Monotonic revision number for the current {@code setting.item} state.
     * <p>
     * Kept as {@code version} for source and storage compatibility while new code uses {@link #getRevision()} and
     * {@link #setRevision(String)}.
     * </p>
     */
    private String version;

    /**
     * Content checksum used for idempotent publish and diff calculation.
     */
    private String checksum;

    /**
     * Encryption flag of the stored content, where {@code 1} means encrypted and {@code 0} means plain text.
     */
    private Integer encrypted;

    /**
     * Optional logical labels.
     */
    @Transient
    private Map<String, String> labels;

    /**
     * Aggregated application bindings loaded from {@code setting_item_binding}.
     */
    @Transient
    private List<String> app_ids;

    /**
     * Aggregated profile bindings loaded from {@code setting_item_binding}.
     */
    @Transient
    private List<String> profile_ids;

    /**
     * Structured extension attributes for adapters that need richer integration parameters.
     */
    @Transient
    private Map<String, Object> extension;

    /**
     * Creates an empty current-state setting item.
     */
    public Item() {
        super();
        setType(Type.ITEM.key());
    }

    /**
     * Returns the current item revision number.
     *
     * @return item revision number
     */
    public String getRevision() {
        return version;
    }

    /**
     * Assigns the current item revision number.
     *
     * @param revision item revision number
     */
    public void setRevision(String revision) {
        this.version = revision;
    }

    /**
     * Returns the current item revision number using storage-oriented naming.
     *
     * @return item revision number
     */
    public String getRevisionNo() {
        return version;
    }

    /**
     * Assigns the current item revision number using storage-oriented naming.
     *
     * @param revisionNo item revision number
     */
    public void setRevisionNo(String revisionNo) {
        this.version = revisionNo;
    }

    /**
     * Role of an item in source and resolved configuration projections.
     */
    public enum Kind {
        /**
         * The item owns editable source content.
         */
        SOURCE,
        /**
         * The item is a resolved projection derived from source content and references.
         */
        EFFECTIVE
    }

    /**
     * Editing model used to mutate an item's content.
     */
    public enum Editor {
        /**
         * Content is edited as one complete source document.
         */
        SOURCE,
        /**
         * Content is edited as structured key-value entries.
         */
        KEY_VALUE
    }

    /**
     * User-facing lifecycle projection of an item.
     */
    public enum State {
        /**
         * The item has no stable published revision.
         */
        UNPUBLISHED,
        /**
         * The editable content matches the stable published revision.
         */
        PUBLISHED,
        /**
         * The editable content differs from the stable published revision.
         */
        CHANGED,
        /**
         * The item currently participates in a gray release.
         */
        GRAY,
        /**
         * The item has been archived.
         */
        ARCHIVED
    }

    /**
     * Mutation applied to one protected secret path.
     */
    public enum SecretAction {
        /**
         * Preserve the currently stored secret value.
         */
        KEEP,
        /**
         * Replace the stored secret with a newly supplied value.
         */
        REPLACE,
        /**
         * Remove the secret value from the content.
         */
        DELETE
    }

    /**
     * Depth of content validation requested by a caller.
     */
    public enum ValidationLevel {
        /**
         * Validate only syntax and basic parsing constraints.
         */
        SYNTAX,
        /**
         * Run syntax, structure, policy, and semantic validation.
         */
        FULL
    }

    /**
     * Severity assigned to a validation finding.
     */
    public enum ValidationSeverity {
        /**
         * The finding blocks the requested operation.
         */
        ERROR,
        /**
         * The finding is risky but does not necessarily block the operation.
         */
        WARNING,
        /**
         * The finding is informational only.
         */
        INFO
    }

    /**
     * Risk classification derived for a configuration change.
     */
    public enum Risk {
        /**
         * The change has low expected operational impact.
         */
        LOW,
        /**
         * The change has moderate expected operational impact.
         */
        MEDIUM,
        /**
         * The change has high expected operational impact and requires stronger review.
         */
        HIGH
    }

    /**
     * Content projection selected when reading or exporting an item.
     */
    public enum View {
        /**
         * Read the active stable revision.
         */
        STABLE,
        /**
         * Read the active gray revision.
         */
        GRAY,
        /**
         * Read the current editable workspace.
         */
        EDITING,
        /**
         * Read the governed effective value after resolving references and overrides.
         */
        EFFECTIVE
    }

}
