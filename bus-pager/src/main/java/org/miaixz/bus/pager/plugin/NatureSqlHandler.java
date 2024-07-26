/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         ~
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
package org.miaixz.bus.pager.plugin;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.logger.Logger;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * 数据库操作性能拦截器,记录耗时
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Intercepts(value = {
        @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class }),
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class }) })
public class NatureSqlHandler extends SqlParserHandler implements Interceptor {

    private static void getSql(Configuration configuration, BoundSql boundSql, String sqlId, long time) {
        Logger.debug(sqlId + " :  ==> " + time + " ms");
        // 获取参数
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String id = ID.objectId();
        // 1.SQL语句多个空格全部使用一个空格代替
        // 2.防止参数值中有问号问题,全部动态替换
        String sql = boundSql.getSql().replaceAll("[\\s]+", Symbol.SPACE).replaceAll("\\?", id);
        if (!CollKit.isEmpty(parameterMappings) && null != parameterObject) {
            // 获取类型处理器注册器,类型处理器的功能是进行java类型和数据库类型的转换
            // 如果根据parameterObject.getClass()可以找到对应的类型,则替换
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst(id, Matcher.quoteReplacement(getParameterValue(parameterObject)));
            } else {
                // MetaObject主要是封装了originalObject对象,提供了get和set的方法
                // 主要支持对JavaBean、Collection、Map三种类型对象的操作
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object object = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst(id, Matcher.quoteReplacement(getParameterValue(object)));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object object = boundSql.getAdditionalParameter(propertyName);
                        // 该分支是动态sql
                        sql = sql.replaceFirst(id, Matcher.quoteReplacement(getParameterValue(object)));
                    } else {
                        // 打印Missing,提醒该参数缺失并防止错位
                        sql = sql.replaceFirst(id, "Missing");
                    }
                }
            }
        }
        Logger.debug(sql);
    }

    private static String getParameterValue(Object object) {
        String value;
        if (object instanceof String) {
            value = Symbol.SINGLE_QUOTE + object + Symbol.SINGLE_QUOTE;
        } else if (object instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = Symbol.SINGLE_QUOTE + formatter.format(new Date()) + Symbol.SINGLE_QUOTE;
        } else {
            if (null != object) {
                value = object.toString();
            } else {
                value = Normal.EMPTY;
            }
        }
        return value;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = System.currentTimeMillis();
        Object returnValue = invocation.proceed();
        long end = System.currentTimeMillis();
        try {
            final Object[] args = invocation.getArgs();
            MappedStatement ms = (MappedStatement) args[0];
            Object parameter = null;
            if (invocation.getArgs().length > 1) {
                parameter = invocation.getArgs()[1];
            }
            getSql(ms.getConfiguration(), ms.getBoundSql(parameter), ms.getId(), end - start);
        } catch (Exception e) {
            throw new InternalException(e);
        }
        return returnValue;
    }

    @Override
    public Object plugin(Object object) {
        if (object instanceof Executor) {
            return Plugin.wrap(object, this);
        }
        return object;
    }

}
