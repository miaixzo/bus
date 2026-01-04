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
package org.miaixz.bus.mapper.handler;

import java.sql.Connection;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.StringBuilderPool;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Context;

/**
 * A MyBatis SQL interceptor that applies custom logic to SQL execution by using registered handlers.
 * <p>
 * This interceptor acts as a central dispatcher, hooking into the MyBatis execution lifecycle (Executor,
 * StatementHandler, ResultSetHandler) and delegating specific logic to a chain of {@link MapperHandler}s.
 * </p>
 *
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li><b>O(1) Handler Lookup:</b> Uses {@link HandlerRegistry} to efficiently find handlers for specific operations
 * (Query, Update, etc).</li>
 * <li><b>SQL Logging:</b> detailed logging of executed SQL statements with parameters filled in.</li>
 * <li><b>Execution Control:</b> Handlers can modify execution arguments or block execution completely.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Intercepts({ @Signature(type = StatementHandler.class, method = "getBoundSql", args = {}),
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = { Statement.class }),
        @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class, Integer.class }),
        @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class }),
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class }) })
public class MybatisInterceptor extends AbstractSqlHandler implements Interceptor {

    /**
     * Pre-compiled pattern for matching multiple whitespace characters. Used to normalize SQL strings for logging.
     */
    private static final Pattern MULTIPLE_SPACES_PATTERN = Pattern.compile("[\\s]+");

    /**
     * Pre-compiled pattern for matching the standard SQL placeholder '?'.
     */
    private static final Pattern PARAM_PLACEHOLDER_PATTERN = Pattern.compile("\\?");

    /**
     * The registry for managing and retrieving SQL handlers efficiently.
     * <p>
     * It optimizes handler lookup from O(n) to O(1) by indexing handlers based on their overridden methods (Operation
     * Type).
     * </p>
     */
    private final HandlerRegistry handlerRegistry = new HandlerRegistry();

    /**
     * Formats a single parameter value for display in SQL logs.
     * <p>
     * Handles Strings (adds quotes), Dates (formats to string), and others (toString).
     * </p>
     *
     * @param object The parameter object.
     * @return The formatted string representation.
     */
    private static String getParameterValue(Object object) {
        if (object instanceof String) {
            return Symbol.SINGLE_QUOTE + object + Symbol.SINGLE_QUOTE;
        } else if (object instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            return Symbol.SINGLE_QUOTE + formatter.format(object) + Symbol.SINGLE_QUOTE;
        } else {
            return object != null ? object.toString() : Normal.EMPTY;
        }
    }

    /**
     * The main interception entry point.
     * <p>
     * Dispatches control to specific handling logic based on the target object type (Executor or StatementHandler) and
     * logs the execution time.
     * </p>
     *
     * @param invocation The invocation details containing target, method, and args.
     * @return The result of the intercepted method.
     * @throws Throwable if an error occurs during interception or subsequent processing.
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = DateKit.current();
        Object target = invocation.getTarget();
        Object[] args = invocation.getArgs();

        if (target instanceof Executor) {
            MappedStatement ms = (MappedStatement) args[0];
            Object parameter = args.length > 1 ? args[1] : null;
            BoundSql boundSql = ms.getBoundSql(parameter);
            Object result = handleExecutor((Executor) target, args, invocation, boundSql);
            logging(ms, boundSql, start);
            return result;
        } else if (target instanceof StatementHandler) {
            Object result = handleStatementHandler((StatementHandler) target, args, invocation);
            MetaObject metaObject = getMetaObject(target);
            MappedStatement ms = getMappedStatement(metaObject);
            BoundSql boundSql = (BoundSql) metaObject.getValue(DELEGATE_BOUNDSQL);
            logging(ms, boundSql, start);
            return result;
        }

        // Default case for unhandled targets (e.g. ResultSetHandler fallback)
        Object result = invocation.proceed();

        // Try to log if possible
        MetaObject metaObject = args != null && args.length > 0 && args[0] instanceof MappedStatement
                ? getMetaObject(args[0])
                : null;
        if (metaObject != null) {
            MappedStatement ms = getMappedStatement(metaObject);
            BoundSql boundSql = ms.getBoundSql(args.length > 1 ? args[1] : null);
            logging(ms, boundSql, start);
        }
        return result;
    }

    /**
     * Handles interception logic for Executor operations (QUERY, UPDATE/INSERT/DELETE).
     *
     * @param executor   The Executor instance.
     * @param args       The arguments array from the invocation.
     * @param invocation The original invocation object.
     * @param boundSql   The BoundSql (pre-computed to avoid redundant getBoundSql() calls).
     * @return The result of the operation.
     * @throws Throwable if an error occurs during processing.
     */
    private Object handleExecutor(Executor executor, Object[] args, Invocation invocation, BoundSql boundSql)
            throws Throwable {
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        SqlCommandType commandType = ms.getSqlCommandType();

        if (commandType == SqlCommandType.SELECT) {
            return processQuery(executor, ms, parameter, args, invocation, boundSql);
        } else if (commandType == SqlCommandType.INSERT || commandType == SqlCommandType.UPDATE
                || commandType == SqlCommandType.DELETE) {
            return processUpdate(executor, ms, parameter, invocation, boundSql);
        }

        return invocation.proceed();
    }

    /**
     * Handles interception logic for StatementHandler operations (getBoundSql, prepare).
     *
     * @param statementHandler The StatementHandler instance.
     * @param args             The arguments array.
     * @param invocation       The invocation details.
     * @return The result of the operation.
     * @throws Throwable if an error occurs during processing.
     */
    private Object handleStatementHandler(StatementHandler statementHandler, Object[] args, Invocation invocation)
            throws Throwable {
        if (args == null) {
            // Handle getBoundSql
            List<MapperHandler> handlers = handlerRegistry.getHandlers(HandlerRegistry.HandlerType.GET_BOUND_SQL);
            handlers.forEach(handler -> handler.getBoundSql(statementHandler));
        } else {
            // Handle prepare
            List<MapperHandler> handlers = handlerRegistry.getHandlers(HandlerRegistry.HandlerType.PREPARE);
            handlers.forEach(handler -> handler.prepare(statementHandler));
        }

        return invocation.proceed();
    }

    /**
     * Processes a query operation by iterating through registered QUERY handlers.
     *
     * @param executor   The Executor instance.
     * @param ms         The MappedStatement instance.
     * @param parameter  The parameter object.
     * @param args       The arguments array.
     * @param invocation The invocation details.
     * @param boundSql   The BoundSql (pre-computed to avoid redundant getBoundSql() calls).
     * @return The query result, or an empty list if blocked by an interceptor.
     * @throws Throwable if an error occurs during processing.
     */
    private Object processQuery(
            Executor executor,
            MappedStatement ms,
            Object parameter,
            Object[] args,
            Invocation invocation,
            BoundSql boundSql) throws Throwable {
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler<?> resultHandler = (ResultHandler<?>) args[3];
        CacheKey cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);

        // O(1) lookup: only get handlers that actually override query methods
        List<MapperHandler> queryHandlers = handlerRegistry.getHandlers(HandlerRegistry.HandlerType.QUERY);

        for (MapperHandler handler : queryHandlers) {
            // Allow handler to block execution
            if (!handler.isQuery(executor, ms, parameter, rowBounds, resultHandler, boundSql)) {
                return Collections.emptyList();
            }
            // Allow handler to execute custom query logic
            Object[] result = new Object[1];
            handler.query(result, executor, ms, parameter, rowBounds, resultHandler, boundSql);
            if (ArrayKit.isNotEmpty(result[0])) {
                return result[0];
            }
        }
        return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
    }

    /**
     * Processes an update operation by iterating through registered UPDATE handlers.
     *
     * @param executor   The Executor instance.
     * @param ms         The MappedStatement instance.
     * @param parameter  The parameter object.
     * @param invocation The invocation details.
     * @param boundSql   The BoundSql (pre-computed to avoid redundant getBoundSql() calls).
     * @return The update result, or -1 if blocked by an interceptor.
     * @throws Throwable if an error occurs during processing.
     */
    private Object processUpdate(
            Executor executor,
            MappedStatement ms,
            Object parameter,
            Invocation invocation,
            BoundSql boundSql) throws Throwable {
        // O(1) lookup: only get handlers that actually override update methods
        List<MapperHandler> updateHandlers = handlerRegistry.getHandlers(HandlerRegistry.HandlerType.UPDATE);

        for (MapperHandler handler : updateHandlers) {
            if (!handler.isUpdate(executor, ms, parameter)) {
                return -1;
            }
            handler.update(executor, ms, parameter);
        }
        return invocation.proceed();
    }

    /**
     * Logs the SQL execution information, including method ID, execution time, and the formatted SQL.
     * <p>
     * <strong>Performance Optimization:</strong> SQL formatting is only performed when DEBUG logging is enabled,
     * avoiding unnecessary string operations in production environments.
     * </p>
     *
     * @param ms       The MappedStatement instance.
     * @param boundSql The BoundSql instance.
     * @param start    The start timestamp (ms).
     */
    private void logging(MappedStatement ms, BoundSql boundSql, long start) {
        long duration = DateKit.current() - start;
        Logger.debug(true, "Method", "{} {}ms", ms.getId(), duration);

        if (Logger.isDebugEnabled()) {
            String sql = format(ms.getConfiguration(), boundSql);
            Logger.debug(true, "Script", "{}", sql);
        }
    }

    /**
     * Formats an SQL statement by replacing placeholders ('?') with actual parameter values.
     * <p>
     * This method uses a temporary unique ID replacement strategy to strictly ensure that parameter values are not
     * mistakenly identified as placeholders during subsequent replacements.
     * </p>
     *
     * @param configuration The MyBatis configuration.
     * @param boundSql      The BoundSql instance.
     * @return The formatted SQL statement string.
     */
    private String format(Configuration configuration, BoundSql boundSql) {
        String id = ID.objectId();
        String originalSql = boundSql.getSql();

        // Estimate buffer size to prevent resizing overhead
        int estimatedLength = originalSql.length() + boundSql.getParameterMappings().size() * 20;
        StringBuilder sqlBuilder = StringBuilderPool.acquireRaw(estimatedLength);

        try {
            // 1. Normalize spaces
            // 2. Replace all '?' with a unique ID to avoid collision during parameter replacement
            String sql = MULTIPLE_SPACES_PATTERN.matcher(originalSql).replaceAll(Symbol.SPACE);
            sql = PARAM_PLACEHOLDER_PATTERN.matcher(sql).replaceAll(id);

            // Get parameters.
            Object parameterObject = boundSql.getParameterObject();
            List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
            if (CollKit.isEmpty(parameterMappings) || parameterObject == null) {
                return sql;
            }

            // If the parameter class itself has a TypeHandler, use it directly (e.g., String, Integer)
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                return sql.replaceFirst(id, Matcher.quoteReplacement(getParameterValue(parameterObject)));
            }

            // Use MetaObject for complex objects (JavaBeans, Maps)
            MetaObject metaObject = configuration.newMetaObject(parameterObject);

            // Efficient replacement using StringBuilder
            int lastEndIndex = 0;
            sqlBuilder.setLength(0);
            sqlBuilder.append(sql);

            for (ParameterMapping mapping : parameterMappings) {
                String propertyName = mapping.getProperty();
                String paramValue;

                if (metaObject.hasGetter(propertyName)) {
                    paramValue = getParameterValue(metaObject.getValue(propertyName));
                } else if (boundSql.hasAdditionalParameter(propertyName)) {
                    // Dynamic SQL parameters (e.g. from <foreach>)
                    paramValue = getParameterValue(boundSql.getAdditionalParameter(propertyName));
                } else {
                    // Placeholder for missing parameters to maintain visual structure
                    paramValue = "Missing";
                }

                // Find the next occurrence of the unique ID and replace it
                int index = sqlBuilder.indexOf(id, lastEndIndex);
                if (index != -1) {
                    sqlBuilder.replace(index, index + id.length(), Matcher.quoteReplacement(paramValue));
                    lastEndIndex = index + paramValue.length();
                }
            }

            return sqlBuilder.toString();

        } finally {
            // Release StringBuilder back to the pool
            StringBuilderPool.release(sqlBuilder);
        }
    }

    /**
     * The plugin method, which determines whether to wrap the target object in a proxy.
     *
     * @param target The target object (Executor, StatementHandler, etc.).
     * @return The proxy object containing this interceptor, or the original object if not intercepted.
     */
    @Override
    public Object plugin(Object target) {
        return (target instanceof Executor || target instanceof StatementHandler || target instanceof ResultSetHandler)
                ? Plugin.wrap(target, this)
                : target;
    }

    /**
     * Adds a custom handler to the registry.
     *
     * @param handler The custom handler instance.
     */
    public void addHandler(MapperHandler handler) {
        handlerRegistry.addHandler(handler);
    }

    /**
     * Gets a snapshot of all registered handlers.
     *
     * @return A mutable copy of the handler list.
     */
    public List<MapperHandler> getHandlers() {
        return ListKit.of(handlerRegistry.getHandlers());
    }

    /**
     * Replaces all handlers with the provided list.
     * <p>
     * Note: This clears existing handlers. Primarily used for configuration initialization.
     * </p>
     *
     * @param handlers The new list of handlers.
     */
    public void setHandlers(List<MapperHandler> handlers) {
        handlerRegistry.clear();
        if (handlers != null) {
            handlerRegistry.addHandlers(handlers);
        }
    }

    /**
     * Dynamically creates and registers handlers based on configuration properties.
     * <p>
     * Parses properties to instantiate handler classes defined in the configuration.
     * </p>
     *
     * @param properties The configuration properties.
     */
    @Override
    public void setProperties(Properties properties) {
        Context context = (Context) Context.newInstance(properties);
        Map<String, Properties> groups = context.group(Symbol.AT);
        groups.forEach((key, value) -> addHandler(ReflectKit.newInstance(key)));
    }

}
