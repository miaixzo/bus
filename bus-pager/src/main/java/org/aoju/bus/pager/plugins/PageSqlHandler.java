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
package org.aoju.bus.pager.plugins;

import org.aoju.bus.core.exception.PageException;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.pager.Dialect;
import org.aoju.bus.pager.Property;
import org.aoju.bus.pager.cache.Cache;
import org.aoju.bus.pager.cache.CacheFactory;
import org.aoju.bus.pager.proxy.CountExecutor;
import org.aoju.bus.pager.proxy.CountMappedStatement;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 通用分页拦截器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Intercepts(
        {
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        }
)
public class PageSqlHandler implements Interceptor {

    protected Cache<String, MappedStatement> ms_count_cache;
    protected CountMsId count_ms_id = CountMsId.DEFAULT;
    private volatile Dialect dialect;
    private String count_suffix = "_COUNT";
    private String default_dialect_class = "org.aoju.bus.pager.PageContext";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            Object[] args = invocation.getArgs();
            MappedStatement ms = (MappedStatement) args[0];
            Object parameter = args[1];
            RowBounds rowBounds = (RowBounds) args[2];
            ResultHandler resultHandler = (ResultHandler) args[3];
            Executor executor = (Executor) invocation.getTarget();
            CacheKey cacheKey;
            BoundSql boundSql;
            // 由于逻辑关系，只会进入一次
            if (args.length == 4) {
                // 4 个参数时
                boundSql = ms.getBoundSql(parameter);
                cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
            } else {
                // 6 个参数时
                cacheKey = (CacheKey) args[4];
                boundSql = (BoundSql) args[5];
            }
            checkDialectExists();
            // 对 boundSql 的拦截处理
            if (dialect instanceof BoundSqlHandler.Chain) {
                boundSql = ((BoundSqlHandler.Chain) dialect).doBoundSql(BoundSqlHandler.Type.ORIGINAL, boundSql, cacheKey);
            }
            List resultList;
            // 调用方法判断是否需要进行分页，如果不需要，直接返回结果
            if (!dialect.skip(ms, parameter, rowBounds)) {
                // 判断是否需要进行 count 查询
                if (dialect.beforeCount(ms, parameter, rowBounds)) {
                    // 查询总数
                    Long count = count(executor, ms, parameter, rowBounds, null, boundSql);
                    // 处理查询总数，返回 true 时继续分页查询，false 时直接返回
                    if (!dialect.afterCount(count, parameter, rowBounds)) {
                        // 当查询总数为 0 时，直接返回空的结果
                        return dialect.afterPage(new ArrayList(), parameter, rowBounds);
                    }
                }
                resultList = CountExecutor.pageQuery(dialect, executor,
                        ms, parameter, rowBounds, resultHandler, boundSql, cacheKey);
            } else {
                // rowBounds用参数值，不使用分页插件处理时，仍然支持默认的内存分页
                resultList = executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
            }
            return dialect.afterPage(resultList, parameter, rowBounds);
        } finally {
            if (dialect != null) {
                dialect.afterAll();
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 缓存 count ms
        this.ms_count_cache = CacheFactory.createCache(properties.getProperty("ms_count_cache"), "ms", properties);
        String dialectClass = properties.getProperty("dialect");
        if (StringKit.isEmpty(dialectClass)) {
            dialectClass = default_dialect_class;
        }
        try {
            Class<?> aClass = Class.forName(dialectClass);
            dialect = (Dialect) aClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new PageException(e);
        }
        dialect.setProperties(properties);

        String countSuffix = properties.getProperty("count_suffix");
        if (StringKit.isNotEmpty(countSuffix)) {
            this.count_suffix = countSuffix;
        }

        // 通过 countMsId 配置自定义类
        String countMsIdGenClass = properties.getProperty("count_ms_id");
        if (StringKit.isNotEmpty(countMsIdGenClass)) {
            try {
                Class<?> aClass = Class.forName(countMsIdGenClass);
                this.count_ms_id = (CountMsId) aClass.getConstructor().newInstance();
                if (count_ms_id instanceof Property) {
                    ((Property) count_ms_id).setProperties(properties);
                }
            } catch (Exception e) {
                throw new PageException(e);
            }
        }
    }

    /**
     * Spring bean 方式配置时，如果没有配置属性就不会执行下面的 setProperties 方法，就不会初始化
     * 因此这里会出现 null 的情况 fixed #26
     */
    private void checkDialectExists() {
        if (dialect == null) {
            synchronized (default_dialect_class) {
                if (dialect == null) {
                    setProperties(new Properties());
                }
            }
        }
    }

    private Long count(Executor executor, MappedStatement ms, Object parameter,
                       RowBounds rowBounds, ResultHandler resultHandler,
                       BoundSql boundSql) throws SQLException {
        String countMsId = this.count_ms_id.genCountMsId(ms, parameter, boundSql, count_suffix);
        Long count;
        // 先判断是否存在手写的 count 查询
        MappedStatement countMs = CountExecutor.getExistedMappedStatement(ms.getConfiguration(), countMsId);
        if (countMs != null) {
            count = CountExecutor.executeManualCount(executor, countMs, parameter, boundSql, resultHandler);
        } else {
            if (this.ms_count_cache != null) {
                countMs = this.ms_count_cache.get(countMsId);
            }
            // 自动创建
            if (countMs == null) {
                // 根据当前的 ms 创建一个返回值为 Long 类型的 ms
                countMs = CountMappedStatement.newCountMappedStatement(ms, countMsId);
                if (this.ms_count_cache != null) {
                    this.ms_count_cache.put(countMsId, countMs);
                }
            }
            count = CountExecutor.executeAutoCount(this.dialect, executor, countMs, parameter, boundSql, rowBounds, resultHandler);
        }
        return count;
    }

}
