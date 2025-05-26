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
import org.miaixz.bus.mapper.Handler;
import org.miaixz.bus.pager.Builder;
import org.miaixz.bus.pager.Dialect;
import org.miaixz.bus.pager.Page;
import org.miaixz.bus.pager.binding.CountExecutor;
import org.miaixz.bus.pager.binding.CountMappedStatement;
import org.miaixz.bus.pager.binding.CountMsId;
import org.miaixz.bus.pager.binding.PageMethod;
import org.miaixz.bus.pager.cache.CacheFactory;

import net.sf.jsqlparser.statement.select.Select;

/**
 * Pagination SQL handler for MyBatis queries
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PageSqlMapperHandler extends SqlParserHandler implements Handler {

    private CacheX<String, MappedStatement> msCountMap;
    private CountMsId countMsId = CountMsId.DEFAULT;
    private volatile Dialect dialect;
    private String countSuffix = "_COUNT";
    private boolean debug;
    private final String default_dialect_class = "org.miaixz.bus.pager.PageContext";

    /**
     * Check if debug mode is enabled
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Log pagination call stack in debug mode
     */
    protected void debugStackTraceLog() {
        if (isDebug()) {
            Page<Object> page = PageMethod.getLocalPage();
            Logger.debug("Pagination call stack: {}", page.getStackTrace());
        }
    }

    /**
     * Ensure dialect is initialized
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

    @Override
    public boolean isQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
            ResultHandler resultHandler, BoundSql boundSql) {
        checkDialectExists();
        if (!dialect.skip(ms, parameter, rowBounds)) {
            try {
                String sql = boundSql.getSql();
                if (StringKit.isBlank(sql)) {
                    Logger.warn("Empty SQL detected, MappedStatement: {}", ms.getId());
                    return false;
                }
                parserSingle(sql, null);
                return true;
            } catch (Exception e) {
                Logger.error("Failed to parse query SQL: {}", e.getMessage());
                return false;
            }
        }
        return true;
    }

    @Override
    public void query(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
            ResultHandler resultHandler, BoundSql boundSql) {
        checkDialectExists();
        try {
            CacheKey cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
            if (dialect instanceof BoundSqlHandler.Chain) {
                boundSql = ((BoundSqlHandler.Chain) dialect).doBoundSql(BoundSqlHandler.Type.ORIGINAL, boundSql,
                        cacheKey);
            }
            if (!dialect.skip(ms, parameter, rowBounds)) {
                debugStackTraceLog();
                Future<Long> countFuture = null;
                if (dialect.beforeCount(ms, parameter, rowBounds)) {
                    if (dialect.isAsyncCount()) {
                        countFuture = asyncCount(ms, boundSql, parameter, rowBounds);
                    } else {
                        Long count = count(executor, ms, parameter, rowBounds, resultHandler, boundSql);
                        if (!dialect.afterCount(count, parameter, rowBounds)) {
                            return;
                        }
                    }
                }
                List<Object> resultList = CountExecutor.pageQuery(dialect, executor, ms, parameter, rowBounds,
                        resultHandler, boundSql, cacheKey);
                if (countFuture != null) {
                    Long count = countFuture.get();
                    dialect.afterCount(count, parameter, rowBounds);
                }
                // Return paginated result
                dialect.afterPage(resultList, parameter, rowBounds);
            } else {
                executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
            }
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
     * Async count query
     */
    private Future<Long> asyncCount(MappedStatement ms, BoundSql boundSql, Object parameter, RowBounds rowBounds) {
        Configuration configuration = ms.getConfiguration();
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
                return count(countExecutor, ms, parameter, rowBounds, null, countBoundSql);
            } finally {
                try {
                    tx.close();
                } catch (SQLException e) {
                    Logger.error("Failed to close transaction: {}", e.getMessage());
                }
            }
        });
    }

    private Long count(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
            ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        String countMsId = this.countMsId.genCountMsId(ms, parameter, boundSql, countSuffix);
        Long count;
        MappedStatement countMs = CountExecutor.getExistedMappedStatement(ms.getConfiguration(), countMsId);
        if (countMs != null) {
            count = CountExecutor.executeManualCount(executor, countMs, parameter, boundSql, resultHandler);
        } else {
            if (msCountMap != null) {
                countMs = msCountMap.read(countMsId);
            }
            if (countMs == null) {
                countMs = CountMappedStatement.newCountMappedStatement(ms, countMsId);
                if (msCountMap != null) {
                    msCountMap.write(countMsId, countMs, 60);
                }
            }
            count = CountExecutor.executeAutoCount(dialect, executor, countMs, parameter, boundSql, rowBounds,
                    resultHandler);
        }
        return count;
    }

    @Override
    protected void processSelect(Select select, int index, String sql, Object obj) {
        Logger.debug("Processing Select statement for pagination: {}", select.toString());
    }

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

}