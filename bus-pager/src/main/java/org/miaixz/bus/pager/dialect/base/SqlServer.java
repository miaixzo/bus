/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mapper.io and other contributors.         ~
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
import java.util.Properties;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pager.Builder;
import org.miaixz.bus.pager.Page;
import org.miaixz.bus.pager.cache.CacheFactory;
import org.miaixz.bus.pager.dialect.AbstractPaging;
import org.miaixz.bus.pager.dialect.ReplaceSql;
import org.miaixz.bus.pager.dialect.replace.RegexWithNolock;
import org.miaixz.bus.pager.dialect.replace.SimpleWithNolock;
import org.miaixz.bus.pager.parsing.SqlServerSqlParser;
import org.miaixz.bus.pager.parsing.DefaultSqlServerSqlParser;

/**
 * 数据库方言 sqlserver
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SqlServer extends AbstractPaging {

    protected SqlServerSqlParser sqlServerSqlParser;
    protected CacheX<String, String> CACHE_COUNTSQL;
    protected CacheX<String, String> CACHE_PAGESQL;
    protected ReplaceSql replaceSql;

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds,
            CacheKey countKey) {
        String sql = boundSql.getSql();
        String cacheSql = CACHE_COUNTSQL.read(sql);
        if (cacheSql != null) {
            return cacheSql;
        } else {
            cacheSql = sql;
        }
        cacheSql = replaceSql.replace(cacheSql);
        cacheSql = countSqlParser.getSmartCountSql(cacheSql);
        cacheSql = replaceSql.restore(cacheSql);
        CACHE_COUNTSQL.write(sql, cacheSql, 60);
        return cacheSql;
    }

    @Override
    public Object processPageParameter(MappedStatement ms, Map<String, Object> paramMap, Page page, BoundSql boundSql,
            CacheKey pageKey) {
        return paramMap;
    }

    @Override
    public String getPageSql(String sql, Page page, CacheKey pageKey) {
        // 处理pageKey
        pageKey.update(page.getStartRow());
        pageKey.update(page.getPageSize());
        String cacheSql = CACHE_PAGESQL.read(sql);
        if (cacheSql == null) {
            cacheSql = sql;
            cacheSql = replaceSql.replace(cacheSql);
            cacheSql = sqlServerSqlParser.convertToPageSql(cacheSql, null, null);
            cacheSql = replaceSql.restore(cacheSql);
            CACHE_PAGESQL.write(sql, cacheSql, 60);
        }
        cacheSql = cacheSql.replace(String.valueOf(Long.MIN_VALUE), String.valueOf(page.getStartRow()));
        cacheSql = cacheSql.replace(String.valueOf(Long.MAX_VALUE), String.valueOf(page.getPageSize()));
        return cacheSql;
    }

    /**
     * 分页查询，Pager转换SQL时报错with(nolock)不识别的问题， 重写父类AbstractPaging.getPageSql转换出错的方法。 1.
     * this.replaceSql.replace(sql);先转换成假的表名 2. 然后进行SQL转换 3. this.replaceSql.restore(sql);最后再恢复成真的with(nolock)
     */
    @Override
    public String getPageSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds,
            CacheKey pageKey) {
        String sql = boundSql.getSql();
        Page page = this.getLocalPage();
        String orderBy = page.getOrderBy();
        if (StringKit.isNotEmpty(orderBy)) {
            pageKey.update(orderBy);
            sql = this.replaceSql.replace(sql);
            sql = orderBySqlParser.converToOrderBySql(sql, orderBy);
            sql = this.replaceSql.restore(sql);
        }

        return page.isOrderByOnly() ? sql : this.getPageSql(sql, page, pageKey);
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        this.sqlServerSqlParser = Builder.newInstance(properties.getProperty("sqlServerSqlParser"),
                SqlServerSqlParser.class, properties, DefaultSqlServerSqlParser::new);
        String replaceSql = properties.getProperty("replaceSql");
        if (StringKit.isEmpty(replaceSql) || "regex".equalsIgnoreCase(replaceSql)) {
            this.replaceSql = new RegexWithNolock();
        } else if ("simple".equalsIgnoreCase(replaceSql)) {
            this.replaceSql = new SimpleWithNolock();
        } else {
            this.replaceSql = Builder.newInstance(replaceSql, properties);
        }
        String sqlCacheClass = properties.getProperty("sqlCacheClass");
        if (StringKit.isNotEmpty(sqlCacheClass) && !sqlCacheClass.equalsIgnoreCase("false")) {
            CACHE_COUNTSQL = CacheFactory.createCache(sqlCacheClass, "count", properties);
            CACHE_PAGESQL = CacheFactory.createCache(sqlCacheClass, "pages", properties);
        } else {
            CACHE_COUNTSQL = CacheFactory.createCache(null, "count", properties);
            CACHE_PAGESQL = CacheFactory.createCache(null, "pages", properties);
        }
    }

}
