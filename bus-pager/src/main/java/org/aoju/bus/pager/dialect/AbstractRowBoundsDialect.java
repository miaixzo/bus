package org.aoju.bus.pager.dialect;

import org.aoju.bus.pager.PageRowBounds;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Properties;

/**
 * 基于 RowBounds 的分页
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class AbstractRowBoundsDialect extends AbstractDialect {

    @Override
    public boolean skip(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        return rowBounds == RowBounds.DEFAULT;
    }

    @Override
    public boolean beforeCount(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        if (rowBounds instanceof PageRowBounds) {
            PageRowBounds pageRowBounds = (PageRowBounds) rowBounds;
            return pageRowBounds.getCount() == null || pageRowBounds.getCount();
        }
        return false;
    }

    @Override
    public boolean afterCount(long count, Object parameterObject, RowBounds rowBounds) {
        //由于 beforeCount 校验，这里一定是 PageRowBounds
        ((PageRowBounds) rowBounds).setTotal(count);
        return count > 0;
    }

    @Override
    public Object processParameterObject(MappedStatement ms, Object parameterObject, BoundSql boundSql, CacheKey pageKey) {
        return parameterObject;
    }

    @Override
    public boolean beforePage(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        return true;
    }

    @Override
    public String getPageSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey pageKey) {
        String sql = boundSql.getSql();
        return getPageSql(sql, rowBounds, pageKey);
    }

    public abstract String getPageSql(String sql, RowBounds rowBounds, CacheKey pageKey);

    @Override
    public Object afterPage(List pageList, Object parameterObject, RowBounds rowBounds) {
        return pageList;
    }

    @Override
    public void afterAll() {

    }

    @Override
    public void setProperties(Properties properties) {

    }
}
