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

import org.miaixz.bus.core.lang.annotation.Logical;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Base class for entities with lifecycle management capabilities.
 * <p>
 * Provides standard audit fields (creator, modifier, timestamps) and a logical status field to track the data's
 * existence and validity.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class Lifecycle extends Entity {

    @Serial
    private static final long serialVersionUID = 2852290719630L;

    /**
     * Constructs an empty lifecycle entity.
     */
    public Lifecycle() {

    }

    /**
     * Data status:
     * <ul>
     * <li>-1: Deleted (Logical deletion)</li>
     * <li>0: Invalid / Disabled</li>
     * <li>1: Normal / Active</li>
     * </ul>
     */
    @Logical
    protected Integer status;

    /**
     * The identifier of the user who created this entity.
     */
    protected String creator;

    /**
     * The creation timestamp (milliseconds) of the entity.
     */
    protected Long created;

    /**
     * The identifier of the user who last modified this entity.
     */
    protected String modifier;

    /**
     * The last modification timestamp (milliseconds) of the entity.
     */
    protected Long modified;

}
