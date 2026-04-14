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
package org.miaixz.bus.mapper.dialect;

import org.miaixz.bus.mapper.support.paging.Pageable;

/**
 * Dialect implementation for PostgreSQL databases.
 *
 * <p>
 * This dialect uses standard {@code LIMIT/OFFSET} pagination and PostgreSQL {@code ON CONFLICT} UPSERT semantics.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PostgreSql extends AbstractDialect {

    /**
     * Creates the PostgreSQL dialect.
     */
    public PostgreSql() {
        super("PostgreSQL", "jdbc:postgresql:");
    }

    /**
     * Returns the UPSERT family used by PostgreSQL.
     *
     * @return {@link Dialect.Type#INSERT_ON_CONFLICT}
     */
    @Override
    public Dialect.Type getUpsertType() {
        return Dialect.Type.INSERT_ON_CONFLICT;
    }

    /**
     * Builds paginated SQL using PostgreSQL {@code LIMIT/OFFSET} syntax.
     *
     * @param originalSql the original SQL statement
     * @param pageable    the requested pagination information
     * @return the paginated SQL statement
     */
    @Override
    public String buildPaginationSql(String originalSql, Pageable pageable) {
        return buildPaginatedSql(originalSql, pageable);
    }

}
