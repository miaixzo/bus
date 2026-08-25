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
package org.miaixz.bus.core.basic.entity;

import java.io.Serial;

import jakarta.persistence.Column;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.lang.annotation.Logical;

/**
 * Base entity carrying logical status and audit metadata.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Audit extends Entity {

    @Serial
    private static final long serialVersionUID = 2852290719630L;

    /**
     * Logical data status:
     * <ul>
     * <li>-1: logically deleted</li>
     * <li>0: invalid or disabled</li>
     * <li>1: normal or active</li>
     * </ul>
     */
    @Logical
    @Column(nullable = false)
    protected Integer status;

    /**
     * Identifier of the user who created the entity.
     */
    @Column(length = 24, nullable = false)
    protected String creator;

    /**
     * Entity creation timestamp in milliseconds.
     */
    @Column(nullable = false)
    protected Long created;

    /**
     * Identifier of the user who last modified the entity.
     */
    @Column(length = 24, nullable = false)
    protected String modifier;

    /**
     * Entity modification timestamp in milliseconds.
     */
    @Column(nullable = false)
    protected Long modified;

    /**
     * Constructs a new {@code Audit} instance.
     */
    public Audit() {
        // No initialization required.
    }

}
