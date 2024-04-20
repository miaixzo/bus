/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         *
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
package org.miaixz.bus.pager.dialect;

import org.miaixz.bus.pager.RowBounds;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.List;
import java.util.Properties;

/**
 * 基于 RowBounds 的分页
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractRowBounds extends AbstractDialect {

    @Override
    public boolean skip(MappedStatement ms, Object parameterObject, org.apache.ibatis.session.RowBounds rowBounds) {
        return rowBounds == org.apache.ibatis.session.RowBounds.DEFAULT;
    }

    @Override
    public boolean beforeCount(MappedStatement ms, Object parameterObject, org.apache.ibatis.session.RowBounds rowBounds) {
        if (rowBounds instanceof RowBounds) {
            RowBounds pageRowBounds = (RowBounds) rowBounds;
            return pageRowBounds.getCount() == null || pageRowBounds.getCount();
        }
        return false;
    }

    @Override
    public boolean afterCount(long count, Object parameterObject, org.apache.ibatis.session.RowBounds rowBounds) {
        // 由于 beforeCount 校验，这里一定是 PageRowBounds
        ((RowBounds) rowBounds).setTotal(count);
        return count > 0;
    }

    @Override
    public Object processParameterObject(MappedStatement ms, Object parameterObject, BoundSql boundSql, CacheKey pageKey) {
        return parameterObject;
    }

    @Override
    public boolean beforePage(MappedStatement ms, Object parameterObject, org.apache.ibatis.session.RowBounds rowBounds) {
        return true;
    }

    @Override
    public String getPageSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, org.apache.ibatis.session.RowBounds rowBounds, CacheKey pageKey) {
        String sql = boundSql.getSql();
        return getPageSql(sql, rowBounds, pageKey);
    }

    public abstract String getPageSql(String sql, org.apache.ibatis.session.RowBounds rowBounds, CacheKey pageKey);

    @Override
    public Object afterPage(List pageList, Object parameterObject, org.apache.ibatis.session.RowBounds rowBounds) {
        return pageList;
    }

    @Override
    public void afterAll() {

    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
    }

}
