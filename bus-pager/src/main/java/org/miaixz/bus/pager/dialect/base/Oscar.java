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
 * Database dialect for Oscar. This class provides Oscar-specific implementations for pagination SQL generation and
 * parameter processing.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Oscar extends AbstractPaging {

    /**
     * Processes the pagination parameters for Oscar. It adds {@code PAGEPARAMETER_FIRST} (limit) and
     * {@code PAGEPARAMETER_SECOND} (offset) to the parameter map and updates the {@link CacheKey}. It also modifies the
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
        paramMap.put(PAGEPARAMETER_FIRST, page.getPageSize());
        paramMap.put(PAGEPARAMETER_SECOND, (int) page.getStartRow());
        // Process pageKey
        pageKey.update(page.getStartRow());
        pageKey.update(page.getPageSize());
        // Process parameter configuration
        if (boundSql.getParameterMappings() != null) {
            List<ParameterMapping> newParameterMappings = new ArrayList<>(boundSql.getParameterMappings());
            if (page.getStartRow() == 0) {
                newParameterMappings.add(
                        new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_FIRST, int.class).build());
            } else {
                newParameterMappings.add(
                        new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_FIRST, int.class).build());
                newParameterMappings.add(
                        new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_SECOND, int.class).build());
            }
            org.apache.ibatis.reflection.MetaObject metaObject = MetaObject.forObject(boundSql);
            metaObject.setValue("parameterMappings", newParameterMappings);
        }
        return paramMap;
    }

    /**
     * Generates the Oscar-specific pagination SQL. It appends {@code LIMIT ?} or {@code LIMIT ? OFFSET ?} to the
     * original SQL based on the start row.
     *
     * @param sql     the original SQL string
     * @param page    the {@link Page} object containing pagination details
     * @param pageKey the CacheKey for the paginated query
     * @return the Oscar-specific paginated SQL string
     */
    @Override
    public String getPageSql(String sql, Page page, CacheKey pageKey) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 14);
        sqlBuilder.append(sql);
        if (page.getStartRow() == 0) {
            sqlBuilder.append("\n LIMIT ? ");
        } else {
            sqlBuilder.append("\n LIMIT ? OFFSET ? ");
        }
        return sqlBuilder.toString();
    }

}
