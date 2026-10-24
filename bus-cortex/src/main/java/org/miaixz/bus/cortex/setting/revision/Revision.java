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

import jakarta.persistence.Lob;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;

/**
 * Immutable protected definition snapshot belonging to one successful formal publication.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Revision extends Tenant {

    /**
     * Identifier of the formal release that first persisted this snapshot.
     */
    private String release_id;

    /**
     * Source item identifier; deleting the current item does not affect this snapshot.
     */
    private String item_id;

    /**
     * Configuration category captured at publication.
     */
    private Integer type;

    /**
     * Configuration code captured at publication.
     */
    private String code;

    /**
     * Protected definition body frozen for publication.
     */
    @Lob
    private String content;

    /**
     * Frozen delivery and export format; the definition body remains canonical JSON.
     */
    private String format;

    /**
     * Frozen JSON Schema for the complete effective configuration.
     */
    @Lob
    private String schema;

    /**
     * Frozen storage protection flag.
     */
    private Integer encrypted;

    /**
     * Frozen definition rules and protection metadata.
     */
    @Lob
    private String extension;

    /**
     * Checksum of the frozen protected definition.
     */
    private String checksum;

    /**
     * Configuration description captured at publication.
     */
    private String description;

    /**
     * Creates an empty published definition snapshot.
     */
    public Revision() {
        // No initialization required.
    }

}
