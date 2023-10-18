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
package org.aoju.bus.pager.dialect.base;

import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.pager.Page;
import org.aoju.bus.pager.Property;
import org.aoju.bus.pager.cache.Cache;
import org.aoju.bus.pager.cache.CacheFactory;
import org.aoju.bus.pager.dialect.AbstractPaging;
import org.aoju.bus.pager.dialect.ReplaceSql;
import org.aoju.bus.pager.dialect.replace.RegexWithNolock;
import org.aoju.bus.pager.dialect.replace.SimpleWithNolock;
import org.aoju.bus.pager.parser.OrderByParser;
import org.aoju.bus.pager.parser.SqlServerParser;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

import java.util.Map;
import java.util.Properties;

/**
 * 数据库方言 sqlserver
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SqlServer extends AbstractPaging {

    protected SqlServerParser pageSql;
    protected Cache<String, String> CACHE_COUNTSQL;
    protected Cache<String, String> CACHE_PAGESQL;
    protected ReplaceSql replaceSql;

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey countKey) {
        String sql = boundSql.getSql();
        String cacheSql = CACHE_COUNTSQL.get(sql);
        if (cacheSql != null) {
            return cacheSql;
        } else {
            cacheSql = sql;
        }
        cacheSql = replaceSql.replace(cacheSql);
        cacheSql = countSqlParser.getSmartCountSql(cacheSql);
        cacheSql = replaceSql.restore(cacheSql);
        CACHE_COUNTSQL.put(sql, cacheSql);
        return cacheSql;
    }

    @Override
    public Object processPageParameter(MappedStatement ms, Map<String, Object> paramMap, Page page, BoundSql boundSql, CacheKey pageKey) {
        return paramMap;
    }

    @Override
    public String getPageSql(String sql, Page page, CacheKey pageKey) {
        // 处理pageKey
        pageKey.update(page.getStartRow());
        pageKey.update(page.getPageSize());
        String cacheSql = CACHE_PAGESQL.get(sql);
        if (cacheSql == null) {
            cacheSql = sql;
            cacheSql = replaceSql.replace(cacheSql);
            cacheSql = pageSql.convertToPageSql(cacheSql, null, null);
            cacheSql = replaceSql.restore(cacheSql);
            CACHE_PAGESQL.put(sql, cacheSql);
        }
        cacheSql = cacheSql.replace(String.valueOf(Long.MIN_VALUE), String.valueOf(page.getStartRow()));
        cacheSql = cacheSql.replace(String.valueOf(Long.MAX_VALUE), String.valueOf(page.getPageSize()));
        return cacheSql;
    }

    @Override
    public String getPageSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey pageKey) {
        String sql = boundSql.getSql();
        Page page = this.getLocalPage();
        String orderBy = page.getOrderBy();
        if (StringKit.isNotEmpty(orderBy)) {
            pageKey.update(orderBy);
            sql = this.replaceSql.replace(sql);
            sql = OrderByParser.converToOrderBySql(sql, orderBy, jSqlParser);
            sql = this.replaceSql.restore(sql);
        }

        return page.isOrderByOnly() ? sql : this.getPageSql(sql, page, pageKey);
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        this.pageSql = new SqlServerParser(jSqlParser);
        String replaceSql = properties.getProperty("replaceSql");
        if (StringKit.isEmpty(replaceSql) || "regex".equalsIgnoreCase(replaceSql)) {
            this.replaceSql = new RegexWithNolock();
        } else if ("simple".equalsIgnoreCase(replaceSql)) {
            this.replaceSql = new SimpleWithNolock();
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
        String sqlCacheClass = properties.getProperty("sqlCacheClass");
        if (StringKit.isNotEmpty(sqlCacheClass) && !sqlCacheClass.equalsIgnoreCase("false")) {
            CACHE_COUNTSQL = CacheFactory.createCache(sqlCacheClass, "count", properties);
            CACHE_PAGESQL = CacheFactory.createCache(sqlCacheClass, "page", properties);
        } else {
            CACHE_COUNTSQL = CacheFactory.createCache(null, "count", properties);
            CACHE_PAGESQL = CacheFactory.createCache(null, "page", properties);
        }
    }

}
