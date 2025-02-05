/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mybatis.io and other contributors.         ~
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
package org.miaixz.bus.pager.dialect.rowbounds;

import java.util.Properties;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pager.Builder;
import org.miaixz.bus.pager.dialect.AbstractRowBounds;
import org.miaixz.bus.pager.dialect.ReplaceSql;
import org.miaixz.bus.pager.dialect.replace.RegexWithNolock;
import org.miaixz.bus.pager.dialect.replace.SimpleWithNolock;
import org.miaixz.bus.pager.parser.SqlServerSqlParser;
import org.miaixz.bus.pager.parser.defaults.DefaultSqlServerSqlParser;

/**
 * sqlserver 基于 RowBounds 的分页
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SqlServerRowBounds extends AbstractRowBounds {

    protected SqlServerSqlParser sqlServerSqlParser;
    protected ReplaceSql replaceSql;

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds,
            CacheKey countKey) {
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
        sql = sqlServerSqlParser.convertToPageSql(sql, null, null);
        sql = replaceSql.restore(sql);
        sql = sql.replace(String.valueOf(Long.MIN_VALUE), String.valueOf(rowBounds.getOffset()));
        sql = sql.replace(String.valueOf(Long.MAX_VALUE), String.valueOf(rowBounds.getLimit()));
        return sql;
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        this.sqlServerSqlParser = Builder.newInstance(properties.getProperty("sqlServerSqlParser"),
                SqlServerSqlParser.class, properties, DefaultSqlServerSqlParser::new);
        String replaceSql = properties.getProperty("replaceSql");
        if (StringKit.isEmpty(replaceSql) || "simple".equalsIgnoreCase(replaceSql)) {
            this.replaceSql = new SimpleWithNolock();
        } else if ("regex".equalsIgnoreCase(replaceSql)) {
            this.replaceSql = new RegexWithNolock();
        } else {
            this.replaceSql = Builder.newInstance(replaceSql, properties);
        }
    }

}
