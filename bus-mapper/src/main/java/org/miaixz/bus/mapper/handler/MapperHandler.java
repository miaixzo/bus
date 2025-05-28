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
 * SQL 拦截处理器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface MapperHandler<T> extends Handler<T> {

    /**
     * 在 StatementHandler 的 prepare 方法执行前的预处理 可用于修改 SQL 或连接配置
     *
     * @param statementHandler 语句处理器，可能为代理对象
     */
    default void prepare(StatementHandler statementHandler) {

    }

    /**
     * 判断是否执行 Executor 的 update 方法 如果返回 false，则 update 操作不执行，影响行数返回 -1
     *
     * @param executor        MyBatis 执行器，可能为代理对象
     * @param mappedStatement 映射语句，包含 SQL 配置
     * @param parameter       更新参数
     * @return 返回 true 表示继续执行更新，返回 false 表示终止更新
     */
    default boolean isUpdate(Executor executor, MappedStatement mappedStatement, Object parameter) {
        return true;
    }

    /**
     * 在 Executor 的 update 方法执行前的预处理 可用于修改 SQL 或参数
     *
     * @param executor        MyBatis 执行器，可能为代理对象
     * @param mappedStatement 映射语句，包含 SQL 配置
     * @param parameter       更新参数
     */
    default void update(Executor executor, MappedStatement mappedStatement, Object parameter) {

    }

    /**
     * 判断是否执行 Executor 的 query 方法 如果返回 false，则不执行查询操作，直接返回空列表
     *
     * @param executor        MyBatis 执行器，可能为代理对象
     * @param mappedStatement 映射语句，包含 SQL 配置
     * @param parameter       查询参数
     * @param rowBounds       分页参数
     * @param resultHandler   结果处理器
     * @param boundSql        绑定的 SQL 对象
     * @return 返回 true 表示继续执行查询，返回 false 表示终止查询
     */
    default boolean isQuery(Executor executor, MappedStatement mappedStatement, Object parameter, RowBounds rowBounds,
            ResultHandler resultHandler, BoundSql boundSql) {
        return true;
    }

    /**
     * 在 Executor 的 query 方法执行前的预处理 可用于修改 SQL、参数或记录日志
     *
     * @param executor        MyBatis 执行器，可能为代理对象
     * @param mappedStatement 映射语句，包含 SQL 配置
     * @param parameter       查询参数
     * @param rowBounds       分页参数
     * @param resultHandler   结果处理器
     * @param boundSql        绑定的 SQL 对象
     */
    default void query(Object result, Executor executor, MappedStatement mappedStatement, Object parameter,
            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {

    }

    /**
     * 在 Executor 的 query 方法执行后的处理 可用于修改结果、记录日志等
     *
     * @param statementHandler 语句处理器，可能为代理对象
     * @param mappedStatement  映射语句，包含 SQL 配置
     * @param resultHandler    结果处理器
     * @param result           执行结果
     */
    default void after(Object result, StatementHandler statementHandler, MappedStatement mappedStatement,
            ResultHandler resultHandler) {

    }

    /**
     * 在 StatementHandler 的 getBoundSql 方法执行前的预处理 仅在 BatchExecutor 和 ReuseExecutor 中调用 可用于修改绑定的 SQL
     *
     * @param statementHandler 语句处理器，可能为代理对象
     */
    default void getBoundSql(StatementHandler statementHandler) {

    }

}