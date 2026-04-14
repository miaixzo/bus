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
 * Dialect implementation for legacy Oracle 9i style pagination.
 *
 * <p>
 * This dialect uses a {@code ROWNUM} wrapping strategy for pagination and maps UPSERT handling to the
 * {@code MERGE_USING_DUAL} family.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Oracle9i extends AbstractDialect {

    /**
     * Creates the Oracle 9i dialect.
     */
    public Oracle9i() {
        super("Oracle9i", "jdbc:oracle:");
    }

    /**
     * Returns the UPSERT family used by Oracle 9i in this framework.
     *
     * @return {@link Dialect.Type#MERGE_USING_DUAL}
     */
    @Override
    public Dialect.Type getUpsertType() {
        return Dialect.Type.MERGE_USING_DUAL;
    }

    /**
     * Returns the keyword used by Oracle 9i to express row limits.
     *
     * @return the keyword {@code ROWNUM}
     */
    @Override
    public String getLimitKeyword() {
        return "ROWNUM";
    }

    /**
     * Builds a count query for Oracle 9i by wrapping the original SQL as a subquery.
     *
     * @param originalSql the original SQL statement
     * @return the generated count SQL
     */
    @Override
    public String buildCountSql(String originalSql) {
        return "SELECT COUNT(*) FROM (" + originalSql + ")";
    }

    /**
     * Builds paginated SQL using a {@code ROWNUM} wrapper query.
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
        long startRow = pageable.getOffset();
        long endRow = startRow + pageable.getPageSize();
        StringBuilder sql = new StringBuilder(originalSql.length() + 200);
        sql.append("SELECT * FROM ( SELECT rownum AS rn__, inner__.* FROM ( ");
        sql.append(originalSql);
        sql.append(" ) inner__ WHERE rownum <= ").append(endRow);
        sql.append(" ) WHERE rn__ > ").append(startRow);
        return sql.toString();
    }

}
