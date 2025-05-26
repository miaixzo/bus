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
import java.util.*;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.mapper.Context;
import org.miaixz.bus.mapper.Handler;

/**
 * MyBatis SQL 拦截处理器，实现 MyBatis 的 Interceptor 接口，用于拦截和处理 SQL 执行过程 支持通过 Handler 接口扩展自定义拦截逻辑
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
public class MybatisInterceptor implements Interceptor {

    /**
     * 拦截器列表，存储自定义的 Handler 实例
     */
    private List<Handler> handlers = new ArrayList<>();

    /**
     * 拦截方法，处理 MyBatis 的 Executor 和 StatementHandler 调用
     *
     * @param invocation 拦截调用信息
     * @return 拦截处理后的结果
     * @throws Throwable 如果拦截过程中发生异常
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        Object[] args = invocation.getArgs();

        // 处理 Executor 拦截
        if (target instanceof Executor) {
            final Executor executor = (Executor) target;
            Object parameter = args[1];
            boolean isUpdate = args.length == 2; // update 方法参数长度为 2
            MappedStatement ms = (MappedStatement) args[0];

            // 处理查询操作
            if (!isUpdate && ms.getSqlCommandType() == SqlCommandType.SELECT) {
                RowBounds rowBounds = (RowBounds) args[2];
                ResultHandler resultHandler = (ResultHandler) args[3];
                BoundSql boundSql;
                if (args.length == 4) {
                    boundSql = ms.getBoundSql(parameter);
                } else {
                    // 特殊情况：通过代理对象调用 query 方法（6 个参数）
                    boundSql = (BoundSql) args[5];
                }
                for (Handler query : handlers) {
                    if (!query.isQuery(executor, ms, parameter, rowBounds, resultHandler, boundSql)) {
                        return Collections.emptyList(); // 终止查询，返回空列表
                    }
                    query.query(executor, ms, parameter, rowBounds, resultHandler, boundSql);
                }
                CacheKey cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
                return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
            } else if (isUpdate) {
                // 处理更新操作
                for (Handler update : handlers) {
                    if (!update.isUpdate(executor, ms, parameter)) {
                        return -1; // 终止更新，返回 -1
                    }
                    update.update(executor, ms, parameter);
                }
            }
        } else {
            // 处理 StatementHandler 拦截
            final StatementHandler sh = (StatementHandler) target;
            if (null == args) {
                // 处理 getBoundSql 方法
                for (Handler mapperHandler : handlers) {
                    mapperHandler.getBoundSql(sh);
                }
            } else {
                // 处理 prepare 方法
                Connection connections = (Connection) args[0];
                Integer transactionTimeout = (Integer) args[1];
                for (Handler mapperHandler : handlers) {
                    mapperHandler.prepare(sh, connections, transactionTimeout);
                }
            }
        }
        // 继续执行原始方法
        return invocation.proceed();
    }

    /**
     * 插件方法，决定是否对目标对象进行代理
     *
     * @param target 目标对象
     * @return 代理对象或原对象
     */
    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor || target instanceof StatementHandler) {
            return Plugin.wrap(target, this); // 对 Executor 和 StatementHandler 进行代理
        }
        return target; // 不代理其他类型
    }

    public void setHandlers(List<Handler> handlers) {
        this.handlers = handlers;
    }

    /**
     * 添加自定义拦截器
     *
     * @param mapperHandler 自定义 Handler 实例
     */
    public void handler(Handler mapperHandler) {
        this.handlers.add(mapperHandler);
    }

    /**
     * 获取拦截器列表
     *
     * @return 拦截器列表的副本
     */
    public List<Handler> getHandlers() {
        return ListKit.of(handlers);
    }

    /**
     * 设置属性配置，根据属性动态创建和配置拦截器
     *
     * @param properties 配置属性
     */
    @Override
    public void setProperties(Properties properties) {
        Context context = (Context) Context.newInstance(properties);
        // 按 "@" 分组解析属性
        Map<String, Properties> group = context.group(Symbol.AT);
        group.forEach((k, v) -> {
            // 动态创建 Handler 实例
            Handler mapperHandler = ReflectKit.newInstance(k);
            mapperHandler.setProperties(v); // 设置属性
            this.handler(mapperHandler); // 添加到拦截器列表
        });
    }

    /**
     * 返回字符串表示，包含拦截器列表信息
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "MybatisInterceptor{" + "handlers=" + handlers + '}';
    }

}