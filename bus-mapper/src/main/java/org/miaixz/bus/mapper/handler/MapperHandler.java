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

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.core.Handler;

/**
 * An interface for SQL interception handlers.
 *
 * <p>
 * Handlers are executed in order based on their {@link #getOrder()} value. Lower values have higher priority.
 * </p>
 *
 * Predefined Order Values
 * 
 * <ul>
 * <li><b>-1000</b> - OperationHandler (SQL safety check, highest priority)</li>
 * <li><b>100</b> - TablePrefixHandler (table prefix modification)</li>
 * <li><b>200</b> - TenantHandler (multi-tenancy filtering)</li>
 * <li><b>300</b> - VisibleHandler (data permission control)</li>
 * <li><b>400</b> - PopulateHandler (field auto-population)</li>
 * <li><b>1000</b> - PageHandler (pagination, lowest priority)</li>
 * </ul>
 *
 * Custom Handler Order
 * 
 * <pre>{@code
 * public class MyCustomHandler implements MapperHandler {
 * 
 *     @Override
 *     public int getOrder() {
 *         return 150; // Execute between TablePrefixHandler and TenantHandler
 *     }
 * }
 * }</pre>
 *
 * @param <T> The type of the object being handled.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface MapperHandler<T> extends Handler<T> {

    /**
     * Get the execution order of this handler.
     *
     * <p>
     * Handlers are sorted by order value in ascending order. Lower values execute first.
     * </p>
     *
     * @return the order value, default is {@link Integer#MAX_VALUE}
     */
    default int getOrder() {
        return Integer.MAX_VALUE;
    }

    /**
     * Pre-processes before the `prepare` method of a {@link StatementHandler} is executed. This can be used to modify
     * the SQL or connection configuration.
     *
     * @param statementHandler The statement handler, which may be a proxy object.
     */
    default void prepare(StatementHandler statementHandler) {

    }

    /**
     * Determines whether to execute the `update` method of an {@link Executor}. If this returns false, the update
     * operation will not be executed, and the number of affected rows will be -1.
     *
     * @param executor        The MyBatis executor, which may be a proxy object.
     * @param mappedStatement The mapped statement, containing SQL configuration.
     * @param parameter       The update parameters.
     * @return {@code true} to continue with the update, {@code false} to terminate it.
     */
    default boolean isUpdate(Executor executor, MappedStatement mappedStatement, Object parameter) {
        return true;
    }

    /**
     * Pre-processes before the `update` method of an {@link Executor} is executed. This can be used to modify the SQL
     * or parameters.
     *
     * @param executor        The MyBatis executor, which may be a proxy object.
     * @param mappedStatement The mapped statement, containing SQL configuration.
     * @param parameter       The update parameters.
     */
    default void update(Executor executor, MappedStatement mappedStatement, Object parameter) {

    }

    /**
     * Determines whether to execute the `query` method of an {@link Executor}. If this returns false, the query
     * operation will not be executed, and an empty list will be returned.
     *
     * @param executor        The MyBatis executor, which may be a proxy object.
     * @param mappedStatement The mapped statement, containing SQL configuration.
     * @param parameter       The query parameters.
     * @param rowBounds       The pagination parameters.
     * @param resultHandler   The result handler.
     * @param boundSql        The bound SQL object.
     * @return {@code true} to continue with the query, {@code false} to terminate it.
     */
    default boolean isQuery(
            Executor executor,
            MappedStatement mappedStatement,
            Object parameter,
            RowBounds rowBounds,
            ResultHandler resultHandler,
            BoundSql boundSql) {
        return true;
    }

    /**
     * Pre-processes before the `query` method of an {@link Executor} is executed. This can be used to modify the SQL,
     * parameters, or log information.
     *
     * @param result          The query result.
     * @param executor        The MyBatis executor, which may be a proxy object.
     * @param mappedStatement The mapped statement, containing SQL configuration.
     * @param parameter       The query parameters.
     * @param rowBounds       The pagination parameters.
     * @param resultHandler   The result handler.
     * @param boundSql        The bound SQL object.
     */
    default void query(
            Object result,
            Executor executor,
            MappedStatement mappedStatement,
            Object parameter,
            RowBounds rowBounds,
            ResultHandler resultHandler,
            BoundSql boundSql) {

    }

    /**
     * Pre-processes before the `getBoundSql` method of a {@link StatementHandler} is executed. This is only called in
     * `BatchExecutor` and `ReuseExecutor` and can be used to modify the bound SQL.
     *
     * @param statementHandler The statement handler, which may be a proxy object.
     */
    default void getBoundSql(StatementHandler statementHandler) {

    }

}
