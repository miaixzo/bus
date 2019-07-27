package org.aoju.bus.pager;

import org.aoju.bus.pager.dialect.AbstractHelperDialect;
import org.aoju.bus.pager.dialect.Dialect;
import org.aoju.bus.pager.parser.CountSqlParser;
import org.aoju.bus.pager.plugin.CountMappedStatement;
import org.aoju.bus.pager.plugin.PageFromObject;
import org.aoju.bus.pager.proxy.PageAutoDialect;
import org.aoju.bus.pager.proxy.PageMethod;
import org.aoju.bus.pager.proxy.PageParams;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Properties;

/**
 * Mybatis - 通用分页拦截器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class PageContext extends PageMethod implements Dialect {

    private PageParams pageParams;
    private PageAutoDialect autoDialect;

    @Override
    public boolean skip(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        if (ms.getId().endsWith(CountMappedStatement.COUNT)) {
            throw new RuntimeException("在系统中发现了多个分页插件，请检查系统配置!");
        }
        Page page = pageParams.getPage(parameterObject, rowBounds);
        if (page == null) {
            return true;
        } else {
            //设置默认的 count 列
            if (PageFromObject.isEmpty(page.getCountColumn())) {
                page.setCountColumn(pageParams.getCountColumn());
            }
            autoDialect.initDelegateDialect(ms);
            return false;
        }
    }

    @Override
    public boolean beforeCount(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        return autoDialect.getDelegate().beforeCount(ms, parameterObject, rowBounds);
    }

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey countKey) {
        return autoDialect.getDelegate().getCountSql(ms, boundSql, parameterObject, rowBounds, countKey);
    }

    @Override
    public boolean afterCount(long count, Object parameterObject, RowBounds rowBounds) {
        return autoDialect.getDelegate().afterCount(count, parameterObject, rowBounds);
    }

    @Override
    public Object processParameterObject(MappedStatement ms, Object parameterObject, BoundSql boundSql, CacheKey pageKey) {
        return autoDialect.getDelegate().processParameterObject(ms, parameterObject, boundSql, pageKey);
    }

    @Override
    public boolean beforePage(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        return autoDialect.getDelegate().beforePage(ms, parameterObject, rowBounds);
    }

    @Override
    public String getPageSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey pageKey) {
        return autoDialect.getDelegate().getPageSql(ms, boundSql, parameterObject, rowBounds, pageKey);
    }

    public String getPageSql(String sql, Page page, RowBounds rowBounds, CacheKey pageKey) {
        return autoDialect.getDelegate().getPageSql(sql, page, pageKey);
    }

    @Override
    public Object afterPage(List pageList, Object parameterObject, RowBounds rowBounds) {
        //这个方法即使不分页也会被执行，所以要判断 null
        AbstractHelperDialect delegate = autoDialect.getDelegate();
        if (delegate != null) {
            return delegate.afterPage(pageList, parameterObject, rowBounds);
        }
        return pageList;
    }

    @Override
    public void afterAll() {
        //这个方法即使不分页也会被执行，所以要判断 null
        AbstractHelperDialect delegate = autoDialect.getDelegate();
        if (delegate != null) {
            delegate.afterAll();
            autoDialect.clearDelegate();
        }
        clearPage();
    }

    @Override
    public void setProperties(Properties properties) {
        setStaticProperties(properties);
        pageParams = new PageParams();
        autoDialect = new PageAutoDialect();
        pageParams.setProperties(properties);
        autoDialect.setProperties(properties);
        //20180902新增 aggregateFunctions, 允许手动添加聚合函数（影响行数）
        CountSqlParser.addAggregateFunctions(properties.getProperty("aggregateFunctions"));
    }

}
