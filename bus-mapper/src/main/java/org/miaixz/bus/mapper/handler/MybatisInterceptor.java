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
 * MyBatis SQL 拦截器
 * <p>
 * 通过注册的处理器应用自定义逻辑处理 SQL 执行。 拦截 Executor 和 StatementHandler，处理查询、更新和 SQL 准备。
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
     * 自定义处理器集合，使用 Set 避免重复
     */
    private final Set<MapperHandler> handlers = new HashSet<>();

    /**
     * 拦截方法，处理 MyBatis 的 Executor 和 StatementHandler 调用
     *
     * @param invocation 拦截调用信息
     * @return 拦截处理后的结果
     * @throws Throwable 如果拦截过程中发生异常
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = DateKit.current();
        Object target = invocation.getTarget();
        Object[] args = invocation.getArgs();

        if (target instanceof Executor) {
            return handleExecutor((Executor) target, args, start, invocation);
        } else if (target instanceof StatementHandler) {
            return handleStatementHandler((StatementHandler) target, args, start, invocation);
        }

        return invocation.proceed();
    }

    /**
     * 处理 Executor 相关拦截逻辑（查询或更新）
     *
     * @param executor   Executor 实例
     * @param args       参数数组
     * @param start      开始时间
     * @param invocation 拦截调用信息
     * @return 处理结果
     * @throws Throwable 如果处理过程中发生异常
     */
    private Object handleExecutor(Executor executor, Object[] args, long start, Invocation invocation)
            throws Throwable {
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        SqlCommandType commandType = ms.getSqlCommandType();

        if (commandType == SqlCommandType.UPDATE) {
            return processUpdate(executor, ms, parameter, invocation);
        } else if (commandType == SqlCommandType.SELECT) {
            return processQuery(executor, ms, parameter, args, invocation);
        }

        Object result = invocation.proceed();
        logging(ms, ms.getBoundSql(parameter), start);
        return result;
    }

    /**
     * 处理 StatementHandler 相关拦截逻辑（getBoundSql 或 prepare）
     *
     * @param statementHandler StatementHandler 实例
     * @param args             参数数组
     * @param start            开始时间
     * @param invocation       拦截调用信息
     * @return 处理结果
     * @throws Throwable 如果处理过程中发生异常
     */
    private Object handleStatementHandler(StatementHandler statementHandler, Object[] args, long start,
            Invocation invocation) throws Throwable {
        if (args == null) {
            handlers.forEach(handler -> handler.getBoundSql(statementHandler));
        } else {
            handlers.forEach(handler -> handler.prepare(statementHandler));
        }

        Object result = invocation.proceed();
        MetaObject metaObject = getMetaObject(statementHandler);
        logging(getMappedStatement(metaObject), (BoundSql) metaObject.getValue(DELEGATE_BOUNDSQL), start);
        return result;
    }

    /**
     * 处理查询操作
     *
     * @param executor   Executor 实例
     * @param ms         MappedStatement 实例
     * @param parameter  参数对象
     * @param args       参数数组
     * @param invocation 拦截调用信息
     * @return 查询结果，拦截器阻止时返回空列表
     * @throws Throwable 如果处理过程中发生异常
     */
    private Object processQuery(Executor executor, MappedStatement ms, Object parameter, Object[] args,
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
     * 处理更新操作
     *
     * @param executor   Executor 实例
     * @param ms         MappedStatement 实例
     * @param parameter  参数对象
     * @param invocation 拦截调用信息
     * @return 更新结果，拦截器阻止时返回 -1
     * @throws Throwable 如果处理过程中发生异常
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
     * 记录 SQL 执行信息
     *
     * @param ms       MappedStatement 实例
     * @param boundSql BoundSql 实例
     * @param start    开始时间
     */
    private void logging(MappedStatement ms, BoundSql boundSql, long start) {
        long duration = DateKit.current() - start;
        Logger.debug("==>     Method: {} {}ms", ms.getId(), duration);
        String sql = format(ms.getConfiguration(), boundSql);
        Logger.debug("==>     Script: {}", sql);
    }

    /**
     * 格式化 SQL 语句，替换参数值
     *
     * @param configuration MyBatis 配置
     * @param boundSql      BoundSql 实例
     * @return 格式化后的 SQL 语句
     */
    private String format(Configuration configuration, BoundSql boundSql) {
        String id = ID.objectId();
        // 1.SQL语句多个空格全部使用一个空格代替
        // 2.防止参数值中有问号问题,全部动态替换
        String sql = boundSql.getSql().replaceAll("[\\s]+", Symbol.SPACE).replaceAll("\\?", id);
        // 获取参数
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (CollKit.isEmpty(parameterMappings) || parameterObject == null) {
            return sql;
        }
        // 获取类型处理器注册器,类型处理器的功能是进行java类型和数据库类型的转换
        // 如果根据parameterObject.getClass()可以找到对应的类型,则替换
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
            return sql.replaceFirst(id, Matcher.quoteReplacement(getParameterValue(parameterObject)));
        }
        // MetaObject主要是封装了originalObject对象,提供了get和set的方法
        // 主要支持对JavaBean、Collection、Map三种类型对象的操作
        MetaObject metaObject = configuration.newMetaObject(parameterObject);
        for (ParameterMapping mapping : parameterMappings) {
            String propertyName = mapping.getProperty();
            if (metaObject.hasGetter(propertyName)) {
                sql = sql.replaceFirst(id,
                        Matcher.quoteReplacement(getParameterValue(metaObject.getValue(propertyName))));
            } else if (boundSql.hasAdditionalParameter(propertyName)) {
                // 该分支是动态sql
                sql = sql.replaceFirst(id,
                        Matcher.quoteReplacement(getParameterValue(boundSql.getAdditionalParameter(propertyName))));
            } else {
                // 打印Missing,提醒该参数缺失并防止错位
                sql = sql.replaceFirst(id, "Missing");
            }
        }
        return sql;
    }

    /**
     * 格式化参数值
     *
     * @param object 参数对象
     * @return 格式化后的参数值
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
     * 插件方法，决定是否对目标对象进行代理
     *
     * @param target 目标对象
     * @return 代理对象或原对象
     */
    @Override
    public Object plugin(Object target) {
        return (target instanceof Executor || target instanceof StatementHandler) ? Plugin.wrap(target, this) : target;
    }

    /**
     * 添加自定义处理器
     *
     * @param handler 自定义处理器实例
     */
    public void addHandler(MapperHandler handler) {
        handlers.add(handler);
    }

    /**
     * 设置处理器列表（兼容旧版本 MybatisPluginBuilder）
     *
     * @param handlers 处理器列表
     */
    public void setHandlers(List<MapperHandler> handlers) {
        this.handlers.clear();
        if (handlers != null) {
            this.handlers.addAll(handlers);
        }
    }

    /**
     * 获取处理器列表
     *
     * @return 处理器列表副本
     */
    public List<MapperHandler> getHandlers() {
        return new ArrayList<>(handlers);
    }

    /**
     * 设置属性配置，动态创建和配置处理器
     *
     * @param properties 配置属性
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