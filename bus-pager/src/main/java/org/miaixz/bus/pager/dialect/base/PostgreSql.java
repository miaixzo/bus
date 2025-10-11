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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.miaixz.bus.pager.Page;
import org.miaixz.bus.pager.binding.MetaObject;
import org.miaixz.bus.pager.dialect.AbstractPaging;

/**
 * Database dialect for PostgreSQL. This class provides PostgreSQL-specific implementations for pagination SQL
 * generation and parameter processing.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PostgreSql extends AbstractPaging {

    /**
     * Processes the pagination parameters for PostgreSQL. It adds {@code PAGEPARAMETER_SECOND} (limit) and
     * {@code PAGEPARAMETER_FIRST} (offset) to the parameter map and updates the {@link CacheKey}. It also modifies the
     * {@link BoundSql}'s parameter mappings to include these pagination parameters.
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
        paramMap.put(PAGEPARAMETER_SECOND, page.getPageSize());
        paramMap.put(PAGEPARAMETER_FIRST, page.getStartRow());
        // Process pageKey
        pageKey.update(page.getPageSize());
        pageKey.update(page.getStartRow());
        // Process parameter configuration
        if (boundSql.getParameterMappings() != null) {
            List<ParameterMapping> newParameterMappings = new ArrayList<>(boundSql.getParameterMappings());
            if (page.getStartRow() == 0) {
                newParameterMappings.add(
                        new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_SECOND, int.class).build());
            } else {
                newParameterMappings.add(
                        new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_SECOND, int.class).build());
                newParameterMappings.add(
                        new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_FIRST, long.class).build());
            }
            org.apache.ibatis.reflection.MetaObject metaObject = MetaObject.forObject(boundSql);
            metaObject.setValue("parameterMappings", newParameterMappings);
        }
        return paramMap;
    }

    /**
     * Generates the PostgreSQL-specific pagination SQL. It appends {@code LIMIT ?} or {@code LIMIT ? OFFSET ?} to the
     * original SQL.
     *
     * @param sql     the original SQL string
     * @param page    the {@link Page} object containing pagination details
     * @param pageKey the CacheKey for the paginated query
     * @return the PostgreSQL-specific paginated SQL string
     * @see <a href="https://www.postgresql.org/docs/current/queries-limit.html">PostgreSQL LIMIT Clause</a>
     */
    @Override
    public String getPageSql(String sql, Page page, CacheKey pageKey) {
        StringBuilder sqlStr = new StringBuilder(sql.length() + 17);
        sqlStr.append(sql);
        if (page.getStartRow() == 0) {
            sqlStr.append(" LIMIT ?");
        } else {
            sqlStr.append(" LIMIT ? OFFSET ?");
        }
        return sqlStr.toString();
    }

}
