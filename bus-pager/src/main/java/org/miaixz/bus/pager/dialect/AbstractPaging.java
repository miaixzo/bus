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
import org.miaixz.bus.pager.binding.CountExecutor;
import org.miaixz.bus.pager.binding.MetaObject;

/**
 * Abstract base class for pagination dialect implementations, specifically for {@link PageContext}. This class provides
 * common logic for handling pagination, including SQL generation, parameter processing, and result handling, while
 * delegating database-specific SQL generation to subclasses.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractPaging extends AbstractDialect {

    /**
     * Suffix for the page ID, used to identify paginated MappedStatements.
     */
    public static String SUFFIX_PAGE = "_PageContext";
    /**
     * Suffix for the count query ID, used to identify count MappedStatements.
     */
    public static String SUFFIX_COUNT = SUFFIX_PAGE + "_Count";
    /**
     * Key for the first pagination parameter in the parameter map.
     */
    public static String PAGEPARAMETER_FIRST = "First" + SUFFIX_PAGE;
    /**
     * Key for the second pagination parameter in the parameter map.
     */
    public static String PAGEPARAMETER_SECOND = "Second" + SUFFIX_PAGE;

    /**
     * Retrieves the {@link Page} object associated with the current thread.
     *
     * @param <T> the type of elements in the paginated data
     * @return the current Page object
     */
    public <T> Page<T> getLocalPage() {
        return PageContext.getLocalPage();
    }

    /**
     * This method is not intended to be called directly in this implementation. Always returns true to indicate that
     * the skip logic is handled elsewhere.
     *
     * @param ms              the MappedStatement object
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @return always true
     */
    @Override
    public final boolean skip(
            MappedStatement ms,
            Object parameterObject,
            org.apache.ibatis.session.RowBounds rowBounds) {
        // This method will not be called
        return true;
    }

    /**
     * Determines whether a count query should be executed before the main pagination query. A count query is executed
     * if the current {@link Page} is not set to {@code orderByOnly} and {@code count} is enabled.
     *
     * @param ms              the MappedStatement object
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @return true if a count query should be executed, false otherwise
     */
    @Override
    public boolean beforeCount(
            MappedStatement ms,
            Object parameterObject,
            org.apache.ibatis.session.RowBounds rowBounds) {
        Page page = getLocalPage();
        return !page.isOrderByOnly() && page.isCount();
    }

    /**
     * Generates the SQL for the count query. It uses {@link org.miaixz.bus.pager.parsing.CountSqlParser} to get a smart
     * count SQL.
     *
     * @param ms              the MappedStatement object
     * @param boundSql        the BoundSql object containing the original SQL and parameters
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @param countKey        the CacheKey for the count query
     * @return the generated count SQL string
     */
    @Override
    public String getCountSql(
            MappedStatement ms,
            BoundSql boundSql,
            Object parameterObject,
            org.apache.ibatis.session.RowBounds rowBounds,
            CacheKey countKey) {
        Page<Object> page = getLocalPage();
        String countColumn = page.getCountColumn();
        if (StringKit.isNotEmpty(countColumn)) {
            return countSqlParser.getSmartCountSql(boundSql.getSql(), countColumn);
        }
        return countSqlParser.getSmartCountSql(boundSql.getSql());
    }

    /**
     * Called after the count query has been executed to process the count result. It sets the total count in the
     * {@link Page} object and determines if the main pagination query should proceed.
     *
     * @param count           the total number of records found by the count query
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @return true to continue with the pagination query, false to return immediately
     */
    @Override
    public boolean afterCount(long count, Object parameterObject, org.apache.ibatis.session.RowBounds rowBounds) {
        Page page = getLocalPage();
        page.setTotal(count);
        if (rowBounds instanceof RowBounds) {
            ((RowBounds) rowBounds).setTotal(count);
        }
        // If pageSize < 0, do not execute pagination query.
        // If pageSize = 0, still execute the subsequent query, but without pagination.
        if (page.getPageSizeZero() != null) {
            // PageSizeZero=false && pageSize<=0
            if (!page.getPageSizeZero() && page.getPageSize() <= 0) {
                return false;
            }
            // PageSizeZero=true && pageSize<0 returns false, only >=0 needs to execute subsequent queries
            else if (page.getPageSizeZero() && page.getPageSize() < 0) {
                return false;
            }
        }
        // pageNo > 0 && startRow < total is sufficient, no need to consider pageSize (the above if already handles
        // invalid values)
        return page.getPageNo() > 0 && count > page.getStartRow();
    }

    /**
     * Processes the parameter object for pagination. This method handles various parameter types and injects
     * pagination-specific parameters into the parameter map.
     *
     * @param ms              the MappedStatement object
     * @param parameterObject the original parameter object for the query
     * @param boundSql        the BoundSql object containing the original SQL and parameters
     * @param pageKey         the CacheKey for the paginated query
     * @return the processed parameter object, typically a Map containing all necessary parameters
     */
    @Override
    public Object processParameterObject(
            MappedStatement ms,
            Object parameterObject,
            BoundSql boundSql,
            CacheKey pageKey) {
        // Process parameters
        Page page = getLocalPage();
        // If it's only an order by query, no need to process parameters
        if (page.isOrderByOnly()) {
            return parameterObject;
        }
        Map<String, Object> paramMap;
        if (parameterObject == null) {
            paramMap = new HashMap<>();
        } else if (parameterObject instanceof Map) {
            // Handle immutable Map cases
            paramMap = new HashMap<>();
            paramMap.putAll((Map) parameterObject);
        } else {
            paramMap = new HashMap<>();
            // When sqlSource is ProviderSqlSource, handle the case with only 1 parameter
            if (ms.getSqlSource() instanceof ProviderSqlSource) {
                String[] providerMethodArgumentNames = CountExecutor
                        .getProviderMethodArgumentNames((ProviderSqlSource) ms.getSqlSource());
                if (providerMethodArgumentNames != null && providerMethodArgumentNames.length == 1) {
                    paramMap.put(providerMethodArgumentNames[0], parameterObject);
                    paramMap.put("param1", parameterObject);
                }
            }
            // Dynamic SQL conditions may not appear in ParameterMapping, but must exist, so all getter properties need
            // to be collected here.
            // TypeHandlerRegistry can directly process objects as direct use objects.
            boolean hasTypeHandler = ms.getConfiguration().getTypeHandlerRegistry()
                    .hasTypeHandler(parameterObject.getClass());
            org.apache.ibatis.reflection.MetaObject metaObject = MetaObject.forObject(parameterObject);
            // For annotated MyProviderSqlSource, save the original value
            if (!hasTypeHandler) {
                for (String name : metaObject.getGetterNames()) {
                    paramMap.put(name, metaObject.getValue(name));
                }
            }
            // The following section primarily addresses issues with a common type of parameter
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
     * Abstract method to process pagination parameters, to be implemented by concrete dialect classes.
     *
     * @param ms       the MappedStatement object
     * @param paramMap a map containing the query parameters
     * @param page     the {@link Page} object containing pagination details
     * @param boundSql the BoundSql object for the query
     * @param pageKey  the CacheKey for the paginated query
     * @return the processed parameter object
     */
    public abstract Object processPageParameter(
            MappedStatement ms,
            Map<String, Object> paramMap,
            Page page,
            BoundSql boundSql,
            CacheKey pageKey);

    /**
     * Determines whether the main pagination query should be executed. It proceeds if the page is set to
     * {@code orderByOnly} or if {@code pageSize} is greater than 0.
     *
     * @param ms              the MappedStatement object
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @return true if the pagination query should be executed, false otherwise
     */
    @Override
    public boolean beforePage(
            MappedStatement ms,
            Object parameterObject,
            org.apache.ibatis.session.RowBounds rowBounds) {
        Page page = getLocalPage();
        if (page.isOrderByOnly() || page.getPageSize() > 0) {
            return true;
        }
        return false;
    }

    /**
     * Generates the SQL for the paginated query. It applies the order by clause if specified in the {@link Page}
     * object, and then delegates to an abstract method for database-specific pagination SQL generation.
     *
     * @param ms              the MappedStatement object
     * @param boundSql        the BoundSql object containing the original SQL and parameters
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @param pageKey         the CacheKey for the paginated query
     * @return the generated paginated SQL string
     */
    @Override
    public String getPageSql(
            MappedStatement ms,
            BoundSql boundSql,
            Object parameterObject,
            org.apache.ibatis.session.RowBounds rowBounds,
            CacheKey pageKey) {
        String sql = boundSql.getSql();
        Page page = getLocalPage();
        // Support order by
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
     * Abstract method to generate the database-specific pagination SQL. To be implemented by concrete dialect classes.
     *
     * @param sql     the original SQL string
     * @param page    the {@link Page} object containing pagination details
     * @param pageKey the CacheKey for the paginated query
     * @return the database-specific paginated SQL string
     */
    public abstract String getPageSql(String sql, Page page, CacheKey pageKey);

    /**
     * Called after the pagination query has been executed to process the results. It adds the results to the
     * {@link Page} object and sets the total count if applicable.
     *
     * @param pageList        the list of results from the paginated query
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @return the processed paginated results, typically the {@link Page} object itself
     */
    @Override
    public Object afterPage(List pageList, Object parameterObject, org.apache.ibatis.session.RowBounds rowBounds) {
        Page page = getLocalPage();
        if (page == null) {
            return pageList;
        }
        page.addAll(pageList);
        // Adjust judgment order: if querying all, total is size; if only ordering, it's also all; otherwise, if count
        // is not queried, it's -1.
        if ((page.getPageSizeZero() != null && page.getPageSizeZero()) && page.getPageSize() == 0) {
            page.setTotal(pageList.size());
        } else if (page.isOrderByOnly()) {
            page.setTotal(pageList.size());
        } else if (!page.isCount()) {
            page.setTotal(-1);
        }
        return page;
    }

    /**
     * Called after all pagination tasks are completed. This implementation does nothing.
     */
    @Override
    public void afterAll() {

    }

    /**
     * Sets the properties for this dialect. Delegates to the superclass to set common properties.
     *
     * @param properties the properties to set
     */
    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
    }

    /**
     * Handles the injection of pagination parameters into the {@link BoundSql} by adding new {@link ParameterMapping}
     * entries for the first and second pagination parameters.
     *
     * @param boundSql    the BoundSql object to modify
     * @param ms          the MappedStatement object
     * @param firstClass  the class type for the first pagination parameter
     * @param secondClass the class type for the second pagination parameter
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
