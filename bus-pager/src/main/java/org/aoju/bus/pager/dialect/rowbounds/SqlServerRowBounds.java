/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org mybatis.io and other contributors.           *
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
package org.aoju.bus.pager.dialect.rowbounds;

import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.pager.Property;
import org.aoju.bus.pager.dialect.AbstractRowBounds;
import org.aoju.bus.pager.dialect.ReplaceSql;
import org.aoju.bus.pager.dialect.replace.RegexWithNolock;
import org.aoju.bus.pager.dialect.replace.SimpleWithNolock;
import org.aoju.bus.pager.parser.SqlServerParser;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

/**
 * sqlserver 基于 RowBounds 的分页
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SqlServerRowBounds extends AbstractRowBounds {

    protected SqlServerParser pageSql = new SqlServerParser();
    protected ReplaceSql replaceSql;

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey countKey) {
        String sql = boundSql.getSql();
        sql = replaceSql.replace(sql);
        sql = countSqlParser.getSmartCountSql(sql);
        sql = replaceSql.restore(sql);
        return sql;
    }

    @Override
    public String getPageSql(String sql, RowBounds rowBounds, CacheKey pageKey) {
        // 处理pageKey
        pageKey.update(rowBounds.getOffset());
        pageKey.update(rowBounds.getLimit());
        sql = replaceSql.replace(sql);
        sql = pageSql.convertToPageSql(sql, null, null);
        sql = replaceSql.restore(sql);
        sql = sql.replace(String.valueOf(Long.MIN_VALUE), String.valueOf(rowBounds.getOffset()));
        sql = sql.replace(String.valueOf(Long.MAX_VALUE), String.valueOf(rowBounds.getLimit()));
        return sql;
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        String replaceSql = properties.getProperty("replaceSql");
        if (StringKit.isEmpty(replaceSql) || "simple".equalsIgnoreCase(replaceSql)) {
            this.replaceSql = new SimpleWithNolock();
        } else if ("regex".equalsIgnoreCase(replaceSql)) {
            this.replaceSql = new RegexWithNolock();
        } else {
            try {
                this.replaceSql = (ReplaceSql) Class.forName(replaceSql).getConstructor().newInstance();
                if (this.replaceSql instanceof Property) {
                    ((Property) this.replaceSql).setProperties(properties);
                }
            } catch (Exception e) {
                throw new RuntimeException("replaceSql 参数配置的值不符合要求，可选值为 simple 和 regex，或者是实现了 "
                        + ReplaceSql.class.getName() + " 接口的全限定类名", e);
            }
        }
    }

}
