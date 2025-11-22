/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.dialect;

import org.miaixz.bus.mapper.support.paging.Pageable;

/**
 * IBM Informix dialect.
 *
 * <p>
 * Supports:
 * </p>
 * <ul>
 * <li>FIRST...SKIP... pagination</li>
 * <li>Multi-Values INSERT (Informix 11.5+)</li>
 * <li>MERGE (UPSERT, Informix 12.1+)</li>
 * <li>JDBC batch operations</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Informix extends AbstractDialect {

    public Informix() {
        super("Informix", "jdbc:informix-sqli:");
    }

    @Override
    public boolean supportsProduct(String productName) {
        if (productName == null) {
            return false;
        }
        String lower = productName.toLowerCase();
        return lower.contains("informix");
    }

    @Override
    public boolean supportsUrl(String jdbcUrl) {
        if (jdbcUrl == null) {
            return false;
        }
        String lower = jdbcUrl.toLowerCase();
        return lower.startsWith("jdbc:informix-sqli:") || lower.startsWith("jdbc:ids:");
    }

    @Override
    public String getPaginationSql(String originalSql, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return originalSql;
        }

        StringBuilder sql = new StringBuilder(originalSql.length() + 100);

        // Informix uses SKIP...FIRST... syntax
        // SELECT SKIP n FIRST m * FROM table
        int selectIndex = originalSql.toUpperCase().indexOf("SELECT");
        if (selectIndex >= 0) {
            sql.append(originalSql, 0, selectIndex + 6); // Include "SELECT"

            if (pageable.getOffset() > 0) {
                sql.append(" SKIP ").append(pageable.getOffset());
            }
            sql.append(" FIRST ").append(pageable.getPageSize());

            sql.append(originalSql.substring(selectIndex + 6));
        } else {
            sql.append(originalSql);
        }

        return sql.toString();
    }

    @Override
    public boolean supportsMultiValuesInsert() {
        return true; // Supported since Informix 11.5
    }

    @Override
    public boolean supportsUpsert() {
        return true; // MERGE supported since Informix 12.1
    }

    @Override
    public String getUpsertTemplate() {
        return "MERGE INTO %s AS target USING (VALUES %s) AS source (%s) ON %s WHEN MATCHED THEN UPDATE SET %s WHEN NOT MATCHED THEN INSERT (%s) VALUES (%s)";
    }

    @Override
    public String getLimitKeyword() {
        return "FIRST";
    }

    @Override
    public String getOffsetKeyword() {
        return "SKIP";
    }

}
