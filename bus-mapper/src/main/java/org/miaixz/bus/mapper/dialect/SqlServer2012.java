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
 * Dialect implementation for SQL Server 2012 style pagination.
 *
 * <p>
 * This dialect uses SQL Server {@code OFFSET ... FETCH NEXT} pagination and injects a neutral {@code ORDER BY}
 * expression when required. UPSERT handling is mapped to the {@code MERGE_USING_VALUES} family.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SqlServer2012 extends AbstractDialect {

    /**
     * Creates the SQL Server 2012 dialect.
     */
    public SqlServer2012() {
        super("Microsoft SQL Server", "jdbc:sqlserver:");
    }

    /**
     * Returns the UPSERT family used by SQL Server 2012 in this framework.
     *
     * @return {@link Dialect.Type#MERGE_USING_VALUES}
     */
    @Override
    public Dialect.Type getUpsertType() {
        return Dialect.Type.MERGE_USING_VALUES;
    }

    /**
     * Returns the keyword used by SQL Server 2012 to express row limits.
     *
     * @return the keyword {@code FETCH NEXT}
     */
    @Override
    public String getLimitKeyword() {
        return "FETCH NEXT";
    }

    /**
     * Builds paginated SQL using SQL Server 2012 {@code OFFSET ... FETCH NEXT} syntax.
     *
     * @param originalSql the original SQL statement
     * @param pageable    the requested pagination information
     * @return the paginated SQL statement
     */
    @Override
    public String buildPaginationSql(String originalSql, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return originalSql;
        }
        StringBuilder sql = new StringBuilder(originalSql.length() + 100);
        sql.append(originalSql);
        if (!originalSql.toUpperCase().contains("ORDER BY")) {
            sql.append(" ORDER BY (SELECT NULL)");
        }
        sql.append(" OFFSET ").append(pageable.getOffset()).append(" ROWS");
        sql.append(" FETCH NEXT ").append(pageable.getPageSize()).append(" ROWS ONLY");
        return sql.toString();
    }

}
