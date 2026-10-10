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
package org.miaixz.bus.cortex.setting.revision;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;

/**
 * Immutable published snapshot of a setting item.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Revision extends Tenant {

    /**
     * Owning item identifier.
     */
    private String item_id;
    /**
     * Rollout that created the revision.
     */
    private String rollout_id;
    /**
     * Source revision used for derivation or rollback.
     */
    private String source_id;
    /**
     * Monotonic item revision number.
     */
    private String revision;
    /**
     * Item edition captured by the snapshot.
     */
    private Long edition;
    /**
     * Item generation captured by the snapshot.
     */
    private Long baseline;
    /**
     * Immutable content.
     */
    private String content;
    /**
     * Immutable content format.
     */
    private String format;
    /**
     * Immutable content source.
     */
    private String source;
    /**
     * Immutable source descriptor.
     */
    private String spec;
    /**
     * Immutable exposure policy.
     */
    private String exposure;
    /**
     * Immutable encryption flag.
     */
    private Integer encrypted;
    /**
     * Immutable labels text.
     */
    private String labels;
    /**
     * Immutable extension text.
     */
    private String extension;
    /**
     * Snapshot checksum.
     */
    private String checksum;
    /**
     * Snapshot operation.
     */
    private String operation;
    /**
     * Binding snapshot stored as text.
     */
    private String bindings;
    /**
     * Reference snapshot stored as text.
     */
    private String reference;
    /**
     * Gray rule snapshot stored as text.
     */
    private String rule;
    /**
     * Editing snapshot stored as text.
     */
    private String editing;
    /**
     * Protected-value snapshot stored as text.
     */
    private String secrets;
    /**
     * Optional snapshot description.
     */
    private String description;

    /**
     * Creates an empty immutable revision.
     */
    public Revision() {
        // No initialization required.
    }

    /**
     * Supported immutable snapshot operations.
     *
     * @author Kimi Liu
     */
    public enum Operation {
        /** Creates or replaces the current item state. */
        UPSERT,
        /** Archives the current item state. */
        DELETE
    }

    /**
     * Supported runtime pointer roles.
     *
     * @author Kimi Liu
     */
    public enum Role {
        /** Stable revision pointer used by ordinary runtime reads. */
        STABLE,
        /** Gray revision pointer used by matched runtime reads. */
        GRAY
    }
}
