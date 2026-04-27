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
import org.miaixz.bus.cortex.Setting;
import org.miaixz.bus.cortex.Type;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.Transient;

/**
 * Full current-state setting item model.
 * <p>
 * This resource owns the durable item coordinates, source metadata, content, gray rule, and runtime application/profile
 * bindings directly. It is no longer a thin runtime patch on top of {@link Setting}; instead it is the primary resource
 * model for current-state setting entries.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
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
     * Setting group name within the namespace.
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

}
