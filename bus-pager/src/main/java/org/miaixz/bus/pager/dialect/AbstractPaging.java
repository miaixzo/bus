/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         ~
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
package org.miaixz.bus.pager.dialect;

import java.util.*;

import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pager.Page;
import org.miaixz.bus.pager.PageContext;
import org.miaixz.bus.pager.RowBounds;
import org.miaixz.bus.pager.builtin.CountExecutor;
import org.miaixz.bus.pager.builtin.MetaObject;

/**
 * 针对 PageContext 的实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractPaging extends AbstractDialect {

    /**
     * 分页的id后缀
     */
    public static String SUFFIX_PAGE = "_PageContext";
    /**
     * count查询的id后缀
     */
    public static String SUFFIX_COUNT = SUFFIX_PAGE + "_Count";
    /**
     * 第一个分页参数
     */
    public static String PAGEPARAMETER_FIRST = "First" + SUFFIX_PAGE;
    /**
     * 第二个分页参数
     */
    public static String PAGEPARAMETER_SECOND = "Second" + SUFFIX_PAGE;

    /**
     * 获取分页参数
     *
     * @param <T> 对象
     * @return 结果
     */
    public <T> Page<T> getLocalPage() {
        return PageContext.getLocalPage();
    }

    @Override
    public final boolean skip(MappedStatement ms, Object parameterObject,
            org.apache.ibatis.session.RowBounds rowBounds) {
        // 该方法不会被调用
        return true;
    }

    @Override
    public boolean beforeCount(MappedStatement ms, Object parameterObject,
            org.apache.ibatis.session.RowBounds rowBounds) {
        Page page = getLocalPage();
        return !page.isOrderByOnly() && page.isCount();
    }

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject,
            org.apache.ibatis.session.RowBounds rowBounds, CacheKey countKey) {
        Page<Object> page = getLocalPage();
        String countColumn = page.getCountColumn();
        if (StringKit.isNotEmpty(countColumn)) {
            return countSqlParser.getSmartCountSql(boundSql.getSql(), countColumn);
        }
        return countSqlParser.getSmartCountSql(boundSql.getSql());
    }

    @Override
    public boolean afterCount(long count, Object parameterObject, org.apache.ibatis.session.RowBounds rowBounds) {
        Page page = getLocalPage();
        page.setTotal(count);
        if (rowBounds instanceof RowBounds) {
            ((RowBounds) rowBounds).setTotal(count);
        }
        // pageSize < 0 的时候，不执行分页查询
        // pageSize = 0 的时候，还需要执行后续查询，但是不会分页
        if (page.getPageSizeZero() != null) {
            // PageSizeZero=false&&pageSize<=0
            if (!page.getPageSizeZero() && page.getPageSize() <= 0) {
                return false;
            }
            // PageSizeZero=true&&pageSize<0 返回 false，只有>=0才需要执行后续的
            else if (page.getPageSizeZero() && page.getPageSize() < 0) {
                return false;
            }
        }
        // 页码>0 && 开始行数<总行数即可，不需要考虑 pageSize（上面的 if 已经处理不符合要求的值了）
        return page.getPageNo() > 0 && count > page.getStartRow();
    }

    @Override
    public Object processParameterObject(MappedStatement ms, Object parameterObject, BoundSql boundSql,
            CacheKey pageKey) {
        // 处理参数
        Page page = getLocalPage();
        // 如果只是 order by 就不必处理参数
        if (page.isOrderByOnly()) {
            return parameterObject;
        }
        Map<String, Object> paramMap;
        if (parameterObject == null) {
            paramMap = new HashMap<>();
        } else if (parameterObject instanceof Map) {
            // 解决不可变Map的情况
            paramMap = new HashMap<>();
            paramMap.putAll((Map) parameterObject);
        } else {
            paramMap = new HashMap<>();
            // sqlSource为ProviderSqlSource时，处理只有1个参数的情况
            if (ms.getSqlSource() instanceof ProviderSqlSource) {
                String[] providerMethodArgumentNames = CountExecutor
                        .getProviderMethodArgumentNames((ProviderSqlSource) ms.getSqlSource());
                if (providerMethodArgumentNames != null && providerMethodArgumentNames.length == 1) {
                    paramMap.put(providerMethodArgumentNames[0], parameterObject);
                    paramMap.put("param1", parameterObject);
                }
            }
            // 动态sql时的判断条件不会出现在ParameterMapping中，但是必须有，所以这里需要收集所有的getter属性
            // TypeHandlerRegistry可以直接处理的会作为一个直接使用的对象进行处理
            boolean hasTypeHandler = ms.getConfiguration().getTypeHandlerRegistry()
                    .hasTypeHandler(parameterObject.getClass());
            org.apache.ibatis.reflection.MetaObject metaObject = MetaObject.forObject(parameterObject);
            // 需要针对注解形式的MyProviderSqlSource保存原值
            if (!hasTypeHandler) {
                for (String name : metaObject.getGetterNames()) {
                    paramMap.put(name, metaObject.getValue(name));
                }
            }
            // 下面这段方法，主要解决一个常见类型的参数时的问题
            if (boundSql.getParameterMappings() != null && boundSql.getParameterMappings().size() > 0) {
                for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                    String name = parameterMapping.getProperty();
                    if (!name.equals(PAGEPARAMETER_FIRST) && !name.equals(PAGEPARAMETER_SECOND)
                            && paramMap.get(name) == null) {
                        if (hasTypeHandler || parameterMapping.getJavaType().equals(parameterObject.getClass())) {
                            paramMap.put(name, parameterObject);
                            break;
                        }
                    }
                }
            }
        }
        return processPageParameter(ms, paramMap, page, boundSql, pageKey);
    }

    /**
     * 处理分页参数
     *
     * @param ms       MappedStatement
     * @param paramMap Map
     * @param page     Page
     * @param boundSql BoundSql
     * @param pageKey  CacheKey
     * @return 结果
     */
    public abstract Object processPageParameter(MappedStatement ms, Map<String, Object> paramMap, Page page,
            BoundSql boundSql, CacheKey pageKey);

    @Override
    public boolean beforePage(MappedStatement ms, Object parameterObject,
            org.apache.ibatis.session.RowBounds rowBounds) {
        Page page = getLocalPage();
        if (page.isOrderByOnly() || page.getPageSize() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public String getPageSql(MappedStatement ms, BoundSql boundSql, Object parameterObject,
            org.apache.ibatis.session.RowBounds rowBounds, CacheKey pageKey) {
        String sql = boundSql.getSql();
        Page page = getLocalPage();
        // 支持 order by
        String orderBy = page.getOrderBy();
        if (StringKit.isNotEmpty(orderBy)) {
            pageKey.update(orderBy);
            sql = orderBySqlParser.converToOrderBySql(sql, orderBy);
        }
        if (page.isOrderByOnly()) {
            return sql;
        }
        return getPageSql(sql, page, pageKey);
    }

    /**
     * 单独处理分页部分
     *
     * @param sql     sql
     * @param page    Page
     * @param pageKey CacheKey
     * @return the string
     */
    public abstract String getPageSql(String sql, Page page, CacheKey pageKey);

    @Override
    public Object afterPage(List pageList, Object parameterObject, org.apache.ibatis.session.RowBounds rowBounds) {
        Page page = getLocalPage();
        if (page == null) {
            return pageList;
        }
        page.addAll(pageList);
        // 调整判断顺序，如果查全部，total就是size，如果只排序，也是全部，其他情况下如果不查询count就是-1
        if ((page.getPageSizeZero() != null && page.getPageSizeZero()) && page.getPageSize() == 0) {
            page.setTotal(pageList.size());
        } else if (page.isOrderByOnly()) {
            page.setTotal(pageList.size());
        } else if (!page.isCount()) {
            page.setTotal(-1);
        }
        return page;
    }

    @Override
    public void afterAll() {

    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
    }

    /**
     * @param boundSql    boundSql
     * @param ms          MappedStatement
     * @param firstClass  第一个分页参数
     * @param secondClass 第二个分页参数
     */
    protected void handleParameter(BoundSql boundSql, MappedStatement ms, Class<?> firstClass, Class<?> secondClass) {
        if (boundSql.getParameterMappings() != null) {
            List<ParameterMapping> newParameterMappings = new ArrayList<>(boundSql.getParameterMappings());
            newParameterMappings
                    .add(new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_FIRST, firstClass).build());
            newParameterMappings.add(
                    new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_SECOND, secondClass).build());
            org.apache.ibatis.reflection.MetaObject metaObject = MetaObject.forObject(boundSql);
            metaObject.setValue("parameterMappings", newParameterMappings);
        }
    }

}
