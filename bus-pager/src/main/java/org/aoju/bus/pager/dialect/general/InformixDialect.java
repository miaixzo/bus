/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.pager.dialect.general;

import org.aoju.bus.pager.Page;
import org.aoju.bus.pager.dialect.AbstractHelperDialect;
import org.aoju.bus.pager.reflect.MetaObject;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据库方言 informix
 *
 * @author Kimi Liu
 * @version 6.1.2
 * @since JDK 1.8+
 */
public class InformixDialect extends AbstractHelperDialect {

    @Override
    public Object processPageParameter(MappedStatement ms, Map<String, Object> paramMap, Page page, BoundSql boundSql, CacheKey pageKey) {
        paramMap.put(PAGEPARAMETER_FIRST, page.getStartRow());
        paramMap.put(PAGEPARAMETER_SECOND, page.getPageSize());
        //处理pageKey
        pageKey.update(page.getStartRow());
        pageKey.update(page.getPageSize());
        //处理参数配置
        if (boundSql.getParameterMappings() != null) {
            List<ParameterMapping> newParameterMappings = new ArrayList<>();
            if (page.getStartRow() > 0) {
                newParameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_FIRST, Integer.class).build());
            }
            if (page.getPageSize() > 0) {
                newParameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_SECOND, Integer.class).build());
            }
            newParameterMappings.addAll(boundSql.getParameterMappings());
            org.apache.ibatis.reflection.MetaObject metaObject = MetaObject.forObject(boundSql);
            metaObject.setValue("parameterMappings", newParameterMappings);
        }
        return paramMap;
    }

    @Override
    public String getPageSql(String sql, Page page, CacheKey pageKey) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 40);
        sqlBuilder.append("SELECT ");
        if (page.getStartRow() > 0) {
            sqlBuilder.append(" SKIP ? ");
        }
        if (page.getPageSize() > 0) {
            sqlBuilder.append(" FIRST ? ");
        }
        sqlBuilder.append(" * FROM ( ");
        sqlBuilder.append(sql);
        sqlBuilder.append(" ) TEMP_T ");
        return sqlBuilder.toString();
    }

}
