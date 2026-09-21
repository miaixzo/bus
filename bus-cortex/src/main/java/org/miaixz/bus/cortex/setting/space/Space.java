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
package org.miaixz.bus.cortex.setting.space;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.cortex.Type;

/**
 * Shared logical-space entity persisted in the {@code space} table.
 * <p>
 * Workspace and namespace records share this entity and are distinguished by the integer {@link #variant} code.
 * Visibility is stored independently as the integer {@link #visibility} code. Enum values define the stable code set,
 * but persistent fields deliberately remain {@link Integer} values for database and mapper compatibility.
 * </p>
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
@Table(name = "space", uniqueConstraints = @UniqueConstraint(name = "uk_space_tenant_variant_code", columnNames = {
        "tenant_id", "variant",
        "code" }), indexes = @Index(name = "idx_space_tenant_variant_status_modified_id", columnList = "tenant_id, variant, status, modified, id"))
public class Space extends Tenant {

    /**
     * Stable business code unique within a tenant and space variant.
     */
    @Column(length = 128)
    private String code;

    /**
     * Human-readable display name.
     */
    @Column(length = 256)
    private String name;

    /**
     * Stable {@link EnumValue.Variant} code identifying whether this record is a workspace or namespace.
     */
    @Column(nullable = false)
    private Integer variant;

    /**
     * Stable {@link EnumValue.Visibility} code defining the discovery and access boundary.
     */
    @Column(nullable = false)
    private Integer visibility;

    /**
     * Human-readable description.
     */
    @Column(length = 2000)
    private String description;

    /**
     * Creates an empty logical-space entity.
     */
    public Space() {
        // No initialization required; callers must choose explicit variant and visibility codes.
    }

    /**
     * Returns the fixed Cortex resource type without persisting a redundant type column.
     *
     * @return stable Cortex space type key
     */
    @Transient
    public Integer getType() {
        return Type.SPACE.key();
    }

    /**
     * Accepts the fixed Cortex resource type for bean and adapter compatibility.
     *
     * @param type supplied Cortex type key
     * @throws IllegalArgumentException when a non-space type is supplied
     */
    @Transient
    public void setType(Integer type) {
        if (type != null && type.intValue() != Type.SPACE.key()) {
            throw new IllegalArgumentException("Unsupported type for Space: " + type);
        }
    }

    /**
     * Returns whether this entity uses the supplied logical variant.
     *
     * @param candidate candidate variant
     * @return {@code true} when the persisted code matches
     */
    public boolean isVariant(EnumValue.Variant candidate) {
        return candidate != null && variant != null && variant.intValue() == candidate.getCode();
    }

    /**
     * Returns whether this entity uses the supplied visibility boundary.
     *
     * @param candidate candidate visibility
     * @return {@code true} when the persisted code matches
     */
    public boolean isVisibility(EnumValue.Visibility candidate) {
        return candidate != null && visibility != null && visibility.intValue() == candidate.getCode();
    }

}
