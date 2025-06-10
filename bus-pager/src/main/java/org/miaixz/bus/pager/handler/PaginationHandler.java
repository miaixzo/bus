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
package org.miaixz.bus.pager.handler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.handler.MapperHandler;
import org.miaixz.bus.pager.Builder;
import org.miaixz.bus.pager.Dialect;
import org.miaixz.bus.pager.Page;
import org.miaixz.bus.pager.binding.CountExecutor;
import org.miaixz.bus.pager.binding.CountMappedStatement;
import org.miaixz.bus.pager.binding.CountMsId;
import org.miaixz.bus.pager.binding.PageMethod;
import org.miaixz.bus.pager.builder.BoundSqlBuilder;
import org.miaixz.bus.pager.cache.CacheFactory;

import net.sf.jsqlparser.statement.select.Select;

/**
 * 分页：查询的分页
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PaginationHandler extends SqlParserHandler implements MapperHandler {

    /**
     * 缓存 MappedStatement 的计数查询结果
     */
    private CacheX<String, MappedStatement> msCountMap;
    /**
     * 计数查询的 MappedStatement ID 生成器
     */
    private CountMsId countMsId = CountMsId.DEFAULT;
    /**
     * 分页方言，控制分页逻辑
     */
    private volatile Dialect dialect;
    /**
     * 计数查询后缀
     */
    private String countSuffix = "_COUNT";
    /**
     * 是否启用调试模式
     */
    private boolean debug;
    /**
     * 默认分页方言类
     */
    private final String default_dialect_class = "org.miaixz.bus.pager.PageContext";

    /**
     * 检查是否启用调试模式。
     *
     * @return 是否启用调试模式
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * 在调试模式下记录分页调用堆栈。
     */
    protected void debugStackTraceLog() {
        if (isDebug()) {
            Page<Object> page = PageMethod.getLocalPage();
            Logger.debug("Pagination call stack: {}", page.getStackTrace());
        }
    }

    /**
     * 确保分页方言已初始化。
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

    /**
     * 判断是否需要执行分页查询。
     *
     * @param executor        MyBatis 执行器
     * @param mappedStatement MappedStatement 对象
     * @param parameter       查询参数
     * @param rowBounds       分页参数
     * @param resultHandler   结果处理器
     * @param boundSql        绑定的 SQL
     * @return 是否需要分页查询
     */
    @Override
    public boolean isQuery(Executor executor, MappedStatement mappedStatement, Object parameter, RowBounds rowBounds,
            ResultHandler resultHandler, BoundSql boundSql) {
        checkDialectExists();
        if (!dialect.skip(mappedStatement, parameter, rowBounds)) {
            try {
                String sql = boundSql.getSql();
                if (StringKit.isBlank(sql)) {
                    Logger.warn("Empty SQL detected, MappedStatement: {}", mappedStatement.getId());
                    return false;
                }
                parserSingle(sql, parameter);
                return true;
            } catch (Exception e) {
                Logger.error("Failed to parse query SQL: {}", e.getMessage());
                return false;
            }
        }
        return false;
    }

    /**
     * 执行分页查询，处理 COUNT 和分页逻辑。
     *
     * @param result          分页结果
     * @param executor        MyBatis 执行器
     * @param mappedStatement MappedStatement 对象
     * @param parameter       查询参数
     * @param rowBounds       分页参数
     * @param resultHandler   结果处理器
     * @param boundSql        绑定的 SQL
     */
    @Override
    public void query(Object result, Executor executor, MappedStatement mappedStatement, Object parameter,
            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        try {
            CacheKey cacheKey = executor.createCacheKey(mappedStatement, parameter, rowBounds, boundSql);
            checkDialectExists();
            // 处理 BoundSql 的拦截逻辑
            if (dialect instanceof BoundSqlBuilder.Chain) {
                boundSql = ((BoundSqlBuilder.Chain) dialect).doBoundSql(BoundSqlBuilder.Type.ORIGINAL, boundSql,
                        cacheKey);
            }
            List resultList;
            // 判断是否需要分页
            if (!dialect.skip(mappedStatement, parameter, rowBounds)) {
                debugStackTraceLog();
                Future<Long> countFuture = null;
                // 判断是否需要 COUNT 查询
                if (dialect.beforeCount(mappedStatement, parameter, rowBounds)) {
                    if (dialect.isAsyncCount()) {
                        countFuture = asyncCount(mappedStatement, boundSql, parameter, rowBounds);
                    } else {
                        Long count = count(executor, mappedStatement, parameter, rowBounds, null, boundSql);
                        if (!dialect.afterCount(count, parameter, rowBounds)) {
                            ((Object[]) result)[0] = dialect.afterPage(new ArrayList(), parameter, rowBounds);
                            return;
                        }
                    }
                }
                resultList = CountExecutor.pageQuery(dialect, executor, mappedStatement, parameter, rowBounds,
                        resultHandler, boundSql, cacheKey);
                if (countFuture != null) {
                    Long count = countFuture.get();
                    dialect.afterCount(count, parameter, rowBounds);
                }
            } else {
                // 默认内存分页
                resultList = executor.query(mappedStatement, parameter, rowBounds, resultHandler, cacheKey, boundSql);
            }
            ((Object[]) result)[0] = dialect.afterPage(resultList, parameter, rowBounds);
        } catch (Exception e) {
            Logger.error("Failed to process pagination SQL: {}", e.getMessage());
            throw new InternalException("Failed to process pagination SQL: " + e.getMessage(), e);
        } finally {
            if (dialect != null) {
                dialect.afterAll();
            }
        }
    }

    /**
     * 异步执行 COUNT 查询。
     *
     * @param mappedStatement MappedStatement 对象
     * @param boundSql        绑定的 SQL
     * @param parameter       查询参数
     * @param rowBounds       分页参数
     * @return COUNT 查询的 Future 结果
     */
    private Future<Long> asyncCount(MappedStatement mappedStatement, BoundSql boundSql, Object parameter,
            RowBounds rowBounds) {
        Configuration configuration = mappedStatement.getConfiguration();
        BoundSql countBoundSql = new BoundSql(configuration, boundSql.getSql(),
                new ArrayList<>(boundSql.getParameterMappings()), parameter);
        Map<String, Object> additionalParameter = CountExecutor.getAdditionalParameter(boundSql);
        if (additionalParameter != null) {
            for (String key : additionalParameter.keySet()) {
                countBoundSql.setAdditionalParameter(key, additionalParameter.get(key));
            }
        }
        TransactionFactory transactionFactory = new ManagedTransactionFactory();
        Transaction tx = transactionFactory.newTransaction(configuration.getEnvironment().getDataSource(), null, false);
        Executor countExecutor = configuration.newExecutor(tx, configuration.getDefaultExecutorType());
        return dialect.asyncCountTask(() -> {
            try {
                return count(countExecutor, mappedStatement, parameter, rowBounds, null, countBoundSql);
            } finally {
                try {
                    tx.close();
                } catch (SQLException e) {
                    Logger.error("Failed to close transaction: {}", e.getMessage());
                }
            }
        });
    }

    public static void main(String[] args) {

    }

    /**
     * 执行 COUNT 查询。
     *
     * @param executor        MyBatis 执行器
     * @param mappedStatement MappedStatement 对象
     * @param parameter       查询参数
     * @param rowBounds       分页参数
     * @param resultHandler   结果处理器
     * @param boundSql        绑定的 SQL
     * @return 记录总数
     * @throws SQLException 如果执行 COUNT 查询失败
     */
    private Long count(Executor executor, MappedStatement mappedStatement, Object parameter, RowBounds rowBounds,
            ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        String countMsId = this.countMsId.genCountMsId(mappedStatement, parameter, boundSql, countSuffix);
        Long count;
        MappedStatement countMs = CountExecutor.getExistedMappedStatement(mappedStatement.getConfiguration(),
                countMsId);
        if (countMs != null) {
            count = CountExecutor.executeManualCount(executor, countMs, parameter, boundSql, resultHandler);
        } else {
            if (msCountMap != null) {
                countMs = msCountMap.read(countMsId);
            }
            if (countMs == null) {
                countMs = CountMappedStatement.newCountMappedStatement(mappedStatement, countMsId);
                if (msCountMap != null) {
                    msCountMap.write(countMsId, countMs, 60);
                }
            }
            count = CountExecutor.executeAutoCount(dialect, executor, countMs, parameter, boundSql, rowBounds,
                    resultHandler);
        }
        return count;
    }

    /**
     * 处理 SELECT 语句，记录分页相关日志。
     *
     * @param select SELECT 语句对象
     * @param index  索引
     * @param sql    原始 SQL 语句
     * @param obj    附加对象
     */
    @Override
    protected void processSelect(Select select, int index, String sql, Object obj) {

    }

    /**
     * 设置分页处理器属性，初始化缓存和方言。
     *
     * @param properties 配置属性
     * @return 是否设置成功
     */
    @Override
    public boolean setProperties(Properties properties) {
        msCountMap = CacheFactory.createCache(properties.getProperty("msCountCache"), "ms", properties);
        String dialectClass = properties.getProperty("dialect");
        if (StringKit.isEmpty(dialectClass)) {
            dialectClass = default_dialect_class;
        }
        Dialect tempDialect = Builder.newInstance(dialectClass, properties);
        tempDialect.setProperties(properties);

        String countSuffix = properties.getProperty("countSuffix");
        if (StringKit.isNotEmpty(countSuffix)) {
            this.countSuffix = countSuffix;
        }

        debug = Boolean.parseBoolean(properties.getProperty("debug"));

        String countMsIdGenClass = properties.getProperty("countMsId");
        if (StringKit.isNotEmpty(countMsIdGenClass)) {
            countMsId = Builder.newInstance(countMsIdGenClass, properties);
        }
        dialect = tempDialect;
        return true;
    }

    /**
     * 获取当前分页方言。
     *
     * @return 分页方言
     */
    public Dialect getDialect() {
        return this.dialect;
    }

}