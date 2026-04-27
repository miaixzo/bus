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

import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Base class for query entities with pagination and sorting capabilities.
 * <p>
 * Extends {@link Lifecycle} to support filtering by audit fields and lifecycle status, while adding transient fields
 * for request control and data navigation.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Query extends Lifecycle {

    @Serial
    private static final long serialVersionUID = 2852290719650L;

    /**
     * A random string to ensure request uniqueness, preventing replay attacks or duplicate submissions.
     */
    @Transient
    protected String nonce;

    /**
     * Extended search parameters, typically used for dynamic or complex query conditions in JSON format.
     */
    @Transient
    protected String params;

    /**
     * The current page number (1-based) for paginated results.
     */
    @Transient
    protected Integer pageNo;

    /**
     * The number of records per page.
     */
    @Transient
    protected Integer pageSize;

    /**
     * Sorting criteria, typically follows the format "field [asc|desc]" or just the direction.
     */
    @Transient
    protected String orderBy;

}
