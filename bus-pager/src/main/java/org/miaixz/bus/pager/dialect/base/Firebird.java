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
package org.miaixz.bus.pager.dialect.base;

import java.util.Map;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.pager.Page;
import org.miaixz.bus.pager.dialect.AbstractPaging;

/**
 * Database dialect for Firebird SQL. This class provides Firebird-specific implementations for pagination SQL
 * generation and parameter processing.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Firebird extends AbstractPaging {

    /**
     * Processes the pagination parameters for Firebird. It adds {@code PAGEPARAMETER_FIRST} (offset) and
     * {@code PAGEPARAMETER_SECOND} (limit) to the parameter map and updates the {@link CacheKey}.
     *
     * @param ms       the MappedStatement object
     * @param paramMap a map containing the query parameters
     * @param page     the {@link Page} object containing pagination details
     * @param boundSql the BoundSql object for the query
     * @param pageKey  the CacheKey for the paginated query
     * @return the processed parameter map
     */
    @Override
    public Object processPageParameter(
            MappedStatement ms,
            Map<String, Object> paramMap,
            Page page,
            BoundSql boundSql,
            CacheKey pageKey) {
        paramMap.put(PAGEPARAMETER_FIRST, page.getStartRow());
        paramMap.put(PAGEPARAMETER_SECOND, page.getPageSize());
        // Process pageKey
        pageKey.update(page.getStartRow());
        pageKey.update(page.getPageSize());
        // Process parameter configuration
        handleParameter(boundSql, ms, long.class, int.class);
        return paramMap;
    }

    /**
     * Generates the Firebird-specific pagination SQL. It appends {@code OFFSET ? ROWS FETCH NEXT ? ROWS ONLY} to the
     * original SQL.
     *
     * @param sql     the original SQL string
     * @param page    the {@link Page} object containing pagination details
     * @param pageKey the CacheKey for the paginated query
     * @return the Firebird-specific paginated SQL string
     */
    @Override
    public String getPageSql(String sql, Page page, CacheKey pageKey) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + Normal._64);
        sqlBuilder.append(sql);
        sqlBuilder.append("\n OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");
        pageKey.update(page.getPageSize());
        return sqlBuilder.toString();
    }

}
