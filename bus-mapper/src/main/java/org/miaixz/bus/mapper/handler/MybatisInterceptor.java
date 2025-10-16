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
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Context;

/**
 * A MyBatis SQL interceptor that applies custom logic to SQL execution by using registered handlers. It intercepts
 * {@link Executor} and {@link StatementHandler} to process queries, updates, and SQL preparation.
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
     * A set of custom handlers to avoid duplicates.
     */
    private final Set<MapperHandler> handlers = new HashSet<>();

    /**
     * Intercepts MyBatis Executor and StatementHandler calls.
     *
     * @param invocation The invocation details.
     * @return The result of the intercepted method.
     * @throws Throwable if an error occurs during interception.
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = DateKit.current();
        Object target = invocation.getTarget();
        Object[] args = invocation.getArgs();

        if (target instanceof Executor) {
            Object result = handleExecutor((Executor) target, args, invocation);
            MappedStatement ms = (MappedStatement) args[0];
            Object parameter = args.length > 1 ? args[1] : null;
            BoundSql boundSql = ms.getBoundSql(parameter);
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
        // Default case for unhandled targets
        Object result = invocation.proceed();
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
     * Handles interception logic for Executor (query or update).
     *
     * @param executor   The Executor instance.
     * @param args       The arguments array.
     * @param invocation The invocation details.
     * @return The result of the operation.
     * @throws Throwable if an error occurs during processing.
     */
    private Object handleExecutor(Executor executor, Object[] args, Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        SqlCommandType commandType = ms.getSqlCommandType();

        if (commandType == SqlCommandType.SELECT) {
            return processQuery(executor, ms, parameter, args, invocation);
        } else if (commandType == SqlCommandType.INSERT || commandType == SqlCommandType.UPDATE
                || commandType == SqlCommandType.DELETE) {
            return processUpdate(executor, ms, parameter, invocation);
        }

        return invocation.proceed();
    }

    /**
     * Handles interception logic for StatementHandler (getBoundSql or prepare).
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
            handlers.forEach(handler -> handler.getBoundSql(statementHandler));
        } else {
            handlers.forEach(handler -> handler.prepare(statementHandler));
        }

        return invocation.proceed();
    }

    /**
     * Processes a query operation.
     *
     * @param executor   The Executor instance.
     * @param ms         The MappedStatement instance.
     * @param parameter  The parameter object.
     * @param args       The arguments array.
     * @param invocation The invocation details.
     * @return The query result, or an empty list if blocked by an interceptor.
     * @throws Throwable if an error occurs during processing.
     */
    private Object processQuery(
            Executor executor,
            MappedStatement ms,
            Object parameter,
            Object[] args,
            Invocation invocation) throws Throwable {
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler<?> resultHandler = (ResultHandler<?>) args[3];
        BoundSql boundSql = args.length == 4 ? ms.getBoundSql(parameter) : (BoundSql) args[5];
        CacheKey cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);

        for (MapperHandler handler : handlers) {
            if (!handler.isQuery(executor, ms, parameter, rowBounds, resultHandler, boundSql)) {
                return Collections.emptyList();
            }
            Object[] result = new Object[1];
            handler.query(result, executor, ms, parameter, rowBounds, resultHandler, boundSql);
            if (ArrayKit.isNotEmpty(result[0])) {
                return result[0];
            }
        }
        return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
    }

    /**
     * Processes an update operation.
     *
     * @param executor   The Executor instance.
     * @param ms         The MappedStatement instance.
     * @param parameter  The parameter object.
     * @param invocation The invocation details.
     * @return The update result, or -1 if blocked by an interceptor.
     * @throws Throwable if an error occurs during processing.
     */
    private Object processUpdate(Executor executor, MappedStatement ms, Object parameter, Invocation invocation)
            throws Throwable {
        for (MapperHandler handler : handlers) {
            if (!handler.isUpdate(executor, ms, parameter)) {
                return -1;
            }
            handler.update(executor, ms, parameter);
        }
        return invocation.proceed();
    }

    /**
     * Logs SQL execution information.
     *
     * @param ms       The MappedStatement instance.
     * @param boundSql The BoundSql instance.
     * @param start    The start time.
     */
    private void logging(MappedStatement ms, BoundSql boundSql, long start) {
        long duration = DateKit.current() - start;
        Logger.debug("==>     Method: {} {}ms", ms.getId(), duration);
        String sql = format(ms.getConfiguration(), boundSql);
        Logger.debug("==>     Script: {}", sql);
    }

    /**
     * Formats an SQL statement by replacing placeholders with parameter values.
     *
     * @param configuration The MyBatis configuration.
     * @param boundSql      The BoundSql instance.
     * @return The formatted SQL statement.
     */
    private String format(Configuration configuration, BoundSql boundSql) {
        String id = ID.objectId();
        // 1. Replace multiple spaces with a single space.
        // 2. Replace question marks with a unique ID to avoid issues with parameter values.
        String sql = boundSql.getSql().replaceAll("[\\s]+", Symbol.SPACE).replaceAll("\\?", id);
        // Get parameters.
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (CollKit.isEmpty(parameterMappings) || parameterObject == null) {
            return sql;
        }
        // Get the type handler registry.
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
            return sql.replaceFirst(id, Matcher.quoteReplacement(getParameterValue(parameterObject)));
        }
        // MetaObject provides get/set methods for JavaBeans, Collections, and Maps.
        MetaObject metaObject = configuration.newMetaObject(parameterObject);
        for (ParameterMapping mapping : parameterMappings) {
            String propertyName = mapping.getProperty();
            if (metaObject.hasGetter(propertyName)) {
                sql = sql.replaceFirst(
                        id,
                        Matcher.quoteReplacement(getParameterValue(metaObject.getValue(propertyName))));
            } else if (boundSql.hasAdditionalParameter(propertyName)) {
                // This branch handles dynamic SQL.
                sql = sql.replaceFirst(
                        id,
                        Matcher.quoteReplacement(getParameterValue(boundSql.getAdditionalParameter(propertyName))));
            } else {
                // Print "Missing" to indicate a missing parameter and prevent misalignment.
                sql = sql.replaceFirst(id, "Missing");
            }
        }
        return sql;
    }

    /**
     * Formats a parameter value.
     *
     * @param object The parameter object.
     * @return The formatted parameter value.
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
     * The plugin method, which determines whether to proxy the target object.
     *
     * @param target The target object.
     * @return The proxy object or the original object.
     */
    @Override
    public Object plugin(Object target) {
        return (target instanceof Executor || target instanceof StatementHandler || target instanceof ResultSetHandler)
                ? Plugin.wrap(target, this)
                : target;
    }

    /**
     * Adds a custom handler.
     *
     * @param handler The custom handler instance.
     */
    public void addHandler(MapperHandler handler) {
        handlers.add(handler);
    }

    /**
     * Sets the list of handlers (for compatibility with older MybatisPluginBuilder versions).
     *
     * @param handlers The list of handlers.
     */
    public void setHandlers(List<MapperHandler> handlers) {
        this.handlers.clear();
        if (handlers != null) {
            this.handlers.addAll(handlers);
        }
    }

    /**
     * Gets the list of handlers.
     *
     * @return A copy of the list of handlers.
     */
    public List<MapperHandler> getHandlers() {
        return new ArrayList<>(handlers);
    }

    /**
     * Sets property configurations, dynamically creating and configuring handlers.
     *
     * @param properties The configuration properties.
     */
    @Override
    public void setProperties(Properties properties) {
        Context context = (Context) Context.newInstance(properties);
        Map<String, Properties> groups = context.group(Symbol.AT);
        groups.forEach((key, value) -> {
            MapperHandler handler = ReflectKit.newInstance(key);
            addHandler(handler);
        });
    }

}
