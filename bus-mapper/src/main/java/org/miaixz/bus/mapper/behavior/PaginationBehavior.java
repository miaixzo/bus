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
package org.miaixz.bus.mapper.behavior;

import org.miaixz.bus.mapper.support.paging.Pageable;

/**
 * Pagination behavior exposed by a database dialect.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface PaginationBehavior {

    /**
     * Gets the SQL keyword for limiting results.
     *
     * @return the LIMIT keyword
     */
    default String getLimitKeyword() {
        return "LIMIT";
    }

    /**
     * Gets the SQL keyword for offset.
     *
     * @return the OFFSET keyword
     */
    default String getOffsetKeyword() {
        return "OFFSET";
    }

    /**
     * Builds count SQL for the specified query.
     *
     * @param originalSql the original SQL query
     * @return the count SQL query
     */
    String buildCountSql(String originalSql);

    /**
     * Builds pagination SQL for the specified query.
     *
     * @param originalSql the original SQL query
     * @param pageable    the pagination information
     * @return the paginated SQL query
     */
    String buildPaginationSql(String originalSql, Pageable pageable);

}
